package com.rescript.plugin.highlight

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors as Defaults
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.rescript.plugin.lang.RescriptLexer
import com.rescript.plugin.lang.RescriptTokenTypes as T

class RescriptSyntaxHighlighter : SyntaxHighlighterBase() {

    companion object {
        // Attribute keys
        val KEYWORD       = createTextAttributesKey("RESCRIPT_KEYWORD", Defaults.KEYWORD)
        val STRING        = createTextAttributesKey("RESCRIPT_STRING", Defaults.STRING)
        val NUMBER        = createTextAttributesKey("RESCRIPT_NUMBER", Defaults.NUMBER)
        val LINE_COMMENT  = createTextAttributesKey("RESCRIPT_LINE_COMMENT", Defaults.LINE_COMMENT)
        val BLOCK_COMMENT = createTextAttributesKey("RESCRIPT_BLOCK_COMMENT", Defaults.BLOCK_COMMENT)
        val OPERATOR      = createTextAttributesKey("RESCRIPT_OPERATOR", Defaults.OPERATION_SIGN)
        val BRACES        = createTextAttributesKey("RESCRIPT_BRACES", Defaults.BRACES)
        val BRACKETS      = createTextAttributesKey("RESCRIPT_BRACKETS", Defaults.BRACKETS)
        val PARENS        = createTextAttributesKey("RESCRIPT_PARENS", Defaults.PARENTHESES)
        val DOT           = createTextAttributesKey("RESCRIPT_DOT", Defaults.DOT)
        val COMMA         = createTextAttributesKey("RESCRIPT_COMMA", Defaults.COMMA)
        val SEMICOLON     = createTextAttributesKey("RESCRIPT_SEMICOLON", Defaults.SEMICOLON)
        val TYPE_ARG      = createTextAttributesKey("RESCRIPT_TYPE_ARGUMENT", Defaults.METADATA)
        val POLY_VARIANT  = createTextAttributesKey("RESCRIPT_POLY_VARIANT", Defaults.CONSTANT)
        val MODULE_NAME   = createTextAttributesKey("RESCRIPT_MODULE_NAME", Defaults.CLASS_NAME)
        val ANNOTATION    = createTextAttributesKey("RESCRIPT_ANNOTATION", Defaults.METADATA)
        val BAD_CHAR      = createTextAttributesKey("RESCRIPT_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER)

        private val ATTR_MAP: Map<IElementType, Array<TextAttributesKey>> = buildMap {
            // Keywords
            for (t in T.KEYWORDS.types) put(t, arrayOf(KEYWORD))

            // Strings & chars
            put(T.STRING_VALUE, arrayOf(STRING))
            put(T.JS_STRING_OPEN, arrayOf(STRING))
            put(T.JS_STRING_CLOSE, arrayOf(STRING))
            put(T.CHAR_VALUE, arrayOf(STRING))

            // Numbers
            put(T.INT_VALUE, arrayOf(NUMBER))
            put(T.FLOAT_VALUE, arrayOf(NUMBER))

            // Comments
            put(T.SINGLE_COMMENT, arrayOf(LINE_COMMENT))
            put(T.MULTI_COMMENT, arrayOf(BLOCK_COMMENT))

            // Operators
            for (t in T.OPERATORS.types) put(t, arrayOf(OPERATOR))

            // Punctuation
            put(T.LBRACE, arrayOf(BRACES));   put(T.RBRACE, arrayOf(BRACES))
            put(T.LBRACKET, arrayOf(BRACKETS)); put(T.RBRACKET, arrayOf(BRACKETS))
            put(T.LPAREN, arrayOf(PARENS));   put(T.RPAREN, arrayOf(PARENS))
            put(T.DOT, arrayOf(DOT))
            put(T.COMMA, arrayOf(COMMA))
            put(T.SEMI, arrayOf(SEMICOLON))

            // Specials
            put(T.TYPE_ARGUMENT, arrayOf(TYPE_ARG))
            put(T.POLY_VARIANT, arrayOf(POLY_VARIANT))
            put(T.UIDENT, arrayOf(MODULE_NAME))
            put(T.ARROBASE, arrayOf(ANNOTATION))

            // Bad character
            put(TokenType.BAD_CHARACTER, arrayOf(BAD_CHAR))
        }
    }

    override fun getHighlightingLexer(): Lexer = RescriptLexer()

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> =
        ATTR_MAP[tokenType] ?: TextAttributesKey.EMPTY_ARRAY
}
