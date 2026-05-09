package com.rescript.plugin.intention

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.InputValidator
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiElement
import com.intellij.refactoring.rename.RenameHandlerRegistry

/**
 * Alt+Enter intention that renames a ReScript variant constructor across
 * the entire project. Activated when the caret sits on a UIDENT that
 * `RescriptConstructorOccurrenceClassifier` reports as either a
 * constructor invocation, an arm pattern, or the trailing tail of a
 * `Module.Foo` qualifier.
 *
 * Unlike Shift+F6 (which routes through `textDocument/rename` on the
 * language server), this intention never calls the LSP server — the
 * project-wide search is driven by [RescriptConstructorOccurrenceFinder]
 * and the per-occurrence classification is handled by
 * [RescriptConstructorOccurrenceClassifier]. That makes the action
 * usable when the language server is not available, at the cost of
 * relying on token-shape heuristics rather than a typed symbol table.
 *
 * @see RenameHandlerRegistry which still owns Shift+F6 / refactor menu
 * @see RescriptConstructorOccurrenceClassifier
 * @see RescriptConstructorOccurrenceFinder
 */
class RescriptRenameVariantConstructorIntention : RescriptBaseIntention() {
    override fun getText(): String = "Rename variant constructor (project-wide, no LSP)"

    override fun isAvailableInRescript(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ): Boolean {
        val doc = editor?.document ?: return false
        val source = doc.text
        val offset = editor.caretModel.offset
        val word = extractWord(source, offset) ?: return false
        if (!word.first().isUpperCase()) return false
        val kind = RescriptConstructorOccurrenceClassifier.classifyAt(source, offset)
        return kind == ConstructorOccurrenceKind.CONSTRUCTOR ||
            kind == ConstructorOccurrenceKind.PATTERN
    }

    override fun invoke(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ) {
        editor ?: return
        val source = editor.document.text
        val offset = editor.caretModel.offset
        val oldName = extractWord(source, offset) ?: return

        val newName =
            Messages.showInputDialog(
                project,
                "Rename variant constructor `$oldName` to:",
                "Rename Variant Constructor",
                null,
                oldName,
                ConstructorNameValidator,
            ) ?: return
        if (newName == oldName) return

        ProgressManager.getInstance().run(
            object : Task.Backgroundable(project, "Searching for `$oldName`…", true) {
                override fun run(indicator: com.intellij.openapi.progress.ProgressIndicator) {
                    val result = RescriptConstructorOccurrenceFinder.findAll(project, oldName)
                    ApplicationManager.getApplication().invokeLater {
                        applyRename(project, result, oldName, newName)
                    }
                }
            },
        )
    }

    private fun applyRename(
        project: Project,
        result: RescriptConstructorOccurrenceFinder.Result,
        oldName: String,
        newName: String,
    ) {
        if (result.truncated) {
            Messages.showErrorDialog(
                project,
                "Found more than ${RescriptConstructorOccurrenceFinder.HARD_CAP} " +
                    "occurrences of `$oldName`. Use Shift+F6 (LSP rename) or narrow " +
                    "the search before retrying.",
                "Rename Variant Constructor",
            )
            return
        }
        if (result.occurrences.isEmpty()) {
            Messages.showInfoMessage(
                project,
                "No constructor-shaped occurrences of `$oldName` were found in the project.",
                "Rename Variant Constructor",
            )
            return
        }
        val fileCount = result.occurrences.distinctBy { it.file }.size
        val confirm =
            Messages.showOkCancelDialog(
                project,
                "Found ${result.occurrences.size} occurrence(s) across $fileCount file(s).\n" +
                    "Rename `$oldName` to `$newName`?",
                "Rename Variant Constructor",
                "Rename",
                "Cancel",
                Messages.getQuestionIcon(),
            )
        if (confirm != Messages.OK) return

        WriteCommandAction.runWriteCommandAction(
            project,
            "Rename variant constructor",
            null,
            {
                val docManager = FileDocumentManager.getInstance()
                val byFile = result.occurrences.groupBy { it.file }
                for ((file, list) in byFile) {
                    val doc = docManager.getDocument(file) ?: continue
                    // Replace from end to start so earlier offsets stay valid as
                    // the document length changes.
                    for (occ in list.sortedByDescending { it.range.startOffset }) {
                        doc.replaceString(occ.range.startOffset, occ.range.endOffset, newName)
                    }
                }
            },
        )
    }

    /**
     * Validator wired into the rename dialog so the user can't submit an
     * empty string, a lowercase name (ReScript constructors must start
     * with an upper-case letter), or a name containing illegal
     * characters.
     */
    private object ConstructorNameValidator : InputValidator {
        override fun checkInput(inputString: String?): Boolean {
            if (inputString.isNullOrBlank()) return false
            val first = inputString.first()
            if (!first.isUpperCase()) return false
            return inputString.all { it.isLetterOrDigit() || it == '_' || it == '\'' }
        }

        override fun canClose(inputString: String?): Boolean = checkInput(inputString)
    }

    companion object {
        /** Extract the identifier word surrounding [offset] in [source]. */
        internal fun extractWord(
            source: String,
            offset: Int,
        ): String? {
            if (offset < 0 || offset > source.length) return null
            var start = offset
            var end = offset
            while (start > 0 && isIdentifierChar(source[start - 1])) start--
            while (end < source.length && isIdentifierChar(source[end])) end++
            if (start == end) return null
            return source.substring(start, end)
        }

        private fun isIdentifierChar(ch: Char): Boolean = ch.isLetterOrDigit() || ch == '_' || ch == '\''
    }
}
