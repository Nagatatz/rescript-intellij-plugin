package com.rescript.plugin.lsp

import com.intellij.openapi.actionSystem.ActionUpdateThread
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Tests for [RescriptRestartLspAction].
 */
class RescriptRestartLspActionTest {
    @Test
    fun `action can be instantiated`() {
        val action = RescriptRestartLspAction()
        assertNotNull(action)
    }

    @Test
    fun `getActionUpdateThread returns BGT`() {
        val action = RescriptRestartLspAction()
        assertEquals(ActionUpdateThread.BGT, action.getActionUpdateThread())
    }
}
