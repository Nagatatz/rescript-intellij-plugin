package com.rescript.plugin.codestyle

import com.intellij.application.options.CodeStyle
import com.intellij.lang.Language
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.codeStyle.lineIndent.LineIndentProvider
import com.intellij.psi.tree.IElementType
import com.rescript.plugin.RescriptFileType
import com.rescript.plugin.RescriptLanguage
import com.rescript.plugin.lang.RescriptLexer
import com.rescript.plugin.lang.RescriptTokenTypes as T

class RescriptLineIndentProvider : LineIndentProvider {
    override fun isSuitableFor(language: Language?): Boolean = language == RescriptLanguage

    override fun getLineIndent(
        project: Project,
        editor: Editor,
        language: Language?,
        offset: Int,
    ): String {
        val document = editor.document
        val lineNumber = document.getLineNumber(offset)
        if (lineNumber == 0) return ""

        val prevLineNumber = findPrevNonEmptyLine(document, lineNumber) ?: return ""
        val prevLineStart = document.getLineStartOffset(prevLineNumber)
        val prevLineEnd = document.getLineEndOffset(prevLineNumber)
        val prevLineText = document.getText(TextRange(prevLineStart, prevLineEnd))

        val baseIndent = prevLineText.takeWhile { it == ' ' || it == '\t' }
        val indentUnit = getIndentUnit(editor)

        val lastToken = findLastSignificantToken(prevLineText)

        if (lastToken != null && lastToken in INDENT_TRIGGERS) {
            return baseIndent + indentUnit
        }

        return baseIndent
    }

    private fun findPrevNonEmptyLine(
        document: com.intellij.openapi.editor.Document,
        fromLine: Int,
    ): Int? {
        for (i in fromLine - 1 downTo 0) {
            val start = document.getLineStartOffset(i)
            val end = document.getLineEndOffset(i)
            val text = document.getText(TextRange(start, end))
            if (text.isNotBlank()) return i
        }
        return null
    }

    internal fun findLastSignificantToken(lineText: String): IElementType? {
        val lexer = RescriptLexer()
        lexer.start(lineText)
        var lastSignificant: IElementType? = null
        while (lexer.tokenType != null) {
            val type = lexer.tokenType!!
            if (type !in SKIP_TOKENS) {
                lastSignificant = type
            }
            lexer.advance()
        }
        return lastSignificant
    }

    private fun getIndentUnit(editor: Editor): String {
        val settings = CodeStyle.getSettings(editor)
        val indentOptions = settings.getIndentOptions(RescriptFileType)
        return if (indentOptions.USE_TAB_CHARACTER) {
            "\t"
        } else {
            " ".repeat(indentOptions.INDENT_SIZE)
        }
    }

    companion object {
        private val INDENT_TRIGGERS =
            setOf(
                T.LBRACE,
                T.LPAREN,
                T.LBRACKET,
                T.ARROW,
            )

        private val SKIP_TOKENS =
            setOf(
                com.intellij.psi.TokenType.WHITE_SPACE,
                T.EOL,
                T.SINGLE_COMMENT,
                T.MULTI_COMMENT,
            )
    }
}
