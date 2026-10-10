package com.rescript.plugin.inspection

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.command.undo.UndoManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.testFramework.PsiTestUtil
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.rescript.plugin.IntelliJPlatformExtension
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/** Exercises inspection fixes through physical documents, editor actions, and undo. */
@ExtendWith(IntelliJPlatformExtension::class)
class RescriptMutabilityInspectionEditorTest {
    private lateinit var myFixture: CodeInsightTestFixture
    private lateinit var project: Project

    @BeforeEach
    fun configureInspection() {
        PsiTestUtil.addContentRoot(myFixture.module, myFixture.tempDirFixture.findOrCreateDir(""))
        myFixture.enableInspections(RescriptMutabilityInspection())
    }

    @AfterEach
    fun removeProjectContentRoot() {
        PsiTestUtil.removeContentEntry(myFixture.module, myFixture.tempDirFixture.findOrCreateDir(""))
    }

    @Test
    fun `inspection fixes initializer and every read in one undo step`() {
        val before =
            "let run = () => {\n<caret>let count = ref(0)\n" +
                "let next = count.contents + 1\ncount.contents + next\n}"
        val after = "let run = () => {\nlet count = 0\nlet next = count + 1\ncount + next\n}"
        myFixture.configureByText("ReadOnly.res", before)
        myFixture.doHighlighting()
        myFixture.launchAction(myFixture.findSingleIntention("Remove unnecessary ref"))
        assertEquals(after, myFixture.editor.document.text)
        val manager = UndoManager.getInstance(project)
        val editor = FileEditorManager.getInstance(project).getSelectedEditor(myFixture.file.virtualFile)
        assertTrue(manager.isUndoAvailable(editor))
        manager.undo(editor)
        assertEquals(before.replace("<caret>", ""), myFixture.editor.document.text)
    }

    @Test
    fun `escaping mutable exported and polymorphic boxes have no quick fix`() {
        listOf(
            "<caret>let count = ref(0)\nlet result = count.contents",
            "let run = () => {\n<caret>let count = ref(0)\ncount := 1\ncount.contents\n}",
            "let run = () => {\n<caret>let count = ref(0)\nlet alias = count\nalias.contents\n}",
            "let run = () => {\n<caret>let count = ref((x) => { x })\ncount.contents\n}",
            "let run = () => {\n<caret>let count = ref(A.identity)\ncount.contents\n}",
        ).forEach { source ->
            myFixture.configureByText("Unsafe.res", source)
            myFixture.doHighlighting()
            assertTrue(myFixture.filterAvailableIntentions("Remove unnecessary ref").isEmpty(), source)
            assertEquals(source.replace("<caret>", ""), myFixture.editor.document.text)
        }
    }

    @Test
    fun `a prepared quick fix refuses changes elsewhere in the same document`() {
        myFixture.configureByText("Stale.res", "let run = () => {\n<caret>let count = ref(0)\ncount.contents\n}")
        myFixture.doHighlighting()
        val action = myFixture.findSingleIntention("Remove unnecessary ref")
        WriteCommandAction.runWriteCommandAction(project) {
            val document = myFixture.editor.document
            document.insertString(document.textLength, "\n// changed after inspection")
        }
        val changed = myFixture.editor.document.text
        PsiDocumentManager.getInstance(project).commitDocument(myFixture.editor.document)
        myFixture.launchAction(action)
        assertEquals(changed, myFixture.editor.document.text)
    }
}
