package com.rescript.plugin.highlight

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import com.rescript.plugin.lang.RescriptTokenTypes as T

class RescriptBraceMatcherTest {
    private val matcher = RescriptBraceMatcher()

    @Test
    fun testPairsContainsThreeEntries() {
        assertEquals(3, matcher.pairs.size)
    }

    @Test
    fun testBracePairExists() {
        val pair = matcher.pairs.find { it.leftBraceType == T.LBRACE }
        assertNotNull(pair)
        assertEquals(T.RBRACE, pair!!.rightBraceType)
    }

    @Test
    fun testBracketPairExists() {
        val pair = matcher.pairs.find { it.leftBraceType == T.LBRACKET }
        assertNotNull(pair)
        assertEquals(T.RBRACKET, pair!!.rightBraceType)
    }

    @Test
    fun testParenPairExists() {
        val pair = matcher.pairs.find { it.leftBraceType == T.LPAREN }
        assertNotNull(pair)
        assertEquals(T.RPAREN, pair!!.rightBraceType)
    }

    @Test
    fun testBracePairIsStructural() {
        val pair = matcher.pairs.find { it.leftBraceType == T.LBRACE }
        assertTrue(pair!!.isStructural)
    }

    @Test
    fun testBracketPairIsNotStructural() {
        val pair = matcher.pairs.find { it.leftBraceType == T.LBRACKET }
        assertFalse(pair!!.isStructural)
    }

    @Test
    fun testParenPairIsNotStructural() {
        val pair = matcher.pairs.find { it.leftBraceType == T.LPAREN }
        assertFalse(pair!!.isStructural)
    }

    @Test
    fun testIsPairedBracesAllowedBeforeTypeAlwaysTrue() {
        assertTrue(matcher.isPairedBracesAllowedBeforeType(T.LBRACE, null))
        assertTrue(matcher.isPairedBracesAllowedBeforeType(T.LBRACE, T.LIDENT))
        assertTrue(matcher.isPairedBracesAllowedBeforeType(T.LPAREN, T.RPAREN))
    }

    @Test
    fun testGetCodeConstructStartReturnsOffset() {
        assertEquals(0, matcher.getCodeConstructStart(null, 0))
        assertEquals(42, matcher.getCodeConstructStart(null, 42))
        assertEquals(100, matcher.getCodeConstructStart(null, 100))
    }
}
