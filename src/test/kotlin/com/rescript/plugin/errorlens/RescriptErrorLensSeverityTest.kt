package com.rescript.plugin.errorlens

import com.intellij.lang.annotation.HighlightSeverity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptErrorLensSeverityTest {
    @Test
    fun `colorFor returns error color for ERROR severity`() {
        val color = RescriptErrorLensSeverity.colorFor(HighlightSeverity.ERROR)
        assertNotNull(color)
    }

    @Test
    fun `colorFor returns warning color for WARNING severity`() {
        val color = RescriptErrorLensSeverity.colorFor(HighlightSeverity.WARNING)
        assertNotNull(color)
    }

    @Test
    fun `colorFor returns info color for INFORMATION severity`() {
        val color = RescriptErrorLensSeverity.colorFor(HighlightSeverity.INFORMATION)
        assertNotNull(color)
    }

    @Test
    fun `colorFor returns info color for WEAK_WARNING severity`() {
        val color = RescriptErrorLensSeverity.colorFor(HighlightSeverity.WEAK_WARNING)
        assertNotNull(color)
    }

    @Test
    fun `error color differs from warning color`() {
        val errorColor = RescriptErrorLensSeverity.colorFor(HighlightSeverity.ERROR)
        val warningColor = RescriptErrorLensSeverity.colorFor(HighlightSeverity.WARNING)
        assertTrue(errorColor != warningColor)
    }

    @Test
    fun `meetsMinimum returns true when severity equals threshold`() {
        assertTrue(RescriptErrorLensSeverity.meetsMinimum(HighlightSeverity.WARNING, "WARNING"))
    }

    @Test
    fun `meetsMinimum returns true when severity exceeds threshold`() {
        assertTrue(RescriptErrorLensSeverity.meetsMinimum(HighlightSeverity.ERROR, "WARNING"))
    }

    @Test
    fun `meetsMinimum returns false when severity below threshold`() {
        assertFalse(RescriptErrorLensSeverity.meetsMinimum(HighlightSeverity.INFORMATION, "WARNING"))
    }

    @Test
    fun `meetsMinimum returns true for ERROR threshold when severity is ERROR`() {
        assertTrue(RescriptErrorLensSeverity.meetsMinimum(HighlightSeverity.ERROR, "ERROR"))
    }

    @Test
    fun `meetsMinimum returns false for ERROR threshold when severity is WARNING`() {
        assertFalse(RescriptErrorLensSeverity.meetsMinimum(HighlightSeverity.WARNING, "ERROR"))
    }

    @Test
    fun `meetsMinimum returns true for INFORMATION threshold for all severities`() {
        assertTrue(RescriptErrorLensSeverity.meetsMinimum(HighlightSeverity.ERROR, "INFORMATION"))
        assertTrue(RescriptErrorLensSeverity.meetsMinimum(HighlightSeverity.WARNING, "INFORMATION"))
        assertTrue(RescriptErrorLensSeverity.meetsMinimum(HighlightSeverity.WEAK_WARNING, "INFORMATION"))
        assertTrue(RescriptErrorLensSeverity.meetsMinimum(HighlightSeverity.INFORMATION, "INFORMATION"))
    }

    @Test
    fun `meetsMinimum returns true for unknown threshold name`() {
        assertTrue(RescriptErrorLensSeverity.meetsMinimum(HighlightSeverity.INFORMATION, "UNKNOWN"))
    }

    @Test
    fun `severityFromName returns correct severity for ERROR`() {
        assertEquals(HighlightSeverity.ERROR, RescriptErrorLensSeverity.severityFromName("ERROR"))
    }

    @Test
    fun `severityFromName returns correct severity for WARNING`() {
        assertEquals(HighlightSeverity.WARNING, RescriptErrorLensSeverity.severityFromName("WARNING"))
    }

    @Test
    fun `severityFromName returns correct severity for WEAK_WARNING`() {
        assertEquals(HighlightSeverity.WEAK_WARNING, RescriptErrorLensSeverity.severityFromName("WEAK_WARNING"))
    }

    @Test
    fun `severityFromName returns correct severity for INFORMATION`() {
        assertEquals(HighlightSeverity.INFORMATION, RescriptErrorLensSeverity.severityFromName("INFORMATION"))
    }

    @Test
    fun `severityFromName returns null for unknown name`() {
        assertNull(RescriptErrorLensSeverity.severityFromName("UNKNOWN"))
    }

    @Test
    fun `SEVERITY_NAMES contains all standard severities`() {
        assertTrue(RescriptErrorLensSeverity.SEVERITY_NAMES.contains("ERROR"))
        assertTrue(RescriptErrorLensSeverity.SEVERITY_NAMES.contains("WARNING"))
        assertTrue(RescriptErrorLensSeverity.SEVERITY_NAMES.contains("WEAK_WARNING"))
        assertTrue(RescriptErrorLensSeverity.SEVERITY_NAMES.contains("INFORMATION"))
    }

    @Test
    fun `SEVERITY_NAMES is ordered from highest to lowest`() {
        val names = RescriptErrorLensSeverity.SEVERITY_NAMES
        assertEquals("ERROR", names[0])
        assertEquals("WARNING", names[1])
        assertEquals("WEAK_WARNING", names[2])
        assertEquals("INFORMATION", names[3])
    }
}
