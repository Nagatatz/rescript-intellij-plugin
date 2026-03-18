package com.rescript.plugin.lsp

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Unit tests for [RescriptLspSignatureParser], verifying correct parsing of
 * ReScript function signatures and variant type definitions from LSP hover responses.
 *
 * @see RescriptLspSignatureParser
 */
class RescriptLspSignatureParserTest {
    // ══════════════════════════════════════════════════════════════════
    // parseSignatureLabels
    // ══════════════════════════════════════════════════════════════════

    // ── Basic labeled parameters ──────────────────────────────────────

    @Test
    fun `parseSignatureLabels parses single labeled param`() {
        val result = RescriptLspSignatureParser.parseSignatureLabels("(~name: string) => unit")

        assertEquals(1, result.size)
        assertEquals("name", result[0].name)
        assertEquals("string", result[0].type)
        assertFalse(result[0].isOptional)
    }

    @Test
    fun `parseSignatureLabels parses multiple labeled params`() {
        val result = RescriptLspSignatureParser.parseSignatureLabels("(~name: string, ~age: int) => person")

        assertEquals(2, result.size)
        assertEquals("name", result[0].name)
        assertEquals("string", result[0].type)
        assertEquals("age", result[1].name)
        assertEquals("int", result[1].type)
    }

    @Test
    fun `parseSignatureLabels parses optional param with =?`() {
        val result = RescriptLspSignatureParser.parseSignatureLabels("(~name: string, ~age: int=?) => person")

        assertEquals(2, result.size)
        assertFalse(result[0].isOptional)
        assertTrue(result[1].isOptional)
        assertEquals("int", result[1].type)
    }

    @Test
    fun `parseSignatureLabels skips non-labeled params`() {
        val result = RescriptLspSignatureParser.parseSignatureLabels("(~name: string, unit) => person")

        assertEquals(1, result.size)
        assertEquals("name", result[0].name)
    }

    // ── Complex type signatures ───────────────────────────────────────

    @Test
    fun `parseSignatureLabels handles function type in param`() {
        val result = RescriptLspSignatureParser.parseSignatureLabels("(~callback: (int, string) => unit) => unit")

        assertEquals(1, result.size)
        assertEquals("callback", result[0].name)
        assertEquals("(int, string) => unit", result[0].type)
    }

    @Test
    fun `parseSignatureLabels handles generic type in param`() {
        val result = RescriptLspSignatureParser.parseSignatureLabels("(~items: array<string>) => unit")

        assertEquals(1, result.size)
        assertEquals("items", result[0].name)
        assertEquals("array<string>", result[0].type)
    }

    @Test
    fun `parseSignatureLabels handles nested generic type`() {
        val result = RescriptLspSignatureParser.parseSignatureLabels("(~data: option<array<int>>) => unit")

        assertEquals(1, result.size)
        assertEquals("data", result[0].name)
        assertEquals("option<array<int>>", result[0].type)
    }

    // ── Edge cases ────────────────────────────────────────────────────

    @Test
    fun `parseSignatureLabels returns empty for no parens`() {
        val result = RescriptLspSignatureParser.parseSignatureLabels("string")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `parseSignatureLabels returns empty for empty string`() {
        val result = RescriptLspSignatureParser.parseSignatureLabels("")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `parseSignatureLabels returns empty for no labeled params`() {
        val result = RescriptLspSignatureParser.parseSignatureLabels("(string, int) => unit")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `parseSignatureLabels handles extra whitespace`() {
        val result = RescriptLspSignatureParser.parseSignatureLabels("(  ~name:   string  ,  ~age:  int  ) => unit")

        assertEquals(2, result.size)
        assertEquals("name", result[0].name)
        assertEquals("string", result[0].type)
        assertEquals("age", result[1].name)
        assertEquals("int", result[1].type)
    }

    // ══════════════════════════════════════════════════════════════════
    // parseVariantConstructors
    // ══════════════════════════════════════════════════════════════════

    // ── Built-in types ────────────────────────────────────────────────

    @Test
    fun `parseVariantConstructors returns Some and None for option`() {
        val result = RescriptLspSignatureParser.parseVariantConstructors("option<int>")

        assertEquals(2, result.size)
        assertEquals("Some", result[0].name)
        assertTrue(result[0].hasPayload)
        assertEquals("None", result[1].name)
        assertFalse(result[1].hasPayload)
    }

    @Test
    fun `parseVariantConstructors returns Some and None for bare option`() {
        val result = RescriptLspSignatureParser.parseVariantConstructors("option")

        assertEquals(2, result.size)
        assertEquals("Some", result[0].name)
        assertEquals("None", result[1].name)
    }

    @Test
    fun `parseVariantConstructors returns Ok and Error for result`() {
        val result = RescriptLspSignatureParser.parseVariantConstructors("result<int, string>")

        assertEquals(2, result.size)
        assertEquals("Ok", result[0].name)
        assertTrue(result[0].hasPayload)
        assertEquals("Error", result[1].name)
        assertTrue(result[1].hasPayload)
    }

    @Test
    fun `parseVariantConstructors returns Ok and Error for bare result`() {
        val result = RescriptLspSignatureParser.parseVariantConstructors("result")

        assertEquals(2, result.size)
        assertEquals("Ok", result[0].name)
        assertEquals("Error", result[1].name)
    }

    // ── Inline variant definitions ────────────────────────────────────

    @Test
    fun `parseVariantConstructors parses pipe-separated constructors`() {
        val result = RescriptLspSignatureParser.parseVariantConstructors("Red | Green | Blue")

        assertEquals(3, result.size)
        assertEquals("Red", result[0].name)
        assertFalse(result[0].hasPayload)
        assertEquals("Green", result[1].name)
        assertEquals("Blue", result[2].name)
    }

    @Test
    fun `parseVariantConstructors parses constructors with payload`() {
        val result = RescriptLspSignatureParser.parseVariantConstructors("Loading | Success(data) | Error(string)")

        assertEquals(3, result.size)
        assertEquals("Loading", result[0].name)
        assertFalse(result[0].hasPayload)
        assertEquals("Success", result[1].name)
        assertTrue(result[1].hasPayload)
        assertEquals("Error", result[2].name)
        assertTrue(result[2].hasPayload)
    }

    @Test
    fun `parseVariantConstructors parses leading pipe`() {
        val result = RescriptLspSignatureParser.parseVariantConstructors("| A | B(int)")

        assertEquals(2, result.size)
        assertEquals("A", result[0].name)
        assertFalse(result[0].hasPayload)
        assertEquals("B", result[1].name)
        assertTrue(result[1].hasPayload)
    }

    // ── Edge cases ────────────────────────────────────────────────────

    @Test
    fun `parseVariantConstructors returns empty for simple type`() {
        val result = RescriptLspSignatureParser.parseVariantConstructors("string")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `parseVariantConstructors returns empty for empty string`() {
        val result = RescriptLspSignatureParser.parseVariantConstructors("")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `parseVariantConstructors returns empty for lowercase pipe text`() {
        // Lowercase identifiers are not valid constructors
        val result = RescriptLspSignatureParser.parseVariantConstructors("a | b | c")

        assertTrue(result.isEmpty())
    }

    // ══════════════════════════════════════════════════════════════════
    // extractParenContent (internal helper)
    // ══════════════════════════════════════════════════════════════════

    @Test
    fun `extractParenContent extracts simple content`() {
        val result = RescriptLspSignatureParser.extractParenContent("foo(bar)")

        assertEquals("bar", result)
    }

    @Test
    fun `extractParenContent handles nested parens`() {
        val result = RescriptLspSignatureParser.extractParenContent("foo(a(b), c)")

        assertEquals("a(b), c", result)
    }

    @Test
    fun `extractParenContent returns null for no parens`() {
        assertNull(RescriptLspSignatureParser.extractParenContent("no parens"))
    }

    @Test
    fun `extractParenContent returns null for unbalanced parens`() {
        assertNull(RescriptLspSignatureParser.extractParenContent("foo(bar"))
    }

    @Test
    fun `extractParenContent handles empty parens`() {
        val result = RescriptLspSignatureParser.extractParenContent("foo()")

        assertEquals("", result)
    }

    // ══════════════════════════════════════════════════════════════════
    // splitByComma (internal helper)
    // ══════════════════════════════════════════════════════════════════

    @Test
    fun `splitByComma splits simple values`() {
        val result = RescriptLspSignatureParser.splitByComma("a, b, c")

        assertEquals(3, result.size)
        assertEquals("a", result[0])
        assertEquals(" b", result[1])
        assertEquals(" c", result[2])
    }

    @Test
    fun `splitByComma respects nested parens`() {
        val result = RescriptLspSignatureParser.splitByComma("(a, b), c")

        assertEquals(2, result.size)
        assertEquals("(a, b)", result[0])
        assertEquals(" c", result[1])
    }

    @Test
    fun `splitByComma respects nested angle brackets`() {
        val result = RescriptLspSignatureParser.splitByComma("array<int, string>, bool")

        assertEquals(2, result.size)
        assertEquals("array<int, string>", result[0])
        assertEquals(" bool", result[1])
    }

    @Test
    fun `splitByComma respects nested curly braces`() {
        val result = RescriptLspSignatureParser.splitByComma("{a: int, b: string}, unit")

        assertEquals(2, result.size)
        assertEquals("{a: int, b: string}", result[0])
        assertEquals(" unit", result[1])
    }

    @Test
    fun `splitByComma handles single element`() {
        val result = RescriptLspSignatureParser.splitByComma("string")

        assertEquals(1, result.size)
        assertEquals("string", result[0])
    }

    @Test
    fun `splitByComma handles empty string`() {
        val result = RescriptLspSignatureParser.splitByComma("")

        assertTrue(result.isEmpty())
    }

    // ══════════════════════════════════════════════════════════════════
    // Data classes
    // ══════════════════════════════════════════════════════════════════

    @Test
    fun `LabeledParam equality works correctly`() {
        val a = RescriptLspSignatureParser.LabeledParam("name", "string", false)
        val b = RescriptLspSignatureParser.LabeledParam("name", "string", false)
        assertEquals(a, b)
    }

    @Test
    fun `VariantInfo equality works correctly`() {
        val a = RescriptLspSignatureParser.VariantInfo("Some", true)
        val b = RescriptLspSignatureParser.VariantInfo("Some", true)
        assertEquals(a, b)
    }
}
