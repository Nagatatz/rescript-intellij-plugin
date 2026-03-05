package com.rescript.plugin.errorlens

import com.intellij.lang.annotation.HighlightSeverity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class RescriptErrorLensHighlighterInfoTest {
    @Test
    fun `data class stores severity message and line`() {
        val info = RescriptErrorLensHighlighterInfo(HighlightSeverity.ERROR, "error message", 5)
        assertEquals(HighlightSeverity.ERROR, info.severity)
        assertEquals("error message", info.message)
        assertEquals(5, info.line)
    }

    @Test
    fun `data class equality works correctly`() {
        val info1 = RescriptErrorLensHighlighterInfo(HighlightSeverity.WARNING, "msg", 10)
        val info2 = RescriptErrorLensHighlighterInfo(HighlightSeverity.WARNING, "msg", 10)
        assertEquals(info1, info2)
    }

    @Test
    fun `data class copy works correctly`() {
        val info = RescriptErrorLensHighlighterInfo(HighlightSeverity.ERROR, "original", 1)
        val copied = info.copy(message = "modified")
        assertEquals("modified", copied.message)
        assertEquals(HighlightSeverity.ERROR, copied.severity)
        assertEquals(1, copied.line)
    }

    @Test
    fun `info can be created with line zero`() {
        val info = RescriptErrorLensHighlighterInfo(HighlightSeverity.INFORMATION, "first line", 0)
        assertEquals(0, info.line)
    }

    @Test
    fun `info can be created with large line numbers`() {
        val info = RescriptErrorLensHighlighterInfo(HighlightSeverity.WARNING, "large file", 10000)
        assertEquals(10000, info.line)
    }

    @Test
    fun `info with different severities are not equal`() {
        val info1 = RescriptErrorLensHighlighterInfo(HighlightSeverity.ERROR, "msg", 1)
        val info2 = RescriptErrorLensHighlighterInfo(HighlightSeverity.WARNING, "msg", 1)
        assertNotNull(info1)
        assertNotNull(info2)
        assert(info1 != info2)
    }
}
