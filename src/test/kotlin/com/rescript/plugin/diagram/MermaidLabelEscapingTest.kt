package com.rescript.plugin.diagram

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Verifies that the Mermaid id/label helpers extracted from
 * [RescriptMermaidExporter] sanitize identifiers, escape label text,
 * and disambiguate id collisions deterministically.
 */
class MermaidLabelEscapingTest {
    @Test
    fun `escapeLabel encodes quotes and backslashes`() {
        assertEquals("&quot;A\\\\B&quot;", MermaidLabelEscaping.escapeLabel("\"A\\B\""))
    }

    @Test
    fun `sanitize replaces non-id chars with underscores`() {
        assertEquals("Foo_Bar", MermaidLabelEscaping.sanitize("Foo.Bar"))
        assertEquals("a_b_c", MermaidLabelEscaping.sanitize("a-b c"))
    }

    @Test
    fun `sanitize prefixes ids that start with digits`() {
        assertEquals("n_1foo", MermaidLabelEscaping.sanitize("1foo"))
    }

    @Test
    fun `assignIds disambiguates collisions`() {
        val ids = MermaidLabelEscaping.assignIds(listOf("Foo.Bar", "Foo Bar", "Foo+Bar"))
        assertEquals(mapOf("Foo.Bar" to "Foo_Bar", "Foo Bar" to "Foo_Bar_1", "Foo+Bar" to "Foo_Bar_2"), ids)
    }

    @Test
    fun `assignIds resolves collisions deterministically`() {
        // Distinct inputs sanitize to the same base, so the second one
        // is disambiguated by appending an underscore-numeric suffix.
        // The first id is kept verbatim ("___") and the second becomes
        // "____1" (base + "_1").
        val ids = MermaidLabelEscaping.assignIds(listOf("***", "?!?"))
        assertEquals(mapOf("***" to "___", "?!?" to "____1"), ids)
    }
}
