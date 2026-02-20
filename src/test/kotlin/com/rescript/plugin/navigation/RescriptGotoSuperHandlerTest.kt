package com.rescript.plugin.navigation

import com.intellij.codeInsight.CodeInsightActionHandler
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptGotoSuperHandlerTest {
    @Test
    fun `instance can be created`() {
        val handler = RescriptGotoSuperHandler()
        assertNotNull(handler)
    }

    @Test
    fun `is a CodeInsightActionHandler`() {
        val handler = RescriptGotoSuperHandler()
        assertTrue(handler is CodeInsightActionHandler)
    }

    @Test
    fun `startInWriteAction returns false`() {
        val handler = RescriptGotoSuperHandler()
        assertFalse(handler.startInWriteAction())
    }
}
