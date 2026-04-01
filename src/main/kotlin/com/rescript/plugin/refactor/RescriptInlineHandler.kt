package com.rescript.plugin.refactor

import com.intellij.lang.Language
import com.intellij.lang.refactoring.InlineActionHandler
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.rescript.plugin.RescriptLanguage
import com.rescript.plugin.lang.psi.RescriptFile
import com.rescript.plugin.util.RescriptEditorUtils.getLineRangeAt
import com.rescript.plugin.util.RescriptEditorUtils.getLineTextAt

/**
 * Handles inline refactoring for ReScript variables and simple functions.
 *
 * Replaces all usages of a `let` binding with its right-hand side expression
 * and removes the original declaration. Adds parentheses around the inlined
 * expression when it appears in an operator context to preserve semantics.
 *
 * Triggered via `Refactor > Inline` or `Ctrl+Alt+N`.
 *
 * @see InlineActionHandler
 * @see RescriptRefactoringSupportProvider
 */
class RescriptInlineHandler : InlineActionHandler() {
    override fun isEnabledForLanguage(language: Language): Boolean = language == RescriptLanguage

    override fun canInlineElement(element: PsiElement): Boolean {
        if (element.containingFile !is RescriptFile) return false
        val lineText = getLineText(element)
        return LET_PATTERN.containsMatchIn(lineText)
    }

    override fun inlineElement(
        project: Project,
        editor: Editor,
        element: PsiElement,
    ) {
        val document = editor.document
        val text = document.text
        val offset = editor.caretModel.offset
        val (lineStart, lineEnd) = document.getLineRangeAt(offset)
        val lineText = document.getLineTextAt(offset)

        val match = LET_PATTERN.find(lineText) ?: return
        val varName = match.groupValues[1]
        val value = match.groupValues[2].trim()

        // Find all usages of the variable in the file
        val usages = findUsages(text, varName, lineStart, lineEnd)
        if (usages.isEmpty()) return

        WriteCommandAction.runWriteCommandAction(project, "Inline Variable", null, {
            // Replace usages from end to start to preserve offsets
            val sortedUsages = usages.sortedByDescending { it }
            for (usageOffset in sortedUsages) {
                val replacement = if (needsParentheses(text, usageOffset, varName.length)) "($value)" else value
                document.replaceString(usageOffset, usageOffset + varName.length, replacement)
            }

            // Remove the let declaration line
            val deleteEnd = if (lineEnd < text.length) lineEnd + 1 else lineEnd
            document.deleteString(lineStart, deleteEnd)
        })
    }

    private fun getLineText(element: PsiElement): String {
        val document =
            com.intellij.psi.PsiDocumentManager
                .getInstance(element.project)
                .getDocument(element.containingFile)
                ?: return ""
        val lineNumber = document.getLineNumber(element.textRange.startOffset)
        return document.getText(
            com.intellij.openapi.util.TextRange(
                document.getLineStartOffset(lineNumber),
                document.getLineEndOffset(lineNumber),
            ),
        )
    }

    companion object {
        // Matches "let name = value" (with possible type annotation)
        internal val LET_PATTERN = Regex("""^\s*let\s+(\w+)\s*(?::\s*[^=]+)?\s*=\s*(.+)$""")

        /**
         * Finds all occurrences of the variable name in the text, excluding the declaration line.
         *
         * @param text the full document text
         * @param name the variable name to search for
         * @param declStart the start offset of the declaration line (excluded from results)
         * @param declEnd the end offset of the declaration line (excluded from results)
         * @return list of offsets where the variable is used
         */
        internal fun findUsages(
            text: String,
            name: String,
            declStart: Int,
            declEnd: Int,
        ): List<Int> {
            val usages = mutableListOf<Int>()
            val pattern = Regex("""\b${Regex.escape(name)}\b""")
            for (match in pattern.findAll(text)) {
                val offset = match.range.first
                // Skip the declaration line itself
                if (offset in declStart..declEnd) continue
                usages.add(offset)
            }
            return usages
        }

        /**
         * Determines whether parentheses are needed around the inlined expression.
         *
         * Parentheses are added when the variable appears adjacent to an operator
         * (e.g., `a + b` where `a` is being inlined with a complex expression).
         *
         * @param text the full document text
         * @param offset the offset of the variable usage
         * @param nameLength the length of the variable name
         * @return true if parentheses should wrap the replacement
         */
        internal fun needsParentheses(
            text: String,
            offset: Int,
            nameLength: Int,
        ): Boolean {
            val after = if (offset + nameLength < text.length) text[offset + nameLength] else ' '
            val before = if (offset > 0) text[offset - 1] else ' '

            // Operators that suggest we need parens
            val operators = setOf('+', '-', '*', '/', '%', '<', '>', '&', '|', '^')
            return after in operators || before in operators
        }
    }
}
