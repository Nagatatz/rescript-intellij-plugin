package com.rescript.plugin.diagram

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Verifies that [DotLabelEscaping.escapeDotLabel] correctly escapes all
 * characters that would break a DOT quoted-label (`"…"` syntax).
 */
class DotLabelEscapingTest {
    @Test
    fun `plain text passes through unchanged`() {
        assertEquals("hello world", DotLabelEscaping.escapeDotLabel("hello world"))
    }

    @Test
    fun `double quote is escaped`() {
        assertEquals("say \\\"hi\\\"", DotLabelEscaping.escapeDotLabel("say \"hi\""))
    }

    @Test
    fun `backslash is escaped`() {
        assertEquals("a\\\\b", DotLabelEscaping.escapeDotLabel("a\\b"))
    }

    @Test
    fun `newline is escaped`() {
        assertEquals("line1\\nline2", DotLabelEscaping.escapeDotLabel("line1\nline2"))
    }

    @Test
    fun `backslash is escaped before quote so double-escape is correct`() {
        // Input: \" (backslash then double-quote)
        // Expected DOT output: \\\" (escaped backslash, then escaped quote)
        assertEquals("\\\\\\\"", DotLabelEscaping.escapeDotLabel("\\\""))
    }

    @Test
    fun `all three special characters together`() {
        val input = "a\\b\"c\nd"
        val expected = "a\\\\b\\\"c\\nd"
        assertEquals(expected, DotLabelEscaping.escapeDotLabel(input))
    }

    @Test
    fun `empty string returns empty`() {
        assertEquals("", DotLabelEscaping.escapeDotLabel(""))
    }
}
