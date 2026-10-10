package com.rescript.plugin.intention

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.rescript.plugin.lang.psi.RescriptFile

/** Removes only proven scalar initializer groups, retaining their original trivia. */
class RescriptRemoveParenthesesIntention : RescriptBaseIntention() {
    override fun getText(): String = "Remove unnecessary parentheses"

    override fun isAvailableInRescript(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ): Boolean = plan(project, editor, element) != null

    override fun invoke(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ) {
        val edit = plan(project, editor, element) ?: return
        val document = editor!!.document
        val caret = editor.caretModel.offset
        WriteCommandAction.runWriteCommandAction(project) {
            if (editor.caretModel.offset != caret || document.text != edit.source ||
                plan(project, editor, element) != edit
            ) {
                return@runWriteCommandAction
            }
            document.replaceString(edit.open, edit.close + 1, edit.source.substring(edit.open + 1, edit.close))
        }
    }

    private fun plan(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ): RescriptParenthesesRemovalPlan? {
        if (project.isDisposed || editor == null || !element.isValid) return null
        val file = element.containingFile as? RescriptFile ?: return null
        val manager = PsiDocumentManager.getInstance(project)
        if (!manager.isCommitted(editor.document) || manager.getPsiFile(editor.document) !== file ||
            file.text != editor.document.text
        ) {
            return null
        }
        return RescriptParenthesesRemovalPlan.create(editor.document.text, editor.caretModel.offset)
    }

    companion object {
        /** Finds real balanced parentheses at a code caret, ignoring literal/comment contents. */
        internal fun findEnclosingParens(
            text: String,
            offset: Int,
        ): Pair<Int, Int>? = RescriptParenthesesRemovalPlan.enclosing(text, offset)

        /** Validates the same supported initializer proof used by the public intention. */
        internal fun isRemovable(
            text: String,
            openParen: Int,
            closeParen: Int,
            inner: String,
        ): Boolean {
            val plan = RescriptParenthesesRemovalPlan.create(text, openParen) ?: return false
            return plan.open == openParen && plan.close == closeParen &&
                text.substring(openParen + 1, closeParen).trim() == inner
        }

        /** Checks commas outside balanced groups using lexer tokens. */
        internal fun containsTopLevelComma(text: String): Boolean =
            RescriptParenthesesRemovalPlan.topLevelContains(text, comma = true)

        /** Checks operators outside balanced groups using lexer tokens. */
        internal fun containsTopLevelOperator(text: String): Boolean =
            RescriptParenthesesRemovalPlan.topLevelContains(text, comma = false)
    }
}
