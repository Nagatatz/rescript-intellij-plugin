package com.rescript.plugin.editor

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.rescript.plugin.lang.RescriptTokenTypes
import com.rescript.plugin.lang.psi.RescriptFile

/**
 * Handles special character typing behavior in ReScript files.
 *
 * Currently supports JSX auto-close: when the user types `>` to complete
 * a JSX opening tag like `<div>`, the corresponding closing tag `</div>`
 * is automatically inserted and the caret is positioned between the tags.
 */
class RescriptTypedHandler : TypedHandlerDelegate() {
    override fun charTyped(
        c: Char,
        project: Project,
        editor: Editor,
        file: PsiFile,
    ): Result {
        if (file !is RescriptFile) return Result.CONTINUE
        if (c != '>') return Result.CONTINUE

        val offset = editor.caretModel.offset
        val document = editor.document
        val text = document.charsSequence

        // Don't act if we're past the end of text
        if (offset < 2) return Result.CONTINUE

        // Check the character before '>' — if it's '/', this is a self-closing tag
        if (text[offset - 2] == '/') return Result.CONTINUE

        // Check if this looks like a JSX opening tag by scanning backward for '<'
        val tagName = extractJsxTagName(text, offset - 1) ?: return Result.CONTINUE

        // Don't insert closing tag if we're in a comment or string
        val psiElement = file.findElementAt(offset - 1)
        if (psiElement != null) {
            val tokenType = psiElement.node?.elementType
            if (tokenType == RescriptTokenTypes.SINGLE_COMMENT ||
                tokenType == RescriptTokenTypes.MULTI_COMMENT ||
                tokenType == RescriptTokenTypes.STRING_VALUE
            ) {
                return Result.CONTINUE
            }
        }

        // Insert closing tag
        val closingTag = "</$tagName>"
        document.insertString(offset, closingTag)

        return Result.STOP
    }

    /**
     * Extracts the JSX tag name by scanning backward from the `>` position.
     *
     * Handles simple tags (`<div>`), component tags (`<Component>`),
     * and module-qualified tags (`<Module.Component>`).
     *
     * @param text the document text
     * @param gtIndex the index of the `>` character
     * @return the full tag name, or null if this doesn't look like a JSX tag
     */
    internal fun extractJsxTagName(
        text: CharSequence,
        gtIndex: Int,
    ): String? {
        // Scan backward from '>' to find '<'
        var i = gtIndex - 1

        // Skip attributes and whitespace to find the tag name region
        // We need to find '<' and then extract the tag name after it
        var depth = 0
        while (i >= 0) {
            when (text[i]) {
                '<' -> {
                    if (depth == 0) {
                        // Found the opening '<'
                        val afterLt = i + 1
                        if (afterLt >= text.length) return null

                        // Check for closing tag pattern '</'
                        if (text[afterLt] == '/') return null

                        // Extract tag name (letters, digits, dots for module paths)
                        val tagName = extractTagNameAfterLt(text, afterLt) ?: return null

                        // Validate: tag name must start with a letter
                        if (tagName.isEmpty() || !tagName[0].isLetter()) return null

                        return tagName
                    }
                }

                '{' -> {
                    depth++
                }

                '}' -> {
                    depth--
                }
            }
            i--
        }
        return null
    }

    /**
     * Extracts the tag name immediately following a `<` character.
     * Supports dotted paths like `Module.Component`.
     */
    private fun extractTagNameAfterLt(
        text: CharSequence,
        start: Int,
    ): String? {
        val sb = StringBuilder()
        var i = start
        while (i < text.length) {
            val c = text[i]
            if (c.isLetterOrDigit() || c == '_' || c == '.') {
                sb.append(c)
            } else {
                break
            }
            i++
        }
        val name = sb.toString()
        // Don't treat as JSX if it's empty or ends with a dot
        if (name.isEmpty() || name.endsWith('.')) return null
        return name
    }
}
