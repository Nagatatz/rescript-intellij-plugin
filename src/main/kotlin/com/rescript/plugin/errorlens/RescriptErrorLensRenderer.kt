package com.rescript.plugin.errorlens

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorCustomElementRenderer
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.markup.TextAttributes
import java.awt.Color
import java.awt.Font
import java.awt.Graphics
import java.awt.Rectangle

/**
 * Custom element renderer for Error Lens inlays that paints diagnostic
 * messages at the end of editor lines.
 *
 * Displays the message text in a color corresponding to the diagnostic
 * severity, with a left-side margin for visual separation from code.
 *
 * For type mismatch errors, displays a structured format showing
 * expected and actual types (e.g., "Expected: int | Actual: string").
 *
 * @property message the diagnostic message to display
 * @property severity the severity level determining the text color
 *
 * @see RescriptErrorLensSeverity for color mapping
 * @see RescriptErrorLensManager for inlay lifecycle management
 * @see RescriptTypeMismatchParser for type mismatch extraction
 */
class RescriptErrorLensRenderer(
    val message: String,
    val severity: HighlightSeverity,
) : EditorCustomElementRenderer {
    companion object {
        /** Horizontal padding (in pixels) before the diagnostic text. */
        const val LEFT_MARGIN = 12

        /** Prefix displayed before the diagnostic message. */
        const val MESSAGE_PREFIX = "  "

        /**
         * Builds the display text for a diagnostic message.
         *
         * If the message contains a type mismatch, returns a structured
         * format. Otherwise returns the original message with prefix.
         *
         * @param message the raw diagnostic message
         * @return the formatted display text
         */
        internal fun buildDisplayText(message: String): String {
            val mismatch = RescriptTypeMismatchParser.parse(message)
            return if (mismatch != null) {
                "$MESSAGE_PREFIX${RescriptTypeMismatchParser.formatMismatch(mismatch)}"
            } else {
                "$MESSAGE_PREFIX$message"
            }
        }

        /** Color used to highlight type diff segments that differ between expected and actual. */
        internal val DIFF_HIGHLIGHT_COLOR = Color(255, 106, 106)
    }

    /** The full display text including prefix, with type mismatch formatting if applicable. */
    val displayText: String = buildDisplayText(message)

    /** Parsed type mismatch, if the message contains one. */
    val typeMismatch: RescriptTypeMismatchParser.TypeMismatch? = RescriptTypeMismatchParser.parse(message)

    /** Diff segments for the expected type, if a type mismatch was found. */
    val diffSegments: Pair<List<RescriptTypeDiffComputer.TypeSegment>, List<RescriptTypeDiffComputer.TypeSegment>>? =
        typeMismatch?.let { RescriptTypeDiffComputer.computeDiff(it.expected, it.actual) }

    override fun calcWidthInPixels(inlay: Inlay<*>): Int {
        val editor = inlay.editor
        val fontMetrics = editor.contentComponent.getFontMetrics(getFont(editor))
        return if (diffSegments != null) {
            val prefix = "$MESSAGE_PREFIX Expected: "
            val separator = " | Actual: "
            val expectedText = diffSegments.first.joinToString("") { it.text }
            val actualText = diffSegments.second.joinToString("") { it.text }
            LEFT_MARGIN + fontMetrics.stringWidth(prefix + expectedText + separator + actualText)
        } else {
            LEFT_MARGIN + fontMetrics.stringWidth(displayText)
        }
    }

    override fun paint(
        inlay: Inlay<*>,
        g: Graphics,
        targetRegion: Rectangle,
        textAttributes: TextAttributes,
    ) {
        val editor = inlay.editor
        val font = getFont(editor)
        val baseColor = RescriptErrorLensSeverity.colorFor(severity)

        g.font = font

        // Baseline calculation: align with editor text baseline
        val fontMetrics = g.getFontMetrics(font)
        val y = targetRegion.y + targetRegion.height - fontMetrics.descent
        var x = targetRegion.x + LEFT_MARGIN

        if (diffSegments != null) {
            // Draw type mismatch with diff highlighting
            g.color = baseColor
            val prefix = "$MESSAGE_PREFIX Expected: "
            g.drawString(prefix, x, y)
            x += fontMetrics.stringWidth(prefix)

            // Draw expected type segments with diff highlighting
            x = paintSegments(g, fontMetrics, diffSegments.first, x, y, baseColor)

            g.color = baseColor
            val separator = " | Actual: "
            g.drawString(separator, x, y)
            x += fontMetrics.stringWidth(separator)

            // Draw actual type segments with diff highlighting
            paintSegments(g, fontMetrics, diffSegments.second, x, y, baseColor)
        } else {
            g.color = baseColor
            g.drawString(displayText, x, y)
        }
    }

    /**
     * Paints a list of type segments, using the diff highlight color for segments
     * marked as different.
     */
    private fun paintSegments(
        g: Graphics,
        fontMetrics: java.awt.FontMetrics,
        segments: List<RescriptTypeDiffComputer.TypeSegment>,
        startX: Int,
        y: Int,
        baseColor: Color,
    ): Int {
        var x = startX
        for (segment in segments) {
            g.color = if (segment.isDifferent) DIFF_HIGHLIGHT_COLOR else baseColor
            g.drawString(segment.text, x, y)
            x += fontMetrics.stringWidth(segment.text)
        }
        return x
    }

    /**
     * Returns the font used for rendering, derived from the editor's
     * default font in italic style.
     *
     * @param editor the editor whose font to derive from
     * @return an italic version of the editor's content font
     */
    private fun getFont(editor: Editor): Font =
        editor.colorsScheme.getFont(com.intellij.openapi.editor.colors.EditorFontType.ITALIC)
}
