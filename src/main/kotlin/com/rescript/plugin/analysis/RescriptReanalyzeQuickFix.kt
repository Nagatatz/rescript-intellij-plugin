package com.rescript.plugin.analysis

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

/**
 * Quick fixes for reanalyze diagnostics.
 */
sealed class RescriptReanalyzeQuickFix(
    private val fixText: String,
) : IntentionAction {
    override fun getText(): String = fixText

    override fun getFamilyName(): String = "ReScript reanalyze"

    override fun isAvailable(
        project: Project,
        editor: Editor?,
        file: PsiFile?,
    ): Boolean = true

    override fun startInWriteAction(): Boolean = true

    /**
     * Prefixes the identifier at the caret with underscore to suppress unused warnings.
     */
    class PrefixWithUnderscore : RescriptReanalyzeQuickFix("Prefix with underscore") {
        override fun invoke(
            project: Project,
            editor: Editor?,
            file: PsiFile?,
        ) {
            if (editor == null || file == null) return
            val document = editor.document
            val selectionModel = editor.selectionModel

            // Use selection or word at caret
            val start: Int
            val end: Int
            if (selectionModel.hasSelection()) {
                start = selectionModel.selectionStart
                end = selectionModel.selectionEnd
            } else {
                val offset = editor.caretModel.offset
                val text = document.text
                start = findWordStart(text, offset)
                end = findWordEnd(text, offset)
            }

            if (start >= end || start < 0) return
            val identifier =
                document.getText(
                    com.intellij.openapi.util
                        .TextRange(start, end),
                )

            // Don't double-prefix
            if (identifier.startsWith("_")) return

            document.replaceString(start, end, "_$identifier")
        }
    }

    /**
     * Removes the unused element (entire line if it's a single-line declaration).
     */
    class RemoveUnused : RescriptReanalyzeQuickFix("Remove unused code") {
        override fun invoke(
            project: Project,
            editor: Editor?,
            file: PsiFile?,
        ) {
            if (editor == null || file == null) return
            val document = editor.document
            val selectionModel = editor.selectionModel

            // Determine the range to delete
            val start: Int
            val end: Int
            if (selectionModel.hasSelection()) {
                // Delete the full lines containing the selection
                val startLine = document.getLineNumber(selectionModel.selectionStart)
                val endLine = document.getLineNumber(selectionModel.selectionEnd)
                start = document.getLineStartOffset(startLine)
                end =
                    if (endLine + 1 < document.lineCount) {
                        document.getLineStartOffset(endLine + 1)
                    } else {
                        document.getLineEndOffset(endLine)
                    }
            } else {
                // Delete the current line
                val line = document.getLineNumber(editor.caretModel.offset)
                start = document.getLineStartOffset(line)
                end =
                    if (line + 1 < document.lineCount) {
                        document.getLineStartOffset(line + 1)
                    } else {
                        document.getLineEndOffset(line)
                    }
            }

            if (start < end) {
                document.deleteString(start, end)
            }
        }
    }

    companion object {
        private val UNUSED_NAMES =
            setOf(
                "unusedVariable",
                "unusedArgument",
                "unusedValue",
                "unusedType",
                "unusedModule",
            )

        private val DEAD_CODE_NAMES =
            setOf(
                "deadCode",
                "deadModule",
                "deadType",
                "deadValue",
                "deadException",
            )

        /**
         * Creates appropriate quick fixes based on the diagnostic name.
         */
        fun createQuickFixes(diagnosticName: String): List<IntentionAction> =
            when {
                diagnosticName in UNUSED_NAMES ->
                    listOf(PrefixWithUnderscore(), RemoveUnused())
                diagnosticName in DEAD_CODE_NAMES ->
                    listOf(RemoveUnused())
                else -> emptyList()
            }

        internal fun findWordStart(
            text: String,
            offset: Int,
        ): Int {
            var i = offset.coerceAtMost(text.length - 1)
            if (i < 0) return 0
            while (i > 0 && isIdentifierChar(text[i - 1])) i--
            return i
        }

        internal fun findWordEnd(
            text: String,
            offset: Int,
        ): Int {
            var i = offset
            while (i < text.length && isIdentifierChar(text[i])) i++
            return i
        }

        private fun isIdentifierChar(c: Char): Boolean = c.isLetterOrDigit() || c == '_' || c == '\''
    }
}
