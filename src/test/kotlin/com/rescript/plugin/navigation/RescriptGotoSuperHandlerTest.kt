package com.rescript.plugin.navigation

import com.intellij.codeInsight.CodeInsightActionHandler
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptGotoSuperHandlerTest {
    @Test
    fun `instance can be created`() {
        val handler = RescriptGotoSuperHandler()
        assertNotNull(handler)
    }

    @Test
    fun `is a CodeInsightActionHandler`() {
        val handler: Any = RescriptGotoSuperHandler()
        assertTrue(handler is CodeInsightActionHandler)
    }

    @Test
    fun `startInWriteAction returns false`() {
        val handler = RescriptGotoSuperHandler()
        assertFalse(handler.startInWriteAction())
    }
}
