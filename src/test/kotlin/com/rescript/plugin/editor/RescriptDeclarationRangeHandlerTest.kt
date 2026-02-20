package com.rescript.plugin.editor

import org.junit.Assert.assertEquals
import org.junit.Test

class RescriptDeclarationRangeHandlerTest {
    @Test
    fun `handler instantiation succeeds`() {
        val handler = RescriptDeclarationRangeHandler()
        // Verify the handler can be instantiated (no-arg constructor)
        assertEquals(RescriptDeclarationRangeHandler::class.java, handler.javaClass)
    }
}
