package com.rescript.plugin.editor

import com.intellij.codeInsight.editorActions.JoinLinesHandlerDelegate
import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiFile
import com.rescript.plugin.RescriptLanguage

/**
 * Smart line-join handler for ReScript (Ctrl+Shift+J).
 *
 * Recognizes language-specific patterns and joins lines accordingly:
 * - `let x =` + value on next line -> `let x = value`
 * - Pipe chains: `expr->` + `fn` -> `expr->fn`
 * - Removes redundant whitespace at join points
 */
class RescriptJoinLinesHandler : JoinLinesHandlerDelegate {
    override fun tryJoinLines(
        document: Document,
        file: PsiFile,
        start: Int,
        end: Int,
    ): Int {
        if (file.language != RescriptLanguage) return CANNOT_JOIN

        val text = document.charsSequence
        val firstLineEnd = findLineContent(text, start, forward = false)
        val secondLineStart = findLineContent(text, end, forward = true)

        if (firstLineEnd < 0 || secondLineStart < 0) return CANNOT_JOIN

        val beforeJoin = text.substring(0, start + 1).trimEnd()
        val afterJoin = text.substring(end).trimStart()

        // Pattern: pipe chain join (line ends with -> or next line starts with ->)
        if (beforeJoin.endsWith("->") || afterJoin.startsWith("->")) {
            // Join without space for pipe chains
            document.replaceString(start + 1, end, "")
            return start + 1
        }

        // Pattern: let binding join (line ends with =)
        if (beforeJoin.endsWith("=") && !beforeJoin.endsWith("==") && !beforeJoin.endsWith("!=")) {
            // Join with single space after =
            document.replaceString(start + 1, end, " ")
            return start + 2
        }

        // Pattern: line ends with => (arrow function body on next line)
        if (beforeJoin.endsWith("=>")) {
            document.replaceString(start + 1, end, " ")
            return start + 2
        }

        return CANNOT_JOIN
    }

    companion object {
        private const val CANNOT_JOIN = -1

        /**
         * Finds the position of the first/last non-whitespace character.
         *
         * @param text the document text
         * @param from starting position for the scan
         * @param forward true to scan forward, false to scan backward
         * @return the position of the content character, or -1 if not found
         */
        private fun findLineContent(
            text: CharSequence,
            from: Int,
            forward: Boolean,
        ): Int {
            var pos = from
            if (forward) {
                while (pos < text.length && (text[pos] == ' ' || text[pos] == '\t')) pos++
            } else {
                while (pos >= 0 && (text[pos] == ' ' || text[pos] == '\t')) pos--
            }
            return if (pos in text.indices) pos else -1
        }
    }
}
