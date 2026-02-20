package com.rescript.plugin.editor

import com.intellij.codeInsight.editorActions.CodeBlockProvider
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.rescript.plugin.lang.psi.RescriptFile

/**
 * Provides code block boundary detection for Ctrl+Shift+[ / Ctrl+Shift+] navigation.
 *
 * Identifies matching brace pairs (`{}`), bracket pairs (`[]`), and parenthesis pairs (`()`)
 * to enable jumping between the start and end of code blocks in ReScript files.
 */
class RescriptCodeBlockHandler : CodeBlockProvider {
    override fun getCodeBlockRange(
        editor: Editor,
        psiFile: PsiFile,
    ): TextRange? {
        if (psiFile !is RescriptFile) return null

        val offset = editor.caretModel.offset
        val document = editor.document
        val text = document.charsSequence
        if (offset >= text.length) return null

        // Find the enclosing brace pair
        val openBraces = charArrayOf('{', '[', '(')
        val closeBraces = charArrayOf('}', ']', ')')

        // Search backward for opening brace
        var depth = 0
        var pos = offset
        while (pos >= 0) {
            val ch = text[pos]
            val closeIdx = closeBraces.indexOf(ch)
            if (closeIdx >= 0) {
                depth++
            }
            val openIdx = openBraces.indexOf(ch)
            if (openIdx >= 0) {
                if (depth == 0) {
                    // Found opening brace, now find matching close
                    val closePos = findMatchingClose(text, pos, openBraces[openIdx], closeBraces[openIdx])
                    if (closePos >= 0) {
                        return TextRange(pos, closePos + 1)
                    }
                    return null
                }
                depth--
            }
            pos--
        }

        return null
    }

    companion object {
        /**
         * Finds the position of the matching closing brace for a given opening brace.
         *
         * @param text the document text
         * @param openPos the position of the opening brace
         * @param openChar the opening brace character
         * @param closeChar the matching closing brace character
         * @return the position of the matching closing brace, or -1 if not found
         */
        internal fun findMatchingClose(
            text: CharSequence,
            openPos: Int,
            openChar: Char,
            closeChar: Char,
        ): Int {
            var depth = 1
            var inString = false
            var pos = openPos + 1
            while (pos < text.length && depth > 0) {
                val ch = text[pos]
                if (ch == '"' && (pos == 0 || text[pos - 1] != '\\')) {
                    inString = !inString
                }
                if (!inString) {
                    if (ch == openChar) depth++
                    if (ch == closeChar) depth--
                }
                if (depth == 0) return pos
                pos++
            }
            return -1
        }
    }
}
