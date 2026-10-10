package com.rescript.plugin.refactor

import com.intellij.openapi.command.undo.UndoManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.rescript.plugin.IntelliJPlatformExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/** Applies the public extraction handler to real documents and verifies atomic undo. */
@ExtendWith(IntelliJPlatformExtension::class)
class RescriptExtractFunctionDocumentTest {
    private lateinit var myFixture: CodeInsightTestFixture
    private lateinit var project: Project

    @Test
    fun `extraction preserves scope avoids name collision and undoes once`() {
        myFixture.configureByText(
            "Extract.res",
            "let extractedFunction = 42\nlet compute = (x) => {\n  <selection>Console.log(x) // hello</selection>\n}",
        )
        val original = myFixture.editor.document.text
        RescriptExtractFunctionHandler().invoke(project, myFixture.editor, myFixture.file, null)
        assertEquals(
            "let extractedFunction = 42\nlet compute = (x) => {\n  let extractedFunction2 = (x) => {\n    Console.log(x) // hello\n  }\n\n  extractedFunction2(x)\n}",
            myFixture.editor.document.text,
        )
        val undo = UndoManager.getInstance(project)
        val editor = FileEditorManager.getInstance(project).getSelectedEditor(myFixture.file.virtualFile)
        assertTrue(undo.isUndoAvailable(editor))
        undo.undo(editor)
        assertEquals(original, myFixture.editor.document.text)
    }

    @Test
    fun `unknown references and escaped local declarations leave documents unchanged`() {
        for (text in listOf(
            "<selection>Console.log(unknown)</selection>",
            "let x = 1\nlet result = {\n<selection>let temp = x + 1</selection>\ntemp * 2\n}",
        )) {
            myFixture.configureByText("Unsafe.res", text)
            val original = myFixture.editor.document.text
            RescriptExtractFunctionHandler().invoke(project, myFixture.editor, myFixture.file, null)
            assertEquals(original, myFixture.editor.document.text)
        }
    }

    @Test
    fun `top level helper is private and existing export declaration remains`() {
        myFixture.configureByText("Export.res", "let x = 1\nlet exported = <selection>x + 1</selection>")
        RescriptExtractFunctionHandler().invoke(project, myFixture.editor, myFixture.file, null)
        assertEquals(
            "let x = 1\n@private\nlet extractedFunction = (x) => {\n  x + 1\n}\n\nlet exported = extractedFunction(x)",
            myFixture.editor.document.text,
        )
    }
}
