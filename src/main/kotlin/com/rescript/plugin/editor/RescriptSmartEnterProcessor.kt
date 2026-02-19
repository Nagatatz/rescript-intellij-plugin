package com.rescript.plugin.editor

import com.intellij.codeInsight.editorActions.smartEnter.SmartEnterProcessor
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.rescript.plugin.lang.RescriptLexer
import com.rescript.plugin.lang.RescriptTokenTypes as T

/**
 * Smart Enter processor for ReScript (Shift+Enter).
 *
 * Analyzes the current line and performs context-aware completions:
 * - `switch expr` without `{` -> inserts `{ | }` block with cursor on the pipe arm
 * - `| pattern` without `=>` -> appends ` => `
 * - Unclosed `{`, `(`, or `[` -> inserts newline with closing bracket and proper indentation
 */
class RescriptSmartEnterProcessor : SmartEnterProcessor() {
    override fun process(
        project: Project,
        editor: Editor,
        psiFile: PsiFile,
    ): Boolean {
        val document = editor.document
        val caretOffset = editor.caretModel.offset
        val lineNumber = document.getLineNumber(caretOffset)
        val lineStart = document.getLineStartOffset(lineNumber)
        val lineEnd = document.getLineEndOffset(lineNumber)
        val lineText = document.getText(TextRange(lineStart, lineEnd))

        val analysis = analyzeLine(lineText)
        val indent = lineText.takeWhile { it == ' ' || it == '\t' }

        when {
            analysis.hasSwitchWithoutBrace -> {
                val text = " {\n$indent| \n$indent}"
                document.insertString(lineEnd, text)
                editor.caretModel.moveToOffset(lineEnd + 4 + indent.length)
                return true
            }

            analysis.hasPipeWithoutArrow -> {
                document.insertString(lineEnd, " => ")
                editor.caretModel.moveToOffset(lineEnd + 4)
                return true
            }

            analysis.unclosedBracket != null -> {
                val closingChar =
                    when (analysis.unclosedBracket) {
                        '{' -> '}'
                        '(' -> ')'
                        '[' -> ']'
                        else -> return false
                    }
                val indentUnit = "  "
                val text = "\n$indent$indentUnit\n$indent$closingChar"
                document.insertString(lineEnd, text)
                editor.caretModel.moveToOffset(lineEnd + 1 + indent.length + indentUnit.length)
                return true
            }

            else -> return false
        }
    }

    internal data class LineAnalysis(
        val hasSwitchWithoutBrace: Boolean = false,
        val hasPipeWithoutArrow: Boolean = false,
        val unclosedBracket: Char? = null,
    )

    internal fun analyzeLine(lineText: String): LineAnalysis {
        val lexer = RescriptLexer()
        lexer.start(lineText)

        var hasSwitch = false
        var hasLBrace = false
        var hasPipe = false
        var hasArrow = false

        var braceBalance = 0
        var parenBalance = 0
        var bracketBalance = 0

        while (lexer.tokenType != null) {
            val type = lexer.tokenType!!

            when (type) {
                T.SWITCH -> hasSwitch = true
                T.LBRACE -> {
                    hasLBrace = true
                    braceBalance++
                }
                T.RBRACE -> braceBalance--
                T.LPAREN -> parenBalance++
                T.RPAREN -> parenBalance--
                T.LBRACKET -> bracketBalance++
                T.RBRACKET -> bracketBalance--
                T.PIPE -> hasPipe = true
                T.ARROW -> hasArrow = true
            }

            lexer.advance()
        }

        val unclosedBracket =
            when {
                braceBalance > 0 -> '{'
                parenBalance > 0 -> '('
                bracketBalance > 0 -> '['
                else -> null
            }

        return LineAnalysis(
            hasSwitchWithoutBrace = hasSwitch && !hasLBrace,
            hasPipeWithoutArrow = hasPipe && !hasArrow && !hasSwitch,
            unclosedBracket = unclosedBracket,
        )
    }
}
