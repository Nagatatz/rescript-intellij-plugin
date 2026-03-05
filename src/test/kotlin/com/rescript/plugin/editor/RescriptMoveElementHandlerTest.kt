package com.rescript.plugin.editor

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class RescriptMoveElementHandlerTest {
    @Test
    fun `handler can be instantiated`() {
        val handler = RescriptMoveElementHandler()
        assertNotNull(handler)
    }
}
