package com.rescript.plugin.intention

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.rescript.plugin.lang.psi.RescriptFile

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
class RescriptConvertPipeToFunctionCallIntention : PsiElementBaseIntentionAction() {
    override fun getText(): String = "Convert pipe to function call"

    override fun getFamilyName(): String = "Convert pipe to function call"

    override fun isAvailable(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ): Boolean {
        if (element.containingFile !is RescriptFile) return false
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
        // Matches: expr->func(args) or expr->func
        // The pipe operator -> must be at or near the cursor
        private val PIPE_PATTERN =
            Regex("""([a-zA-Z0-9_.)}\]\['"]+)\s*->\s*([a-zA-Z0-9_.]+)(\([^)]*\))?""")

        /**
         * Finds a pipe expression around the given offset in the text.
         *
         * @param text the full document text
         * @param offset the caret position
         * @return the parsed pipe expression, or null if none found
         */
        internal fun findPipeExpression(
            text: String,
            offset: Int,
        ): PipeExpression? {
            // Search in a window around the cursor for the pipe operator
            val lineStart = text.lastIndexOf('\n', (offset - 1).coerceAtLeast(0)) + 1
            val lineEnd = text.indexOf('\n', offset).let { if (it < 0) text.length else it }
            val line = text.substring(lineStart, lineEnd)
            val offsetInLine = offset - lineStart

            for (match in PIPE_PATTERN.findAll(line)) {
                val pipeIndex = line.indexOf("->", match.range.first)
                if (pipeIndex < 0) continue

                // Check if cursor is within reasonable distance of the pipe operator
                if (offsetInLine in (match.range.first)..(match.range.last + 1)) {
                    val lhs = match.groupValues[1]
                    val funcName = match.groupValues[2]
                    val argsWithParens = match.groupValues[3]

                    return PipeExpression(
                        lhs = lhs,
                        funcName = funcName,
                        args =
                            if (argsWithParens.isNotEmpty()) {
                                // Remove surrounding parens
                                argsWithParens.substring(1, argsWithParens.length - 1)
                            } else {
                                null
                            },
                        fullStart = lineStart + match.range.first,
                        fullEnd = lineStart + match.range.last + 1,
                    )
                }
            }
            return null
        }

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
