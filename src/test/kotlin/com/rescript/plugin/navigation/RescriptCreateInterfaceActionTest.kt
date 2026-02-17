package com.rescript.plugin.navigation

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class RescriptCreateInterfaceActionTest : BasePlatformTestCase() {
    private val action = RescriptCreateInterfaceAction()

    fun testEnabledForResFile() {
        val resFile = myFixture.addFileToProject("Foo.res", "let x = 1")
        val event = createEvent(resFile.virtualFile)

        action.update(event)

        assertTrue(event.presentation.isEnabledAndVisible)
    }

    fun testDisabledForResiFile() {
        val resiFile = myFixture.addFileToProject("Foo.resi", "let x: int")
        val event = createEvent(resiFile.virtualFile)

        action.update(event)

        assertFalse(event.presentation.isEnabledAndVisible)
    }

    fun testDisabledForNonRescriptFile() {
        val txtFile = myFixture.addFileToProject("test.txt", "hello")
        val event = createEvent(txtFile.virtualFile)

        action.update(event)

        assertFalse(event.presentation.isEnabledAndVisible)
    }

    fun testDisabledWhenNoFile() {
        val event = createEvent(null)

        action.update(event)

        assertFalse(event.presentation.isEnabledAndVisible)
    }

    fun testActionUpdateThreadIsBgt() {
        assertEquals(ActionUpdateThread.BGT, action.actionUpdateThread)
    }

    private fun createEvent(file: com.intellij.openapi.vfs.VirtualFile?): AnActionEvent {
        val builder =
            SimpleDataContext
                .builder()
                .add(CommonDataKeys.PROJECT, project)
        if (file != null) {
            builder.add(CommonDataKeys.VIRTUAL_FILE, file)
        }
        val context = builder.build()
        @Suppress("DEPRECATION")
        return AnActionEvent.createFromDataContext("test", Presentation(), context)
    }
}
