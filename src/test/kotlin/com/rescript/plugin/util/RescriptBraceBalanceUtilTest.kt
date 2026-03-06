package com.rescript.plugin.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RescriptBraceBalanceUtilTest {
    // ── findMatchingBracket ──────────────────────────────────────

    @Test
    fun `findMatchingBracket finds simple match`() {
        assertEquals(2, RescriptBraceBalanceUtil.findMatchingBracket("(a)", 0, '(', ')'))
    }

    @Test
    fun `findMatchingBracket handles nested brackets`() {
        assertEquals(6, RescriptBraceBalanceUtil.findMatchingBracket("(a(b)c)", 0, '(', ')'))
    }

    @Test
    fun `findMatchingBracket handles deeply nested brackets`() {
        assertEquals(7, RescriptBraceBalanceUtil.findMatchingBracket("(((())))ab", 0, '(', ')'))
    }

    @Test
    fun `findMatchingBracket returns null for unmatched`() {
        assertNull(RescriptBraceBalanceUtil.findMatchingBracket("(abc", 0, '(', ')'))
    }

    @Test
    fun `findMatchingBracket returns null for wrong char at index`() {
        assertNull(RescriptBraceBalanceUtil.findMatchingBracket("abc)", 0, '(', ')'))
    }

    @Test
    fun `findMatchingBracket returns null for negative index`() {
        assertNull(RescriptBraceBalanceUtil.findMatchingBracket("()", -1, '(', ')'))
    }

    @Test
    fun `findMatchingBracket returns null for out of bounds index`() {
        assertNull(RescriptBraceBalanceUtil.findMatchingBracket("()", 5, '(', ')'))
    }

    @Test
    fun `findMatchingBracket works with braces`() {
        assertEquals(5, RescriptBraceBalanceUtil.findMatchingBracket("{a{b}}", 0, '{', '}'))
    }

    @Test
    fun `findMatchingBracket starts from non-zero index`() {
        assertEquals(5, RescriptBraceBalanceUtil.findMatchingBracket("xx(ab)yy", 2, '(', ')'))
    }

    // ── findMatchingParen ────────────────────────────────────────

    @Test
    fun `findMatchingParen finds simple parens`() {
        assertEquals(4, RescriptBraceBalanceUtil.findMatchingParen("a(bc)d", 1))
    }

    @Test
    fun `findMatchingParen handles nested parens`() {
        assertEquals(9, RescriptBraceBalanceUtil.findMatchingParen("f(a, g(b))", 1))
    }

    @Test
    fun `findMatchingParen returns null when unmatched`() {
        assertNull(RescriptBraceBalanceUtil.findMatchingParen("f(a, b", 1))
    }

    @Test
    fun `findMatchingParen returns null for negative index`() {
        assertNull(RescriptBraceBalanceUtil.findMatchingParen("()", -1))
    }

    // ── findMatchingBrace ────────────────────────────────────────

    @Test
    fun `findMatchingBrace finds simple braces`() {
        assertEquals(4, RescriptBraceBalanceUtil.findMatchingBrace("{abc}", 0))
    }

    @Test
    fun `findMatchingBrace handles nested braces`() {
        assertEquals(8, RescriptBraceBalanceUtil.findMatchingBrace("{ a { } }", 0))
    }

    @Test
    fun `findMatchingBrace returns null when unmatched`() {
        assertNull(RescriptBraceBalanceUtil.findMatchingBrace("{ abc", 0))
    }

    // ── skipWhitespace / skipWhitespaceBackward ──────────────────
    // PSI-based methods require IDE test infrastructure (PsiElement mocking).
    // Tested indirectly via RescriptHighlightUsagesHandlerFactory.
}
