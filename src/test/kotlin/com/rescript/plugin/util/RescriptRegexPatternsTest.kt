package com.rescript.plugin.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptRegexPatternsTest {
    // ── LIDENT ──────────────────────────────────────────────────────

    @Test
    fun `LIDENT matches simple lowercase identifier`() {
        assertTrue(RescriptRegexPatterns.LIDENT.matches("foo"))
    }

    @Test
    fun `LIDENT matches underscore-prefixed identifier`() {
        assertTrue(RescriptRegexPatterns.LIDENT.matches("_bar"))
    }

    @Test
    fun `LIDENT matches identifier with prime`() {
        assertTrue(RescriptRegexPatterns.LIDENT.matches("x'"))
    }

    @Test
    fun `LIDENT matches identifier with digits`() {
        assertTrue(RescriptRegexPatterns.LIDENT.matches("foo123"))
    }

    @Test
    fun `LIDENT rejects uppercase start`() {
        assertFalse(RescriptRegexPatterns.LIDENT.matches("Foo"))
    }

    @Test
    fun `LIDENT rejects digit start`() {
        assertFalse(RescriptRegexPatterns.LIDENT.matches("1foo"))
    }

    @Test
    fun `LIDENT rejects empty string`() {
        assertFalse(RescriptRegexPatterns.LIDENT.matches(""))
    }

    // ── UIDENT ──────────────────────────────────────────────────────

    @Test
    fun `UIDENT matches simple uppercase identifier`() {
        assertTrue(RescriptRegexPatterns.UIDENT.matches("Foo"))
    }

    @Test
    fun `UIDENT matches single uppercase letter`() {
        assertTrue(RescriptRegexPatterns.UIDENT.matches("A"))
    }

    @Test
    fun `UIDENT matches identifier with digits and prime`() {
        assertTrue(RescriptRegexPatterns.UIDENT.matches("Module123'"))
    }

    @Test
    fun `UIDENT rejects lowercase start`() {
        assertFalse(RescriptRegexPatterns.UIDENT.matches("foo"))
    }

    @Test
    fun `UIDENT rejects underscore start`() {
        assertFalse(RescriptRegexPatterns.UIDENT.matches("_Foo"))
    }

    @Test
    fun `UIDENT rejects empty string`() {
        assertFalse(RescriptRegexPatterns.UIDENT.matches(""))
    }

    // ── WHITESPACE ──────────────────────────────────────────────────

    @Test
    fun `WHITESPACE splits by spaces`() {
        val result = "a b c".split(RescriptRegexPatterns.WHITESPACE)
        assertTrue(result == listOf("a", "b", "c"))
    }

    @Test
    fun `WHITESPACE splits by tabs and newlines`() {
        val result = "a\tb\nc".split(RescriptRegexPatterns.WHITESPACE)
        assertTrue(result == listOf("a", "b", "c"))
    }

    @Test
    fun `WHITESPACE splits by multiple spaces`() {
        val result = "a   b".split(RescriptRegexPatterns.WHITESPACE)
        assertTrue(result == listOf("a", "b"))
    }
}
