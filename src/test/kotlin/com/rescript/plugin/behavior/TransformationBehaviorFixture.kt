package com.rescript.plugin.behavior

import com.intellij.openapi.command.undo.UndoManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue

/**
 * Checks real document application, committed PSI text, one-command undo and optional observations.
 * Observations may compile and execute each source snapshot in a dedicated SDK/CLI task;
 * committed PSI alone is never presented as proof of compiler or semantic correctness.
 */
class TransformationBehaviorFixture(
    private val fixture: CodeInsightTestFixture,
) {
    /**
     * Invokes a registered action through the SDK and checks its document and undo behavior.
     *
     * @param actionName the registered Intention text
     * @param expected the exact source after application
     * @param observe optional compiler/runtime observation for before, after and undo
     */
    fun registeredIntentionAndUndo(
        actionName: String,
        expected: String,
        observe: ((String) -> String)? = null,
    ) {
        appliedAndUndo(expected, observe) {
            fixture.launchAction(fixture.findSingleIntention(actionName))
        }
    }

    /**
     * Exercises an actual public handler/action and requires one undo to restore the full source.
     *
     * @param expected the exact document after the operation
     * @param observe optional compiler/runtime observation, kept out of ordinary unit tasks
     * @param apply the production entry point, including its standard write command
     */
    fun appliedAndUndo(
        expected: String,
        observe: ((String) -> String)? = null,
        apply: () -> Unit,
    ) {
        val original = fixture.editor.document.text
        assertNotEquals(original, expected, "A positive case must apply an actual change")
        val before = observe?.invoke(original)
        apply()
        committedSource(expected)
        if (observe != null) assertEquals(before, observe(expected), "After observation differs from before")
        val undo = UndoManager.getInstance(fixture.project)
        val editor = FileEditorManager.getInstance(fixture.project).getSelectedEditor(fixture.file.virtualFile)
        assertTrue(undo.isUndoAvailable(editor), "The production operation must create one undoable command")
        undo.undo(editor)
        committedSource(original)
        if (observe != null) assertEquals(before, observe(original), "Undo observation differs from before")
    }

    /**
     * Requires an unsupported operation to preserve its complete document and optional observation.
     *
     * @param observe optional compiler/runtime observation for before and unchanged after
     * @param apply the production invocation, even when availability refused the case
     */
    fun unchanged(
        observe: ((String) -> String)? = null,
        apply: () -> Unit,
    ) {
        val original = fixture.editor.document.text
        val before = observe?.invoke(original)
        apply()
        committedSource(original)
        if (observe != null) assertEquals(before, observe(original), "Refused observation differs from before")
    }

    /** Commits the document and confirms the resulting PSI reflects exactly the expected source. */
    private fun committedSource(expected: String) {
        val document = fixture.editor.document
        assertEquals(expected, document.text)
        PsiDocumentManager.getInstance(fixture.project).commitDocument(document)
        assertTrue(fixture.file.isValid)
        assertEquals(expected, fixture.file.text, "Committed PSI must reflect the changed document")
    }
}
