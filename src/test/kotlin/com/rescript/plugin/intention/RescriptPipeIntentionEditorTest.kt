package com.rescript.plugin.intention

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.command.undo.UndoManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.testFramework.PsiTestUtil
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.rescript.plugin.IntelliJPlatformExtension
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/** Drives actual intentions through the editor action and one-step undo paths. */
@ExtendWith(IntelliJPlatformExtension::class)
class RescriptPipeIntentionEditorTest {
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

    @Test
    fun `pipe intention preserves a unit call and undo restores the document`() {
        applyAndUndo(
            "let n = getItems()-<caret>>Array.length",
            "let n = Array.length(getItems())",
            RescriptConvertPipeToFunctionCallIntention(),
        )
    }

    @Test
    fun `call intention preserves nested multiline arguments and comments`() {
        applyAndUndo(
            "let items = Array.<caret>map(\ngetItems(/* ), */),\n x => f(x, 2)\n)",
            "let items = getItems(/* ), */)->Array.map(x => f(x, 2))",
            RescriptConvertFunctionCallToPipeIntention(),
        )
    }

    @Test
    fun `pipe intention works inside a lambda without changing its parameter or surrounding call`() {
        applyAndUndo(
            "let sizes = Array.map([[1], [2, 3]], items => items-<caret>>Array.length)",
            "let sizes = Array.map([[1], [2, 3]], items => Array.length(items))",
            RescriptConvertPipeToFunctionCallIntention(),
        )
    }

    @Test
    fun `call intention works inside a lambda without moving the lambda itself`() {
        applyAndUndo(
            "let sizes = Array.map([[1], [2, 3]], items => Array.<caret>length(items))",
            "let sizes = Array.map([[1], [2, 3]], items => items->Array.length)",
            RescriptConvertFunctionCallToPipeIntention(),
        )
    }

    @Test
    fun `unsafe syntax is unavailable and invoking it leaves the document unchanged`() {
        listOf(
            "let x = a + b-<caret>>Array.length",
            "let x = a|<caret>>Array.length",
            "let x = Array.<caret>map(~items=arr, f)",
            "let x = Array.<caret>map(arr, a b)",
            "let x = Array.<caret>length(getItems(a b))",
            "let x = arr-<caret>>Array.map([a b])",
            "let f = a b => items-<caret>>Array.length",
        ).forEach { text ->
            myFixture.configureByText("Unsafe.res", text)
            val before = myFixture.editor.document.text
            listOf(
                RescriptConvertPipeToFunctionCallIntention(),
                RescriptConvertFunctionCallToPipeIntention(),
            ).forEach { intention ->
                assertFalse(intention.isAvailable(project, myFixture.editor, myFixture.file))
                WriteCommandAction.runWriteCommandAction(
                    project,
                ) { intention.invoke(project, myFixture.editor, myFixture.file) }
                assertEquals(before, myFixture.editor.document.text)
            }
        }
    }

    private fun applyAndUndo(
        before: String,
        after: String,
        intention: RescriptBaseIntention,
    ) {
        myFixture.configureByText("Pipe.res", before)
        val offset = myFixture.editor.caretModel.offset
        val element = myFixture.file.findElementAt(offset)
        assertTrue(
            intention.isAvailable(project, myFixture.editor, myFixture.file),
            "file=${myFixture.file.javaClass.name}, offset=$offset, element=${element?.javaClass?.name}, " +
                "modifiable=${intention.checkFile(myFixture.file)}, " +
                "elementAvailable=${element?.let { intention.isAvailable(project, myFixture.editor, it) }}",
        )
        myFixture.launchAction(myFixture.findSingleIntention(intention.text))
        assertEquals(after, myFixture.editor.document.text)
        val manager = UndoManager.getInstance(project)
        val editor = FileEditorManager.getInstance(project).getSelectedEditor(myFixture.file.virtualFile)
        assertTrue(manager.isUndoAvailable(editor))
        manager.undo(editor)
        assertEquals(before.replace("<caret>", ""), myFixture.editor.document.text)
    }
}
