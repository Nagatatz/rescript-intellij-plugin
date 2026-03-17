package com.rescript.plugin.refactor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [RescriptRenameHandler]'s companion utility functions.
 *
 * Tests the identifier character detection and word extraction logic used
 * to determine rename availability and extract the current identifier name.
 * The LSP-dependent rename workflow (prepareRename, applyWorkspaceEdit) is
 * not tested here as it requires a running language server.
 *
 * @see RescriptRenameHandler
 */
class RescriptRenameHandlerTest {
    // ── isIdentifierChar ────────────────────────────────────────────────

    @Test
    fun testIsIdentifierCharWithLetter() {
        assertTrue(RescriptRenameHandler.isIdentifierChar('a'))
        assertTrue(RescriptRenameHandler.isIdentifierChar('Z'))
    }

    @Test
    fun testIsIdentifierCharWithDigit() {
        assertTrue(RescriptRenameHandler.isIdentifierChar('0'))
        assertTrue(RescriptRenameHandler.isIdentifierChar('9'))
    }

    @Test
    fun testIsIdentifierCharWithUnderscore() {
        assertTrue(RescriptRenameHandler.isIdentifierChar('_'))
    }

    @Test
    fun testIsIdentifierCharWithPrime() {
        assertTrue(RescriptRenameHandler.isIdentifierChar('\''))
    }

    @Test
    fun testIsIdentifierCharWithSpace() {
        assertFalse(RescriptRenameHandler.isIdentifierChar(' '))
    }

    @Test
    fun testIsIdentifierCharWithOperators() {
        assertFalse(RescriptRenameHandler.isIdentifierChar('+'))
        assertFalse(RescriptRenameHandler.isIdentifierChar('-'))
        assertFalse(RescriptRenameHandler.isIdentifierChar('='))
        assertFalse(RescriptRenameHandler.isIdentifierChar('.'))
    }

    @Test
    fun testIsIdentifierCharWithBrackets() {
        assertFalse(RescriptRenameHandler.isIdentifierChar('('))
        assertFalse(RescriptRenameHandler.isIdentifierChar(')'))
        assertFalse(RescriptRenameHandler.isIdentifierChar('{'))
        assertFalse(RescriptRenameHandler.isIdentifierChar('}'))
    }

    @Test
    fun testIsIdentifierCharWithAt() {
        assertFalse(RescriptRenameHandler.isIdentifierChar('@'))
    }

    // ── extractWordFromText ─────────────────────────────────────────────

    @Test
    fun testExtractWordFromTextSimpleIdentifier() {
        val text = "let foo = 42"
        // Cursor on 'f' (offset 4)
        assertEquals("foo", RescriptRenameHandler.extractWordFromText(text, 4))
    }

    @Test
    fun testExtractWordFromTextMiddleOfWord() {
        val text = "let myVariable = 0"
        // Cursor on 'V' (offset 6)
        assertEquals("myVariable", RescriptRenameHandler.extractWordFromText(text, 6))
    }

    @Test
    fun testExtractWordFromTextAtEnd() {
        val text = "let foo = 42"
        // Cursor at end of "foo" (offset 7, right after 'o')
        assertEquals("foo", RescriptRenameHandler.extractWordFromText(text, 7))
    }

    @Test
    fun testExtractWordFromTextWithUnderscore() {
        val text = "let my_var = 0"
        assertEquals("my_var", RescriptRenameHandler.extractWordFromText(text, 4))
    }

    @Test
    fun testExtractWordFromTextWithPrime() {
        val text = "let x' = 0"
        assertEquals("x'", RescriptRenameHandler.extractWordFromText(text, 4))
    }

    @Test
    fun testExtractWordFromTextOnSpaceAfterIdent() {
        val text = "let foo = 42"
        // Cursor on space between "let" and "foo" (offset 3)
        // The char at offset 3 is space (not ident), but offset-1 is 't' (ident),
        // so expansion left finds "let"
        assertEquals("let", RescriptRenameHandler.extractWordFromText(text, 3))
    }

    @Test
    fun testExtractWordFromTextOnSpaceBetweenNonIdent() {
        val text = "( )"
        // Cursor on space (offset 1), surrounded by non-ident chars
        assertNull(RescriptRenameHandler.extractWordFromText(text, 1))
    }

    @Test
    fun testExtractWordFromTextOnOperator() {
        val text = "a + b"
        // Cursor on '+' (offset 2)
        assertNull(RescriptRenameHandler.extractWordFromText(text, 2))
    }

    @Test
    fun testExtractWordFromTextEmptyString() {
        assertNull(RescriptRenameHandler.extractWordFromText("", 0))
    }

    @Test
    fun testExtractWordFromTextAtStartOfText() {
        val text = "foo bar"
        assertEquals("foo", RescriptRenameHandler.extractWordFromText(text, 0))
    }

    @Test
    fun testExtractWordFromTextAtEndOfText() {
        val text = "foo bar"
        // Cursor at very end (offset 7)
        assertEquals("bar", RescriptRenameHandler.extractWordFromText(text, 7))
    }

    @Test
    fun testExtractWordFromTextModuleName() {
        val text = "Belt.Array.map"
        // Cursor on 'B' (offset 0)
        assertEquals("Belt", RescriptRenameHandler.extractWordFromText(text, 0))
    }

    @Test
    fun testExtractWordFromTextAfterDot() {
        val text = "Belt.Array.map"
        // Cursor on 'A' (offset 5)
        assertEquals("Array", RescriptRenameHandler.extractWordFromText(text, 5))
    }

    @Test
    fun testExtractWordFromTextDigitsInIdentifier() {
        val text = "let foo123bar = 0"
        assertEquals("foo123bar", RescriptRenameHandler.extractWordFromText(text, 6))
    }
}
