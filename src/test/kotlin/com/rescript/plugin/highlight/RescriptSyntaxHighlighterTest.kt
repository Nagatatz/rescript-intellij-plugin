package com.rescript.plugin.highlight

import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.rescript.plugin.RescriptLanguage
import com.rescript.plugin.lang.RescriptLexer
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import com.rescript.plugin.lang.RescriptTokenTypes as T

class RescriptSyntaxHighlighterTest {
    private val highlighter = RescriptSyntaxHighlighter()

    // ── Keywords ──

    @Test
    fun testAllKeywordsMapToKeywordAttribute() {
        for (token in T.KEYWORDS.types) {
            val attrs = highlighter.getTokenHighlights(token)
            assertTrue(
                attrs.any { it == RescriptSyntaxHighlighter.KEYWORD },
                "Keyword token $token should map to KEYWORD attribute",
            )
        }
    }

    // ── Operators ──

    @Test
    fun testAllOperatorsMapToOperatorAttribute() {
        for (token in T.OPERATORS.types) {
            val attrs = highlighter.getTokenHighlights(token)
            assertTrue(
                attrs.any { it == RescriptSyntaxHighlighter.OPERATOR },
                "Operator token $token should map to OPERATOR attribute",
            )
        }
    }

    // ── Strings ──

    @Test
    fun testStringValueMapsToString() {
        val attrs = highlighter.getTokenHighlights(T.STRING_VALUE)
        assertArrayEquals(arrayOf(RescriptSyntaxHighlighter.STRING), attrs)
    }

    @Test
    fun testJsStringOpenMapsToString() {
        val attrs = highlighter.getTokenHighlights(T.JS_STRING_OPEN)
        assertArrayEquals(arrayOf(RescriptSyntaxHighlighter.STRING), attrs)
    }

    @Test
    fun testJsStringCloseMapsToString() {
        val attrs = highlighter.getTokenHighlights(T.JS_STRING_CLOSE)
        assertArrayEquals(arrayOf(RescriptSyntaxHighlighter.STRING), attrs)
    }

    @Test
    fun testCharValueMapsToString() {
        val attrs = highlighter.getTokenHighlights(T.CHAR_VALUE)
        assertArrayEquals(arrayOf(RescriptSyntaxHighlighter.STRING), attrs)
    }

    // ── Numbers ──

    @Test
    fun testIntValueMapsToNumber() {
        val attrs = highlighter.getTokenHighlights(T.INT_VALUE)
        assertArrayEquals(arrayOf(RescriptSyntaxHighlighter.NUMBER), attrs)
    }

    @Test
    fun testFloatValueMapsToNumber() {
        val attrs = highlighter.getTokenHighlights(T.FLOAT_VALUE)
        assertArrayEquals(arrayOf(RescriptSyntaxHighlighter.NUMBER), attrs)
    }

    // ── Comments ──

    @Test
    fun testSingleCommentMapsToLineComment() {
        val attrs = highlighter.getTokenHighlights(T.SINGLE_COMMENT)
        assertArrayEquals(arrayOf(RescriptSyntaxHighlighter.LINE_COMMENT), attrs)
    }

    @Test
    fun testMultiCommentMapsToBlockComment() {
        val attrs = highlighter.getTokenHighlights(T.MULTI_COMMENT)
        assertArrayEquals(arrayOf(RescriptSyntaxHighlighter.BLOCK_COMMENT), attrs)
    }

    // ── Braces / Brackets / Parens ──

    @Test
    fun testBracesMappedCorrectly() {
        assertArrayEquals(arrayOf(RescriptSyntaxHighlighter.BRACES), highlighter.getTokenHighlights(T.LBRACE))
        assertArrayEquals(arrayOf(RescriptSyntaxHighlighter.BRACES), highlighter.getTokenHighlights(T.RBRACE))
    }

    @Test
    fun testBracketsMappedCorrectly() {
        assertArrayEquals(arrayOf(RescriptSyntaxHighlighter.BRACKETS), highlighter.getTokenHighlights(T.LBRACKET))
        assertArrayEquals(arrayOf(RescriptSyntaxHighlighter.BRACKETS), highlighter.getTokenHighlights(T.RBRACKET))
    }

    @Test
    fun testParensMappedCorrectly() {
        assertArrayEquals(arrayOf(RescriptSyntaxHighlighter.PARENS), highlighter.getTokenHighlights(T.LPAREN))
        assertArrayEquals(arrayOf(RescriptSyntaxHighlighter.PARENS), highlighter.getTokenHighlights(T.RPAREN))
    }

    // ── Punctuation ──

    @Test
    fun testDotMapping() {
        assertArrayEquals(arrayOf(RescriptSyntaxHighlighter.DOT), highlighter.getTokenHighlights(T.DOT))
    }

    @Test
    fun testCommaMapping() {
        assertArrayEquals(arrayOf(RescriptSyntaxHighlighter.COMMA), highlighter.getTokenHighlights(T.COMMA))
    }

    @Test
    fun testSemicolonMapping() {
        assertArrayEquals(arrayOf(RescriptSyntaxHighlighter.SEMICOLON), highlighter.getTokenHighlights(T.SEMI))
    }

    // ── Special tokens ──

    @Test
    fun testTypeArgumentMapping() {
        assertArrayEquals(arrayOf(RescriptSyntaxHighlighter.TYPE_ARG), highlighter.getTokenHighlights(T.TYPE_ARGUMENT))
    }

    @Test
    fun testPolyVariantMapping() {
        assertArrayEquals(
            arrayOf(RescriptSyntaxHighlighter.POLY_VARIANT),
            highlighter.getTokenHighlights(T.POLY_VARIANT),
        )
    }

    @Test
    fun testUidentMapsToModuleName() {
        assertArrayEquals(
            arrayOf(RescriptSyntaxHighlighter.MODULE_NAME),
            highlighter.getTokenHighlights(T.UIDENT),
        )
    }

    @Test
    fun testAnnotationMapping() {
        assertArrayEquals(
            arrayOf(RescriptSyntaxHighlighter.ANNOTATION),
            highlighter.getTokenHighlights(T.ARROBASE),
        )
        assertArrayEquals(
            arrayOf(RescriptSyntaxHighlighter.ANNOTATION),
            highlighter.getTokenHighlights(T.ANNOTATION_NAME),
        )
    }

    // ── JSX ──

    @Test
    fun testJsxTagNameMapping() {
        assertArrayEquals(
            arrayOf(RescriptSyntaxHighlighter.MARKUP_TAG),
            highlighter.getTokenHighlights(T.JSX_TAG_NAME),
        )
    }

    @Test
    fun testJsxComponentNameMapping() {
        assertArrayEquals(
            arrayOf(RescriptSyntaxHighlighter.MODULE_NAME),
            highlighter.getTokenHighlights(T.JSX_COMPONENT_NAME),
        )
    }

    @Test
    fun testTagBracketsMappedToMarkupTagBracket() {
        val expected = arrayOf(RescriptSyntaxHighlighter.MARKUP_TAG_BRACKET)
        assertArrayEquals(expected, highlighter.getTokenHighlights(T.TAG_LT))
        assertArrayEquals(expected, highlighter.getTokenHighlights(T.TAG_GT))
        assertArrayEquals(expected, highlighter.getTokenHighlights(T.TAG_LT_SLASH))
        assertArrayEquals(expected, highlighter.getTokenHighlights(T.TAG_AUTO_CLOSE))
    }

    // ── BAD_CHARACTER ──

    @Test
    fun testBadCharacterMapping() {
        val attrs = highlighter.getTokenHighlights(TokenType.BAD_CHARACTER)
        assertArrayEquals(arrayOf(RescriptSyntaxHighlighter.BAD_CHAR), attrs)
    }

    // ── Unknown token ──

    @Test
    fun testUnmappedTokenReturnsEmptyArray() {
        val unknownType = IElementType("UNKNOWN_TEST_TOKEN", RescriptLanguage)
        val attrs = highlighter.getTokenHighlights(unknownType)
        assertTrue(attrs.isEmpty())
    }

    // ── Lexer ──

    @Test
    fun testGetHighlightingLexerReturnsRescriptLexer() {
        assertTrue(highlighter.highlightingLexer is RescriptLexer)
    }
}
