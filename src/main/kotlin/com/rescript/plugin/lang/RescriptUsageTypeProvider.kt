package com.rescript.plugin.lang

import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import com.intellij.usages.impl.rules.UsageType
import com.intellij.usages.impl.rules.UsageTypeProvider

/**
 * Provides usage type classification for Find Usages results in ReScript files.
 *
 * Categorizes usages by analyzing the surrounding token context:
 * - Open statement: `open ModuleName`
 * - Type reference: after `type` keyword
 * - Type annotation: after `:`
 * - Pattern match: after `|`
 * - JSX usage: inside JSX elements
 *
 * This enables grouped display in the Find Usages tool window.
 */
class RescriptUsageTypeProvider : UsageTypeProvider {
    override fun getUsageType(element: PsiElement): UsageType? {
        val prev = findPrevSignificantToken(element) ?: return null
        val prevType = prev.node?.elementType ?: return null

        return when (prevType) {
            RescriptTokenTypes.OPEN -> OPEN_USAGE

            RescriptTokenTypes.TYPE -> TYPE_REFERENCE

            RescriptTokenTypes.COLON -> TYPE_ANNOTATION

            RescriptTokenTypes.PIPE -> PATTERN_MATCH

            RescriptTokenTypes.INCLUDE -> INCLUDE_USAGE

            RescriptTokenTypes.MODULE -> MODULE_DECLARATION

            RescriptTokenTypes.LET -> VALUE_DEFINITION

            RescriptTokenTypes.TAG_LT,
            RescriptTokenTypes.TAG_LT_SLASH,
            -> JSX_USAGE

            else -> null
        }
    }

    companion object {
        val OPEN_USAGE = UsageType { "Open statement" }
        val TYPE_REFERENCE = UsageType { "Type reference" }
        val TYPE_ANNOTATION = UsageType { "Type annotation" }
        val PATTERN_MATCH = UsageType { "Pattern match" }
        val INCLUDE_USAGE = UsageType { "Include statement" }
        val MODULE_DECLARATION = UsageType { "Module declaration" }
        val VALUE_DEFINITION = UsageType { "Value definition" }
        val JSX_USAGE = UsageType { "JSX element" }

        /**
         * Finds the previous significant token, skipping whitespace and EOL.
         *
         * @param element the starting element
         * @return the previous non-whitespace sibling, or null
         */
        private fun findPrevSignificantToken(element: PsiElement): PsiElement? {
            var prev = element.prevSibling
            while (prev != null) {
                val type = prev.node?.elementType
                if (type != TokenType.WHITE_SPACE && type != RescriptTokenTypes.EOL) {
                    return prev
                }
                prev = prev.prevSibling
            }
            return null
        }
    }
}
