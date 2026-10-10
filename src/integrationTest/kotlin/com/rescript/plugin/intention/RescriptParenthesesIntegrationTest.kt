package com.rescript.plugin.intention

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.command.undo.UndoManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.testFramework.PsiTestUtil
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

/** Compiles registered intention/undo edits and valid refused programs. */
@ExtendWith(IntelliJPlatformExtension::class)
class RescriptParenthesesIntegrationTest {
    private lateinit var myFixture: CodeInsightTestFixture
    private lateinit var project: Project

    @TempDir
    lateinit var projectDir: Path

    @Test
    fun `registered action preserves output and unsafe programs stay unchanged`() {
        IntegrationTestSupport.requireBinary("npm")
        IntegrationTestSupport.requireBinary("node")
        Files.writeString(
            projectDir.resolve("package.json"),
            """{"name":"parens-regression","type":"module","private":true,"devDependencies":{"rescript":"12.2.0"}}""",
        )
        Files.writeString(
            projectDir.resolve("rescript.json"),
            """{"name":"parens-regression","sources":[{"dir":"src"}],"package-specs":{"module":"esmodule","in-source":true},"suffix":".res.js"}""",
        )
        val source = Files.createDirectory(projectDir.resolve("src")).resolve("Example.res")
        val install = IntegrationTestSupport.exec(projectDir, listOf("npm", "install", "--no-audit", "--no-fund"))
        assertTrue(install.succeeded, install.stdout + install.stderr)
        val cases =
            listOf(
                Triple("let value = <caret>( /* (comment) */ 42 // )\n)\nConsole.log(value)", "42", true),
                Triple("let message = <caret>(\"(hello)\")\nConsole.log(message)", "(hello)", true),
                Triple("let message = \"(<caret>hello)\"\nConsole.log(message)", "(hello)", false),
                Triple("let a = 1\nlet b = 2\nlet value = (<caret>a < b) == true\nConsole.log(value)", "true", false),
                Triple("let fn = (<caret>x) => {x}\nConsole.log(fn(42))", "42", false),
                Triple("Console.log /* comment */ (<caret>42)", "42", false),
                Triple("let value = ([1, 2]-<caret>>Array.length)\nConsole.log(value)", "2", false),
            )
        val root = myFixture.tempDirFixture.findOrCreateDir("")
        PsiTestUtil.addContentRoot(myFixture.module, root)
        try {
            for ((marked, output, supported) in cases) {
                myFixture.configureByText("Example.res", marked)
                val original = myFixture.editor.document.text
                val intention = RescriptRemoveParenthesesIntention()
                assertEquals(supported, intention.isAvailable(project, myFixture.editor, myFixture.file))
                if (supported) {
                    myFixture.launchAction(myFixture.findSingleIntention(intention.text))
                } else {
                    WriteCommandAction.runWriteCommandAction(project) {
                        intention.invoke(project, myFixture.editor, myFixture.file)
                    }
                }
                val after = myFixture.editor.document.text
                if (supported) {
                    assertTrue(original != after)
                    val undo = UndoManager.getInstance(project)
                    val editor = FileEditorManager.getInstance(project).getSelectedEditor(myFixture.file.virtualFile)
                    assertTrue(undo.isUndoAvailable(editor))
                    undo.undo(editor)
                }
                val restored = myFixture.editor.document.text
                assertEquals(original, restored)
                for (text in if (supported) listOf(original, after, restored) else listOf(original, after)) {
                    Files.writeString(source, text)
                    val compile = IntegrationTestSupport.exec(projectDir, listOf("npx", "rescript", "build"))
                    assertTrue(compile.succeeded, compile.stdout + compile.stderr)
                    val run = IntegrationTestSupport.exec(projectDir, listOf("node", "src/Example.res.js"))
                    assertTrue(run.succeeded, run.stdout + run.stderr)
                    assertEquals(output, run.stdout.trim())
                }
            }
        } finally {
            PsiTestUtil.removeContentEntry(myFixture.module, root)
        }
    }
}
