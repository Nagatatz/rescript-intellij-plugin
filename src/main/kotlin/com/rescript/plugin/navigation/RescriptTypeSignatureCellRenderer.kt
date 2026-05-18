package com.rescript.plugin.navigation

import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.SimpleTextAttributes
import javax.swing.JList

/**
 * Search Everywhere list renderer that paints one
 * [RescriptTypeSignatureSearchHit] per row as
 * `name: signatureDisplay  (relativePath:line)`.
 *
 * `name` uses the regular foreground colour. The signature is
 * tokenised through [RescriptSignatureTokenColorizer] so each token
 * (keyword, type constructor, operator, type variable, …) receives
 * the same colour it would inside an open `.res` editor — the match
 * structure is visible at a glance instead of being one grey-italic
 * blob. The file location stays in the standard "secondary
 * information" grey so the list remains scannable.
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
        for (token in RescriptSignatureTokenColorizer.tokenize(value.signatureDisplay)) {
            append(token.text, token.attributes)
        }
        append(
            "  (${value.relativePath}:${value.line})",
            SimpleTextAttributes.GRAYED_ATTRIBUTES,
        )
    }
}
