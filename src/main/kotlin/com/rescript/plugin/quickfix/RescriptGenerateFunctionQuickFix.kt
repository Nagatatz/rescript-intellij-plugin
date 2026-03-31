package com.rescript.plugin.quickfix

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.rescript.plugin.lang.RescriptTokenTypes
import com.rescript.plugin.lang.psi.RescriptFile

/**
 * Quick fix that generates a stub function from an unresolved function call.
 *
 * When the caret is on a lowercase identifier that appears to be a function call
 * (followed by parentheses), this action generates a stub `let` binding with
 * placeholder parameters and inserts it before the current top-level declaration.
 *
 * Triggered via Alt+Enter > "Generate function".
 *
 * @see RescriptAddOpenQuickFix
 */
class RescriptGenerateFunctionQuickFix : PsiElementBaseIntentionAction() {
    override fun getText(): String = "Generate function"

    override fun getFamilyName(): String = "Generate function"

    override fun isAvailable(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ): Boolean {
        if (element.containingFile !is RescriptFile) return false
        val tokenType = element.node?.elementType ?: return false
        if (tokenType != RescriptTokenTypes.LIDENT) return false

        // Check if followed by parentheses (function call pattern)
        val text = editor?.document?.text ?: return false
        val endOffset = element.textRange.endOffset
        if (endOffset >= text.length) return false

        val afterIdent = text.substring(endOffset).trimStart()
        return afterIdent.startsWith("(")
    }

    override fun invoke(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ) {
        editor ?: return
        val document = editor.document
        val text = document.text
        val funcName = element.text
        val endOffset = element.textRange.endOffset

        // Parse arguments from the call site
        val argCount = countArguments(text, endOffset)
        val stubFunction = generateStubFunction(funcName, argCount)

        // Find the start of the current top-level declaration to insert before it
        val insertOffset = findTopLevelDeclarationStart(text, element.textRange.startOffset)

        WriteCommandAction.runWriteCommandAction(project) {
            document.insertString(insertOffset, "$stubFunction\n\n")
        }
    }

    companion object {
        /**
         * Counts the number of arguments in a function call.
         *
         * @param text the document text
         * @param callStart the offset just after the function name
         * @return the number of arguments (at least 1 if parens contain non-empty content)
         */
        internal fun countArguments(
            text: String,
            callStart: Int,
        ): Int {
            val parenStart = text.indexOf('(', callStart)
            if (parenStart < 0) return 0

            var depth = 0
            var commaCount = 0
            var hasContent = false

            for (i in parenStart until text.length) {
                when (text[i]) {
                    '(' -> {
                        depth++
                    }

                    ')' -> {
                        depth--
                        if (depth == 0) {
                            return if (hasContent) commaCount + 1 else 0
                        }
                    }

                    ',' -> {
                        if (depth == 1) commaCount++
                    }

                    else -> {
                        if (depth == 1 && !text[i].isWhitespace()) hasContent = true
                    }
                }
            }
            return 0
        }

        /**
         * Generates a stub function definition.
         *
         * @param name the function name
         * @param argCount the number of arguments
         * @return the generated function text
         */
        internal fun generateStubFunction(
            name: String,
            argCount: Int,
        ): String {
            if (argCount == 0) {
                return "let $name = () => todo"
            }
            val params = (1..argCount).joinToString(", ") { "arg$it" }
            return "let $name = ($params) => todo"
        }

        /**
         * Finds the start of the top-level declaration containing the given offset.
         *
         * @param text the document text
         * @param offset the current offset
         * @return the start of the top-level declaration line
         */
        internal fun findTopLevelDeclarationStart(
            text: String,
            offset: Int,
        ): Int {
            // Walk backwards to find a line starting with "let" or "type" at column 0
            var pos = offset
            while (pos > 0) {
                val lineStart = text.lastIndexOf('\n', pos - 1).let { if (it < 0) 0 else it + 1 }
                val lineText = text.substring(lineStart, pos.coerceAtMost(text.length)).trimStart()
                if (lineText.startsWith("let ") ||
                    lineText.startsWith("type ") ||
                    lineText.startsWith("module ") ||
                    lineText.startsWith("external ")
                ) {
                    return lineStart
                }
                pos = lineStart - 1
                if (pos < 0) break
            }
            return 0
        }
    }
}
