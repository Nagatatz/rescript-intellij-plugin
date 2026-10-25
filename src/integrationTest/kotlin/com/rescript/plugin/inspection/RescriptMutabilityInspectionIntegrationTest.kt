package com.rescript.plugin.inspection

import com.intellij.openapi.command.undo.UndoManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.testFramework.PsiTestUtil
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.rescript.plugin.IntelliJPlatformExtension
import com.rescript.plugin.wizard.IntegrationTestSupport
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

/** Compiles actual inspection fixes and refused box usages, checking results and undo. */
@ExtendWith(IntelliJPlatformExtension::class)
class RescriptMutabilityInspectionIntegrationTest {
    private lateinit var myFixture: CodeInsightTestFixture
    private lateinit var project: Project

    @BeforeEach
    fun configureProjectContentRoot() {
        // The extension's EMPTY_PROJECT_DESCRIPTOR has no content root. A real
        // registered intention intentionally refuses physical files outside a project.
        PsiTestUtil.addContentRoot(myFixture.module, myFixture.tempDirFixture.findOrCreateDir(""))
        myFixture.enableInspections(RescriptMutabilityInspection())
    }

    @AfterEach
    fun removeProjectContentRoot() {
        PsiTestUtil.removeContentEntry(myFixture.module, myFixture.tempDirFixture.findOrCreateDir(""))
    }

    @TempDir
    lateinit var projectDir: Path

    @Test
    fun `actual quick fixes and refused cases preserve compiler and runtime behavior`() {
        IntegrationTestSupport.requireBinary("npm")
        IntegrationTestSupport.requireBinary("node")
        IntegrationTestSupport.writeFiles(
            projectDir,
            mapOf(
                "package.json" to
                    """{"name":"mutability-regression","private":true,"type":"module","devDependencies":{"rescript":"12.2.0"}}""",
                "rescript.json" to
                    """{"name":"mutability-regression","sources":[{"dir":"src"}],"package-specs":{"module":"esmodule","in-source":true},"suffix":".res.js"}""",
            ),
        )
        val src = Files.createDirectory(projectDir.resolve("src"))
        val install = IntegrationTestSupport.exec(projectDir, listOf("npm", "install", "--no-audit", "--no-fund"))
        assertTrue(install.succeeded, install.stdout + install.stderr)
        val cases =
            listOf(
                "let count = ref(0)\nlet next = count.contents + 1\ncount.contents + next" to
                    "let count = 0\nlet next = count + 1\ncount + next",
                "let value = ref(\"hello\")\nvalue.contents" to "let value = \"hello\"\nvalue",
                "let value = ref(1.5)\nvalue.contents" to "let value = 1.5\nvalue",
                "let value = ref(())\nvalue.contents" to "let value = ()\nvalue",
            )
        cases.forEach { (input, expected) ->
            val before = "let run = () => {\n<caret>$input\n}\nlet result = run()\n"
            val after = "let run = () => {\n$expected\n}\nlet result = run()\n"
            myFixture.configureByText("Mutability.res", before)
            val original = myFixture.editor.document.text
            val beforeOutput = compileAndRun(src, original)
            myFixture.doHighlighting()
            assertTrue(
                myFixture.filterAvailableIntentions("Remove unnecessary ref").isNotEmpty(),
                original,
            )
            myFixture.launchAction(myFixture.findSingleIntention("Remove unnecessary ref"))
            assertEquals(after, myFixture.editor.document.text)
            assertEquals(beforeOutput, compileAndRun(src, myFixture.editor.document.text))
            val manager = UndoManager.getInstance(project)
            val editor = FileEditorManager.getInstance(project).getSelectedEditor(myFixture.file.virtualFile)
            assertTrue(manager.isUndoAvailable(editor))
            manager.undo(editor)
            assertEquals(original, myFixture.editor.document.text)
            assertEquals(beforeOutput, compileAndRun(src, myFixture.editor.document.text))
        }
        val refused =
            listOf(
                "let count = ref(0)\nlet result = count.contents\n",
                local("let count = ref(0)\ncount := 2\ncount.contents"),
                local("let count = ref(0)\ncount.contents = 2\ncount.contents"),
                local("let count = ref(0)\nlet alias = count\nalias.contents"),
                local("let count = ref(0)\nlet get = () => count.contents\nget()"),
                local("let count = ref((x) => { x })\ncount.contents(1)"),
                "module A = { let identity = x => x }\n" +
                    local("let count = ref(A.identity)\ncount.contents(1)"),
                "@val external eval: string => unit = \"eval\"\n" +
                    local("let count = ref(0)\nlet _ = eval(\"typeof count\")\ncount.contents"),
            )
        refused.forEach { source ->
            myFixture.configureByText("Mutability.res", source.replace("let count =", "<caret>let count ="))
            val original = myFixture.editor.document.text
            val output = compileAndRun(src, original)
            myFixture.doHighlighting()
            assertTrue(myFixture.filterAvailableIntentions("Remove unnecessary ref").isEmpty(), source)
            assertEquals(original, myFixture.editor.document.text)
            assertEquals(output, compileAndRun(src, myFixture.editor.document.text))
        }
    }

    private fun local(body: String): String = "let run = () => {\n$body\n}\nlet result = run()\n"

    private fun compileAndRun(
        src: Path,
        text: String,
    ): String {
        Files.writeString(src.resolve("Mutability.res"), text)
        val build = IntegrationTestSupport.exec(projectDir, listOf("npx", "--no-install", "rescript", "build"))
        assertTrue(build.succeeded, build.stdout + build.stderr)
        val run =
            IntegrationTestSupport.exec(
                projectDir,
                listOf(
                    "node",
                    "--input-type=module",
                    "-e",
                    "import {result} from './src/Mutability.res.js'; console.log(JSON.stringify(result));",
                ),
            )
        assertTrue(run.succeeded, run.stdout + run.stderr)
        return run.stdout.trim()
    }
}
