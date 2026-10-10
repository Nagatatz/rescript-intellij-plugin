package com.rescript.plugin.refactor

import com.intellij.openapi.command.undo.UndoManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.rescript.plugin.IntelliJPlatformExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/** Exercises the registered handler against actual editor documents and the undo stack. */
@ExtendWith(IntelliJPlatformExtension::class)
class RescriptInlineDocumentTest {
    private lateinit var myFixture: CodeInsightTestFixture
    private lateinit var project: Project

    @Test
    fun `inline preserves arithmetic precedence and one undo restores declaration and usages`() {
        myFixture.configureByText(
            "Inline.res",
            "let result = {\n  let <caret>x = 1 + 2\n  Console.log(\"x\") // x\n  x * 3\n}",
        )
        val original = myFixture.editor.document.text
        val element = myFixture.file.findElementAt(myFixture.editor.caretModel.offset)!!
        val handler = RescriptInlineHandler()
        assertTrue(handler.canInlineElement(element))
        handler.inlineElement(project, myFixture.editor, element)
        assertEquals("let result = {\n  Console.log(\"x\") // x\n  (1 + 2) * 3\n}", myFixture.editor.document.text)

        val undo = UndoManager.getInstance(project)
        val editor = FileEditorManager.getInstance(project).getSelectedEditor(myFixture.file.virtualFile)
        assertTrue(undo.isUndoAvailable(editor))
        undo.undo(editor)
        assertEquals(original, myFixture.editor.document.text)
    }

    @Test
    fun `unsupported shadowing and side effects do not modify documents`() {
        for (text in listOf(
            "let <caret>x = run()\nConsole.log(x)\nConsole.log(x)",
            "let <caret>x = 1\nlet f = x => x + 1",
            "let <caret>x = 1\nlet y = {\nlet x = 2\nx\n}",
        )) {
            myFixture.configureByText("Unsafe.res", text)
            val original = myFixture.editor.document.text
            val element = myFixture.file.findElementAt(myFixture.editor.caretModel.offset)!!
            val handler = RescriptInlineHandler()
            assertFalse(handler.canInlineElement(element))
            handler.inlineElement(project, myFixture.editor, element)
            assertEquals(original, myFixture.editor.document.text)
        }
    }

    @Test
    fun `top level declaration remains available to other modules`() {
        myFixture.configureByText("Export.res", "let <caret>x = 1 + 2\nConsole.log(x * 3)")
        val element = myFixture.file.findElementAt(myFixture.editor.caretModel.offset)!!
        RescriptInlineHandler().inlineElement(project, myFixture.editor, element)
        assertEquals("let x = 1 + 2\nConsole.log((1 + 2) * 3)", myFixture.editor.document.text)
    }
}
