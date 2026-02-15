package com.rescript.plugin.codestyle

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import com.rescript.plugin.lang.RescriptTokenTypes as T

class RescriptLineIndentProviderTest {
    private val provider = RescriptLineIndentProvider()

    private fun lastToken(line: String) = provider.findLastSignificantToken(line)

    // ── Brace triggers ───────────────────────────────────────────────

    @Test
    fun `lbrace triggers indent`() {
        assertEquals(T.LBRACE, lastToken("module Foo = {"))
    }

    @Test
    fun `lparen triggers indent`() {
        assertEquals(T.LPAREN, lastToken("let foo = bar("))
    }

    @Test
    fun `lbracket triggers indent`() {
        assertEquals(T.LBRACKET, lastToken("let arr = ["))
    }

    // ── Arrow trigger ────────────────────────────────────────────────

    @Test
    fun `fat arrow triggers indent`() {
        assertEquals(T.ARROW, lastToken("| Some(x) =>"))
    }

    @Test
    fun `fat arrow in let binding`() {
        assertEquals(T.ARROW, lastToken("let greet = (name) =>"))
    }

    // ── Trailing whitespace ignored ──────────────────────────────────

    @Test
    fun `lbrace with trailing spaces`() {
        assertEquals(T.LBRACE, lastToken("module Foo = {   "))
    }

    @Test
    fun `arrow with trailing spaces`() {
        assertEquals(T.ARROW, lastToken("| None =>  "))
    }

    // ── Trailing comment ignored ─────────────────────────────────────

    @Test
    fun `lbrace before trailing comment`() {
        assertEquals(T.LBRACE, lastToken("module Foo = { // open module"))
    }

    @Test
    fun `arrow before trailing comment`() {
        assertEquals(T.ARROW, lastToken("| Some(x) => // handle some"))
    }

    // ── Non-triggers (indent maintained) ─────────────────────────────

    @Test
    fun `identifier does not trigger indent`() {
        assertEquals(T.INT_VALUE, lastToken("let x = 5"))
    }

    @Test
    fun `rbrace does not trigger indent`() {
        assertEquals(T.RBRACE, lastToken("}"))
    }

    @Test
    fun `rparen does not trigger indent`() {
        assertEquals(T.RPAREN, lastToken(")"))
    }

    @Test
    fun `rbracket does not trigger indent`() {
        assertEquals(T.RBRACKET, lastToken("]"))
    }

    @Test
    fun `string value does not trigger indent`() {
        assertEquals(T.STRING_VALUE, lastToken("let name = \"hello\""))
    }

    // ── Edge cases ───────────────────────────────────────────────────

    @Test
    fun `empty line returns null`() {
        assertNull(lastToken(""))
    }

    @Test
    fun `whitespace only line returns null`() {
        assertNull(lastToken("   "))
    }

    @Test
    fun `comment only line returns null`() {
        assertNull(lastToken("// this is a comment"))
    }

    // ── Nested structures ────────────────────────────────────────────

    @Test
    fun `lbrace after switch keyword`() {
        assertEquals(T.LBRACE, lastToken("  switch value {"))
    }

    @Test
    fun `lbrace after type declaration`() {
        assertEquals(T.LBRACE, lastToken("type user = {"))
    }

    @Test
    fun `lparen in function call`() {
        assertEquals(T.LPAREN, lastToken("  Array.map("))
    }

    @Test
    fun `int value after complete expression`() {
        assertEquals(T.INT_VALUE, lastToken("let x = 1 + 2"))
    }
}
