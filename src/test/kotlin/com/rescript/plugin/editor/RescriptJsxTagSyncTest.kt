package com.rescript.plugin.editor

import com.intellij.openapi.command.undo.UndoManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.rescript.plugin.IntelliJPlatformExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Editor-integration smoke test for paired JSX tag synchronized rename.
 *
 * The synchronized-rename wiring lives in [RescriptTypedHandler.beforeCharTyped] /
 * [RescriptTypedHandler.charTyped], which cannot be driven reliably by a light parsing
 * fixture — `TypedHandlerDelegate` only fires through the real typed-action pipeline.
 * This single heavy-fixture test exercises that pipeline end to end; the edit-computation
 * logic itself is covered exhaustively by the pure-function tests in
 * `RescriptJsxTagPairUtilTest`.
 *
 * @see RescriptTypedHandler
 * @see com.rescript.plugin.lang.psi.RescriptJsxTagPairUtil
 */
@ExtendWith(IntelliJPlatformExtension::class)
class RescriptJsxTagSyncTest {
    private lateinit var myFixture: CodeInsightTestFixture
    private lateinit var project: Project

    @Test
    fun testTypingMirrorsOpenNameOntoCloseTag() {
        // Caret sits at the end of the open tag name; typing extends "div" to "divx".
        myFixture.configureByText("Sync.res", "let x = <div<caret>></div>")
        myFixture.type("x")
        assertEquals("let x = <divx></divx>", myFixture.editor.document.text)
    }

    @Test
    fun testSyncedRenameUndoesInOneStep() {
        myFixture.configureByText("Sync.res", "let x = <div<caret>></div>")
        myFixture.type("x")
        assertEquals("let x = <divx></divx>", myFixture.editor.document.text)

        val undoManager = UndoManager.getInstance(project)
        val fileEditor = FileEditorManager.getInstance(project).getSelectedEditor(myFixture.file.virtualFile)
        assertTrue(undoManager.isUndoAvailable(fileEditor))
        undoManager.undo(fileEditor)

        // A single undo must restore both the typed character and its mirrored edit.
        assertEquals("let x = <div></div>", myFixture.editor.document.text)
    }
}
