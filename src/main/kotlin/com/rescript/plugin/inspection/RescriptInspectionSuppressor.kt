package com.rescript.plugin.inspection

import com.intellij.codeInspection.InspectionSuppressor
import com.intellij.codeInspection.SuppressQuickFix
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import com.rescript.plugin.lang.RescriptTokenTypes
import com.rescript.plugin.lang.psi.RescriptElementTypes

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
        // Suppress LongLine inspection for annotations, %raw/%ffi, and string literals
        if (toolId == "LongLine" && isInLongLineSuppressedContext(element)) return true

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

    companion object {
        // Token types in which long lines should be tolerated
        private val LONG_LINE_SUPPRESSED_TOKENS =
            setOf(
                RescriptTokenTypes.STRING_VALUE,
                RescriptTokenTypes.ANNOTATION_NAME,
                RescriptTokenTypes.JS_STRING_OPEN,
                RescriptTokenTypes.JS_STRING_CLOSE,
            )

        /**
         * Determines whether a long-line inspection should be suppressed for the element.
         *
         * Suppresses in the following contexts:
         * - Inside annotation bodies (`@module(...)`, `@val(...)`, etc.)
         * - Inside `%raw(...)` or `%ffi(...)` extension points
         * - Inside string literals
         *
         * @param element the PSI element at the long line
         * @return true if the long-line warning should be suppressed
         */
        fun isInLongLineSuppressedContext(element: PsiElement): Boolean {
            val tokenType = element.node?.elementType

            // Direct string/annotation token
            if (tokenType in LONG_LINE_SUPPRESSED_TOKENS) return true

            // Walk up to check for ANNOTATION parent or %raw/%ffi context
            var current: PsiElement? = element
            while (current != null) {
                val currentType = current.node?.elementType
                if (currentType == RescriptElementTypes.ANNOTATION) return true

                // Check for %raw or %ffi keywords
                if (currentType == RescriptTokenTypes.RAW || currentType == RescriptTokenTypes.FFI) return true

                current = current.parent
            }

            return false
        }
    }
}
