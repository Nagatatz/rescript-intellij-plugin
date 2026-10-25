package com.rescript.plugin.imports

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.command.undo.UndoManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.rescript.plugin.IntelliJPlatformExtension
import com.rescript.plugin.wizard.IntegrationTestSupport
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

/** Checks actual optimizer edits against compiler output and preserves meaningful reopens. */
@ExtendWith(IntelliJPlatformExtension::class)
class RescriptOpenRemovalIntegrationTest {
    private lateinit var myFixture: CodeInsightTestFixture
    private lateinit var project: Project

    @TempDir
    lateinit var projectDir: Path

    @Test
    fun `optimization preserves outputs and undo across duplicates reopens and shadowing`() {
        IntegrationTestSupport.requireBinary("npm")
        IntegrationTestSupport.requireBinary("node")
        Files.writeString(
            projectDir.resolve("package.json"),
            """{"name":"open-regression","type":"module","private":true,"devDependencies":{"rescript":"12.2.0"}}""",
        )
        Files.writeString(
            projectDir.resolve("rescript.json"),
            """
            {"name":"open-regression","sources":[{"dir":"src"}],
            "package-specs":{"module":"esmodule","in-source":true},"suffix":".res.js"}
            """.trimIndent(),
        )
        val source = Files.createDirectory(projectDir.resolve("src")).resolve("Example.res")
        val install = IntegrationTestSupport.exec(projectDir, listOf("npm", "install", "--no-audit", "--no-fund"))
        assertTrue(install.succeeded, install.stdout + install.stderr)
        val cases =
            listOf(
                """
                module A = {let value = 1}
                open A
                open A
                Console.log(value)
                """.trimIndent() to "1",
                """
                module A = {let value = 1}
                module B = {let value = 2}
                open A
                Console.log(value)
                open B
                Console.log(value)
                open A
                Console.log(value)
                """.trimIndent() to "1\n2\n1",
                """
                module A = {let value = 1
                  module A = {let value = 2}
                }
                open A
                Console.log(value)
                open A
                Console.log(value)
                """.trimIndent() to "1\n2",
            )
        for ((original, output) in cases) {
            myFixture.configureByText("Example.res", original)
            val action = RescriptImportOptimizer().processFile(myFixture.file)
            WriteCommandAction.runWriteCommandAction(project) { action.run() }
            PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(myFixture.editor.document)
            val transformed = myFixture.editor.document.text
            if (output == "1") {
                assertTrue(transformed != original)
                val undo = UndoManager.getInstance(project)
                val editor = FileEditorManager.getInstance(project).getSelectedEditor(myFixture.file.virtualFile)
                assertTrue(undo.isUndoAvailable(editor))
                undo.undo(editor)
                assertEquals(original, myFixture.editor.document.text)
            } else {
                assertEquals(original, transformed)
            }
            for (text in listOf(original, transformed, myFixture.editor.document.text)) {
                Files.writeString(source, text)
                val compile = IntegrationTestSupport.exec(projectDir, listOf("npx", "rescript", "build"))
                assertTrue(compile.succeeded, compile.stdout + compile.stderr)
                val run = IntegrationTestSupport.exec(projectDir, listOf("node", "src/Example.res.js"))
                assertTrue(run.succeeded, run.stdout + run.stderr)
                assertEquals(output, run.stdout.trim())
            }
        }
    }
}
