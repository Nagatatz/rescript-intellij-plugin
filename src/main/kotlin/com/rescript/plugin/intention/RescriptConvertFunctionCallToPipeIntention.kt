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
        // Matches: Module.func(args) — qualified function call with at least one dot
        private val FUNC_CALL_PATTERN =
            Regex("""([A-Z][a-zA-Z0-9_]*(?:\.[a-zA-Z_][a-zA-Z0-9_]*)*)\.([a-zA-Z_][a-zA-Z0-9_]*)\(([^)]+)\)""")

        /**
         * Finds a qualified function call around the given offset in the text.
         *
         * @param text the full document text
         * @param offset the caret position
         * @return the parsed function call, or null if none found
         */
        internal fun findFunctionCall(
            text: String,
            offset: Int,
        ): FunctionCall? {
            val lineStart = text.lastIndexOf('\n', (offset - 1).coerceAtLeast(0)) + 1
            val lineEnd = text.indexOf('\n', offset).let { if (it < 0) text.length else it }
            val line = text.substring(lineStart, lineEnd)
            val offsetInLine = offset - lineStart

            for (match in FUNC_CALL_PATTERN.findAll(line)) {
                if (offsetInLine in (match.range.first)..(match.range.last + 1)) {
                    val modulePath = match.groupValues[1]
                    val funcName = match.groupValues[2]
                    val allArgs = match.groupValues[3]

                    // Split arguments by comma, respecting nested parentheses
                    val args = splitArgs(allArgs)
                    if (args.isEmpty()) return null

                    val firstArg = args[0].trim()
                    val remainingArgs = args.drop(1).map { it.trim() }

                    return FunctionCall(
                        modulePath = modulePath,
                        funcName = funcName,
                        firstArg = firstArg,
                        remainingArgs = remainingArgs,
                        fullStart = lineStart + match.range.first,
                        fullEnd = lineStart + match.range.last + 1,
                    )
                }
            }
            return null
        }

        /**
         * Splits a comma-separated argument list, respecting nested parentheses.
         *
         * @param args the argument string (without outer parentheses)
         * @return a list of individual argument strings
         */
        internal fun splitArgs(args: String): List<String> {
            val result = mutableListOf<String>()
            var depth = 0
            var start = 0

            for (i in args.indices) {
                when (args[i]) {
                    '(' -> {
                        depth++
                    }

                    ')' -> {
                        depth--
                    }

                    ',' -> {
                        if (depth == 0) {
                            result.add(args.substring(start, i))
                            start = i + 1
                        }
                    }
                }
            }
            result.add(args.substring(start))
            return result
        }

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
