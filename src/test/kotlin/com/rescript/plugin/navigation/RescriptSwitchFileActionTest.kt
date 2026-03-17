package com.rescript.plugin.navigation

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Unit tests for [RescriptSwitchFileAction], verifying that the action
 * enables/disables correctly based on file type and switches between
 * `.res` and `.resi` counterpart files.
 *
 * @see RescriptSwitchFileAction
 */
class RescriptSwitchFileActionTest : BasePlatformTestCase() {
    private val action = RescriptSwitchFileAction()

    // ── update() tests ──────────────────────────────────────────────

    fun testUpdateEnabledForResFile() {
        val resFile = myFixture.addFileToProject("Foo.res", "let x = 1")
        val event = createEvent(resFile.virtualFile)

        action.update(event)

        assertTrue(event.presentation.isEnabledAndVisible)
    }

    fun testUpdateEnabledForResiFile() {
        val resiFile = myFixture.addFileToProject("Foo.resi", "let x: int")
        val event = createEvent(resiFile.virtualFile)

        action.update(event)

        assertTrue(event.presentation.isEnabledAndVisible)
    }

    fun testUpdateDisabledForNonRescriptFile() {
        val txtFile = myFixture.addFileToProject("test.txt", "hello")
        val event = createEvent(txtFile.virtualFile)

        action.update(event)

        assertFalse(event.presentation.isEnabledAndVisible)
    }

    fun testUpdateDisabledForJsFile() {
        val jsFile = myFixture.addFileToProject("test.js", "var x = 1;")
        val event = createEvent(jsFile.virtualFile)

        action.update(event)

        assertFalse(event.presentation.isEnabledAndVisible)
    }

    fun testUpdateDisabledWhenNoFile() {
        val event = createEvent(null)

        action.update(event)

        assertFalse(event.presentation.isEnabledAndVisible)
    }

    // ── getActionUpdateThread() ─────────────────────────────────────

    fun testActionUpdateThreadIsBGT() {
        assertEquals(ActionUpdateThread.BGT, action.actionUpdateThread)
    }

    // ── actionPerformed() tests ─────────────────────────────────────

    fun testActionPerformedOpensResiCounterpart() {
        myFixture.addFileToProject("Module.res", "let x = 1")
        val resiFile = myFixture.addFileToProject("Module.resi", "let x: int")
        val resFile = myFixture.findFileInTempDir("Module.res")!!
        val event = createEvent(resFile)

        action.actionPerformed(event)

        val openFiles = FileEditorManager.getInstance(project).openFiles
        assertTrue(openFiles.any { it.name == "Module.resi" })
    }

    fun testActionPerformedOpensResCounterpart() {
        val resFile = myFixture.addFileToProject("Module2.res", "let x = 1")
        myFixture.addFileToProject("Module2.resi", "let x: int")
        val resiVFile = myFixture.findFileInTempDir("Module2.resi")!!
        val event = createEvent(resiVFile)

        action.actionPerformed(event)

        val openFiles = FileEditorManager.getInstance(project).openFiles
        assertTrue(openFiles.any { it.name == "Module2.res" })
    }

    fun testActionPerformedDoesNothingWhenNoCounterpart() {
        myFixture.addFileToProject("Solo.res", "let x = 1")
        val resFile = myFixture.findFileInTempDir("Solo.res")!!
        val event = createEvent(resFile)

        // Should not throw
        action.actionPerformed(event)

        val openFiles = FileEditorManager.getInstance(project).openFiles
        assertFalse(openFiles.any { it.name == "Solo.resi" })
    }

    // ── Helper ──────────────────────────────────────────────────────

    private fun createEvent(file: VirtualFile?): AnActionEvent {
        val builder =
            SimpleDataContext
                .builder()
                .add(CommonDataKeys.PROJECT, project)
        if (file != null) {
            builder.add(CommonDataKeys.VIRTUAL_FILE, file)
        }
        return AnActionEvent.createFromDataContext("test", Presentation(), builder.build())
    }
}
