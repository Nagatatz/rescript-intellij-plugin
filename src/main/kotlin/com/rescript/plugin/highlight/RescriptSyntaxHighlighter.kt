package com.rescript.plugin.highlight

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.rescript.plugin.lang.RescriptLexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors as Defaults
import com.rescript.plugin.lang.RescriptTokenTypes as T

class RescriptSyntaxHighlighter : SyntaxHighlighterBase() {
    companion object {
        // Attribute keys
        val KEYWORD = createTextAttributesKey("RESCRIPT_KEYWORD", Defaults.KEYWORD)
        val STRING = createTextAttributesKey("RESCRIPT_STRING", Defaults.STRING)
        val NUMBER = createTextAttributesKey("RESCRIPT_NUMBER", Defaults.NUMBER)
        val LINE_COMMENT = createTextAttributesKey("RESCRIPT_LINE_COMMENT", Defaults.LINE_COMMENT)
        val BLOCK_COMMENT = createTextAttributesKey("RESCRIPT_BLOCK_COMMENT", Defaults.BLOCK_COMMENT)
        val OPERATOR = createTextAttributesKey("RESCRIPT_OPERATOR", Defaults.OPERATION_SIGN)
        val BRACES = createTextAttributesKey("RESCRIPT_BRACES", Defaults.BRACES)
        val BRACKETS = createTextAttributesKey("RESCRIPT_BRACKETS", Defaults.BRACKETS)
        val PARENS = createTextAttributesKey("RESCRIPT_PARENS", Defaults.PARENTHESES)
        val DOT = createTextAttributesKey("RESCRIPT_DOT", Defaults.DOT)
        val COMMA = createTextAttributesKey("RESCRIPT_COMMA", Defaults.COMMA)
        val SEMICOLON = createTextAttributesKey("RESCRIPT_SEMICOLON", Defaults.SEMICOLON)
        val TYPE_ARG = createTextAttributesKey("RESCRIPT_TYPE_ARGUMENT", Defaults.METADATA)
        val POLY_VARIANT = createTextAttributesKey("RESCRIPT_POLY_VARIANT", Defaults.CONSTANT)
        val MODULE_NAME = createTextAttributesKey("RESCRIPT_MODULE_NAME", Defaults.CLASS_NAME)
        val ANNOTATION = createTextAttributesKey("RESCRIPT_ANNOTATION", Defaults.METADATA)
        val PATTERN_PIPE = createTextAttributesKey("RESCRIPT_PATTERN_PIPE", Defaults.KEYWORD)
        val WILDCARD = createTextAttributesKey("RESCRIPT_WILDCARD", Defaults.KEYWORD)
        val MARKUP_TAG = createTextAttributesKey("RESCRIPT_MARKUP_TAG", Defaults.MARKUP_TAG)
        val MARKUP_TAG_BRACKET = createTextAttributesKey("RESCRIPT_MARKUP_TAG_BRACKET", Defaults.MARKUP_TAG)
        val BAD_CHAR = createTextAttributesKey("RESCRIPT_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER)

        // ── Semantic token attribute keys (LSP) ──
        val SEMANTIC_VARIABLE = createTextAttributesKey("RESCRIPT_SEMANTIC_VARIABLE", Defaults.LOCAL_VARIABLE)
        val SEMANTIC_TYPE = createTextAttributesKey("RESCRIPT_SEMANTIC_TYPE", Defaults.CLASS_NAME)
        val SEMANTIC_NAMESPACE = createTextAttributesKey("RESCRIPT_SEMANTIC_NAMESPACE", MODULE_NAME)
        val SEMANTIC_ENUM_MEMBER = createTextAttributesKey("RESCRIPT_SEMANTIC_ENUM_MEMBER", POLY_VARIANT)
        val SEMANTIC_PROPERTY = createTextAttributesKey("RESCRIPT_SEMANTIC_PROPERTY", Defaults.INSTANCE_FIELD)
        val SEMANTIC_INTERFACE = createTextAttributesKey("RESCRIPT_SEMANTIC_INTERFACE", MARKUP_TAG)
        val SEMANTIC_OPERATOR = createTextAttributesKey("RESCRIPT_SEMANTIC_OPERATOR", OPERATOR)
        val SEMANTIC_MODIFIER = createTextAttributesKey("RESCRIPT_SEMANTIC_MODIFIER", MARKUP_TAG_BRACKET)

        private val ATTR_MAP: Map<IElementType, Array<TextAttributesKey>> =
            buildMap {
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

                // Pattern-related
                put(T.PIPE, arrayOf(PATTERN_PIPE))
                put(T.UNDERSCORE, arrayOf(WILDCARD))
                put(T.DOTDOTDOT, arrayOf(OPERATOR))
                put(T.SHORTCUT, arrayOf(OPERATOR))

                // Punctuation
                put(T.LBRACE, arrayOf(BRACES))
                put(T.RBRACE, arrayOf(BRACES))
                put(T.LBRACKET, arrayOf(BRACKETS))
                put(T.RBRACKET, arrayOf(BRACKETS))
                put(T.LPAREN, arrayOf(PARENS))
                put(T.RPAREN, arrayOf(PARENS))
                put(T.DOT, arrayOf(DOT))
                put(T.COMMA, arrayOf(COMMA))
                put(T.SEMI, arrayOf(SEMICOLON))

                // Specials
                put(T.TYPE_ARGUMENT, arrayOf(TYPE_ARG))
                put(T.POLY_VARIANT, arrayOf(POLY_VARIANT))
                put(T.UIDENT, arrayOf(MODULE_NAME))
                put(T.ARROBASE, arrayOf(ANNOTATION))
                put(T.ANNOTATION_NAME, arrayOf(ANNOTATION))
                put(T.TILDE, arrayOf(OPERATOR))
                put(T.QUESTION_MARK, arrayOf(OPERATOR))

                // JSX
                put(T.JSX_TAG_NAME, arrayOf(MARKUP_TAG))
                put(T.JSX_COMPONENT_NAME, arrayOf(MODULE_NAME))
                put(T.TAG_LT, arrayOf(MARKUP_TAG_BRACKET))
                put(T.TAG_GT, arrayOf(MARKUP_TAG_BRACKET))
                put(T.TAG_LT_SLASH, arrayOf(MARKUP_TAG_BRACKET))
                put(T.TAG_AUTO_CLOSE, arrayOf(MARKUP_TAG_BRACKET))

                // Bad character
                put(TokenType.BAD_CHARACTER, arrayOf(BAD_CHAR))
            }
    }

    override fun getHighlightingLexer(): Lexer = RescriptLexer()

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> =
        ATTR_MAP[tokenType] ?: TextAttributesKey.EMPTY_ARRAY
}
