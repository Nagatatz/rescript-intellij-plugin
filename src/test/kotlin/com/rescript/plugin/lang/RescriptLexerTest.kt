package com.rescript.plugin.lang

import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptLexerTest {
    private fun tokenize(input: String): List<Pair<IElementType, String>> {
        val lexer = RescriptLexer()
        lexer.start(input)
        val tokens = mutableListOf<Pair<IElementType, String>>()
        while (lexer.tokenType != null) {
            tokens.add(lexer.tokenType!! to lexer.tokenText)
            lexer.advance()
        }
        return tokens
    }

    private fun tokenTypes(input: String): List<IElementType> = tokenize(input).map { it.first }

    private fun tokenizeNoWs(input: String): List<Pair<IElementType, String>> =
        tokenize(input).filter { it.first != TokenType.WHITE_SPACE && it.first != RescriptTokenTypes.EOL }

    private fun tokenTypesNoWs(input: String): List<IElementType> = tokenizeNoWs(input).map { it.first }

    // ════════════════════════════════════════════════════════════════
    // JSX (existing tests)
    // ════════════════════════════════════════════════════════════════

    @Test
    fun `JSX opening tag - div`() {
        val tokens = tokenize("<div>")
        assertEquals(
            listOf(
                RescriptTokenTypes.TAG_LT to "<",
                RescriptTokenTypes.JSX_TAG_NAME to "div",
                RescriptTokenTypes.TAG_GT to ">",
            ),
            tokens,
        )
    }

    @Test
    fun `JSX closing tag - div`() {
        val tokens = tokenize("</div>")
        assertEquals(
            listOf(
                RescriptTokenTypes.TAG_LT_SLASH to "</",
                RescriptTokenTypes.JSX_TAG_NAME to "div",
                RescriptTokenTypes.TAG_GT to ">",
            ),
            tokens,
        )
    }

    @Test
    fun `JSX component tag - uppercase`() {
        val tokens = tokenize("<Component>")
        assertEquals(
            listOf(
                RescriptTokenTypes.TAG_LT to "<",
                RescriptTokenTypes.JSX_COMPONENT_NAME to "Component",
                RescriptTokenTypes.TAG_GT to ">",
            ),
            tokens,
        )
    }

    @Test
    fun `JSX module path tag`() {
        val tokens = tokenize("<Module.Sub.Comp>")
        assertEquals(
            listOf(
                RescriptTokenTypes.TAG_LT to "<",
                RescriptTokenTypes.JSX_COMPONENT_NAME to "Module",
                RescriptTokenTypes.DOT to ".",
                RescriptTokenTypes.JSX_COMPONENT_NAME to "Sub",
                RescriptTokenTypes.DOT to ".",
                RescriptTokenTypes.JSX_COMPONENT_NAME to "Comp",
                RescriptTokenTypes.TAG_GT to ">",
            ),
            tokens,
        )
    }

    @Test
    fun `JSX self-closing tag`() {
        val tokens = tokenize("<br />")
        val types = tokens.map { it.first }
        assertEquals(RescriptTokenTypes.TAG_LT, types[0])
        assertEquals(RescriptTokenTypes.JSX_TAG_NAME, types[1])
        assertEquals("br", tokens[1].second)
        assertEquals(RescriptTokenTypes.TAG_AUTO_CLOSE, types.last())
    }

    @Test
    fun `JSX closing module tag`() {
        val tokens = tokenize("</Module.Comp>")
        assertEquals(
            listOf(
                RescriptTokenTypes.TAG_LT_SLASH to "</",
                RescriptTokenTypes.JSX_COMPONENT_NAME to "Module",
                RescriptTokenTypes.DOT to ".",
                RescriptTokenTypes.JSX_COMPONENT_NAME to "Comp",
                RescriptTokenTypes.TAG_GT to ">",
            ),
            tokens,
        )
    }

    @Test
    fun `JSX tag with props`() {
        val tokens = tokenize("<Comp name>")
        val types = tokens.map { it.first }
        assertEquals(RescriptTokenTypes.TAG_LT, types[0])
        assertEquals(RescriptTokenTypes.JSX_COMPONENT_NAME, types[1])
        assertEquals("Comp", tokens[1].second)
        // After tag name, space triggers exit from IN_JSX_TAG_NAME
        // "name" should be LIDENT (not JSX_TAG_NAME)
        assertEquals(RescriptTokenTypes.LIDENT, types[3]) // index 3 because whitespace is 2
    }

    @Test
    fun `JSX tag with attributes - closing GT becomes TAG_GT`() {
        // <div className="test"> — after attributes, > should be TAG_GT
        val tokens = tokenizeNoWs("""<div className="test">""")
        assertEquals(RescriptTokenTypes.TAG_LT, tokens[0].first)
        assertEquals(RescriptTokenTypes.JSX_TAG_NAME, tokens[1].first)
        assertEquals(RescriptTokenTypes.LIDENT, tokens[2].first)
        assertEquals("className", tokens[2].second)
        assertEquals(RescriptTokenTypes.EQ, tokens[3].first)
        assertEquals(RescriptTokenTypes.STRING_VALUE, tokens[4].first)
        assertEquals(RescriptTokenTypes.TAG_GT, tokens[5].first)
    }

    @Test
    fun `JSX tag with brace expression - GT inside braces stays GT`() {
        // <div onClick={x > 0}> — > inside {} should be GT, closing > should be TAG_GT
        val tokens = tokenizeNoWs("<div onClick={x > 0}>")
        val types = tokens.map { it.first }
        assertEquals(RescriptTokenTypes.TAG_LT, types[0])
        assertEquals(RescriptTokenTypes.JSX_TAG_NAME, types[1])
        // Find the > inside braces (after x and before 0)
        val braceStart = types.indexOf(RescriptTokenTypes.LBRACE)
        val braceEnd = types.indexOf(RescriptTokenTypes.RBRACE)
        val gtInsideBrace = tokens.subList(braceStart, braceEnd).find { it.second == ">" }
        assertEquals(RescriptTokenTypes.GT, gtInsideBrace?.first)
        // The last > should be TAG_GT
        assertEquals(RescriptTokenTypes.TAG_GT, types.last())
    }

    @Test
    fun `JSX component with multiple attributes`() {
        val tokens = tokenizeNoWs("<Component name age>")
        assertEquals(RescriptTokenTypes.TAG_LT, tokens[0].first)
        assertEquals(RescriptTokenTypes.JSX_COMPONENT_NAME, tokens[1].first)
        assertEquals(RescriptTokenTypes.LIDENT, tokens[2].first)
        assertEquals(RescriptTokenTypes.LIDENT, tokens[3].first)
        assertEquals(RescriptTokenTypes.TAG_GT, tokens[4].first)
    }

    @Test
    fun `plain less-than is not JSX`() {
        val tokens = tokenize("1 < 2")
        val types = tokens.map { it.first }
        // "<" followed by space, not a letter - should stay as LT, no JSX
        assertEquals(RescriptTokenTypes.LT, types[2])
    }

    @Test
    fun `let binding not affected`() {
        val types = tokenTypes("let x = 5")
        assertEquals(RescriptTokenTypes.LET, types[0])
    }

    // ════════════════════════════════════════════════════════════════
    // Keywords
    // ════════════════════════════════════════════════════════════════

    @Test
    fun `keyword - let`() {
        assertEquals(listOf(RescriptTokenTypes.LET), tokenTypesNoWs("let"))
    }

    @Test
    fun `keyword - type`() {
        assertEquals(listOf(RescriptTokenTypes.TYPE), tokenTypesNoWs("type"))
    }

    @Test
    fun `keyword - module`() {
        assertEquals(listOf(RescriptTokenTypes.MODULE), tokenTypesNoWs("module"))
    }

    @Test
    fun `keyword - external`() {
        assertEquals(listOf(RescriptTokenTypes.EXTERNAL), tokenTypesNoWs("external"))
    }

    @Test
    fun `keyword - open`() {
        assertEquals(listOf(RescriptTokenTypes.OPEN), tokenTypesNoWs("open"))
    }

    @Test
    fun `keyword - include`() {
        assertEquals(listOf(RescriptTokenTypes.INCLUDE), tokenTypesNoWs("include"))
    }

    @Test
    fun `keyword - exception`() {
        assertEquals(listOf(RescriptTokenTypes.EXCEPTION), tokenTypesNoWs("exception"))
    }

    @Test
    fun `keyword - switch`() {
        assertEquals(listOf(RescriptTokenTypes.SWITCH), tokenTypesNoWs("switch"))
    }

    @Test
    fun `keyword - if and else`() {
        val types = tokenTypesNoWs("if else")
        assertEquals(listOf(RescriptTokenTypes.IF, RescriptTokenTypes.ELSE), types)
    }

    @Test
    fun `keyword - for`() {
        assertEquals(listOf(RescriptTokenTypes.FOR), tokenTypesNoWs("for"))
    }

    @Test
    fun `keyword - while`() {
        assertEquals(listOf(RescriptTokenTypes.WHILE), tokenTypesNoWs("while"))
    }

    @Test
    fun `keyword - try and catch`() {
        val types = tokenTypesNoWs("try catch")
        assertEquals(listOf(RescriptTokenTypes.TRY, RescriptTokenTypes.CATCH), types)
    }

    @Test
    fun `keyword - async and await`() {
        val types = tokenTypesNoWs("async await")
        assertEquals(listOf(RescriptTokenTypes.ASYNC, RescriptTokenTypes.AWAIT), types)
    }

    @Test
    fun `keyword - rec`() {
        assertEquals(listOf(RescriptTokenTypes.REC), tokenTypesNoWs("rec"))
    }

    @Test
    fun `keyword - mutable`() {
        assertEquals(listOf(RescriptTokenTypes.MUTABLE), tokenTypesNoWs("mutable"))
    }

    @Test
    fun `keyword - lazy`() {
        assertEquals(listOf(RescriptTokenTypes.LAZY), tokenTypesNoWs("lazy"))
    }

    @Test
    fun `keyword - ffi`() {
        assertEquals(listOf(RescriptTokenTypes.FFI), tokenTypesNoWs("ffi"))
    }

    @Test
    fun `keyword - dict`() {
        assertEquals(listOf(RescriptTokenTypes.DICT), tokenTypesNoWs("dict"))
    }

    @Test
    fun `keyword - dict with braces`() {
        val types = tokenTypesNoWs("""dict{"key": "value"}""")
        assertEquals(RescriptTokenTypes.DICT, types[0])
        assertEquals(RescriptTokenTypes.LBRACE, types[1])
        assertEquals(RescriptTokenTypes.STRING_VALUE, types[2])
        assertEquals(RescriptTokenTypes.COLON, types[3])
        assertEquals(RescriptTokenTypes.STRING_VALUE, types[4])
        assertEquals(RescriptTokenTypes.RBRACE, types[5])
    }

    // ════════════════════════════════════════════════════════════════
    // Keyword operators
    // ════════════════════════════════════════════════════════════════

    @Test
    fun `keyword operator - mod`() {
        assertEquals(listOf(RescriptTokenTypes.MOD), tokenTypesNoWs("mod"))
    }

    @Test
    fun `keyword operator - land`() {
        assertEquals(listOf(RescriptTokenTypes.LAND), tokenTypesNoWs("land"))
    }

    @Test
    fun `keyword operator - lor`() {
        assertEquals(listOf(RescriptTokenTypes.LOR), tokenTypesNoWs("lor"))
    }

    @Test
    fun `keyword operator - lxor`() {
        assertEquals(listOf(RescriptTokenTypes.LXOR), tokenTypesNoWs("lxor"))
    }

    @Test
    fun `keyword operator - lsl and lsr and asr`() {
        val types = tokenTypesNoWs("lsl lsr asr")
        assertEquals(listOf(RescriptTokenTypes.LSL, RescriptTokenTypes.LSR, RescriptTokenTypes.ASR), types)
    }

    // ════════════════════════════════════════════════════════════════
    // Built-ins
    // ════════════════════════════════════════════════════════════════

    @Test
    fun `builtin - unit`() {
        assertEquals(listOf(RescriptTokenTypes.UNIT), tokenTypesNoWs("unit"))
    }

    @Test
    fun `builtin - ref`() {
        assertEquals(listOf(RescriptTokenTypes.REF), tokenTypesNoWs("ref"))
    }

    @Test
    fun `builtin - raise`() {
        assertEquals(listOf(RescriptTokenTypes.RAISE), tokenTypesNoWs("raise"))
    }

    @Test
    fun `builtin - option`() {
        assertEquals(listOf(RescriptTokenTypes.OPTION), tokenTypesNoWs("option"))
    }

    @Test
    fun `builtin - Some`() {
        val tokens = tokenizeNoWs("Some(x)")
        assertEquals(RescriptTokenTypes.SOME, tokens[0].first)
        assertEquals("Some", tokens[0].second)
    }

    @Test
    fun `builtin - None`() {
        val tokens = tokenizeNoWs("None")
        assertEquals(1, tokens.size)
        assertEquals(RescriptTokenTypes.NONE, tokens[0].first)
    }

    // ════════════════════════════════════════════════════════════════
    // Integer literals
    // ════════════════════════════════════════════════════════════════

    @Test
    fun `integer - decimal`() {
        val tokens = tokenizeNoWs("42")
        assertEquals(RescriptTokenTypes.INT_VALUE, tokens[0].first)
        assertEquals("42", tokens[0].second)
    }

    @Test
    fun `integer - hex`() {
        val tokens = tokenizeNoWs("0xFF")
        assertEquals(RescriptTokenTypes.INT_VALUE, tokens[0].first)
        assertEquals("0xFF", tokens[0].second)
    }

    @Test
    fun `integer - octal`() {
        val tokens = tokenizeNoWs("0o77")
        assertEquals(RescriptTokenTypes.INT_VALUE, tokens[0].first)
        assertEquals("0o77", tokens[0].second)
    }

    @Test
    fun `integer - binary`() {
        val tokens = tokenizeNoWs("0b1010")
        assertEquals(RescriptTokenTypes.INT_VALUE, tokens[0].first)
        assertEquals("0b1010", tokens[0].second)
    }

    @Test
    fun `integer - underscore separator`() {
        val tokens = tokenizeNoWs("1_000_000")
        assertEquals(RescriptTokenTypes.INT_VALUE, tokens[0].first)
        assertEquals("1_000_000", tokens[0].second)
    }

    @Test
    fun `integer - with literal modifier`() {
        val tokens = tokenizeNoWs("42n")
        assertEquals(RescriptTokenTypes.INT_VALUE, tokens[0].first)
        assertEquals("42n", tokens[0].second)
    }

    // ════════════════════════════════════════════════════════════════
    // Float literals
    // ════════════════════════════════════════════════════════════════

    @Test
    fun `float - simple`() {
        val tokens = tokenizeNoWs("3.14")
        assertEquals(RescriptTokenTypes.FLOAT_VALUE, tokens[0].first)
        assertEquals("3.14", tokens[0].second)
    }

    @Test
    fun `float - exponent`() {
        val tokens = tokenizeNoWs("1e10")
        assertEquals(RescriptTokenTypes.FLOAT_VALUE, tokens[0].first)
        assertEquals("1e10", tokens[0].second)
    }

    @Test
    fun `float - negative exponent`() {
        val tokens = tokenizeNoWs("2.5e-3")
        assertEquals(RescriptTokenTypes.FLOAT_VALUE, tokens[0].first)
        assertEquals("2.5e-3", tokens[0].second)
    }

    @Test
    fun `float - hex float`() {
        val tokens = tokenizeNoWs("0xAp3")
        assertEquals(RescriptTokenTypes.FLOAT_VALUE, tokens[0].first)
        assertEquals("0xAp3", tokens[0].second)
    }

    // ════════════════════════════════════════════════════════════════
    // Character literals
    // ════════════════════════════════════════════════════════════════

    @Test
    fun `char - simple`() {
        val tokens = tokenizeNoWs("'a'")
        assertEquals(RescriptTokenTypes.CHAR_VALUE, tokens[0].first)
        assertEquals("'a'", tokens[0].second)
    }

    @Test
    fun `char - escape newline`() {
        val tokens = tokenizeNoWs("'\\n'")
        assertEquals(RescriptTokenTypes.CHAR_VALUE, tokens[0].first)
        assertEquals("'\\n'", tokens[0].second)
    }

    @Test
    fun `char - escape hex`() {
        val tokens = tokenizeNoWs("'\\x41'")
        assertEquals(RescriptTokenTypes.CHAR_VALUE, tokens[0].first)
        assertEquals("'\\x41'", tokens[0].second)
    }

    // ════════════════════════════════════════════════════════════════
    // String literals
    // ════════════════════════════════════════════════════════════════

    @Test
    fun `string - simple`() {
        val tokens = tokenizeNoWs("\"hello\"")
        assertEquals(1, tokens.size)
        assertEquals(RescriptTokenTypes.STRING_VALUE, tokens[0].first)
        assertEquals("\"hello\"", tokens[0].second)
    }

    @Test
    fun `string - with escaped quotes`() {
        val input = "\"he\\\"llo\""
        val tokens = tokenizeNoWs(input)
        assertEquals(1, tokens.size)
        assertEquals(RescriptTokenTypes.STRING_VALUE, tokens[0].first)
    }

    @Test
    fun `string - empty`() {
        val tokens = tokenizeNoWs("\"\"")
        assertEquals(1, tokens.size)
        assertEquals(RescriptTokenTypes.STRING_VALUE, tokens[0].first)
        assertEquals("\"\"", tokens[0].second)
    }

    @Test
    fun `string - with escape sequences`() {
        val tokens = tokenizeNoWs("\"\\n\\t\\r\\\\\"")
        assertEquals(1, tokens.size)
        assertEquals(RescriptTokenTypes.STRING_VALUE, tokens[0].first)
    }

    // ════════════════════════════════════════════════════════════════
    // Boolean literals
    // ════════════════════════════════════════════════════════════════

    @Test
    fun `boolean - true`() {
        val tokens = tokenizeNoWs("true")
        assertEquals(RescriptTokenTypes.BOOL_VALUE, tokens[0].first)
    }

    @Test
    fun `boolean - false`() {
        val tokens = tokenizeNoWs("false")
        assertEquals(RescriptTokenTypes.BOOL_VALUE, tokens[0].first)
    }

    // ════════════════════════════════════════════════════════════════
    // Comments
    // ════════════════════════════════════════════════════════════════

    @Test
    fun `comment - single line`() {
        val tokens = tokenizeNoWs("// this is a comment")
        assertEquals(1, tokens.size)
        assertEquals(RescriptTokenTypes.SINGLE_COMMENT, tokens[0].first)
    }

    @Test
    fun `comment - block`() {
        val tokens = tokenizeNoWs("/* block comment */")
        assertEquals(1, tokens.size)
        assertEquals(RescriptTokenTypes.MULTI_COMMENT, tokens[0].first)
    }

    @Test
    fun `comment - nested block`() {
        val tokens = tokenizeNoWs("/* outer /* inner */ still comment */")
        assertEquals(1, tokens.size)
        assertEquals(RescriptTokenTypes.MULTI_COMMENT, tokens[0].first)
    }

    @Test
    fun `comment - doc comment`() {
        val tokens = tokenizeNoWs("/** doc comment */")
        assertEquals(1, tokens.size)
        assertEquals(RescriptTokenTypes.MULTI_COMMENT, tokens[0].first)
    }

    @Test
    fun `comment - multiline block`() {
        val input = "/* line1\n * line2\n */"
        val tokens = tokenizeNoWs(input)
        assertEquals(1, tokens.size)
        assertEquals(RescriptTokenTypes.MULTI_COMMENT, tokens[0].first)
    }

    @Test
    fun `comment - block with string inside`() {
        val tokens = tokenizeNoWs("/* \"not a string\" */")
        assertEquals(1, tokens.size)
        assertEquals(RescriptTokenTypes.MULTI_COMMENT, tokens[0].first)
    }

    // ════════════════════════════════════════════════════════════════
    // Template literals
    // ════════════════════════════════════════════════════════════════

    @Test
    fun `template - simple`() {
        val tokens = tokenizeNoWs("`hello`")
        assertEquals(RescriptTokenTypes.JS_STRING_OPEN, tokens[0].first)
        assertEquals(RescriptTokenTypes.STRING_VALUE, tokens[1].first)
        assertEquals("hello", tokens[1].second)
        assertEquals(RescriptTokenTypes.JS_STRING_CLOSE, tokens[2].first)
    }

    @Test
    fun `template - with interpolation`() {
        val input = "`hello ${'$'}{name}`"
        val tokens = tokenizeNoWs(input)
        assertEquals(RescriptTokenTypes.JS_STRING_OPEN, tokens[0].first)
        assertTrue(tokens.any { it.first == RescriptTokenTypes.DOLLAR }, "Should contain DOLLAR")
        assertTrue(tokens.any { it.first == RescriptTokenTypes.LBRACE }, "Should contain LBRACE")
        assertTrue(tokens.any { it.first == RescriptTokenTypes.RBRACE }, "Should contain RBRACE")
        assertEquals(RescriptTokenTypes.JS_STRING_CLOSE, tokens.last().first)
    }

    @Test
    fun `template - multiple interpolations`() {
        val input = "`${'$'}{a} and ${'$'}{b}`"
        val tokens = tokenizeNoWs(input)
        assertEquals(RescriptTokenTypes.JS_STRING_OPEN, tokens[0].first)
        val dollarCount = tokens.count { it.first == RescriptTokenTypes.DOLLAR }
        assertEquals(2, dollarCount)
        assertEquals(RescriptTokenTypes.JS_STRING_CLOSE, tokens.last().first)
    }

    @Test
    fun `template - empty`() {
        val tokens = tokenizeNoWs("``")
        assertEquals(RescriptTokenTypes.JS_STRING_OPEN, tokens[0].first)
        assertEquals(RescriptTokenTypes.JS_STRING_CLOSE, tokens[1].first)
    }

    // ════════════════════════════════════════════════════════════════
    // Operators
    // ════════════════════════════════════════════════════════════════

    @Test
    fun `operator - arithmetic`() {
        val types = tokenTypesNoWs("+ - * / %")
        assertEquals(
            listOf(
                RescriptTokenTypes.PLUS,
                RescriptTokenTypes.MINUS,
                RescriptTokenTypes.STAR,
                RescriptTokenTypes.SLASH,
                RescriptTokenTypes.PERCENT,
            ),
            types,
        )
    }

    @Test
    fun `operator - float arithmetic`() {
        val types = tokenTypesNoWs("+. -. *. /.")
        assertEquals(
            listOf(
                RescriptTokenTypes.PLUSDOT,
                RescriptTokenTypes.MINUSDOT,
                RescriptTokenTypes.STARDOT,
                RescriptTokenTypes.SLASHDOT,
            ),
            types,
        )
    }

    @Test
    fun `operator - comparison`() {
        val types = tokenTypesNoWs("== === != !==")
        assertEquals(
            listOf(
                RescriptTokenTypes.EQEQ,
                RescriptTokenTypes.EQEQEQ,
                RescriptTokenTypes.NOT_EQ,
                RescriptTokenTypes.NOT_EQEQ,
            ),
            types,
        )
    }

    @Test
    fun `operator - less than and greater than`() {
        // Standalone < and > (not JSX context)
        val tokens = tokenizeNoWs("1 < 2")
        assertEquals(RescriptTokenTypes.LT, tokens[1].first)

        val tokens2 = tokenizeNoWs("1 > 2")
        assertEquals(RescriptTokenTypes.GT, tokens2[1].first)
    }

    @Test
    fun `operator - less than or equal`() {
        val tokens = tokenizeNoWs("x <= y")
        assertEquals(RescriptTokenTypes.LT_OR_EQUAL, tokens[1].first)
    }

    @Test
    fun `operator - logical`() {
        val types = tokenTypesNoWs("a && b || c")
        assertEquals(RescriptTokenTypes.L_AND, types[1])
        assertEquals(RescriptTokenTypes.L_OR, types[3])
    }

    @Test
    fun `operator - pipe forward`() {
        val tokens = tokenizeNoWs("x |> f")
        assertEquals(RescriptTokenTypes.PIPE_FORWARD, tokens[1].first)
    }

    @Test
    fun `operator - fat arrow`() {
        val tokens = tokenizeNoWs("x => y")
        assertEquals(RescriptTokenTypes.ARROW, tokens[1].first)
    }

    @Test
    fun `operator - right arrow`() {
        val tokens = tokenizeNoWs("x -> y")
        assertEquals(RescriptTokenTypes.RIGHT_ARROW, tokens[1].first)
    }

    @Test
    fun `operator - left arrow`() {
        val tokens = tokenizeNoWs("x <- y")
        assertEquals(RescriptTokenTypes.LEFT_ARROW, tokens[1].first)
    }

    @Test
    fun `operator - string concat`() {
        val tokens = tokenizeNoWs("a ++ b")
        assertEquals(RescriptTokenTypes.STRING_CONCAT, tokens[1].first)
    }

    @Test
    fun `operator - list cons`() {
        val tokens = tokenizeNoWs("x :: xs")
        assertEquals(RescriptTokenTypes.SHORTCUT, tokens[1].first)
    }

    @Test
    fun `operator - colon eq and colon gt`() {
        val types = tokenTypesNoWs(":= :>")
        assertEquals(listOf(RescriptTokenTypes.COLON_EQ, RescriptTokenTypes.COLON_GT), types)
    }

    @Test
    fun `operator - assignment`() {
        val tokens = tokenizeNoWs("x = 1")
        assertEquals(RescriptTokenTypes.EQ, tokens[1].first)
    }

    // ════════════════════════════════════════════════════════════════
    // Annotations
    // ════════════════════════════════════════════════════════════════

    @Test
    fun `annotation - simple`() {
        val tokens = tokenizeNoWs("@module")
        assertEquals(RescriptTokenTypes.ARROBASE, tokens[0].first)
        assertEquals(RescriptTokenTypes.ANNOTATION_NAME, tokens[1].first)
        assertEquals("module", tokens[1].second)
    }

    @Test
    fun `annotation - dotted name`() {
        val tokens = tokenizeNoWs("@react.component")
        assertEquals(RescriptTokenTypes.ARROBASE, tokens[0].first)
        assertEquals(RescriptTokenTypes.ANNOTATION_NAME, tokens[1].first)
        assertEquals("react.component", tokens[1].second)
    }

    @Test
    fun `annotation - with arguments`() {
        val tokens = tokenizeNoWs("""@module("fs")""")
        assertEquals(RescriptTokenTypes.ARROBASE, tokens[0].first)
        assertEquals(RescriptTokenTypes.ANNOTATION_NAME, tokens[1].first)
        assertEquals("module", tokens[1].second)
        assertEquals(RescriptTokenTypes.LPAREN, tokens[2].first)
        assertEquals(RescriptTokenTypes.STRING_VALUE, tokens[3].first)
        assertEquals(RescriptTokenTypes.RPAREN, tokens[4].first)
    }

    @Test
    fun `annotation - double at`() {
        val tokens = tokenizeNoWs("@@deriving")
        assertEquals(3, tokens.size)
        assertEquals(RescriptTokenTypes.ARROBASE, tokens[0].first)
        assertEquals(RescriptTokenTypes.ARROBASE, tokens[1].first)
        assertEquals(RescriptTokenTypes.ANNOTATION_NAME, tokens[2].first)
        assertEquals("deriving", tokens[2].second)
    }

    // ════════════════════════════════════════════════════════════════
    // Identifiers and special tokens
    // ════════════════════════════════════════════════════════════════

    @Test
    fun `identifier - lowercase`() {
        val tokens = tokenizeNoWs("myVar")
        assertEquals(RescriptTokenTypes.LIDENT, tokens[0].first)
        assertEquals("myVar", tokens[0].second)
    }

    @Test
    fun `identifier - lowercase with underscore`() {
        val tokens = tokenizeNoWs("my_var")
        assertEquals(RescriptTokenTypes.LIDENT, tokens[0].first)
        assertEquals("my_var", tokens[0].second)
    }

    @Test
    fun `identifier - uppercase`() {
        val tokens = tokenizeNoWs("MyModule")
        assertEquals(RescriptTokenTypes.UIDENT, tokens[0].first)
        assertEquals("MyModule", tokens[0].second)
    }

    @Test
    fun `identifier - underscore wildcard`() {
        val tokens = tokenizeNoWs("_")
        assertEquals(RescriptTokenTypes.UNDERSCORE, tokens[0].first)
    }

    @Test
    fun `identifier - type argument`() {
        val tokens = tokenizeNoWs("'a")
        assertEquals(RescriptTokenTypes.TYPE_ARGUMENT, tokens[0].first)
        assertEquals("'a", tokens[0].second)
    }

    @Test
    fun `identifier - type argument long name`() {
        val tokens = tokenizeNoWs("'myType")
        assertEquals(RescriptTokenTypes.TYPE_ARGUMENT, tokens[0].first)
        assertEquals("'myType", tokens[0].second)
    }

    @Test
    fun `identifier - poly variant uppercase`() {
        val tokens = tokenizeNoWs("#Success")
        assertEquals(RescriptTokenTypes.POLY_VARIANT, tokens[0].first)
        assertEquals("#Success", tokens[0].second)
    }

    @Test
    fun `identifier - poly variant lowercase`() {
        val tokens = tokenizeNoWs("#error")
        assertEquals(RescriptTokenTypes.POLY_VARIANT, tokens[0].first)
        assertEquals("#error", tokens[0].second)
    }

    // ════════════════════════════════════════════════════════════════
    // Punctuation
    // ════════════════════════════════════════════════════════════════

    @Test
    fun `punctuation - braces brackets parens`() {
        val types = tokenTypesNoWs("{ } [ ] ( )")
        assertEquals(
            listOf(
                RescriptTokenTypes.LBRACE,
                RescriptTokenTypes.RBRACE,
                RescriptTokenTypes.LBRACKET,
                RescriptTokenTypes.RBRACKET,
                RescriptTokenTypes.LPAREN,
                RescriptTokenTypes.RPAREN,
            ),
            types,
        )
    }

    @Test
    fun `punctuation - dot dotdot dotdotdot`() {
        val types = tokenTypesNoWs(". .. ...")
        assertEquals(
            listOf(
                RescriptTokenTypes.DOT,
                RescriptTokenTypes.DOTDOT,
                RescriptTokenTypes.DOTDOTDOT,
            ),
            types,
        )
    }

    @Test
    fun `punctuation - comma colon semicolon pipe`() {
        val types = tokenTypesNoWs(", : ; |")
        assertEquals(
            listOf(
                RescriptTokenTypes.COMMA,
                RescriptTokenTypes.COLON,
                RescriptTokenTypes.SEMI,
                RescriptTokenTypes.PIPE,
            ),
            types,
        )
    }

    @Test
    fun `punctuation - tilde question exclamation`() {
        val types = tokenTypesNoWs("~ ? !")
        assertEquals(
            listOf(
                RescriptTokenTypes.TILDE,
                RescriptTokenTypes.QUESTION_MARK,
                RescriptTokenTypes.EXCLAMATION_MARK,
            ),
            types,
        )
    }

    // ════════════════════════════════════════════════════════════════
    // Lexer state transitions - boundary cases
    // ════════════════════════════════════════════════════════════════

    @Test
    fun `state - AFTER_IDENT prevents JSX after identifier`() {
        // After an identifier, < should be LT not TAG_LT
        val tokens = tokenizeNoWs("option<int>")
        assertEquals(RescriptTokenTypes.OPTION, tokens[0].first)
        assertEquals(RescriptTokenTypes.LT, tokens[1].first)
        assertEquals(RescriptTokenTypes.LIDENT, tokens[2].first)
        assertEquals(RescriptTokenTypes.GT, tokens[3].first)
    }

    @Test
    fun `state - AFTER_IDENT resets on newline`() {
        // After newline, JSX should be recognized again
        val tokens = tokenizeNoWs("someFunc\n<div>")
        assertEquals(RescriptTokenTypes.LIDENT, tokens[0].first)
        assertEquals(RescriptTokenTypes.TAG_LT, tokens[1].first)
        assertEquals(RescriptTokenTypes.JSX_TAG_NAME, tokens[2].first)
        assertEquals(RescriptTokenTypes.TAG_GT, tokens[3].first)
    }

    @Test
    fun `state - AFTER_IDENT for uppercase prevents JSX`() {
        // Module<int> — type params, not JSX
        val tokens = tokenizeNoWs("Module<int>")
        assertEquals(RescriptTokenTypes.UIDENT, tokens[0].first)
        assertEquals(RescriptTokenTypes.LT, tokens[1].first)
        assertEquals(RescriptTokenTypes.LIDENT, tokens[2].first)
    }

    @Test
    fun `state - IN_LOWER_DECLARATION after let`() {
        // let name = ... — name should be LIDENT
        val tokens = tokenizeNoWs("let myFunc = 1")
        assertEquals(RescriptTokenTypes.LET, tokens[0].first)
        assertEquals(RescriptTokenTypes.LIDENT, tokens[1].first)
        assertEquals("myFunc", tokens[1].second)
    }

    @Test
    fun `state - IN_LOWER_DECLARATION after external`() {
        val tokens = tokenizeNoWs("external log : string => unit = \"console.log\"")
        assertEquals(RescriptTokenTypes.EXTERNAL, tokens[0].first)
        assertEquals(RescriptTokenTypes.LIDENT, tokens[1].first)
        assertEquals("log", tokens[1].second)
    }

    @Test
    fun `state - IN_LOWER_DECLARATION rec keyword`() {
        val tokens = tokenizeNoWs("let rec fib = x")
        assertEquals(RescriptTokenTypes.LET, tokens[0].first)
        assertEquals(RescriptTokenTypes.REC, tokens[1].first)
        assertEquals(RescriptTokenTypes.LIDENT, tokens[2].first)
        assertEquals("fib", tokens[2].second)
    }

    @Test
    fun `state - JSX tag name exits on space`() {
        // After JSX tag name, space returns to INITIAL
        val tokens = tokenize("<div className>")
        val types = tokens.map { it.first }
        assertEquals(RescriptTokenTypes.TAG_LT, types[0])
        assertEquals(RescriptTokenTypes.JSX_TAG_NAME, types[1])
        // After whitespace, className should be LIDENT (not JSX_TAG_NAME)
        assertEquals(RescriptTokenTypes.LIDENT, types[3])
    }

    @Test
    fun `state - JSX after closing paren`() {
        // ) transitions to AFTER_IDENT, so < should be LT
        val tokens = tokenizeNoWs("f(x)<y>")
        val ltIdx = tokens.indexOfFirst { it.second == "<" }
        assertEquals(RescriptTokenTypes.LT, tokens[ltIdx].first)
    }

    @Test
    fun `state - JSX after closing bracket`() {
        // ] transitions to AFTER_IDENT, so < should be LT
        val tokens = tokenizeNoWs("a[0]<y>")
        val ltIdx = tokens.indexOfFirst { it.second == "<" }
        assertEquals(RescriptTokenTypes.LT, tokens[ltIdx].first)
    }

    // ════════════════════════════════════════════════════════════════
    // Combined / real-world patterns
    // ════════════════════════════════════════════════════════════════

    @Test
    fun `pattern - let with arrow function`() {
        val types = tokenTypesNoWs("let add = (a, b) => a + b")
        assertEquals(RescriptTokenTypes.LET, types[0])
        assertEquals(RescriptTokenTypes.LIDENT, types[1])
        assertEquals(RescriptTokenTypes.EQ, types[2])
        assertEquals(RescriptTokenTypes.LPAREN, types[3])
        assertEquals(RescriptTokenTypes.ARROW, types[8])
    }

    @Test
    fun `pattern - module access`() {
        val types = tokenTypesNoWs("Belt.Array.map")
        assertEquals(RescriptTokenTypes.UIDENT, types[0])
        assertEquals(RescriptTokenTypes.DOT, types[1])
        assertEquals(RescriptTokenTypes.UIDENT, types[2])
        assertEquals(RescriptTokenTypes.DOT, types[3])
        assertEquals(RescriptTokenTypes.LIDENT, types[4])
    }

    @Test
    fun `pattern - pipe chain`() {
        val types = tokenTypesNoWs("x->f->g")
        assertEquals(RescriptTokenTypes.LIDENT, types[0])
        assertEquals(RescriptTokenTypes.RIGHT_ARROW, types[1])
        assertEquals(RescriptTokenTypes.LIDENT, types[2])
        assertEquals(RescriptTokenTypes.RIGHT_ARROW, types[3])
        assertEquals(RescriptTokenTypes.LIDENT, types[4])
    }

    @Test
    fun `pattern - switch expression`() {
        val types = tokenTypesNoWs("switch x { | Some(v) => v | None => 0 }")
        assertEquals(RescriptTokenTypes.SWITCH, types[0])
        assertEquals(RescriptTokenTypes.LIDENT, types[1])
        assertEquals(RescriptTokenTypes.LBRACE, types[2])
        assertEquals(RescriptTokenTypes.PIPE, types[3])
        assertEquals(RescriptTokenTypes.SOME, types[4])
    }

    @Test
    fun `pattern - labeled argument`() {
        val tokens = tokenizeNoWs("~label:")
        assertEquals(RescriptTokenTypes.TILDE, tokens[0].first)
        assertEquals(RescriptTokenTypes.LIDENT, tokens[1].first)
        assertEquals("label", tokens[1].second)
        assertEquals(RescriptTokenTypes.COLON, tokens[2].first)
    }

    // ════════════════════════════════════════════════════════════════
    // Edge cases: Strings
    // ════════════════════════════════════════════════════════════════

    @Test
    fun `edge - unclosed string at EOF`() {
        val tokens = tokenizeNoWs("\"unclosed")
        assertEquals(1, tokens.size)
        assertEquals(RescriptTokenTypes.STRING_VALUE, tokens[0].first)
    }

    @Test
    fun `edge - string with embedded newline`() {
        val tokens = tokenizeNoWs("\"line1\nline2\"")
        assertEquals(RescriptTokenTypes.STRING_VALUE, tokens[0].first)
    }

    // ════════════════════════════════════════════════════════════════
    // Edge cases: Template literals
    // ════════════════════════════════════════════════════════════════

    @Test
    fun `edge - unclosed template literal`() {
        val tokens = tokenizeNoWs("`unclosed")
        assertTrue(tokens.any { it.first == RescriptTokenTypes.JS_STRING_OPEN }, "Should contain JS_STRING_OPEN")
    }

    @Test
    fun `edge - template dollar without brace`() {
        val tokens = tokenizeNoWs("`\${'$'} alone`")
        assertTrue(tokens.any { it.first == RescriptTokenTypes.DOLLAR }, "Should contain DOLLAR")
    }

    @Test
    fun `edge - template with nested braces in interpolation`() {
        val input = "`\${'$'}{obj.x}`"
        val tokens = tokenizeNoWs(input)
        assertTrue(tokens.any { it.first == RescriptTokenTypes.JS_STRING_OPEN })
        assertTrue(tokens.any { it.first == RescriptTokenTypes.JS_STRING_CLOSE })
    }

    // ════════════════════════════════════════════════════════════════
    // Edge cases: Comments
    // ════════════════════════════════════════════════════════════════

    @Test
    fun `edge - deeply nested block comment`() {
        val nested = "/* " + "/* ".repeat(20) + "deep" + " */".repeat(20) + " */"
        val tokens = tokenizeNoWs(nested)
        assertEquals(1, tokens.size)
        assertEquals(RescriptTokenTypes.MULTI_COMMENT, tokens[0].first)
    }

    @Test
    fun `edge - unclosed nested comment`() {
        val tokens = tokenizeNoWs("/* outer /* inner */")
        // The lexer should produce at least one MULTI_COMMENT token
        assertTrue(tokens.any { it.first == RescriptTokenTypes.MULTI_COMMENT })
    }

    // ════════════════════════════════════════════════════════════════
    // Edge cases: Numeric literals
    // ════════════════════════════════════════════════════════════════

    @Test
    fun `edge - leading zero integer`() {
        val tokens = tokenizeNoWs("0123")
        assertEquals(RescriptTokenTypes.INT_VALUE, tokens[0].first)
    }

    @Test
    fun `edge - float dot without trailing digits`() {
        // "1." may parse as INT + DOT or as FLOAT
        val tokens = tokenizeNoWs("1.")
        assertTrue(
            tokens[0].first == RescriptTokenTypes.INT_VALUE || tokens[0].first == RescriptTokenTypes.FLOAT_VALUE,
            "Should be INT_VALUE or FLOAT_VALUE",
        )
    }

    // ════════════════════════════════════════════════════════════════
    // Edge cases: Lexer state reset
    // ════════════════════════════════════════════════════════════════

    @Test
    fun `edge - lexer resets state correctly between uses`() {
        val lexer = RescriptLexer()
        // First use: tokenize a let binding
        lexer.start("let x = 1")
        while (lexer.tokenType != null) lexer.advance()

        // Second use: tokenize JSX — should correctly recognize TAG_LT
        lexer.start("<div>")
        assertEquals(RescriptTokenTypes.TAG_LT, lexer.tokenType)
    }

    // ════════════════════════════════════════════════════════════════
    // Smoke test
    // ════════════════════════════════════════════════════════════════

    @Test
    fun `pattern - no BAD_CHARACTER in typical code`() {
        val input =
            """
            @react.component
            let make = (~name: string, ~age: int) => {
              let greeting = `Hello ${'$'}{name}`
              <div className="container">
                {React.string(greeting)}
                <span> {React.int(age)} </span>
              </div>
            }
            """.trimIndent()
        val tokens = tokenize(input)
        val badTokens = tokens.filter { it.first == TokenType.BAD_CHARACTER }
        assertTrue(
            badTokens.isEmpty(),
            "Should have no BAD_CHARACTER, found: ${badTokens.take(3)}",
        )
    }
}
