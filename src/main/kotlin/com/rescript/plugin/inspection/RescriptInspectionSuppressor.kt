package com.rescript.plugin.inspection

import com.intellij.codeInspection.InspectionSuppressor
import com.intellij.codeInspection.SuppressQuickFix
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import com.rescript.plugin.lang.RescriptTokenTypes

/**
 * Supports suppression of inspections via `// noinspection` comments in ReScript files.
 *
 * Recognizes comment patterns placed on the line immediately before the target element:
 * - `// noinspection RescriptDuplicateOpen` — suppresses a specific inspection by tool ID
 * - `// noinspection RescriptDuplicateOpen, RescriptEmptyModule` — suppresses multiple inspections
 * - `// noinspection ALL` — suppresses all inspections for the following element
 *
 * @see RescriptDuplicateOpenInspection for an example of a suppressible inspection
 */
class RescriptInspectionSuppressor : InspectionSuppressor {
    override fun isSuppressedFor(
        element: PsiElement,
        toolId: String,
    ): Boolean {
        // Walk backward from element to find a preceding comment, skipping whitespace
        var prev = element.prevSibling
        while (prev != null && prev.node?.elementType == TokenType.WHITE_SPACE) {
            prev = prev.prevSibling
        }
        if (prev == null) return false

        val tokenType = prev.node?.elementType
        if (tokenType != RescriptTokenTypes.SINGLE_COMMENT) return false

        // Strip leading slashes and whitespace from the comment text
        val commentText = prev.text.trimStart('/').trim()
        if (!commentText.startsWith("noinspection")) return false

        val suppressedIds = commentText.removePrefix("noinspection").trim()
        if (suppressedIds.isEmpty()) return false
        if (suppressedIds == "ALL") return true

        // Support comma-separated list of inspection tool IDs
        return suppressedIds.split(",").any { it.trim() == toolId }
    }

    override fun getSuppressActions(
        element: PsiElement?,
        toolId: String,
    ): Array<SuppressQuickFix> = SuppressQuickFix.EMPTY_ARRAY
}
