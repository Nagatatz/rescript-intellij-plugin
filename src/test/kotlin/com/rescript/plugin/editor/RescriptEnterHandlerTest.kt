package com.rescript.plugin.editor

import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegateAdapter
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptEnterHandlerTest {
    @Test
    fun `instance can be created`() {
        val handler = RescriptEnterHandler()
        assertNotNull(handler)
    }

    @Test
    fun `is an EnterHandlerDelegateAdapter`() {
        val handler: Any = RescriptEnterHandler()
        assertTrue(handler is EnterHandlerDelegateAdapter)
    }
}
