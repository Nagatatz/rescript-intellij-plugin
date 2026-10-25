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

/** Exercises registered intentions, direct invocation refusal and atomic undo. */
@ExtendWith(IntelliJPlatformExtension::class)
class RescriptRemoveParenthesesEditorTest {
    private lateinit var myFixture: CodeInsightTestFixture
    private lateinit var project: Project

    @BeforeEach
    fun addContentRoot() {
        PsiTestUtil.addContentRoot(myFixture.module, myFixture.tempDirFixture.findOrCreateDir(""))
    }

    @AfterEach
    fun removeContentRoot() {
        PsiTestUtil.removeContentEntry(myFixture.module, myFixture.tempDirFixture.findOrCreateDir(""))
    }

    @Test
    fun `registered intention preserves trivia and one undo restores parentheses`() {
        myFixture.configureByText("Parens.res", "let value = <caret>( /* (comment) */ 42 // )\n)")
        val original = myFixture.editor.document.text
        val intention = myFixture.findSingleIntention("Remove unnecessary parentheses")
        myFixture.launchAction(intention)
        assertEquals("let value =  /* (comment) */ 42 // )\n", myFixture.editor.document.text)
        val undo = UndoManager.getInstance(project)
        val editor = FileEditorManager.getInstance(project).getSelectedEditor(myFixture.file.virtualFile)
        assertTrue(undo.isUndoAvailable(editor))
        undo.undo(editor)
        assertEquals(original, myFixture.editor.document.text)
    }

    @Test
    fun `unavailable inputs remain unchanged even on direct invoke`() {
        for (marked in listOf(
            "let message = \"(<caret>hello)\"",
            "let value = (<caret>a < b) == true",
            "let value = (arr-<caret>>Array.length)",
            "Console.log /* comment */ (<caret>42)",
            "let fn = (<caret>x) => {x}",
            "let value = ((<caret>42))",
        )) {
            myFixture.configureByText("Unsafe.res", marked)
            val original = myFixture.editor.document.text
            val intention = RescriptRemoveParenthesesIntention()
            assertFalse(intention.isAvailable(project, myFixture.editor, myFixture.file))
            WriteCommandAction.runWriteCommandAction(project) {
                intention.invoke(project, myFixture.editor, myFixture.file)
            }
            assertEquals(original, myFixture.editor.document.text)
        }
    }

    @Test
    fun `uncommitted source and mismatched file refuse direct invocation`() {
        myFixture.configureByText("Other.res", "let other = (42)")
        val other = myFixture.file
        myFixture.configureByText("Parens.res", "let value = <caret>(42)")
        val intention = RescriptRemoveParenthesesIntention()
        WriteCommandAction.runWriteCommandAction(project) {
            intention.invoke(project, myFixture.editor, other)
        }
        assertEquals("let value = (42)", myFixture.editor.document.text)
        val file = myFixture.file
        WriteCommandAction.runWriteCommandAction(project) {
            myFixture.editor.document.replaceString(0, 0, "// pending\n")
            val original = myFixture.editor.document.text
            intention.invoke(project, myFixture.editor, file)
            assertEquals(original, myFixture.editor.document.text)
        }
    }
}
