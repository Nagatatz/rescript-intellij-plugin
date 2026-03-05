package com.rescript.plugin.lang

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptTokenTypesTest {
    // ── KEYWORDS TokenSet ──

    @Test
    fun testKeywordsContainsAllExpectedTokens() {
        val expected =
            listOf(
                RescriptTokenTypes.ASYNC,
                RescriptTokenTypes.AWAIT,
                RescriptTokenTypes.AND,
                RescriptTokenTypes.AS,
                RescriptTokenTypes.ASSERT,
                RescriptTokenTypes.BEGIN,
                RescriptTokenTypes.CATCH,
                RescriptTokenTypes.CLASS,
                RescriptTokenTypes.CONSTRAINT,
                RescriptTokenTypes.DO,
                RescriptTokenTypes.DONE,
                RescriptTokenTypes.DOWNTO,
                RescriptTokenTypes.ELSE,
                RescriptTokenTypes.END,
                RescriptTokenTypes.EXCEPTION,
                RescriptTokenTypes.EXTERNAL,
                RescriptTokenTypes.FOR,
                RescriptTokenTypes.FUNCTOR,
                RescriptTokenTypes.IF,
                RescriptTokenTypes.IN,
                RescriptTokenTypes.INCLUDE,
                RescriptTokenTypes.INHERIT,
                RescriptTokenTypes.INITIALIZER,
                RescriptTokenTypes.LAZY,
                RescriptTokenTypes.LET,
                RescriptTokenTypes.LIST,
                RescriptTokenTypes.DICT,
                RescriptTokenTypes.MODULE,
                RescriptTokenTypes.MUTABLE,
                RescriptTokenTypes.NEW,
                RescriptTokenTypes.NONREC,
                RescriptTokenTypes.OBJECT,
                RescriptTokenTypes.OF,
                RescriptTokenTypes.OPEN,
                RescriptTokenTypes.OR,
                RescriptTokenTypes.PUB,
                RescriptTokenTypes.PRI,
                RescriptTokenTypes.RAW,
                RescriptTokenTypes.FFI,
                RescriptTokenTypes.REC,
                RescriptTokenTypes.SIG,
                RescriptTokenTypes.STRUCT,
                RescriptTokenTypes.SWITCH,
                RescriptTokenTypes.THEN,
                RescriptTokenTypes.TRY,
                RescriptTokenTypes.TYPE,
                RescriptTokenTypes.UNPACK,
                RescriptTokenTypes.VAL,
                RescriptTokenTypes.VIRTUAL,
                RescriptTokenTypes.WHEN,
                RescriptTokenTypes.WHILE,
                RescriptTokenTypes.WITH,
                // Built-ins
                RescriptTokenTypes.UNIT,
                RescriptTokenTypes.REF,
                RescriptTokenTypes.RAISE,
                RescriptTokenTypes.METHOD,
                RescriptTokenTypes.PRIVATE,
                RescriptTokenTypes.MATCH,
                RescriptTokenTypes.OPTION,
                RescriptTokenTypes.NONE,
                RescriptTokenTypes.SOME,
                // Boolean
                RescriptTokenTypes.BOOL_VALUE,
            )
        for (token in expected) {
            assertTrue(RescriptTokenTypes.KEYWORDS.contains(token), "KEYWORDS should contain $token")
        }
        assertEquals(expected.size, RescriptTokenTypes.KEYWORDS.types.size)
    }

    @Test
    fun testKeywordsCount() {
        assertEquals(62, RescriptTokenTypes.KEYWORDS.types.size)
    }

    @Test
    fun testKeywordsDoesNotContainIdentifiers() {
        assertFalse(RescriptTokenTypes.KEYWORDS.contains(RescriptTokenTypes.LIDENT))
        assertFalse(RescriptTokenTypes.KEYWORDS.contains(RescriptTokenTypes.UIDENT))
    }

    // ── OPERATORS TokenSet ──

    @Test
    fun testOperatorsCount() {
        assertEquals(32, RescriptTokenTypes.OPERATORS.types.size)
    }

    @Test
    fun testOperatorsContainsExpectedTokens() {
        assertTrue(RescriptTokenTypes.OPERATORS.contains(RescriptTokenTypes.PIPE_FORWARD))
        assertTrue(RescriptTokenTypes.OPERATORS.contains(RescriptTokenTypes.ARROW))
        assertTrue(RescriptTokenTypes.OPERATORS.contains(RescriptTokenTypes.EQEQ))
        assertTrue(RescriptTokenTypes.OPERATORS.contains(RescriptTokenTypes.PLUS))
        assertTrue(RescriptTokenTypes.OPERATORS.contains(RescriptTokenTypes.MOD))
    }

    @Test
    fun testOperatorsDoesNotContainPunctuation() {
        assertFalse(RescriptTokenTypes.OPERATORS.contains(RescriptTokenTypes.COMMA))
        assertFalse(RescriptTokenTypes.OPERATORS.contains(RescriptTokenTypes.SEMI))
        assertFalse(RescriptTokenTypes.OPERATORS.contains(RescriptTokenTypes.DOT))
    }

    // ── TOP_LEVEL_KEYWORDS TokenSet ──

    @Test
    fun testTopLevelKeywordsCount() {
        assertEquals(7, RescriptTokenTypes.TOP_LEVEL_KEYWORDS.types.size)
    }

    @Test
    fun testTopLevelKeywordsContainsAllDeclarationStarters() {
        assertTrue(RescriptTokenTypes.TOP_LEVEL_KEYWORDS.contains(RescriptTokenTypes.LET))
        assertTrue(RescriptTokenTypes.TOP_LEVEL_KEYWORDS.contains(RescriptTokenTypes.TYPE))
        assertTrue(RescriptTokenTypes.TOP_LEVEL_KEYWORDS.contains(RescriptTokenTypes.MODULE))
        assertTrue(RescriptTokenTypes.TOP_LEVEL_KEYWORDS.contains(RescriptTokenTypes.EXTERNAL))
        assertTrue(RescriptTokenTypes.TOP_LEVEL_KEYWORDS.contains(RescriptTokenTypes.OPEN))
        assertTrue(RescriptTokenTypes.TOP_LEVEL_KEYWORDS.contains(RescriptTokenTypes.INCLUDE))
        assertTrue(RescriptTokenTypes.TOP_LEVEL_KEYWORDS.contains(RescriptTokenTypes.EXCEPTION))
    }

    // ── COMMENTS TokenSet ──

    @Test
    fun testCommentsCount() {
        assertEquals(2, RescriptTokenTypes.COMMENTS.types.size)
    }

    // ── NUMBERS TokenSet ──

    @Test
    fun testNumbersCount() {
        assertEquals(2, RescriptTokenTypes.NUMBERS.types.size)
    }

    // ── STRINGS TokenSet ──

    @Test
    fun testStringsCount() {
        assertEquals(3, RescriptTokenTypes.STRINGS.types.size)
    }
}
