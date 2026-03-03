package com.rescript.plugin.editor

/**
 * Utility functions for bracket matching and text range operations
 * used by the ReScript unwrap/remove feature.
 *
 * @see RescriptUnwrapDescriptor
 */
object RescriptUnwrapUtils {
    /**
     * Finds the matching closing bracket for an opening bracket at [openIndex].
     *
     * @param text the source text to scan
     * @param openIndex the index of the opening bracket character
     * @param openChar the opening bracket character (e.g. '(' or '{')
     * @param closeChar the closing bracket character (e.g. ')' or '}')
     * @return the index of the matching closing bracket, or null if not found
     */
    fun findMatchingBracket(
        text: String,
        openIndex: Int,
        openChar: Char,
        closeChar: Char,
    ): Int? {
        if (openIndex >= text.length || text[openIndex] != openChar) return null
        var depth = 0
        var i = openIndex
        while (i < text.length) {
            when (text[i]) {
                openChar -> depth++
                closeChar -> {
                    depth--
                    if (depth == 0) return i
                }
            }
            i++
        }
        return null
    }

    /** Finds the matching closing parenthesis for an opening paren at [openIndex]. */
    fun findMatchingParen(
        text: String,
        openIndex: Int,
    ): Int? = findMatchingBracket(text, openIndex, '(', ')')

    /** Finds the matching closing brace for an opening brace at [openIndex]. */
    fun findMatchingBrace(
        text: String,
        openIndex: Int,
    ): Int? = findMatchingBracket(text, openIndex, '{', '}')

    /**
     * Finds the index of the next non-whitespace character starting from [start].
     *
     * @param text the source text
     * @param start the starting index
     * @return the index of the next non-whitespace character, or null if end of text
     */
    fun findNextNonWhitespace(
        text: String,
        start: Int,
    ): Int? {
        var i = start
        while (i < text.length) {
            if (!text[i].isWhitespace()) return i
            i++
        }
        return null
    }

    /** Data holder for block unwrap ranges. */
    data class Quadruple(
        val outerStart: Int,
        val bodyStart: Int,
        val bodyEnd: Int,
        val outerEnd: Int,
    )
}
