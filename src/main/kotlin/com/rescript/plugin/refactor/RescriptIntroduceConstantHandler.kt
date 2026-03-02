package com.rescript.plugin.refactor

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.refactoring.RefactoringActionHandler
import com.rescript.plugin.lang.psi.RescriptFile

/**
 * Handles the Introduce Constant refactoring for ReScript.
 *
 * Extracts a literal value (string, number, or boolean) from the selected text
 * into a top-level `let` binding and replaces the original occurrence(s) with
 * the new constant name.
 *
 * @see RefactoringActionHandler
 * @see RescriptRefactoringSupportProvider which can register this handler
 */
class RescriptIntroduceConstantHandler : RefactoringActionHandler {
    override fun invoke(
        project: Project,
        editor: Editor,
        file: PsiFile,
        dataContext: DataContext?,
    ) {
        if (file !is RescriptFile) return

        val selectedText = editor.selectionModel.selectedText
        if (selectedText.isNullOrBlank()) return
        if (!isLiteral(selectedText)) return

        val selectionStart = editor.selectionModel.selectionStart
        val selectionEnd = editor.selectionModel.selectionEnd
        val document = editor.document

        val constName = suggestName(selectedText)
        val binding = generateBinding(constName, selectedText)

        WriteCommandAction.runWriteCommandAction(project, "Introduce Constant", null, {
            // Insert the constant binding at the top of the file (after any open statements)
            val insertOffset = findInsertOffset(document.text)
            document.insertString(insertOffset, "$binding\n\n")

            // Adjust selection offsets after the insertion
            val shift = binding.length + 2
            document.replaceString(selectionStart + shift, selectionEnd + shift, constName)

            // Position caret at the constant name for renaming
            val nameStart = insertOffset + "let ".length
            editor.selectionModel.setSelection(nameStart, nameStart + constName.length)
            editor.caretModel.moveToOffset(nameStart + constName.length)
        })
    }

    override fun invoke(
        project: Project,
        elements: Array<out PsiElement>,
        dataContext: DataContext?,
    ) {
        // Not used — extraction requires an editor with a selection
    }

    companion object {
        // Pattern for numeric literals (integers and floats)
        private val NUMBER_PATTERN = Regex("""^-?\d+\.?\d*$""")

        // Pattern for string literals (double-quoted)
        private val STRING_PATTERN = Regex("""^"[^"]*"$""")

        // Pattern for template string literals
        private val TEMPLATE_PATTERN = Regex("""^`[^`]*`$""")

        /**
         * Checks whether the given text represents a literal value.
         *
         * @param text the selected text
         * @return true if the text is a string, number, boolean, or template literal
         */
        internal fun isLiteral(text: String): Boolean {
            val trimmed = text.trim()
            return NUMBER_PATTERN.matches(trimmed) ||
                STRING_PATTERN.matches(trimmed) ||
                TEMPLATE_PATTERN.matches(trimmed) ||
                trimmed == "true" ||
                trimmed == "false"
        }

        /**
         * Suggests a constant name based on the literal value.
         *
         * @param literal the literal value
         * @return a suggested name (e.g., "value", "message", "flag")
         */
        internal fun suggestName(literal: String): String {
            val trimmed = literal.trim()
            return when {
                NUMBER_PATTERN.matches(trimmed) -> "value"
                trimmed == "true" || trimmed == "false" -> "flag"
                STRING_PATTERN.matches(trimmed) || TEMPLATE_PATTERN.matches(trimmed) -> "message"
                else -> "constant"
            }
        }

        /**
         * Generates a `let` binding string for the constant.
         *
         * @param name the constant name
         * @param value the literal value
         * @return the binding text (e.g., `let value = 42`)
         */
        internal fun generateBinding(
            name: String,
            value: String,
        ): String = "let $name = $value"

        /**
         * Finds the offset where a new top-level binding should be inserted.
         *
         * Inserts after the last `open` statement, or at the beginning of the file.
         *
         * @param text the full document text
         * @return the insertion offset
         */
        internal fun findInsertOffset(text: String): Int {
            val lines = text.lines()
            var lastOpenEnd = 0
            var offset = 0
            for (line in lines) {
                if (line.trimStart().startsWith("open ")) {
                    lastOpenEnd = offset + line.length + 1
                }
                offset += line.length + 1
            }
            return lastOpenEnd
        }
    }
}
