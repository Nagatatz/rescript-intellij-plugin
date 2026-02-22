package com.rescript.plugin.lsp

import org.junit.Assert.assertEquals
import org.junit.Test

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
}
