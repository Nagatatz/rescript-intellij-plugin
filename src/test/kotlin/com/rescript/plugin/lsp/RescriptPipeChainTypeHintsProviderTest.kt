package com.rescript.plugin.lsp

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Tests for [RescriptPipeChainTypeHintsProvider] utility methods.
 *
 * Note: Full InlayHints integration tests require IntelliJ Platform test fixtures.
 * Tests here focus on the static helper methods for pipe position finding and type extraction.
 */
class RescriptPipeChainTypeHintsProviderTest {
    @Test
    fun `extractReturnType extracts type after arrow`() {
        assertEquals("bool", RescriptPipeChainTypeHintsProvider.extractReturnType("(int, string) => bool"))
    }

    @Test
    fun `extractReturnType returns simple type as-is`() {
        assertEquals("int", RescriptPipeChainTypeHintsProvider.extractReturnType("int"))
    }

    @Test
    fun `extractReturnType handles nested arrow types`() {
        assertEquals("unit", RescriptPipeChainTypeHintsProvider.extractReturnType("int => string => unit"))
    }

    @Test
    fun `extractReturnType trims whitespace`() {
        assertEquals("string", RescriptPipeChainTypeHintsProvider.extractReturnType("int =>  string "))
    }

    @Test
    fun `extractReturnType handles generic types`() {
        assertEquals("array<int>", RescriptPipeChainTypeHintsProvider.extractReturnType("array<string> => array<int>"))
    }

    @Test
    fun `extractReturnType handles blank input`() {
        assertEquals("", RescriptPipeChainTypeHintsProvider.extractReturnType(""))
    }

    // ── findPipePositions ───────────────────────────────────────────────

    @Test
    fun `findPipePositions matches pipe arrows and not fat arrows`() {
        // `x => x` is a lambda (fat arrow, ARROW); `->` are the pipes.
        val text = "let f = x => x\nlet r = arr->map(f)->filter(g)"
        val positions = RescriptPipeChainTypeHintsProvider.findPipePositions(text)

        assertEquals(2, positions.size)
        // Both positions must point at a `->`, never at the `=>`.
        positions.forEach { assertEquals("->", text.substring(it, it + 2)) }
    }

    @Test
    fun `findPipePositions returns empty when there are no pipes`() {
        val text = "let f = x => x + 1"
        assertEquals(0, RescriptPipeChainTypeHintsProvider.findPipePositions(text).size)
    }

    @Test
    fun `findPipePositions caps the number of positions at the limit`() {
        val text = "x" + "->f".repeat(10)
        assertEquals(3, RescriptPipeChainTypeHintsProvider.findPipePositions(text, limit = 3).size)
    }
}
