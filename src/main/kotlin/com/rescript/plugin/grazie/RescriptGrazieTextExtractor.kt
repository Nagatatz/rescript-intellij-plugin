package com.rescript.plugin.grazie

import com.intellij.grazie.text.TextContent
import com.intellij.grazie.text.TextContentBuilder
import com.intellij.grazie.text.TextExtractor
import com.intellij.psi.PsiElement
import com.rescript.plugin.lang.RescriptTokenTypes

/**
 * Extracts natural language text from ReScript files for grammar checking by the Grazie plugin.
 *
 * Recognizes single-line comments, multi-line comments, and string literals as
 * sources of natural language text. Uses the same token type classification as
 * [com.rescript.plugin.spellcheck.RescriptSpellcheckingStrategy].
 * Implements [TextExtractor].
 */
class RescriptGrazieTextExtractor : TextExtractor() {
    companion object {
        /** Token types that contain comment text eligible for grammar checking. */
        internal val COMMENT_TYPES =
            setOf(
                RescriptTokenTypes.SINGLE_COMMENT,
                RescriptTokenTypes.MULTI_COMMENT,
            )

        /** Token types that contain string literal text eligible for grammar checking. */
        internal val STRING_TYPES =
            setOf(
                RescriptTokenTypes.STRING_VALUE,
            )

        /**
         * Determines the text domain for a given element type.
         *
         * @return the text domain, or null if the element type is not eligible
         */
        internal fun domainFor(elementType: com.intellij.psi.tree.IElementType?): TextContent.TextDomain? =
            when (elementType) {
                in COMMENT_TYPES -> TextContent.TextDomain.COMMENTS
                in STRING_TYPES -> TextContent.TextDomain.LITERALS
                else -> null
            }
    }

    override fun buildTextContent(
        root: PsiElement,
        allowedDomains: Set<TextContent.TextDomain>,
    ): TextContent? {
        val elementType = root.node?.elementType ?: return null
        val domain = domainFor(elementType) ?: return null
        if (domain !in allowedDomains) return null
        return TextContentBuilder.FromPsi.build(root, domain)
    }
}
