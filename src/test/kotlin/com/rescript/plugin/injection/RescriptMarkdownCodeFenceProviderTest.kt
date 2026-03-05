package com.rescript.plugin.injection

import com.rescript.plugin.RescriptLanguage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class RescriptMarkdownCodeFenceProviderTest {
    private val provider = RescriptMarkdownCodeFenceProvider()

    @Test
    fun `getLanguageByInfoString returns ReScript for rescript`() {
        assertEquals(RescriptLanguage, provider.getLanguageByInfoString("rescript"))
    }

    @Test
    fun `getLanguageByInfoString returns ReScript for res`() {
        assertEquals(RescriptLanguage, provider.getLanguageByInfoString("res"))
    }

    @Test
    fun `getLanguageByInfoString returns ReScript for resi`() {
        assertEquals(RescriptLanguage, provider.getLanguageByInfoString("resi"))
    }

    @Test
    fun `getLanguageByInfoString is case insensitive`() {
        assertEquals(RescriptLanguage, provider.getLanguageByInfoString("ReScript"))
        assertEquals(RescriptLanguage, provider.getLanguageByInfoString("RESCRIPT"))
        assertEquals(RescriptLanguage, provider.getLanguageByInfoString("RES"))
    }

    @Test
    fun `getLanguageByInfoString handles whitespace`() {
        assertEquals(RescriptLanguage, provider.getLanguageByInfoString("  rescript  "))
    }

    @Test
    fun `getLanguageByInfoString returns null for unknown languages`() {
        assertNull(provider.getLanguageByInfoString("javascript"))
        assertNull(provider.getLanguageByInfoString("kotlin"))
        assertNull(provider.getLanguageByInfoString(""))
    }
}
