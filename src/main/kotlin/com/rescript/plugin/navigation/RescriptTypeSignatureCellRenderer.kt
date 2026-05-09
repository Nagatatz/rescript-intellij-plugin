package com.rescript.plugin.navigation

import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.SimpleTextAttributes
import javax.swing.JList

/**
 * Search Everywhere list renderer that paints one
 * [RescriptTypeSignatureSearchHit] per row as
 * `name: signatureDisplay  (relativePath:line)`.
 *
 * `name` uses the regular foreground colour, the signature renders
 * grey-italic so the structural match itself stands out, and the
 * file location uses the standard "secondary information" grey so
 * results stay scannable even when many candidates share a signature.
 */
class RescriptTypeSignatureCellRenderer : ColoredListCellRenderer<RescriptTypeSignatureSearchHit>() {
    override fun customizeCellRenderer(
        list: JList<out RescriptTypeSignatureSearchHit>,
        value: RescriptTypeSignatureSearchHit?,
        index: Int,
        selected: Boolean,
        hasFocus: Boolean,
    ) {
        if (value == null) return
        append("${value.name}: ", SimpleTextAttributes.REGULAR_ATTRIBUTES)
        append(
            value.signatureDisplay,
            SimpleTextAttributes.GRAY_ITALIC_ATTRIBUTES,
        )
        append(
            "  (${value.relativePath}:${value.line})",
            SimpleTextAttributes.GRAYED_ATTRIBUTES,
        )
    }
}
