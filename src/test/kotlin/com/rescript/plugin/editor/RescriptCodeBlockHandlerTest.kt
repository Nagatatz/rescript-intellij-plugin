package com.rescript.plugin.editor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class RescriptCodeBlockHandlerTest {
    @Test
    fun `handler can be instantiated`() {
        val handler = RescriptCodeBlockHandler()
        assertNotNull(handler)
    }

    @Test
    fun `findMatchingClose finds matching brace`() {
        val text = "{ hello }"
        assertEquals(8, RescriptCodeBlockHandler.findMatchingClose(text, 0, '{', '}'))
    }

    @Test
    fun `findMatchingClose handles nested braces`() {
        val text = "{ { inner } }"
        assertEquals(12, RescriptCodeBlockHandler.findMatchingClose(text, 0, '{', '}'))
    }

    @Test
    fun `findMatchingClose handles brackets`() {
        val text = "[1, [2, 3], 4]"
        assertEquals(13, RescriptCodeBlockHandler.findMatchingClose(text, 0, '[', ']'))
    }

    @Test
    fun `findMatchingClose handles parentheses`() {
        val text = "(a, (b, c))"
        assertEquals(10, RescriptCodeBlockHandler.findMatchingClose(text, 0, '(', ')'))
    }

    @Test
    fun `findMatchingClose returns -1 for unmatched`() {
        val text = "{ no close"
        assertEquals(-1, RescriptCodeBlockHandler.findMatchingClose(text, 0, '{', '}'))
    }

    @Test
    fun `findMatchingClose skips braces in strings`() {
        val text = """{ "}" }"""
        assertEquals(6, RescriptCodeBlockHandler.findMatchingClose(text, 0, '{', '}'))
    }
}
