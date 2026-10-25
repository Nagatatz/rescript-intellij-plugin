package com.rescript.plugin.intention

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

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

    @Test
    fun `scalar initializers preserve trivia and literal contents`() {
        for (inner in listOf("42", "true", "name", "/* (ignored) */ 42 // )\n")) {
            val source = "let value = ($inner)"
            val plan = RescriptParenthesesRemovalPlan.create(source, source.indexOf('('))!!
            assertEquals(inner, plan.source.substring(plan.open + 1, plan.close))
        }
        assertFalse(RescriptRemoveParenthesesIntention.containsTopLevelOperator("\"a + b\""))
        assertFalse(RescriptRemoveParenthesesIntention.containsTopLevelComma("\"a, b\""))
        assertTrue(RescriptRemoveParenthesesIntention.containsTopLevelOperator("a < b"))
    }

    @Test
    fun `literal comment call parameter and precedence boundaries refuse edits`() {
        for (marked in listOf(
            "let value = \"(<caret>hello)\"",
            "// (<caret>42)\nlet value = 1",
            "let value = (/* <caret>comment */42)",
            "let value = `(<caret>hello)`",
            "let value = ((<caret>42))",
            "Console.log (<caret>42)",
            "Console.log /* comment */ (<caret>42)",
            "let value = (<caret>)",
            "let value = (<caret>1, 2)",
            "let fn = (<caret>x) => {x}",
            "let (<caret>x) = 1",
            "let value: int = (<caret>42)",
            "let value = (<caret>_)",
            "let value = (<caret>a < b) == true",
            "let value = (<caret>arr->Array.length)",
            "let value = (f(<caret>a + b))",
            "let value = (<caret>-1)",
            "let value = (<caret>42) + 1",
            "let value = (<caret>42)\n->Int.toString",
            "let value = (<caret>42",
            "let value = [<caret>42)",
            "let value = (<caret>\"unfinished)",
        )) {
            val offset = marked.indexOf("<caret>")
            val source = marked.replace("<caret>", "")
            assertNull(RescriptParenthesesRemovalPlan.create(source, offset), marked)
        }
    }
}
