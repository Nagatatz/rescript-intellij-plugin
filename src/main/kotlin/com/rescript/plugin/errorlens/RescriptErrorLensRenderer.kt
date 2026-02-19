package com.rescript.plugin.errorlens

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorCustomElementRenderer
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.markup.TextAttributes
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
 * @property message the diagnostic message to display
 * @property severity the severity level determining the text color
 *
 * @see RescriptErrorLensSeverity for color mapping
 * @see RescriptErrorLensManager for inlay lifecycle management
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
    }

    /** The full display text including prefix. */
    val displayText: String = "$MESSAGE_PREFIX$message"

    override fun calcWidthInPixels(inlay: Inlay<*>): Int {
        val editor = inlay.editor
        val fontMetrics = editor.contentComponent.getFontMetrics(getFont(editor))
        return LEFT_MARGIN + fontMetrics.stringWidth(displayText)
    }

    override fun paint(
        inlay: Inlay<*>,
        g: Graphics,
        targetRegion: Rectangle,
        textAttributes: TextAttributes,
    ) {
        val editor = inlay.editor
        val font = getFont(editor)
        val color = RescriptErrorLensSeverity.colorFor(severity)

        g.font = font
        g.color = color

        // Baseline calculation: align with editor text baseline
        val fontMetrics = g.getFontMetrics(font)
        val y = targetRegion.y + targetRegion.height - fontMetrics.descent
        val x = targetRegion.x + LEFT_MARGIN

        g.drawString(displayText, x, y)
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
