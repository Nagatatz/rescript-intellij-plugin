package com.rescript.plugin.editor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.awt.Color

class RescriptColorProviderTest {
    private val provider = RescriptColorProvider()

    @Test
    fun `provider can be instantiated`() {
        assertNotNull(provider)
    }

    @Test
    fun `parseHexColor parses 3-digit hex`() {
        val color = provider.parseHexColor("\"#f00\"")
        assertNotNull(color)
        assertEquals(Color(255, 0, 0), color)
    }

    @Test
    fun `parseHexColor parses 6-digit hex`() {
        val color = provider.parseHexColor("\"#ff8000\"")
        assertNotNull(color)
        assertEquals(Color(255, 128, 0), color)
    }

    @Test
    fun `parseHexColor parses 8-digit hex with alpha`() {
        val color = provider.parseHexColor("\"#ff000080\"")
        assertNotNull(color)
        assertEquals(Color(255, 0, 0, 128), color)
    }

    @Test
    fun `parseHexColor returns null for invalid hex`() {
        assertNull(provider.parseHexColor("\"not-a-color\""))
        assertNull(provider.parseHexColor("\"#zz\""))
    }

    @Test
    fun `parseRgbColor parses rgb`() {
        val color = provider.parseRgbColor("\"rgb(255, 128, 0)\"")
        assertNotNull(color)
        assertEquals(Color(255, 128, 0), color)
    }

    @Test
    fun `parseRgbColor parses rgba`() {
        val color = provider.parseRgbColor("\"rgba(255, 0, 0, 0.5)\"")
        assertNotNull(color)
        assertEquals(Color(255, 0, 0, 127), color)
    }

    @Test
    fun `parseRgbColor returns null for invalid input`() {
        assertNull(provider.parseRgbColor("\"not-rgb\""))
    }

    @Test
    fun `parseHslColor parses hsl`() {
        val color = provider.parseHslColor("\"hsl(0, 100%, 50%)\"")
        assertNotNull(color)
        // Pure red in HSL
        assertEquals(255, color!!.red)
        assertEquals(0, color.green)
        assertEquals(0, color.blue)
    }

    @Test
    fun `parseHslColor parses hsla`() {
        val color = provider.parseHslColor("\"hsla(120, 100%, 50%, 0.5)\"")
        assertNotNull(color)
        // Pure green in HSL with 50% alpha
        assertEquals(0, color!!.red)
        assertEquals(255, color.green)
        assertEquals(0, color.blue)
        assertEquals(127, color.alpha)
    }

    @Test
    fun `parseHslColor returns null for invalid input`() {
        assertNull(provider.parseHslColor("\"not-hsl\""))
    }
}
