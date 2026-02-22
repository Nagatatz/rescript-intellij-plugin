package com.rescript.plugin.refactor

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.refactoring.RefactoringActionHandler
import com.rescript.plugin.lang.psi.RescriptFile

/**
 * Handles the Extract Variable (Introduce Variable) refactoring for ReScript.
 *
 * Extracts the selected expression into a `let` binding placed before the
 * containing statement, and replaces the original selection with the new
 * variable name. After extraction, the variable name is selected so the
 * user can immediately rename it.
 *
 * Triggered via `Ctrl+Alt+V` or Refactor > Introduce Variable.
 *
 * @see RescriptRefactoringSupportProvider which registers this handler
 * @see RescriptExtractVariableUtil for the underlying text-manipulation algorithms
 */
class RescriptExtractVariableHandler : RefactoringActionHandler {
    override fun invoke(
        project: Project,
        editor: Editor,
        file: PsiFile,
        dataContext: DataContext?,
    ) {
        if (file !is RescriptFile) return

        val selectionModel = editor.selectionModel
        val selectedText = selectionModel.selectedText
        if (selectedText.isNullOrBlank()) return

        val selectionStart = selectionModel.selectionStart
        val selectionEnd = selectionModel.selectionEnd
        val document = editor.document
        val documentText = document.text

        // Find where to insert the let binding
        val statementLine = RescriptExtractVariableUtil.findStatementStartLine(documentText, selectionStart)
        val lineStartOffset = document.getLineStartOffset(statementLine)
        val lineText =
            document.getText(
                TextRange(lineStartOffset, document.getLineEndOffset(statementLine)),
            )
        val indent = RescriptExtractVariableUtil.getIndentation(lineText)

        // Generate variable name and let binding
        val varName = RescriptExtractVariableUtil.suggestVariableName(selectedText)
        val letBinding = RescriptExtractVariableUtil.buildLetBinding(varName, selectedText, indent)

        WriteCommandAction.runWriteCommandAction(project, "Extract Variable", null, {
            // Insert let binding before the statement line
            document.insertString(lineStartOffset, letBinding)

            // The original selection has shifted by the length of the inserted text
            val shift = letBinding.length
            val newSelectionStart = selectionStart + shift
            val newSelectionEnd = selectionEnd + shift

            // Replace the original selected expression with the variable name
            document.replaceString(newSelectionStart, newSelectionEnd, varName)

            // Select the variable name in the let binding for easy rename
            // The variable name starts after "let " in the inserted binding
            val varNameInLetStart = lineStartOffset + indent.length + "let ".length
            val varNameInLetEnd = varNameInLetStart + varName.length
            editor.selectionModel.setSelection(varNameInLetStart, varNameInLetEnd)
            editor.caretModel.moveToOffset(varNameInLetEnd)
        })
    }

    override fun invoke(
        project: Project,
        elements: Array<out PsiElement>,
        dataContext: DataContext?,
    ) {
        // Not used — extraction requires an editor with a selection
    }
}
