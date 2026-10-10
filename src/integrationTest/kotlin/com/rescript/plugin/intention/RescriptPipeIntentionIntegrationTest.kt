package com.rescript.plugin.intention

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

/** Compiles both sides of real editor conversions and checks runtime results and undo. */
@ExtendWith(IntelliJPlatformExtension::class)
class RescriptPipeIntentionIntegrationTest {
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
    fun `real intentions compile in both directions without changing results`() {
        IntegrationTestSupport.requireBinary("npm")
        IntegrationTestSupport.requireBinary("node")
        IntegrationTestSupport.writeFiles(
            projectDir,
            mapOf(
                "package.json" to
                    """{"name":"pipe-regression","private":true,"type":"module","devDependencies":{"rescript":"12.2.0"}}""",
                "rescript.json" to
                    """{"name":"pipe-regression","sources":[{"dir":"src"}],"package-specs":{"module":"esmodule","in-source":true},"suffix":".res.js"}""",
            ),
        )
        val src = Files.createDirectory(projectDir.resolve("src"))
        val install = IntegrationTestSupport.exec(projectDir, listOf("npm", "install", "--no-audit", "--no-fund"))
        assertTrue(install.succeeded, install.stdout + install.stderr)
        val prefix = "let getItems = () => [1, 2, 3]\n"
        val cases =
            listOf(
                Triple(
                    "getItems()-<caret>>Array.length",
                    "Array.length(getItems())",
                    RescriptConvertPipeToFunctionCallIntention(),
                ),
                Triple(
                    "Array.<caret>length(getItems())",
                    "getItems()->Array.length",
                    RescriptConvertFunctionCallToPipeIntention(),
                ),
                Triple(
                    "getItems(/* ), -> */)-<caret>>Array.map(x => x + 1)",
                    "Array.map(getItems(/* ), -> */), x => x + 1)",
                    RescriptConvertPipeToFunctionCallIntention(),
                ),
                Triple(
                    "Array.<caret>map(\n getItems(/* , ) */),\n x => x + 1\n)",
                    "getItems(/* , ) */)->Array.map(x => x + 1)",
                    RescriptConvertFunctionCallToPipeIntention(),
                ),
                Triple(
                    "Array.<caret>map([\"a,) ->\", \"b\"], x => x)",
                    "[\"a,) ->\", \"b\"]->Array.map(x => x)",
                    RescriptConvertFunctionCallToPipeIntention(),
                ),
                Triple(
                    "Array.map([[1], [2, 3]], items => items-<caret>>Array.length)",
                    "Array.map([[1], [2, 3]], items => Array.length(items))",
                    RescriptConvertPipeToFunctionCallIntention(),
                ),
                Triple(
                    "Array.map([[1], [2, 3]], items => Array.<caret>length(items))",
                    "Array.map([[1], [2, 3]], items => items->Array.length)",
                    RescriptConvertFunctionCallToPipeIntention(),
                ),
            )
        cases.forEach { (input, expected, intention) ->
            val before = prefix + "let result = $input\n"
            myFixture.configureByText("Pipe.res", before)
            val original = myFixture.editor.document.text
            val beforeOutput = compileAndRun(src, original)
            assertTrue(intention.isAvailable(project, myFixture.editor, myFixture.file))
            myFixture.launchAction(myFixture.findSingleIntention(intention.text))
            assertEquals(prefix + "let result = $expected\n", myFixture.editor.document.text)
            assertEquals(beforeOutput, compileAndRun(src, myFixture.editor.document.text))
            val manager = UndoManager.getInstance(project)
            val editor = FileEditorManager.getInstance(project).getSelectedEditor(myFixture.file.virtualFile)
            assertTrue(manager.isUndoAvailable(editor))
            manager.undo(editor)
            assertEquals(original, myFixture.editor.document.text)
            assertEquals(beforeOutput, compileAndRun(src, myFixture.editor.document.text))
        }
    }

    private fun compileAndRun(
        src: Path,
        text: String,
    ): String {
        Files.writeString(src.resolve("Pipe.res"), text)
        val build = IntegrationTestSupport.exec(projectDir, listOf("npx", "--no-install", "rescript", "build"))
        assertTrue(build.succeeded, build.stdout + build.stderr)
        val run =
            IntegrationTestSupport.exec(
                projectDir,
                listOf(
                    "node",
                    "--input-type=module",
                    "-e",
                    "import {result} from './src/Pipe.res.js'; console.log(JSON.stringify(result));",
                ),
            )
        assertTrue(run.succeeded, run.stdout + run.stderr)
        return run.stdout.trim()
    }
}
