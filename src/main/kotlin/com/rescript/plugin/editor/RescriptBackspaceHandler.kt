package com.rescript.plugin.editor

import com.intellij.codeInsight.editorActions.BackspaceHandlerDelegate
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import com.rescript.plugin.lang.psi.RescriptFile

/**
 * Custom backspace handler for ReScript files.
 *
 * When the caret is positioned between an empty JSX tag pair like `<div>|</div>`,
 * pressing backspace deletes both the opening `>` and the entire closing tag.
 *
 * @see RescriptTypedHandler for the complementary auto-close behavior
 */
class RescriptBackspaceHandler : BackspaceHandlerDelegate() {
    override fun beforeCharDeleted(
        c: Char,
        file: PsiFile,
        editor: Editor,
    ) {
        // Only handle in ReScript files
        if (file !is RescriptFile) return

        val offset = editor.caretModel.offset
        val document = editor.document
        val text = document.charsSequence

        // Check if we're deleting '>' and there's a closing tag right after
        if (offset < 1 || offset >= text.length) return
        if (text[offset - 1] != '>') return

        // Find the opening tag name by scanning backward from '>'
        val tagName = extractTagNameBeforeGt(text, offset - 1) ?: return

        // Check if closing tag follows immediately: </tagName>
        val closingTag = "</$tagName>"
        val remaining = text.subSequence(offset, minOf(offset + closingTag.length, text.length))
        if (remaining.toString() != closingTag) return

        // Delete the closing tag
        document.deleteString(offset, offset + closingTag.length)
    }

    override fun charDeleted(
        c: Char,
        file: PsiFile,
        editor: Editor,
    ): Boolean = false

    /**
     * Extracts the JSX tag name by scanning backward from the `>` character.
     *
     * Skips over attributes and whitespace to find the opening `<`, then reads
     * the tag name. Handles nested braces from JSX expressions in attributes.
     *
     * @param text the document text
     * @param gtIndex the index of the `>` character
     * @return the tag name, or null if not a valid JSX opening tag
     */
    private fun extractTagNameBeforeGt(
        text: CharSequence,
        gtIndex: Int,
    ): String? {
        var i = gtIndex - 1
        // Skip attributes and whitespace to find '<'
        var depth = 0
        while (i >= 0) {
            when (text[i]) {
                '<' -> {
                    if (depth == 0) {
                        val afterLt = i + 1
                        if (afterLt >= text.length) return null
                        // Not a closing tag
                        if (text[afterLt] == '/') return null
                        // Extract tag name (letters, digits, underscores, dots for module paths)
                        val sb = StringBuilder()
                        var j = afterLt
                        while (j < text.length && (text[j].isLetterOrDigit() || text[j] == '_' || text[j] == '.')) {
                            sb.append(text[j])
                            j++
                        }
                        val name = sb.toString()
                        if (name.isEmpty() || name.endsWith('.') || !name[0].isLetter()) return null
                        return name
                    }
                }
                '{' -> depth++
                '}' -> depth--
            }
            i--
        }
        return null
    }
}
