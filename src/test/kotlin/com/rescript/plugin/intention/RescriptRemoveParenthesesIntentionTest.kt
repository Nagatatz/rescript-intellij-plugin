package com.rescript.plugin.intention

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Tests for [RescriptRemoveParenthesesIntention] static helper methods. */
class RescriptRemoveParenthesesIntentionTest {
    @Test
    fun `findEnclosingParens finds simple parens`() {
        val text = "let x = (42)"
        // offset inside parens (at '4')
        val result = RescriptRemoveParenthesesIntention.findEnclosingParens(text, 9)
        assertNotNull(result)
        assertEquals(8, result!!.first)
        assertEquals(11, result.second)
    }

    @Test
    fun `findEnclosingParens returns null when no parens`() {
        val text = "let x = 42"
        val result = RescriptRemoveParenthesesIntention.findEnclosingParens(text, 8)
        assertNull(result)
    }

    @Test
    fun `findEnclosingParens handles nested parens`() {
        val text = "let x = (foo(1))"
        // offset at 'f' in foo
        val result = RescriptRemoveParenthesesIntention.findEnclosingParens(text, 9)
        assertNotNull(result)
        assertEquals(8, result!!.first)
        assertEquals(15, result.second)
    }

    @Test
    fun `isRemovable returns true for simple expression`() {
        val text = "let x = (42)"
        assertTrue(RescriptRemoveParenthesesIntention.isRemovable(text, 8, 11, "42"))
    }

    @Test
    fun `isRemovable returns false for tuple`() {
        val text = "let x = (1, 2)"
        assertFalse(RescriptRemoveParenthesesIntention.isRemovable(text, 8, 13, "1, 2"))
    }

    @Test
    fun `isRemovable returns false for function call`() {
        val text = "foo(42)"
        assertFalse(RescriptRemoveParenthesesIntention.isRemovable(text, 3, 6, "42"))
    }

    @Test
    fun `isRemovable returns false for operator expression`() {
        val text = "let x = (a + b)"
        assertFalse(RescriptRemoveParenthesesIntention.isRemovable(text, 8, 14, "a + b"))
    }

    @Test
    fun `containsTopLevelComma returns true for comma at top level`() {
        assertTrue(RescriptRemoveParenthesesIntention.containsTopLevelComma("a, b"))
    }

    @Test
    fun `containsTopLevelComma returns false for comma in nested parens`() {
        assertFalse(RescriptRemoveParenthesesIntention.containsTopLevelComma("foo(a, b)"))
    }

    @Test
    fun `containsTopLevelOperator returns true for plus`() {
        assertTrue(RescriptRemoveParenthesesIntention.containsTopLevelOperator("a + b"))
    }

    @Test
    fun `containsTopLevelOperator returns false for nested operator`() {
        assertFalse(RescriptRemoveParenthesesIntention.containsTopLevelOperator("foo(a + b)"))
    }

    @Test
    fun `containsTopLevelOperator returns true for logical and`() {
        assertTrue(RescriptRemoveParenthesesIntention.containsTopLevelOperator("a && b"))
    }

    @Test
    fun `containsTopLevelOperator returns true for logical or`() {
        assertTrue(RescriptRemoveParenthesesIntention.containsTopLevelOperator("a || b"))
    }

    @Test
    fun `containsTopLevelOperator returns true for equality`() {
        assertTrue(RescriptRemoveParenthesesIntention.containsTopLevelOperator("a == b"))
    }

    @Test
    fun `containsTopLevelOperator returns false for simple identifier`() {
        assertFalse(RescriptRemoveParenthesesIntention.containsTopLevelOperator("myVar"))
    }
}
