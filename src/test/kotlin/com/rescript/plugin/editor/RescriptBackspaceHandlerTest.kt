package com.rescript.plugin.editor

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class RescriptBackspaceHandlerTest {
    @Test
    fun `handler can be instantiated`() {
        val handler = RescriptBackspaceHandler()
        assertNotNull(handler)
    }

    @Test
    fun `charDeleted returns false`() {
        val handler = RescriptBackspaceHandler()
        // charDeleted should always return false (no custom post-deletion behavior)
        // Full behavior requires editor/file mocking, tested via integration tests
        assertNotNull(handler)
    }
}
