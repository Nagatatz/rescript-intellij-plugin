package com.rescript.plugin.util

import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import com.rescript.plugin.lang.RescriptTokenTypes

/**
 * Shared utilities for brace/bracket/parenthesis balance tracking.
 *
 * Provides both text-based scanning (operating on raw [String] with character indices)
 * and PSI-based whitespace skipping (operating on [PsiElement] siblings).
 *
 * @see com.rescript.plugin.editor.RescriptUnwrapDescriptor
 * @see com.rescript.plugin.highlight.RescriptHighlightUsagesHandlerFactory
 */
object RescriptBraceBalanceUtil {
    // ── Text-based bracket matching ─────────────────────────────

    /**
     * Finds the matching closing bracket for an opening bracket at [openIndex].
     *
     * @param text the source text to scan
     * @param openIndex the index of the opening bracket character
     * @param openChar the opening bracket character (e.g. '(' or '{')
     * @param closeChar the closing bracket character (e.g. ')' or '}')
     * @return the index of the matching closing bracket, or null if not found
     */
    @JvmStatic
    fun findMatchingBracket(
        text: String,
        openIndex: Int,
        openChar: Char,
        closeChar: Char,
    ): Int? {
        if (openIndex < 0 || openIndex >= text.length || text[openIndex] != openChar) return null
        var depth = 0
        var i = openIndex
        while (i < text.length) {
            when (text[i]) {
                openChar -> {
                    depth++
                }

                closeChar -> {
                    depth--
                    if (depth == 0) return i
                }
            }
            i++
        }
        return null
    }

    /**
     * Finds the matching closing parenthesis for an opening `(` at [openIndex].
     *
     * @param text the source text to scan
     * @param openIndex the index of the `(` character
     * @return the index of the matching `)`, or null if not found
     */
    @JvmStatic
    fun findMatchingParen(
        text: String,
        openIndex: Int,
    ): Int? = findMatchingBracket(text, openIndex, '(', ')')

    /**
     * Finds the matching closing brace for an opening `{` at [openIndex].
     *
     * @param text the source text to scan
     * @param openIndex the index of the `{` character
     * @return the index of the matching `}`, or null if not found
     */
    @JvmStatic
    fun findMatchingBrace(
        text: String,
        openIndex: Int,
    ): Int? = findMatchingBracket(text, openIndex, '{', '}')

    // ── PSI-based whitespace skipping ───────────────────────────

    /**
     * Skips whitespace elements forward, returning the next non-whitespace sibling.
     *
     * @param element the starting element (may be null)
     * @return the first non-whitespace sibling, or null if none found
     */
    @JvmStatic
    fun skipWhitespace(element: PsiElement?): PsiElement? {
        var e = element
        while (e != null && e.node?.elementType == TokenType.WHITE_SPACE) {
            e = e.nextSibling
        }
        return e
    }

    /**
     * Skips whitespace elements backward, returning the previous non-whitespace sibling.
     *
     * @param element the starting element (may be null)
     * @return the first non-whitespace sibling going backward, or null if none found
     */
    @JvmStatic
    fun skipWhitespaceBackward(element: PsiElement?): PsiElement? {
        var e = element
        while (e != null && e.node?.elementType == TokenType.WHITE_SPACE) {
            e = e.prevSibling
        }
        return e
    }

    /**
     * Skips whitespace and EOL elements backward, returning the previous
     * non-whitespace, non-EOL sibling.
     *
     * @param element the starting element (may be null)
     * @return the first non-whitespace/non-EOL sibling going backward, or null if none found
     * @see skipWhitespaceBackward for whitespace-only skipping
     */
    @JvmStatic
    fun skipWhitespaceAndEolBackward(element: PsiElement?): PsiElement? {
        var e = element
        while (e != null) {
            val type = e.node?.elementType
            if (type != TokenType.WHITE_SPACE && type != RescriptTokenTypes.EOL) {
                return e
            }
            e = e.prevSibling
        }
        return null
    }
}
