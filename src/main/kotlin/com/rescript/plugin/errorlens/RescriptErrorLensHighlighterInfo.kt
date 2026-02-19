package com.rescript.plugin.errorlens

import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.markup.RangeHighlighter

/**
 * Holds extracted diagnostic information from a [RangeHighlighter].
 *
 * Provides a clean data model for Error Lens rendering, decoupling
 * the inlay display logic from the IntelliJ markup model internals.
 *
 * @property severity the diagnostic severity level
 * @property message the diagnostic message text
 * @property line the zero-based line number where the diagnostic appears
 *
 * @see RescriptErrorLensManager for the consumer of this info
 */
data class RescriptErrorLensHighlighterInfo(
    val severity: HighlightSeverity,
    val message: String,
    val line: Int,
) {
    companion object {
        /**
         * Minimum severity that qualifies as a displayable diagnostic.
         * Filters out non-diagnostic highlighters (e.g., search results, caret markers).
         */
        private val MIN_DIAGNOSTIC_SEVERITY = HighlightSeverity.INFORMATION

        /**
         * Extracts diagnostic information from a [RangeHighlighter].
         *
         * Returns null if the highlighter does not represent a diagnostic
         * (e.g., it is a search highlight, caret row highlight, or lacks
         * a description).
         *
         * @param highlighter the range highlighter to inspect
         * @return extracted info, or null if not a displayable diagnostic
         */
        fun fromHighlighter(highlighter: RangeHighlighter): RescriptErrorLensHighlighterInfo? {
            if (!highlighter.isValid) return null

            val info = HighlightInfo.fromRangeHighlighter(highlighter) ?: return null
            val severity = info.severity
            val description = info.description

            // Filter out non-diagnostic highlighters
            if (severity < MIN_DIAGNOSTIC_SEVERITY) return null
            if (description.isNullOrBlank()) return null

            val document = highlighter.document
            val offset = highlighter.startOffset
            if (offset > document.textLength) return null

            val line = document.getLineNumber(offset)
            return RescriptErrorLensHighlighterInfo(severity, description, line)
        }
    }
}
