package com.rescript.plugin.refactor

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.refactoring.RefactoringActionHandler
import com.rescript.plugin.lang.psi.RescriptFile

/**
 * Handles the Extract Function refactoring for ReScript.
 *
 * Extracts the selected code into a new `let` binding before the selected
 * statement in the same block, resolving free variables in a supported lexical subset to use as function
 * parameters. The original selection is replaced with a call to the new function.
 *
 * @see RefactoringActionHandler
 * @see RescriptRefactoringSupportProvider which registers this handler
 */
class RescriptExtractFunctionHandler : RefactoringActionHandler {
    override fun invoke(
        project: Project,
        editor: Editor,
        file: PsiFile,
        dataContext: DataContext?,
    ) {
        if (file !is RescriptFile) return

        val document = editor.document
        if (PsiDocumentManager.getInstance(project).getDocument(file) !== document) return
        val original = document.text
        val start = editor.selectionModel.selectionStart
        val end = editor.selectionModel.selectionEnd
        val plan = RescriptExtractFunctionPlan.create(original, start, end) ?: return
        val declaration = generateFunction(plan.name, plan.body, plan.parameters)
        val function =
            (if (plan.topLevel) "@private\n$declaration" else declaration)
                .lines()
                .joinToString("\n") { plan.indent + it } + "\n\n"
        val call = generateCallSite(plan.name, plan.parameters)
        WriteCommandAction.runWriteCommandAction(project, "Extract Function", null, {
            if (document.text != original) return@runWriteCommandAction
            document.replaceString(start, end, call)
            document.insertString(plan.insertOffset, function)
            editor.caretModel.moveToOffset(plan.insertOffset + plan.indent.length + "let ".length + plan.name.length)
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
        /**
         * Resolves references against the complete snapshot rather than guessing from spelling.
         *
         * @param selectedText the exact selected source slice
         * @param fullText complete source context
         * @param selectionStart the slice's start offset
         * @return external binding names; unsupported input has no valid parameter list
         */
        internal fun findFreeVariables(
            selectedText: String,
            fullText: String,
            selectionStart: Int,
        ): List<String>? {
            val end = selectionStart + selectedText.length
            if (selectionStart < 0 || end > fullText.length ||
                fullText.substring(selectionStart, end) != selectedText
            ) {
                return null
            }
            return RescriptExtractFunctionPlan.create(fullText, selectionStart, end)?.parameters
        }

        /**
         * Generates a `let` function binding from the selected code and free variables.
         *
         * @param name the function name
         * @param body the function body (selected code)
         * @param params the parameter names (free variables)
         * @return the function source text
         */
        internal fun generateFunction(
            name: String,
            body: String,
            params: List<String>,
        ): String {
            val paramList = if (params.isEmpty()) "()" else "(${params.joinToString(", ")})"
            val indentedBody = body.lines().joinToString("\n") { "  $it" }

            return "let $name = $paramList => {\n$indentedBody\n}"
        }

        /**
         * Generates a call expression for the extracted function.
         *
         * @param name the function name
         * @param args the argument names (matching the parameters)
         * @return the call expression string
         */
        internal fun generateCallSite(
            name: String,
            args: List<String>,
        ): String =
            if (args.isEmpty()) {
                "$name()"
            } else {
                "$name(${args.joinToString(", ")})"
            }
    }
}
