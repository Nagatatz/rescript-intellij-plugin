package com.rescript.plugin.intention

import com.intellij.openapi.project.Project
import com.intellij.testFramework.PsiTestUtil
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.rescript.plugin.IntelliJPlatformExtension
import com.rescript.plugin.behavior.TransformationBehaviorFixture
import com.rescript.plugin.wizard.IntegrationTestSupport
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

/** Compiles actual qualifier removals and refused symbol contexts, checking results and undo. */
@ExtendWith(IntelliJPlatformExtension::class)
class RescriptRemoveQualifierIntentionIntegrationTest {
    private lateinit var myFixture: CodeInsightTestFixture
    private lateinit var project: Project

    @BeforeEach
    fun configureProjectContentRoot() {
        // The extension's EMPTY_PROJECT_DESCRIPTOR has no content root. A real
        // registered intention intentionally refuses physical files outside a project.
        PsiTestUtil.addContentRoot(myFixture.module, myFixture.tempDirFixture.findOrCreateDir(""))
    }

    @AfterEach
    fun removeProjectContentRoot() {
        PsiTestUtil.removeContentEntry(myFixture.module, myFixture.tempDirFixture.findOrCreateDir(""))
    }

    @TempDir
    lateinit var projectDir: Path

    @Test
    fun `actual registered intentions and refused cases preserve compiler and runtime behavior`() {
        IntegrationTestSupport.requireBinary("npm")
        IntegrationTestSupport.requireBinary("node")
        IntegrationTestSupport.writeFiles(
            projectDir,
            mapOf(
                "package.json" to
                    """{"name":"qualifier-regression","private":true,"type":"module","devDependencies":{"rescript":"12.2.0"}}""",
                "rescript.json" to
                    """{"name":"qualifier-regression","sources":[{"dir":"src"}],"package-specs":{"module":"esmodule","in-source":true},"suffix":".res.js"}""",
            ),
        )
        val src = Files.createDirectory(projectDir.resolve("src"))
        val install = IntegrationTestSupport.exec(projectDir, listOf("npm", "install", "--no-audit", "--no-fund"))
        assertTrue(install.succeeded, install.stdout + install.stderr)
        val prefix = "module A = { let value = 1 }\nopen A\n"
        val cases =
            listOf(
                prefix + "let result = <caret>A.value" to prefix + "let result = value",
                prefix + "let result = Int.toString((<caret>A.value + 2))" to
                    prefix + "let result = Int.toString((value + 2))",
                prefix + "let result = <caret>A /* keep */ . /* read */ value" to
                    prefix + "let result =  /* keep */  /* read */ value",
            )
        cases.forEach { (before, after) ->
            myFixture.configureByText("Qualifier.res", before)
            TransformationBehaviorFixture(myFixture).registeredIntentionAndUndo(
                "Remove redundant qualifier",
                after,
            ) { source -> compileAndRun(src, source) }
        }
        val refused =
            listOf(
                "module A = { let value = 1 }\nlet result = <caret>A.value\nopen A",
                prefix + "let value = 2\nlet result = <caret>A.value",
                prefix + "let run = value => <caret>A.value + value\nlet result = run(2)",
                "module A = { let value = 1 }\n/*\nopen A\n*/\nlet result = <caret>A.value",
                "module A = { let value = 1 }\nmodule B = { let value = 2 }\n" +
                    "open A\nopen B\nlet result = <caret>A.value",
                "module Original = { let value = 1 }\nmodule A = Original\n" +
                    "open A\nlet result = <caret>A.value",
            )
        refused.forEach { source ->
            myFixture.configureByText("Qualifier.res", source)
            assertTrue(myFixture.filterAvailableIntentions("Remove redundant qualifier").isEmpty(), source)
            val intention = RescriptRemoveQualifierIntention()
            TransformationBehaviorFixture(myFixture).unchanged({ text -> compileAndRun(src, text) }) {
                com.intellij.openapi.command.WriteCommandAction.runWriteCommandAction(project) {
                    intention.invoke(project, myFixture.editor, myFixture.file)
                }
            }
        }
    }

    private fun compileAndRun(
        src: Path,
        text: String,
    ): String {
        Files.writeString(src.resolve("Qualifier.res"), text)
        val build = IntegrationTestSupport.exec(projectDir, listOf("npx", "--no-install", "rescript", "build"))
        assertTrue(build.succeeded, build.stdout + build.stderr)
        val run =
            IntegrationTestSupport.exec(
                projectDir,
                listOf(
                    "node",
                    "--input-type=module",
                    "-e",
                    "import {result} from './src/Qualifier.res.js'; console.log(JSON.stringify(result));",
                ),
            )
        assertTrue(run.succeeded, run.stdout + run.stderr)
        return run.stdout.trim()
    }
}
