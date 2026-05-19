package com.rescript.plugin.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.awt.Color

/**
 * Unit tests for [RescriptColorUtils.colorToHexString]. The helper is
 * a thin formatting wrapper, so the tests focus on edge cases —
 * single-digit channels (`#000000`, `#0F1A2B`) and the upper-case
 * convention used across the HTML-emitting panels.
 */
class RescriptColorUtilsTest {
    @Test
    fun `pure black formats as 000000`() {
        assertEquals("#000000", RescriptColorUtils.colorToHexString(Color(0, 0, 0)))
    }

    @Test
    fun `pure white formats as FFFFFF`() {
        assertEquals("#FFFFFF", RescriptColorUtils.colorToHexString(Color(255, 255, 255)))
    }

    @Test
    fun `pure red preserves channel order RGB`() {
        assertEquals("#FF0000", RescriptColorUtils.colorToHexString(Color(255, 0, 0)))
    }

    @Test
    fun `single-digit channels are zero-padded`() {
        // 0x0F = 15, 0x1A = 26, 0x2B = 43 — all need explicit zero padding.
        assertEquals("#0F1A2B", RescriptColorUtils.colorToHexString(Color(0x0F, 0x1A, 0x2B)))
    }

    @Test
    fun `mid-grey formats with consistent upper-case digits`() {
        assertEquals("#808080", RescriptColorUtils.colorToHexString(Color(0x80, 0x80, 0x80)))
    }
}
