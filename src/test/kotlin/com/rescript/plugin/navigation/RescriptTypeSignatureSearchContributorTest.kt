package com.rescript.plugin.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptTypeSignatureSearchContributorTest {
    @Test
    fun `looksLikeTypeQuery detects arrow type`() {
        assertTrue(RescriptTypeSignatureSearchContributor.looksLikeTypeQuery("string -> int"))
    }

    @Test
    fun `looksLikeTypeQuery detects type keywords`() {
        assertTrue(RescriptTypeSignatureSearchContributor.looksLikeTypeQuery("option"))
        assertTrue(RescriptTypeSignatureSearchContributor.looksLikeTypeQuery("string"))
        assertTrue(RescriptTypeSignatureSearchContributor.looksLikeTypeQuery("array"))
    }

    @Test
    fun `looksLikeTypeQuery rejects plain names`() {
        assertFalse(RescriptTypeSignatureSearchContributor.looksLikeTypeQuery("myFunction"))
    }

    @Test
    fun `tokenizeSignature splits arrow type`() {
        val tokens = RescriptTypeSignatureSearchContributor.tokenizeSignature("string -> int")
        assertTrue(tokens.contains("string"))
        assertTrue(tokens.contains("int"))
        assertTrue(tokens.contains("->"))
    }

    @Test
    fun `tokenizeSignature splits complex type`() {
        val tokens = RescriptTypeSignatureSearchContributor.tokenizeSignature("option<'a> -> 'a")
        assertTrue(tokens.contains("option"))
        assertTrue(tokens.contains("'a"))
    }

    @Test
    fun `tokenizeSignature handles simple type`() {
        val tokens = RescriptTypeSignatureSearchContributor.tokenizeSignature("string")
        assertTrue(tokens.contains("string"))
    }

    @Test
    fun `matchSignature returns positive for matching types`() {
        val query = RescriptTypeSignatureSearchContributor.tokenizeSignature("string -> int")
        val candidate = RescriptTypeSignatureSearchContributor.tokenizeSignature("string -> int")
        val score = RescriptTypeSignatureSearchContributor.matchSignature(query, candidate)
        assertTrue(score > 0)
    }

    @Test
    fun `matchSignature returns zero for non-matching`() {
        val query = RescriptTypeSignatureSearchContributor.tokenizeSignature("string -> int")
        val candidate = RescriptTypeSignatureSearchContributor.tokenizeSignature("bool -> float")
        val score = RescriptTypeSignatureSearchContributor.matchSignature(query, candidate)
        assertEquals(0, score)
    }

    @Test
    fun `matchSignature handles partial match`() {
        val query = RescriptTypeSignatureSearchContributor.tokenizeSignature("string -> int")
        val candidate = RescriptTypeSignatureSearchContributor.tokenizeSignature("string -> int -> bool")
        val score = RescriptTypeSignatureSearchContributor.matchSignature(query, candidate)
        assertTrue(score > 0)
    }

    @Test
    fun `matchSignature returns zero for empty tokens`() {
        val score = RescriptTypeSignatureSearchContributor.matchSignature(emptyList(), emptyList())
        assertEquals(0, score)
    }

    @Test
    fun `extractTypeAnnotation finds colon annotation`() {
        val result =
            RescriptTypeSignatureSearchContributor.extractTypeAnnotation(
                "let add: (int, int) => int = (a, b) => a + b",
            )
        assertNotNull(result)
        assertTrue(result!!.contains("int"))
    }

    @Test
    fun `extractTypeAnnotation returns null for no annotation`() {
        val result = RescriptTypeSignatureSearchContributor.extractTypeAnnotation("let x = 42")
        assertNull(result)
    }

    @Test
    fun `rankMatch gives bonus for exact match`() {
        val query = RescriptTypeSignatureSearchContributor.tokenizeSignature("string -> int")
        val candidate = RescriptTypeSignatureSearchContributor.tokenizeSignature("string -> int")
        val rank = RescriptTypeSignatureSearchContributor.rankMatch(query, candidate)
        assertTrue(rank > 0)
    }

    @Test
    fun `rankMatch returns zero for no match`() {
        val query = RescriptTypeSignatureSearchContributor.tokenizeSignature("string")
        val candidate = RescriptTypeSignatureSearchContributor.tokenizeSignature("float")
        val rank = RescriptTypeSignatureSearchContributor.rankMatch(query, candidate)
        assertEquals(0, rank)
    }
}
