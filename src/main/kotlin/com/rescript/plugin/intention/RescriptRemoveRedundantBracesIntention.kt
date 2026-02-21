package com.rescript.plugin.intention

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.rescript.plugin.lang.RescriptTokenTypes
import com.rescript.plugin.lang.psi.RescriptFile

/**
 * Intention action that removes redundant braces around a single expression.
 *
 * When a block `{ expr }` contains only one expression (no semicolons or
 * multiple statements), the braces are redundant and can be safely removed.
 * Available when the caret is on a `{` or `}` token.
 *
 * @see RescriptWrapWithIntention for wrapping expressions
 */
class RescriptRemoveRedundantBracesIntention : PsiElementBaseIntentionAction() {
    override fun getText(): String = "Remove redundant braces"

    override fun getFamilyName(): String = "Remove redundant braces"

    override fun isAvailable(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ): Boolean {
        if (element.containingFile !is RescriptFile) return false
        val document = editor?.document ?: return false
        val tokenType = element.node?.elementType ?: return false

        // Must be on a brace token
        if (tokenType != RescriptTokenTypes.LBRACE && tokenType != RescriptTokenTypes.RBRACE) return false

        // Find the matching brace pair and check if it contains a single expression
        val braceRange = findBracePairRange(element, document.text) ?: return false
        return isSingleExpression(document.text, braceRange.first + 1, braceRange.second)
    }

    override fun invoke(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ) {
        val document = editor?.document ?: return
        val text = document.text
        val braceRange = findBracePairRange(element, text) ?: return
        val innerText = text.substring(braceRange.first + 1, braceRange.second).trim()
        document.replaceString(braceRange.first, braceRange.second + 1, innerText)
    }

    companion object {
        /**
         * Finds the start and end offsets of the brace pair surrounding the element.
         *
         * @param element a `{` or `}` PSI element
         * @param text the full document text
         * @return pair of (lbraceOffset, rbraceOffset), or null if no matching brace found
         */
        fun findBracePairRange(
            element: PsiElement,
            text: String,
        ): Pair<Int, Int>? {
            val tokenType = element.node?.elementType ?: return null
            val offset = element.textRange.startOffset

            return if (tokenType == RescriptTokenTypes.LBRACE) {
                val end = findMatchingRBrace(text, offset) ?: return null
                Pair(offset, end)
            } else {
                val start = findMatchingLBrace(text, offset) ?: return null
                Pair(start, offset)
            }
        }

        /**
         * Checks whether the text between two offsets contains a single expression
         * (no semicolons, no additional braces at the same level).
         *
         * @param text the full document text
         * @param start the start offset (exclusive of opening brace)
         * @param end the end offset (exclusive, at closing brace)
         * @return true if the content is a single expression
         */
        fun isSingleExpression(
            text: String,
            start: Int,
            end: Int,
        ): Boolean {
            if (start >= end) return false
            val content = text.substring(start, end).trim()
            if (content.isEmpty()) return false

            // Check for semicolons outside of nested braces/parens/strings
            var depth = 0
            var inString = false
            var prevChar = ' '
            for (ch in content) {
                when {
                    inString -> if (ch == '"' && prevChar != '\\') inString = false
                    ch == '"' -> inString = true
                    ch == '{' || ch == '(' || ch == '[' -> depth++
                    ch == '}' || ch == ')' || ch == ']' -> depth--
                    ch == ';' && depth == 0 -> return false
                }
                prevChar = ch
            }
            return true
        }

        private fun findMatchingRBrace(
            text: String,
            lbraceOffset: Int,
        ): Int? {
            var depth = 0
            for (i in lbraceOffset until text.length) {
                when (text[i]) {
                    '{' -> depth++
                    '}' -> {
                        depth--
                        if (depth == 0) return i
                    }
                }
            }
            return null
        }

        private fun findMatchingLBrace(
            text: String,
            rbraceOffset: Int,
        ): Int? {
            var depth = 0
            for (i in rbraceOffset downTo 0) {
                when (text[i]) {
                    '}' -> depth++
                    '{' -> {
                        depth--
                        if (depth == 0) return i
                    }
                }
            }
            return null
        }
    }
}
