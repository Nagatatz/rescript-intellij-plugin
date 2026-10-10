package com.rescript.plugin.intention

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement

/**
 * Intention action that converts a pipe expression to a function call.
 *
 * Transforms `expr->Module.func(args)` into `Module.func(expr, args)` and
 * `expr->Module.func` (no args) into `Module.func(expr)`.
 *
 * Available when the caret is on or adjacent to the `->` pipe operator.
 *
 * @see RescriptConvertFunctionCallToPipeIntention for the reverse conversion
 */
class RescriptConvertPipeToFunctionCallIntention : RescriptBaseIntention() {
    override fun getText(): String = "Convert pipe to function call"

    override fun isAvailableInRescript(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ): Boolean {
        val document = editor?.document ?: return false
        val offset = editor.caretModel.offset
        val text = document.text

        return findPipeExpression(text, offset) != null
    }

    override fun invoke(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ) {
        val document = editor?.document ?: return
        val offset = editor.caretModel.offset
        val text = document.text

        val match = findPipeExpression(text, offset) ?: return
        val replacement = convertPipeToFunctionCall(match)

        document.replaceString(match.fullStart, match.fullEnd, replacement)
    }

    companion object {
        /** Finds a balanced, supported pipe expression at the caret. */
        internal fun findPipeExpression(
            text: String,
            offset: Int,
        ): PipeExpression? = RescriptPipeConversion.pipe(text, offset)

        /**
         * Converts a pipe expression to its function call equivalent.
         *
         * @param expr the parsed pipe expression
         * @return the function call string
         */
        internal fun convertPipeToFunctionCall(expr: PipeExpression): String {
            val args = expr.args
            return if (args.isNullOrBlank()) {
                "${expr.funcName}(${expr.lhs})"
            } else {
                "${expr.funcName}(${expr.lhs}, $args)"
            }
        }
    }

    /**
     * Represents a parsed pipe expression `lhs->funcName(args)`.
     *
     * @property lhs the left-hand side expression (before the pipe)
     * @property funcName the function being piped into
     * @property args the arguments inside parentheses (without parens), or null
     * @property fullStart start offset of the entire expression in the document
     * @property fullEnd end offset of the entire expression in the document
     */
    data class PipeExpression(
        val lhs: String,
        val funcName: String,
        val args: String?,
        val fullStart: Int,
        val fullEnd: Int,
    )
}
