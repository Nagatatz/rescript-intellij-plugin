package com.rescript.plugin.navigation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class RescriptElementSignatureProviderTest {
    private val provider = RescriptElementSignatureProvider()

    @Test
    fun testProviderCanBeInstantiated() {
        assertNotNull(provider)
    }

    // -- parseSignature tests --

    @Test
    fun testParseValidSignature() {
        val result = RescriptElementSignatureProvider.parseSignature("LET_DECLARATION#myFunc#42")
        assertNotNull(result)
        assertEquals("LET_DECLARATION", result!!.first)
        assertEquals("myFunc", result.second)
        assertEquals(42, result.third)
    }

    @Test
    fun testParseSignatureWithZeroOffset() {
        val result = RescriptElementSignatureProvider.parseSignature("MODULE_DECLARATION#Utils#0")
        assertNotNull(result)
        assertEquals("MODULE_DECLARATION", result!!.first)
        assertEquals("Utils", result.second)
        assertEquals(0, result.third)
    }

    @Test
    fun testParseSignatureWithLargeOffset() {
        val result = RescriptElementSignatureProvider.parseSignature("TYPE_DECLARATION#user#99999")
        assertNotNull(result)
        assertEquals(99999, result!!.third)
    }

    @Test
    fun testParseInvalidSignatureTooFewParts() {
        assertNull(RescriptElementSignatureProvider.parseSignature("LET_DECLARATION#myFunc"))
    }

    @Test
    fun testParseInvalidSignatureTooManyParts() {
        assertNull(RescriptElementSignatureProvider.parseSignature("LET#my#Func#42"))
    }

    @Test
    fun testParseInvalidSignatureNonNumericOffset() {
        assertNull(RescriptElementSignatureProvider.parseSignature("LET_DECLARATION#myFunc#abc"))
    }

    @Test
    fun testParseEmptySignature() {
        assertNull(RescriptElementSignatureProvider.parseSignature(""))
    }

    // -- createSignature tests (uses stubPsiElement) --

    @Test
    fun testCreateSignatureReturnsNullForNonDeclaration() {
        val element =
            com.rescript.plugin.RescriptTestUtils.stubPsiElement(
                com.rescript.plugin.lang.psi.RescriptElementTypes.OPEN_STATEMENT,
            )
        assertNull(RescriptElementSignatureProvider.createSignature(element))
    }

    // -- round-trip tests --

    @Test
    fun testSignatureRoundTrip() {
        val signature = "EXTERNAL_DECLARATION#log#100"
        val parsed = RescriptElementSignatureProvider.parseSignature(signature)
        assertNotNull(parsed)
        assertEquals("EXTERNAL_DECLARATION", parsed!!.first)
        assertEquals("log", parsed.second)
        assertEquals(100, parsed.third)
    }
}
