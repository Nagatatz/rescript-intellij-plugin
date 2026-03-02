package com.rescript.plugin.refactor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptInlineHandlerTest {
    @Test
    fun `findUsages finds simple variable usage`() {
        val source = "let x = 42\nlet y = x + 1"
        // Declaration is "let x = 42" (offsets 0..9), usage of x is at offset 18
        val usages = RescriptInlineHandler.findUsages(source, "x", 0, 10)
        assertTrue(usages.isNotEmpty())
    }

    @Test
    fun `findUsages does not match partial names`() {
        val source = "let x = 42\nlet xy = 1"
        // Declaration line is 0..10
        val usages = RescriptInlineHandler.findUsages(source, "x", 0, 10)
        // "xy" should not be matched as a usage of "x" due to word boundary matching
        assertTrue(usages.isEmpty())
    }

    @Test
    fun `needsParentheses returns true when preceded by operator`() {
        // Operator immediately adjacent to the variable name
        val text = "a+x"
        // "x" is at offset 2, length 1
        assertTrue(RescriptInlineHandler.needsParentheses(text, 2, 1))
    }

    @Test
    fun `needsParentheses returns false for standalone identifier`() {
        val text = "let y = x"
        // "x" is at offset 8, length 1
        assertFalse(RescriptInlineHandler.needsParentheses(text, 8, 1))
    }

    @Test
    fun `needsParentheses returns true when followed by operator`() {
        // Operator immediately after the variable name
        val text = "x+1"
        // "x" is at offset 0, length 1
        assertTrue(RescriptInlineHandler.needsParentheses(text, 0, 1))
    }

    @Test
    fun `needsParentheses returns false in function call context`() {
        val text = "foo(x)"
        // "x" is at offset 4, length 1
        assertFalse(RescriptInlineHandler.needsParentheses(text, 4, 1))
    }

    @Test
    fun `findUsages excludes declaration line`() {
        val source = "let value = 42\nlet result = value + 1\nlet other = value"
        // Declaration line is "let value = 42" (offsets 0..13)
        val usages = RescriptInlineHandler.findUsages(source, "value", 0, 13)
        // Should find usages only in the other lines
        assertEquals(2, usages.size)
    }
}
