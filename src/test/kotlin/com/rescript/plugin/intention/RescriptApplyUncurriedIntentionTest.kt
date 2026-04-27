package com.rescript.plugin.intention

import com.rescript.plugin.intention.RescriptApplyUncurriedIntention.Companion.findCallExpressionAt
import com.rescript.plugin.intention.RescriptApplyUncurriedIntention.Companion.insertDotAfterOpenParen
import com.rescript.plugin.intention.RescriptApplyUncurriedIntention.Companion.isAlreadyUncurried
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** Tests for [RescriptApplyUncurriedIntention] companion helpers. */
class RescriptApplyUncurriedIntentionTest {
    @Test
    fun `findCallExpressionAt locates simple call`() {
        val line = "let result = add(1, 2)"
        // Caret on `add`
        val range = findCallExpressionAt(line, line.indexOf("add"))
        assertNotNull(range)
        assertEquals("add(1, 2)", line.substring(range!!))
    }

    @Test
    fun `findCallExpressionAt returns null when caret is outside any call`() {
        val line = "let result = add(1, 2)"
        // Caret on `result` (before any call expression starts)
        val range = findCallExpressionAt(line, line.indexOf("result"))
        assertNull(range)
    }

    @Test
    fun `findCallExpressionAt handles nested parens`() {
        val line = "let result = wrap(inner(1, 2), 3)"
        val range = findCallExpressionAt(line, line.indexOf("wrap"))
        assertNotNull(range)
        assertEquals("wrap(inner(1, 2), 3)", line.substring(range!!))
    }

    @Test
    fun `findCallExpressionAt skips identifier without parens`() {
        val line = "let result = identity"
        val range = findCallExpressionAt(line, line.indexOf("identity"))
        assertNull(range)
    }

    @Test
    fun `isAlreadyUncurried detects leading dot`() {
        val line = "let r = f(. x, y)"
        val range = findCallExpressionAt(line, line.indexOf("f("))!!
        assertTrue(isAlreadyUncurried(line, range))
    }

    @Test
    fun `isAlreadyUncurried returns false for curried call`() {
        val line = "let r = f(x, y)"
        val range = findCallExpressionAt(line, line.indexOf("f("))!!
        assertFalse(isAlreadyUncurried(line, range))
    }

    @Test
    fun `isAlreadyUncurried tolerates whitespace before dot`() {
        val line = "let r = f(  . x, y)"
        val range = findCallExpressionAt(line, line.indexOf("f("))!!
        assertTrue(isAlreadyUncurried(line, range))
    }

    @Test
    fun `insertDotAfterOpenParen converts curried call`() {
        assertEquals("f(. 1, 2)", insertDotAfterOpenParen("f(1, 2)"))
    }

    @Test
    fun `insertDotAfterOpenParen handles single argument`() {
        assertEquals("foo(. x)", insertDotAfterOpenParen("foo(x)"))
    }

    @Test
    fun `insertDotAfterOpenParen handles zero arguments`() {
        assertEquals("g(.)", insertDotAfterOpenParen("g()"))
    }

    @Test
    fun `insertDotAfterOpenParen handles whitespace-only arg block as zero args`() {
        assertEquals("g(.)", insertDotAfterOpenParen("g(  )"))
    }

    @Test
    fun `insertDotAfterOpenParen returns null when no parens present`() {
        assertNull(insertDotAfterOpenParen("identifier"))
    }

    @Test
    fun `insertDotAfterOpenParen preserves trailing characters after closing paren`() {
        // The helper trims to the last `)`; if the caller passed a wider slice, the
        // trailing characters stay where they were relative to the closing paren.
        assertEquals("f(. x);", insertDotAfterOpenParen("f(x);"))
    }
}
