package com.rescript.plugin.editor

import com.intellij.codeInsight.editorActions.CopyPastePreProcessor
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.RawText
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.rescript.plugin.lang.RescriptTokenTypes
import com.rescript.plugin.lang.psi.RescriptFile

/**
 * Pre-processes paste operations to escape special characters when pasting into string literals.
 *
 * When the caret is inside a `STRING_VALUE` token, automatically escapes `"`, `\`, newlines,
 * and tabs in the pasted text. Inside template literals (`JS_STRING_OPEN`..`JS_STRING_CLOSE`),
 * also escapes `$` and backtick characters.
 */
class RescriptCopyPastePreProcessor : CopyPastePreProcessor {
    override fun preprocessOnCopy(
        file: PsiFile?,
        startOffsets: IntArray?,
        endOffsets: IntArray?,
        text: String?,
    ): String? = null

    override fun preprocessOnPaste(
        project: Project?,
        file: PsiFile?,
        editor: Editor?,
        text: String?,
        rawText: RawText?,
    ): String {
        if (text == null) return ""
        if (file !is RescriptFile || editor == null) return text

        val offset = editor.caretModel.offset
        val psiElement = file.findElementAt(offset) ?: return text
        val tokenType = psiElement.node?.elementType ?: return text

        return when (tokenType) {
            RescriptTokenTypes.STRING_VALUE -> escapeForString(text)

            RescriptTokenTypes.JS_STRING_OPEN,
            RescriptTokenTypes.JS_STRING_CLOSE,
            -> escapeForTemplateString(text)

            else -> text
        }
    }

    companion object {
        /**
         * Escapes special characters for a regular string literal.
         *
         * @param text the raw text to escape
         * @return the escaped text safe for insertion into a `"..."` string
         */
        internal fun escapeForString(text: String): String =
            text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\t", "\\t")

        /**
         * Escapes special characters for a template string literal.
         *
         * In addition to standard escapes, also escapes `$` and backtick characters
         * that have special meaning in template strings.
         *
         * @param text the raw text to escape
         * @return the escaped text safe for insertion into a template string
         */
        internal fun escapeForTemplateString(text: String): String =
            text
                .replace("\\", "\\\\")
                .replace("`", "\\`")
                .replace("$", "\\$")
                .replace("\n", "\\n")
                .replace("\t", "\\t")
    }
}
