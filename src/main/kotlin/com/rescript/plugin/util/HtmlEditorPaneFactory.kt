package com.rescript.plugin.util

import com.intellij.util.ui.JBUI
import java.awt.Font
import javax.swing.JEditorPane

/**
 * Centralised factory for read-only HTML-rendering [JEditorPane]
 * instances used by tool window panels that display colourised
 * summaries (PPX View, Variant Flow source mode, Module Dependency
 * source mode).
 *
 * Until now each panel repeated the same four-line configuration
 * block (`contentType`, `isEditable`, `HONOR_DISPLAY_PROPERTIES`,
 * monospace font). Routing through this helper keeps the setup in
 * one tested place and makes future tweaks (e.g. a different default
 * font, accessibility hints) a single-file change.
 */
internal object HtmlEditorPaneFactory {
    /** Default editor font size used when the caller does not specify one. */
    const val DEFAULT_FONT_SIZE: Int = 13

    /**
     * Builds a fresh read-only `text/html` pane.
     *
     * @param fontSize editor font size in points; defaults to
     *   [DEFAULT_FONT_SIZE] to match the historical configuration of
     *   the existing panels
     * @param borderInset optional uniform `JBUI.Borders.empty(inset)`
     *   applied to the pane; pass `null` to leave the border unset so
     *   the caller can install something custom via `apply { … }`
     * @return a [JEditorPane] ready to receive HTML through
     *   `text = …`
     */
    fun createReadOnlyHtmlPane(
        fontSize: Int = DEFAULT_FONT_SIZE,
        borderInset: Int? = null,
    ): JEditorPane =
        JEditorPane().apply {
            contentType = "text/html"
            isEditable = false
            putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true)
            font = Font(Font.MONOSPACED, Font.PLAIN, fontSize)
            if (borderInset != null) {
                border = JBUI.Borders.empty(borderInset)
            }
        }
}
