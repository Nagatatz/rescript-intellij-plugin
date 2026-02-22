package com.rescript.plugin.completion

import com.intellij.codeInsight.completion.CompletionLocation
import com.intellij.codeInsight.completion.CompletionWeigher
import com.intellij.codeInsight.lookup.LookupElement
import com.rescript.plugin.RescriptLanguage

/**
 * Weighs ReScript completion candidates for context-based ordering.
 *
 * Prioritizes candidates based on:
 *  1. LSP sortText (if provided, respects server ordering)
 *  2. Local scope variables (detail contains "local")
 *  3. Currently opened modules' symbols
 *  4. Keywords at lowest priority
 *
 * @see CompletionWeigher
 */
class RescriptCompletionWeigher : CompletionWeigher() {
    override fun weigh(
        element: LookupElement,
        location: CompletionLocation,
    ): Comparable<*> {
        val psiFile = location.completionParameters.originalFile
        if (psiFile.language != RescriptLanguage) return 0

        val lookupString = element.lookupString

        // Keywords get lowest priority
        if (lookupString in RESCRIPT_KEYWORDS) {
            return -10
        }

        // Check if this is a local variable (LSP typically marks these)
        val detail = element.getUserData(DETAIL_KEY)
        if (detail != null && detail.contains("local", ignoreCase = true)) {
            return 10
        }

        // Prefer shorter names (likely more relevant)
        val lengthBonus =
            when {
                lookupString.length <= 5 -> 3
                lookupString.length <= 10 -> 2
                lookupString.length <= 20 -> 1
                else -> 0
            }

        // Prefer lowercase-starting (values) over uppercase (modules/types)
        // when in expression context
        val caseBonus = if (lookupString.first().isLowerCase()) 1 else 0

        return lengthBonus + caseBonus
    }

    companion object {
        /**
         * Key for storing detail information on lookup elements.
         * Set by LSP completion when detail text is available.
         */
        val DETAIL_KEY =
            com.intellij.openapi.util.Key
                .create<String>("rescript.completion.detail")

        private val RESCRIPT_KEYWORDS =
            setOf(
                "let",
                "type",
                "module",
                "open",
                "include",
                "external",
                "if",
                "else",
                "switch",
                "while",
                "for",
                "in",
                "to",
                "downto",
                "try",
                "catch",
                "raise",
                "exception",
                "assert",
                "true",
                "false",
                "and",
                "as",
                "rec",
                "mutable",
                "pub",
                "pri",
                "constraint",
                "when",
                "unpack",
            )

        /**
         * Checks whether the given lookup string is a ReScript keyword.
         *
         * @param lookupString the completion candidate text
         * @return true if it is a keyword
         */
        internal fun isKeyword(lookupString: String): Boolean = lookupString in RESCRIPT_KEYWORDS

        /**
         * Computes a weight for a completion candidate based on its properties.
         *
         * @param lookupString the completion candidate text
         * @param detail optional detail text (e.g. from LSP)
         * @param isLocal whether the candidate is a local variable
         * @return integer weight (higher = more preferred)
         */
        internal fun computeWeight(
            lookupString: String,
            detail: String?,
            isLocal: Boolean,
        ): Int {
            if (lookupString in RESCRIPT_KEYWORDS) return -10
            if (isLocal) return 10

            val lengthBonus =
                when {
                    lookupString.length <= 5 -> 3
                    lookupString.length <= 10 -> 2
                    lookupString.length <= 20 -> 1
                    else -> 0
                }
            val caseBonus = if (lookupString.isNotEmpty() && lookupString.first().isLowerCase()) 1 else 0
            return lengthBonus + caseBonus
        }
    }
}
