package com.rescript.plugin.editor

import com.intellij.codeInsight.hint.DeclarationRangeHandler
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.rescript.plugin.lang.RescriptTokenTypes
import com.rescript.plugin.lang.psi.RescriptPsiUtils

/**
 * Provides the declaration header range for ReScript top-level declarations.
 *
 * When the user scrolls inside a long declaration body, the IDE displays
 * the declaration header (e.g., "let myFunction = ..." or "module Foo")
 * as a sticky line at the top of the editor (View > Context Info).
 *
 * @see RescriptPsiUtils.NAVIGABLE_TYPES
 */
class RescriptDeclarationRangeHandler : DeclarationRangeHandler<PsiElement> {
    override fun getDeclarationRange(element: PsiElement): TextRange {
        val node = element.node ?: return element.textRange
        val startOffset = node.startOffset

        // Walk child nodes to find the declaration header:
        // keyword [rec] name
        var child = node.firstChildNode
        var lastHeaderOffset = startOffset
        var afterKeyword = false

        while (child != null) {
            val type = child.elementType
            when {
                RescriptTokenTypes.TOP_LEVEL_KEYWORDS.contains(type) -> {
                    afterKeyword = true
                    lastHeaderOffset = child.startOffset + child.textLength
                }

                type == RescriptTokenTypes.REC -> {
                    lastHeaderOffset = child.startOffset + child.textLength
                }

                afterKeyword &&
                    (
                        type == RescriptTokenTypes.LIDENT ||
                            type == RescriptTokenTypes.UIDENT ||
                            type == RescriptTokenTypes.UNDERSCORE
                    ) -> {
                    // Found the name — header ends here
                    lastHeaderOffset = child.startOffset + child.textLength
                    break
                }
            }
            child = child.treeNext
        }

        return TextRange(startOffset, lastHeaderOffset)
    }
}
