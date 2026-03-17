package com.rescript.plugin.errorlens

import com.intellij.lang.annotation.HighlightSeverity
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [RescriptErrorLensManager]'s display logic.
 *
 * Tests the [RescriptErrorLensManager.buildDisplayData] companion function
 * which selects the highest-severity diagnostic and formats the display
 * message with same-line consolidation.
 *
 * @see RescriptErrorLensManager
 */
class RescriptErrorLensManagerTest {
    @Test
    fun testSingleDiagnosticReturnsMessageAsIs() {
        val diagnostics =
            listOf(
                RescriptErrorLensHighlighterInfo(HighlightSeverity.ERROR, "Type mismatch", 0),
            )
        val (message, severity) = RescriptErrorLensManager.buildDisplayData(diagnostics)
        assertEquals("Type mismatch", message)
        assertEquals(HighlightSeverity.ERROR, severity)
    }

    @Test
    fun testMultipleDiagnosticsAppendsCountSuffix() {
        val diagnostics =
            listOf(
                RescriptErrorLensHighlighterInfo(HighlightSeverity.WARNING, "Unused variable", 5),
                RescriptErrorLensHighlighterInfo(HighlightSeverity.ERROR, "Type mismatch", 5),
            )
        val (message, severity) = RescriptErrorLensManager.buildDisplayData(diagnostics)
        assertEquals("Type mismatch (+1 more)", message)
        assertEquals(HighlightSeverity.ERROR, severity)
    }

    @Test
    fun testHighestSeverityIsSelectedAsPrimary() {
        val diagnostics =
            listOf(
                RescriptErrorLensHighlighterInfo(HighlightSeverity.INFORMATION, "Info hint", 1),
                RescriptErrorLensHighlighterInfo(HighlightSeverity.WARNING, "Warning msg", 1),
                RescriptErrorLensHighlighterInfo(HighlightSeverity.ERROR, "Error msg", 1),
            )
        val (message, severity) = RescriptErrorLensManager.buildDisplayData(diagnostics)
        assertEquals("Error msg (+2 more)", message)
        assertEquals(HighlightSeverity.ERROR, severity)
    }

    @Test
    fun testThreeDiagnosticsShowsPlusTwoMore() {
        val diagnostics =
            listOf(
                RescriptErrorLensHighlighterInfo(HighlightSeverity.ERROR, "First error", 3),
                RescriptErrorLensHighlighterInfo(HighlightSeverity.WARNING, "A warning", 3),
                RescriptErrorLensHighlighterInfo(HighlightSeverity.INFORMATION, "An info", 3),
            )
        val (message, _) = RescriptErrorLensManager.buildDisplayData(diagnostics)
        assertEquals("First error (+2 more)", message)
    }

    @Test
    fun testSameSeverityDiagnosticsPicksFirstAfterSort() {
        val diagnostics =
            listOf(
                RescriptErrorLensHighlighterInfo(HighlightSeverity.WARNING, "Warning A", 2),
                RescriptErrorLensHighlighterInfo(HighlightSeverity.WARNING, "Warning B", 2),
            )
        val (message, severity) = RescriptErrorLensManager.buildDisplayData(diagnostics)
        assertEquals(HighlightSeverity.WARNING, severity)
        assertEquals("Warning A (+1 more)", message)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testEmptyDiagnosticsListThrowsException() {
        RescriptErrorLensManager.buildDisplayData(emptyList())
    }

    @Test
    fun testWeakWarningIsLowerPriorityThanWarning() {
        val diagnostics =
            listOf(
                RescriptErrorLensHighlighterInfo(HighlightSeverity.WEAK_WARNING, "Weak warning", 0),
                RescriptErrorLensHighlighterInfo(HighlightSeverity.WARNING, "Warning", 0),
            )
        val (message, severity) = RescriptErrorLensManager.buildDisplayData(diagnostics)
        assertEquals("Warning (+1 more)", message)
        assertEquals(HighlightSeverity.WARNING, severity)
    }

    @Test
    fun testSingleInformationSeverityDiagnostic() {
        val diagnostics =
            listOf(
                RescriptErrorLensHighlighterInfo(HighlightSeverity.INFORMATION, "Hint text", 10),
            )
        val (message, severity) = RescriptErrorLensManager.buildDisplayData(diagnostics)
        assertEquals("Hint text", message)
        assertEquals(HighlightSeverity.INFORMATION, severity)
    }

    @Test
    fun testManyDiagnosticsOnSameLine() {
        val diagnostics =
            (1..10).map { i ->
                RescriptErrorLensHighlighterInfo(HighlightSeverity.WARNING, "Warning $i", 0)
            }
        val (message, _) = RescriptErrorLensManager.buildDisplayData(diagnostics)
        assert(message.endsWith("(+9 more)")) { "Expected message to end with '(+9 more)' but was: $message" }
    }
}
