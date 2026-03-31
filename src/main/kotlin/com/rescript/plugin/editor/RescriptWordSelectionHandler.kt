package com.rescript.plugin.editor

import com.intellij.codeInsight.editorActions.ExtendWordSelectionHandler
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.rescript.plugin.RescriptLanguage
import com.rescript.plugin.lang.RescriptTokenTypes

/**
 * Extends word selection for ReScript string literals (Ctrl+W).
 *
 * Provides two-stage selection: first selects the string content
 * (without quotes), then the entire string including quotes.
 */
class RescriptStringSelectionHandler : ExtendWordSelectionHandler {
    override fun canSelect(e: PsiElement): Boolean {
        if (e.language != RescriptLanguage) return false
        return e.node?.elementType == RescriptTokenTypes.STRING_VALUE
    }

    override fun select(
        e: PsiElement,
        editorText: CharSequence,
        cursorOffset: Int,
        editor: Editor,
    ): List<TextRange> {
        val range = e.textRange
        val text = e.text

        val ranges = mutableListOf<TextRange>()

        // If string has quotes, offer inner content selection
        if (text.length >= 2 && text.startsWith("\"") && text.endsWith("\"")) {
            val innerRange = TextRange(range.startOffset + 1, range.endOffset - 1)
            if (innerRange.length > 0) {
                ranges.add(innerRange)
            }
        }
        // Full string including quotes
        ranges.add(range)

        return ranges
    }
}

/**
 * Extends word selection for bracket-enclosed content in ReScript (Ctrl+W).
 *
 * Provides two-stage selection: first selects content inside brackets,
 * then the brackets and their content together. Supports `()`, `{}`, and `[]`.
 */
class RescriptBracketSelectionHandler : ExtendWordSelectionHandler {
    override fun canSelect(e: PsiElement): Boolean {
        if (e.language != RescriptLanguage) return false
        val tokenType = e.node?.elementType ?: return false
        return tokenType in BRACKET_TOKENS
    }

    override fun select(
        e: PsiElement,
        editorText: CharSequence,
        cursorOffset: Int,
        editor: Editor,
    ): List<TextRange> {
        val tokenType = e.node?.elementType ?: return emptyList()

        // Find the matching bracket by scanning siblings
        val isOpen = tokenType in OPEN_BRACKETS
        val targetType =
            when (tokenType) {
                RescriptTokenTypes.LPAREN -> RescriptTokenTypes.RPAREN
                RescriptTokenTypes.RPAREN -> RescriptTokenTypes.LPAREN
                RescriptTokenTypes.LBRACE -> RescriptTokenTypes.RBRACE
                RescriptTokenTypes.RBRACE -> RescriptTokenTypes.LBRACE
                RescriptTokenTypes.LBRACKET -> RescriptTokenTypes.RBRACKET
                RescriptTokenTypes.RBRACKET -> RescriptTokenTypes.LBRACKET
                else -> return emptyList()
            }

        val matchingBracket = findMatchingBracket(e, targetType, isOpen) ?: return emptyList()

        val openOffset = if (isOpen) e.textRange.startOffset else matchingBracket.textRange.startOffset
        val closeOffset = if (isOpen) matchingBracket.textRange.endOffset else e.textRange.endOffset

        val ranges = mutableListOf<TextRange>()

        // Inner content (between brackets)
        val innerStart = openOffset + 1
        val innerEnd = closeOffset - 1
        if (innerEnd > innerStart) {
            ranges.add(TextRange(innerStart, innerEnd))
        }

        // Full bracket pair
        ranges.add(TextRange(openOffset, closeOffset))

        return ranges
    }

    companion object {
        private val OPEN_BRACKETS =
            setOf(
                RescriptTokenTypes.LPAREN,
                RescriptTokenTypes.LBRACE,
                RescriptTokenTypes.LBRACKET,
            )

        private val BRACKET_TOKENS =
            setOf(
                RescriptTokenTypes.LPAREN,
                RescriptTokenTypes.RPAREN,
                RescriptTokenTypes.LBRACE,
                RescriptTokenTypes.RBRACE,
                RescriptTokenTypes.LBRACKET,
                RescriptTokenTypes.RBRACKET,
            )

        /**
         * Scans sibling PSI elements to find the matching bracket.
         *
         * @param element the starting bracket element
         * @param targetType the element type of the matching bracket
         * @param forward true to scan forward (open->close), false for reverse
         * @return the matching bracket element, or null if not found
         */
        private fun findMatchingBracket(
            element: PsiElement,
            targetType: com.intellij.psi.tree.IElementType,
            forward: Boolean,
        ): PsiElement? {
            val openType = element.node.elementType
            var depth = 1
            var current = if (forward) element.nextSibling else element.prevSibling

            while (current != null) {
                val type = current.node?.elementType
                if (type == openType) depth++
                if (type == targetType) {
                    depth--
                    if (depth == 0) return current
                }
                current = if (forward) current.nextSibling else current.prevSibling
            }
            return null
        }
    }
}

/**
 * Extends word selection for ReScript comments (Ctrl+W).
 *
 * For single-line comments (`//`), selects comment text then the full line.
 * For multi-line comments, selects inner content then the
 * full comment including delimiters.
 */
class RescriptCommentSelectionHandler : ExtendWordSelectionHandler {
    override fun canSelect(e: PsiElement): Boolean {
        if (e.language != RescriptLanguage) return false
        val tokenType = e.node?.elementType ?: return false
        return tokenType == RescriptTokenTypes.SINGLE_COMMENT ||
            tokenType == RescriptTokenTypes.MULTI_COMMENT
    }

    override fun select(
        e: PsiElement,
        editorText: CharSequence,
        cursorOffset: Int,
        editor: Editor,
    ): List<TextRange> {
        val range = e.textRange
        val text = e.text
        val ranges = mutableListOf<TextRange>()

        when (e.node?.elementType) {
            RescriptTokenTypes.SINGLE_COMMENT -> {
                // Select content after // (without the // prefix)
                val prefixLen =
                    if (text.startsWith("/// ")) {
                        4
                    } else if (text.startsWith("// ")) {
                        3
                    } else {
                        2
                    }
                if (range.length > prefixLen) {
                    ranges.add(TextRange(range.startOffset + prefixLen, range.endOffset))
                }
                ranges.add(range)
            }

            RescriptTokenTypes.MULTI_COMMENT -> {
                // Select content inside /* */ (without delimiters)
                val startLen = if (text.startsWith("/**")) 3 else 2
                val endLen = if (text.endsWith("*/")) 2 else 0
                val innerStart = range.startOffset + startLen
                val innerEnd = range.endOffset - endLen
                if (innerEnd > innerStart) {
                    ranges.add(TextRange(innerStart, innerEnd))
                }
                ranges.add(range)
            }
        }

        return ranges
    }
}
