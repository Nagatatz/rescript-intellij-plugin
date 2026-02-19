package com.rescript.plugin.errorlens

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.ui.JBColor
import java.awt.Color

/**
 * Maps [HighlightSeverity] levels to display colors for Error Lens inlays.
 *
 * Provides color lookup and severity threshold filtering used by the
 * Error Lens rendering pipeline to determine whether a diagnostic should
 * be displayed and in what color.
 *
 * @see RescriptErrorLensRenderer for the rendering side
 */
object RescriptErrorLensSeverity {
    // Semi-transparent red for errors
    private val ERROR_COLOR = JBColor(Color(0xCC, 0x33, 0x33, 0xCC), Color(0xFF, 0x66, 0x66, 0xCC))

    // Semi-transparent yellow/orange for warnings
    private val WARNING_COLOR = JBColor(Color(0xCC, 0x88, 0x00, 0xCC), Color(0xFF, 0xCC, 0x44, 0xCC))

    // Semi-transparent gray for info and weak warnings
    private val INFO_COLOR = JBColor(Color(0x80, 0x80, 0x80, 0xCC), Color(0xA0, 0xA0, 0xA0, 0xCC))

    /**
     * Ordered list of severity names from highest to lowest,
     * matching the combo box choices in settings UI.
     */
    val SEVERITY_NAMES = listOf("ERROR", "WARNING", "WEAK_WARNING", "INFORMATION")

    /**
     * Returns the display color for the given [severity].
     *
     * @param severity the highlight severity of the diagnostic
     * @return a [JBColor] appropriate for the severity level
     */
    fun colorFor(severity: HighlightSeverity): JBColor =
        when {
            severity >= HighlightSeverity.ERROR -> ERROR_COLOR
            severity >= HighlightSeverity.WARNING -> WARNING_COLOR
            else -> INFO_COLOR
        }

    /**
     * Checks whether [severity] meets or exceeds the minimum threshold
     * identified by [minSeverityName].
     *
     * @param severity the severity to test
     * @param minSeverityName one of [SEVERITY_NAMES] representing the minimum threshold
     * @return true if the severity is at or above the threshold
     */
    fun meetsMinimum(
        severity: HighlightSeverity,
        minSeverityName: String,
    ): Boolean {
        val threshold = severityFromName(minSeverityName) ?: return true
        return severity >= threshold
    }

    /**
     * Converts a severity name string to its corresponding [HighlightSeverity].
     *
     * @param name the severity name (e.g., "ERROR", "WARNING")
     * @return the matching [HighlightSeverity], or null if unrecognized
     */
    fun severityFromName(name: String): HighlightSeverity? =
        when (name) {
            "ERROR" -> HighlightSeverity.ERROR
            "WARNING" -> HighlightSeverity.WARNING
            "WEAK_WARNING" -> HighlightSeverity.WEAK_WARNING
            "INFORMATION" -> HighlightSeverity.INFORMATION
            else -> null
        }
}
