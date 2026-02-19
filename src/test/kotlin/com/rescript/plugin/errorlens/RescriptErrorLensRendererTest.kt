package com.rescript.plugin.errorlens

import com.intellij.lang.annotation.HighlightSeverity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptErrorLensRendererTest {
    @Test
    fun `renderer stores message and severity`() {
        val renderer = RescriptErrorLensRenderer("test message", HighlightSeverity.ERROR)
        assertEquals("test message", renderer.message)
        assertEquals(HighlightSeverity.ERROR, renderer.severity)
    }

    @Test
    fun `displayText includes prefix and message`() {
        val renderer = RescriptErrorLensRenderer("test message", HighlightSeverity.WARNING)
        assertTrue(renderer.displayText.contains("test message"))
        assertTrue(renderer.displayText.startsWith(RescriptErrorLensRenderer.MESSAGE_PREFIX))
    }

    @Test
    fun `displayText for empty message includes prefix`() {
        val renderer = RescriptErrorLensRenderer("", HighlightSeverity.INFORMATION)
        assertEquals("${RescriptErrorLensRenderer.MESSAGE_PREFIX}", renderer.displayText)
    }

    @Test
    fun `LEFT_MARGIN is positive`() {
        assertTrue(RescriptErrorLensRenderer.LEFT_MARGIN > 0)
    }

    @Test
    fun `renderer can be created with different severities`() {
        val errorRenderer = RescriptErrorLensRenderer("error", HighlightSeverity.ERROR)
        val warningRenderer = RescriptErrorLensRenderer("warning", HighlightSeverity.WARNING)
        val infoRenderer = RescriptErrorLensRenderer("info", HighlightSeverity.INFORMATION)

        assertNotNull(errorRenderer)
        assertNotNull(warningRenderer)
        assertNotNull(infoRenderer)
    }

    @Test
    fun `renderer with long message creates correct display text`() {
        val longMessage = "A".repeat(200)
        val renderer = RescriptErrorLensRenderer(longMessage, HighlightSeverity.ERROR)
        assertTrue(renderer.displayText.contains(longMessage))
    }
}
