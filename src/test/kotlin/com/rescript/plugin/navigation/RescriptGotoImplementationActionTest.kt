package com.rescript.plugin.navigation

import com.intellij.openapi.actionSystem.ActionUpdateThread
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class RescriptGotoImplementationActionTest {
    @Test
    fun testActionUpdateThreadIsBGT() {
        val action = RescriptGotoImplementationAction()
        assertEquals(ActionUpdateThread.BGT, action.actionUpdateThread)
    }

    @Test
    fun testActionCanBeInstantiated() {
        val action = RescriptGotoImplementationAction()
        assertNotNull(action)
    }
}
