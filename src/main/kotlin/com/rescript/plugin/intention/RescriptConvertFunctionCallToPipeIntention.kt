package com.rescript.plugin.intention

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement

/**
 * Intention action that converts a function call to a pipe expression.
 *
 * Transforms `Module.func(expr, args)` into `expr->Module.func(args)` and
 * `Module.func(expr)` (single arg) into `expr->Module.func`.
 *
 * Available when the caret is on a qualified function call with at least one argument.
 *
 * @see RescriptConvertPipeToFunctionCallIntention for the reverse conversion
 */
class RescriptConvertFunctionCallToPipeIntention : RescriptBaseIntention() {
    override fun getText(): String = "Convert function call to pipe"

    override fun isAvailableInRescript(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ): Boolean {
        val document = editor?.document ?: return false
        val offset = editor.caretModel.offset
        val text = document.text

        return findFunctionCall(text, offset) != null
    }

    override fun invoke(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ) {
        val document = editor?.document ?: return
        val offset = editor.caretModel.offset
        val text = document.text

        val match = findFunctionCall(text, offset) ?: return
        val replacement = convertFunctionCallToPipe(match)

        document.replaceString(match.fullStart, match.fullEnd, replacement)
    }

    companion object {
        /** Finds a balanced qualified call with a safely movable first argument. */
        internal fun findFunctionCall(
            text: String,
            offset: Int,
        ): FunctionCall? = RescriptPipeConversion.call(text, offset)

        /** Splits commas outside strings, comments and balanced delimiters. */
        internal fun splitArgs(args: String): List<String> = RescriptPipeConversion.splitArguments(args)

        /**
         * Converts a function call to its pipe expression equivalent.
         *
         * @param call the parsed function call
         * @return the pipe expression string
         */
        internal fun convertFunctionCallToPipe(call: FunctionCall): String {
            val qualifiedFunc = "${call.modulePath}.${call.funcName}"
            return if (call.remainingArgs.isEmpty()) {
                "${call.firstArg}->$qualifiedFunc"
            } else {
                val remaining = call.remainingArgs.joinToString(", ")
                "${call.firstArg}->$qualifiedFunc($remaining)"
            }
        }
    }

    /**
     * Represents a parsed function call `Module.func(firstArg, remainingArgs...)`.
     *
     * @property modulePath the module path (e.g., "Array" or "Belt.Array")
     * @property funcName the function name
     * @property firstArg the first argument (becomes the pipe LHS)
     * @property remainingArgs remaining arguments after the first
     * @property fullStart start offset of the entire expression in the document
     * @property fullEnd end offset of the entire expression in the document
     */
    data class FunctionCall(
        val modulePath: String,
        val funcName: String,
        val firstArg: String,
        val remainingArgs: List<String>,
        val fullStart: Int,
        val fullEnd: Int,
    )
}
