package com.rescript.plugin.editor

import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegateAdapter
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptEnterHandlerTest {
    @Test
    fun `instance can be created`() {
        val handler = RescriptEnterHandler()
        assertNotNull(handler)
    }

    @Test
    fun `is an EnterHandlerDelegateAdapter`() {
        val handler = RescriptEnterHandler()
        assertTrue(handler is EnterHandlerDelegateAdapter)
    }
}
