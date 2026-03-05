package com.rescript.plugin.editor

import com.intellij.openapi.editor.highlighter.HighlighterIterator
import com.intellij.psi.tree.IElementType
import com.rescript.plugin.lang.RescriptTokenTypes
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptQuoteHandlerTest {
    private val handler = RescriptQuoteHandler()

    /**
     * Creates a stub HighlighterIterator with the given token type, start, and end offsets.
     */
    private fun stubIterator(
        tokenType: IElementType,
        start: Int,
        end: Int,
    ): HighlighterIterator =
        java.lang.reflect.Proxy.newProxyInstance(
            HighlighterIterator::class.java.classLoader,
            arrayOf(HighlighterIterator::class.java),
        ) { _, method, _ ->
            when (method.name) {
                "getTokenType" -> tokenType
                "getStart" -> start
                "getEnd" -> end
                "atEnd" -> false
                "toString" -> "StubHighlighterIterator($tokenType, $start, $end)"
                "hashCode" -> tokenType.hashCode()
                "equals" -> false
                else -> null
            }
        } as HighlighterIterator

    // ── isOpeningQuote ─────────────────────────────────────────────

    @Test
    fun `isOpeningQuote returns true for STRING_VALUE at start`() {
        val iter = stubIterator(RescriptTokenTypes.STRING_VALUE, 5, 12)
        assertTrue(handler.isOpeningQuote(iter, 5))
    }

    @Test
    fun `isOpeningQuote returns false for STRING_VALUE not at start`() {
        val iter = stubIterator(RescriptTokenTypes.STRING_VALUE, 5, 12)
        assertFalse(handler.isOpeningQuote(iter, 6))
    }

    @Test
    fun `isOpeningQuote returns true for JS_STRING_OPEN at start`() {
        val iter = stubIterator(RescriptTokenTypes.JS_STRING_OPEN, 0, 2)
        assertTrue(handler.isOpeningQuote(iter, 0))
    }

    @Test
    fun `isOpeningQuote returns true for JS_STRING_CLOSE at start`() {
        val iter = stubIterator(RescriptTokenTypes.JS_STRING_CLOSE, 10, 12)
        assertTrue(handler.isOpeningQuote(iter, 10))
    }

    @Test
    fun `isOpeningQuote returns false for unrelated token type`() {
        val iter = stubIterator(RescriptTokenTypes.LIDENT, 0, 5)
        assertFalse(handler.isOpeningQuote(iter, 0))
    }

    @Test
    fun `isOpeningQuote returns false for keyword token`() {
        val iter = stubIterator(RescriptTokenTypes.LET, 0, 3)
        assertFalse(handler.isOpeningQuote(iter, 0))
    }

    // ── isClosingQuote ─────────────────────────────────────────────

    @Test
    fun `isClosingQuote returns true for STRING_VALUE at end minus one`() {
        // Token from offset 5 to 12, closing quote at offset 11 (end - 1)
        val iter = stubIterator(RescriptTokenTypes.STRING_VALUE, 5, 12)
        assertTrue(handler.isClosingQuote(iter, 11))
    }

    @Test
    fun `isClosingQuote returns false for STRING_VALUE not at end minus one`() {
        val iter = stubIterator(RescriptTokenTypes.STRING_VALUE, 5, 12)
        assertFalse(handler.isClosingQuote(iter, 10))
    }

    @Test
    fun `isClosingQuote returns true for JS_STRING_OPEN at end minus one`() {
        val iter = stubIterator(RescriptTokenTypes.JS_STRING_OPEN, 0, 2)
        assertTrue(handler.isClosingQuote(iter, 1))
    }

    @Test
    fun `isClosingQuote returns true for JS_STRING_CLOSE at end minus one`() {
        val iter = stubIterator(RescriptTokenTypes.JS_STRING_CLOSE, 10, 12)
        assertTrue(handler.isClosingQuote(iter, 11))
    }

    @Test
    fun `isClosingQuote returns false for unrelated token type`() {
        val iter = stubIterator(RescriptTokenTypes.LIDENT, 0, 5)
        assertFalse(handler.isClosingQuote(iter, 4))
    }

    @Test
    fun `isClosingQuote returns false for keyword token`() {
        val iter = stubIterator(RescriptTokenTypes.LET, 0, 3)
        assertFalse(handler.isClosingQuote(iter, 2))
    }

    // ── isInsideLiteral ────────────────────────────────────────────

    @Test
    fun `isInsideLiteral returns true for STRING_VALUE`() {
        val iter = stubIterator(RescriptTokenTypes.STRING_VALUE, 0, 10)
        assertTrue(handler.isInsideLiteral(iter))
    }

    @Test
    fun `isInsideLiteral returns true for JS_STRING_OPEN`() {
        val iter = stubIterator(RescriptTokenTypes.JS_STRING_OPEN, 0, 2)
        assertTrue(handler.isInsideLiteral(iter))
    }

    @Test
    fun `isInsideLiteral returns true for JS_STRING_CLOSE`() {
        val iter = stubIterator(RescriptTokenTypes.JS_STRING_CLOSE, 0, 2)
        assertTrue(handler.isInsideLiteral(iter))
    }

    @Test
    fun `isInsideLiteral returns false for non-string token`() {
        val iter = stubIterator(RescriptTokenTypes.LIDENT, 0, 5)
        assertFalse(handler.isInsideLiteral(iter))
    }

    @Test
    fun `isInsideLiteral returns false for INT_VALUE`() {
        val iter = stubIterator(RescriptTokenTypes.INT_VALUE, 0, 3)
        assertFalse(handler.isInsideLiteral(iter))
    }

    @Test
    fun `isInsideLiteral returns false for SINGLE_COMMENT`() {
        val iter = stubIterator(RescriptTokenTypes.SINGLE_COMMENT, 0, 10)
        assertFalse(handler.isInsideLiteral(iter))
    }

    // ── Edge cases ─────────────────────────────────────────────────

    @Test
    fun `isClosingQuote with single-char token returns true at offset zero`() {
        // Token spanning exactly 1 character: start=3, end=4, offset=3 (end - 1)
        val iter = stubIterator(RescriptTokenTypes.STRING_VALUE, 3, 4)
        assertTrue(handler.isClosingQuote(iter, 3))
    }

    @Test
    fun `isOpeningQuote at offset zero with token starting at zero`() {
        val iter = stubIterator(RescriptTokenTypes.STRING_VALUE, 0, 7)
        assertTrue(handler.isOpeningQuote(iter, 0))
    }

    @Test
    fun `isClosingQuote returns false at start of token`() {
        val iter = stubIterator(RescriptTokenTypes.STRING_VALUE, 5, 12)
        assertFalse(handler.isClosingQuote(iter, 5))
    }

    @Test
    fun `isOpeningQuote returns false at end of token`() {
        val iter = stubIterator(RescriptTokenTypes.STRING_VALUE, 5, 12)
        assertFalse(handler.isOpeningQuote(iter, 11))
    }
}
