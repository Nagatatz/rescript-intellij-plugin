package com.rescript.plugin.navigation

import com.intellij.openapi.actionSystem.ActionUpdateThread
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

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
