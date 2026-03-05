package com.rescript.plugin.errorlens

import com.intellij.lang.annotation.HighlightSeverity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

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

    @Test
    fun `displayText formats type mismatch as structured output`() {
        val message = "This has type: int  Somewhere wanted: string"
        val renderer = RescriptErrorLensRenderer(message, HighlightSeverity.ERROR)
        assertTrue(renderer.displayText.contains("Expected: string"))
        assertTrue(renderer.displayText.contains("Actual: int"))
    }

    @Test
    fun `displayText preserves non-mismatch message as-is`() {
        val message = "Unbound value foo"
        val renderer = RescriptErrorLensRenderer(message, HighlightSeverity.ERROR)
        assertEquals("${RescriptErrorLensRenderer.MESSAGE_PREFIX}$message", renderer.displayText)
    }

    @Test
    fun `buildDisplayText formats type mismatch`() {
        val result =
            RescriptErrorLensRenderer.buildDisplayText(
                "This has type: float  But somewhere wanted: int",
            )
        assertTrue(result.contains("Expected: int"))
        assertTrue(result.contains("Actual: float"))
    }

    @Test
    fun `buildDisplayText returns plain message for non-mismatch`() {
        val result = RescriptErrorLensRenderer.buildDisplayText("Some other error")
        assertEquals("${RescriptErrorLensRenderer.MESSAGE_PREFIX}Some other error", result)
    }
}
