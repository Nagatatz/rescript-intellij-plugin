package com.rescript.plugin.intention

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.rescript.plugin.util.RescriptEditorUtils.replaceInWriteAction

/**
 * Intention action to remove unnecessary parentheses around expressions.
 *
 * Detects parenthesized expressions where the parentheses are redundant
 * (e.g., single identifier, literal, or simple function call) and offers
 * to remove them. JSX attribute values `prop={...}` and operator expressions
 * are excluded to avoid changing semantics.
 *
 * Triggered via Alt+Enter > "Remove unnecessary parentheses".
 */
class RescriptRemoveParenthesesIntention : RescriptBaseIntention() {
    override fun getText(): String = "Remove unnecessary parentheses"

    override fun isAvailableInRescript(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ): Boolean {
        val text = editor?.document?.text ?: return false
        val offset = editor.caretModel.offset

        // Find the enclosing parenthesized expression
        val parenRange = findEnclosingParens(text, offset) ?: return false
        val inner = text.substring(parenRange.first + 1, parenRange.second).trim()

        // Don't offer for empty parens or multi-expression content
        if (inner.isEmpty()) return false

        // Check if parentheses are removable
        return isRemovable(text, parenRange.first, parenRange.second, inner)
    }

    override fun invoke(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ) {
        editor ?: return
        val document = editor.document
        val text = document.text
        val offset = editor.caretModel.offset

        val parenRange = findEnclosingParens(text, offset) ?: return
        val inner = text.substring(parenRange.first + 1, parenRange.second).trim()

        document.replaceInWriteAction(project, parenRange.first, parenRange.second + 1, inner)
    }

    companion object {
        /**
         * Finds the enclosing parenthesized expression around the given offset.
         *
         * @param text the document text
         * @param offset the caret offset
         * @return pair of (openParen, closeParen) offsets, or null if not found
         */
        internal fun findEnclosingParens(
            text: String,
            offset: Int,
        ): Pair<Int, Int>? {
            // Search backwards for opening paren
            var depth = 0
            var openParen = -1
            for (i in offset - 1 downTo 0) {
                when (text[i]) {
                    ')' -> {
                        depth++
                    }

                    '(' -> {
                        if (depth == 0) {
                            openParen = i
                            break
                        }
                        depth--
                    }
                }
            }
            if (openParen < 0) return null

            // Find matching closing paren
            depth = 0
            for (i in openParen until text.length) {
                when (text[i]) {
                    '(' -> {
                        depth++
                    }

                    ')' -> {
                        depth--
                        if (depth == 0) {
                            return Pair(openParen, i)
                        }
                    }
                }
            }
            return null
        }

        /**
         * Checks if the parentheses can be safely removed.
         *
         * @param text the full document text
         * @param openParen offset of the opening parenthesis
         * @param closeParen offset of the closing parenthesis
         * @param inner the trimmed content between parentheses
         * @return true if the parentheses are redundant and can be removed
         */
        internal fun isRemovable(
            text: String,
            openParen: Int,
            closeParen: Int,
            inner: String,
        ): Boolean {
            // Don't remove if inner contains commas (tuple)
            if (containsTopLevelComma(inner)) return false

            // Don't remove if inner contains operators that might change precedence
            if (containsTopLevelOperator(inner)) return false

            // Don't remove if part of a function call: ident(...)
            if (openParen > 0) {
                val charBefore = text[openParen - 1]
                if (charBefore.isLetterOrDigit() || charBefore == '_') return false
            }

            // Don't remove if inside JSX attribute: prop={...}
            if (openParen > 0 && text[openParen - 1] == '=') {
                // Check if preceded by an identifier (JSX attribute)
                val beforeEq = text.substring(0, openParen - 1).trimEnd()
                if (beforeEq.isNotEmpty() && (beforeEq.last().isLetterOrDigit() || beforeEq.last() == '_')) {
                    return false
                }
            }

            return true
        }

        /** Checks if the text contains a comma at the top nesting level. */
        internal fun containsTopLevelComma(text: String): Boolean {
            var depth = 0
            for (ch in text) {
                when (ch) {
                    '(', '[', '{' -> depth++
                    ')', ']', '}' -> depth--
                    ',' -> if (depth == 0) return true
                }
            }
            return false
        }

        /** Checks if the text contains a binary operator at the top nesting level. */
        internal fun containsTopLevelOperator(text: String): Boolean {
            var depth = 0
            var i = 0
            while (i < text.length) {
                when (text[i]) {
                    '(', '[', '{' -> {
                        depth++
                    }

                    ')', ']', '}' -> {
                        depth--
                    }

                    '+', '-', '*', '/' -> {
                        if (depth == 0 && i > 0 && i < text.length - 1) {
                            // Check it's a binary operator by looking at the non-whitespace char before
                            val beforeText = text.substring(0, i).trimEnd()
                            if (beforeText.isNotEmpty()) {
                                val before = beforeText.last()
                                if (before.isLetterOrDigit() || before == '_' || before == ')') {
                                    return true
                                }
                            }
                        }
                    }

                    '=' -> {
                        // Check for == comparison
                        if (depth == 0 && i + 1 < text.length && text[i + 1] == '=') {
                            return true
                        }
                    }

                    '|' -> {
                        // Check for || logical or
                        if (depth == 0 && i + 1 < text.length && text[i + 1] == '|') {
                            return true
                        }
                    }

                    '&' -> {
                        // Check for && logical and
                        if (depth == 0 && i + 1 < text.length && text[i + 1] == '&') {
                            return true
                        }
                    }
                }
                i++
            }
            return false
        }
    }
}
