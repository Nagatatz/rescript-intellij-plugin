package com.rescript.plugin.util

import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.ui.EditorTextField

/**
 * Centralised settings helper for the [EditorTextField] instances embedded
 * in tool window panels (REPL input, Notebook cells, Type Info display).
 *
 * Until now each panel repeated the same `addSettingsProvider` block hiding
 * line numbers, the folding outline, and the right margin. Routing through
 * this helper keeps the shared defaults in one tested place while each
 * panel applies its single panel-specific tweak via [applyPanelDefaults]'s
 * customizer parameter.
 *
 * @see HtmlEditorPaneFactory for the equivalent helper for read-only HTML panes
 */
internal object EditorTextFieldFactory {
    /**
     * Registers a settings provider on [field] that applies the
     * panel-standard editor settings — no line numbers, no folding
     * outline, no right margin — and then forwards the editor to
     * [customizer] for per-panel adjustments.
     *
     * @param field the editor field to configure
     * @param customizer invoked with the created [EditorEx] after the
     *   shared defaults are applied; defaults to a no-op
     */
    fun applyPanelDefaults(
        field: EditorTextField,
        customizer: (EditorEx) -> Unit = {},
    ) {
        field.addSettingsProvider { editor ->
            editor.settings.isLineNumbersShown = false
            editor.settings.isFoldingOutlineShown = false
            editor.settings.isRightMarginShown = false
            customizer(editor)
        }
    }
}
