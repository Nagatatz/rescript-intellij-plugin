package com.rescript.plugin.navigation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

/**
 * Unit tests for [RescriptTypeParser.parse].
 *
 * Each case asserts the AST shape produced by a single textual type
 * expression. The matrix covers the four AST families (Ctor / TypeVar /
 * App / Tuple / Arrow), unit-literal handling, the special leading-`=>`
 * "return query" mode, and the negative cases that must yield `null`
 * (records, polymorphic variants, malformed input).
 */
class RescriptTypeParserTest {
    private fun parse(text: String): RescriptTypeAst? = RescriptTypeParser.parse(text)

    // ── Ctor ──

    @Test
    fun `int parses as Ctor`() = assertEquals(RescriptTypeAst.Ctor("int"), parse("int"))

    @Test
    fun `string parses as Ctor`() = assertEquals(RescriptTypeAst.Ctor("string"), parse("string"))

    @Test
    fun `whitespace around ctor is tolerated`() = assertEquals(RescriptTypeAst.Ctor("int"), parse("  int  "))

    // ── TypeVar ──

    @Test
    fun `single quoted lowercase is TypeVar`() = assertEquals(RescriptTypeAst.TypeVar("a"), parse("'a"))

    @Test
    fun `multi-letter type variable`() = assertEquals(RescriptTypeAst.TypeVar("foo"), parse("'foo"))

    // ── App ──

    @Test
    fun `option of int is App`() =
        assertEquals(
            RescriptTypeAst.App("option", listOf(RescriptTypeAst.Ctor("int"))),
            parse("option<int>"),
        )

    @Test
    fun `nested generic is parsed as nested App`() =
        assertEquals(
            RescriptTypeAst.App(
                "array",
                listOf(RescriptTypeAst.App("option", listOf(RescriptTypeAst.Ctor("int")))),
            ),
            parse("array<option<int>>"),
        )

    @Test
    fun `result with two type args`() =
        assertEquals(
            RescriptTypeAst.App(
                "result",
                listOf(RescriptTypeAst.Ctor("int"), RescriptTypeAst.Ctor("string")),
            ),
            parse("result<int, string>"),
        )

    @Test
    fun `app of type var`() =
        assertEquals(
            RescriptTypeAst.App("option", listOf(RescriptTypeAst.TypeVar("a"))),
            parse("option<'a>"),
        )

    // ── Unit & Tuple ──

    @Test
    fun `unit parses as UnitT`() = assertEquals(RescriptTypeAst.UnitT, parse("()"))

    @Test
    fun `parenthesised type strips parens`() = assertEquals(RescriptTypeAst.Ctor("int"), parse("(int)"))

    @Test
    fun `pair tuple`() =
        assertEquals(
            RescriptTypeAst.Tuple(listOf(RescriptTypeAst.Ctor("int"), RescriptTypeAst.Ctor("string"))),
            parse("(int, string)"),
        )

    @Test
    fun `triple tuple`() =
        assertEquals(
            RescriptTypeAst.Tuple(
                listOf(
                    RescriptTypeAst.Ctor("int"),
                    RescriptTypeAst.Ctor("string"),
                    RescriptTypeAst.Ctor("bool"),
                ),
            ),
            parse("(int, string, bool)"),
        )

    @Test
    fun `tuple of generic types`() =
        assertEquals(
            RescriptTypeAst.Tuple(
                listOf(
                    RescriptTypeAst.App("option", listOf(RescriptTypeAst.Ctor("int"))),
                    RescriptTypeAst.Ctor("string"),
                ),
            ),
            parse("(option<int>, string)"),
        )

    // ── Arrow ──

    @Test
    fun `simple arrow`() =
        assertEquals(
            RescriptTypeAst.Arrow(RescriptTypeAst.Ctor("int"), RescriptTypeAst.Ctor("int")),
            parse("int => int"),
        )

    @Test
    fun `arrow chain is right-associative`() =
        assertEquals(
            RescriptTypeAst.Arrow(
                RescriptTypeAst.Ctor("int"),
                RescriptTypeAst.Arrow(
                    RescriptTypeAst.Ctor("string"),
                    RescriptTypeAst.Ctor("bool"),
                ),
            ),
            parse("int => string => bool"),
        )

    @Test
    fun `arrow with tuple argument`() =
        assertEquals(
            RescriptTypeAst.Arrow(
                RescriptTypeAst.Tuple(
                    listOf(RescriptTypeAst.Ctor("int"), RescriptTypeAst.Ctor("string")),
                ),
                RescriptTypeAst.Ctor("bool"),
            ),
            parse("(int, string) => bool"),
        )

    @Test
    fun `polymorphic identity`() =
        assertEquals(
            RescriptTypeAst.Arrow(RescriptTypeAst.TypeVar("a"), RescriptTypeAst.TypeVar("a")),
            parse("'a => 'a"),
        )

    @Test
    fun `arrow returning generic`() =
        assertEquals(
            RescriptTypeAst.Arrow(
                RescriptTypeAst.Ctor("string"),
                RescriptTypeAst.App(
                    "result",
                    listOf(RescriptTypeAst.Ctor("int"), RescriptTypeAst.Ctor("string")),
                ),
            ),
            parse("string => result<int, string>"),
        )

    // ── ReturnQuery ──

    @Test
    fun `leading arrow yields ReturnQuery`() =
        assertEquals(
            RescriptTypeAst.ReturnQuery(
                RescriptTypeAst.App(
                    "result",
                    listOf(RescriptTypeAst.Ctor("int"), RescriptTypeAst.Ctor("string")),
                ),
            ),
            parse("=> result<int, string>"),
        )

    @Test
    fun `leading arrow with tuple`() =
        assertEquals(
            RescriptTypeAst.ReturnQuery(
                RescriptTypeAst.Tuple(
                    listOf(RescriptTypeAst.Ctor("int"), RescriptTypeAst.Ctor("string")),
                ),
            ),
            parse("=> (int, string)"),
        )

    @Test
    fun `leading arrow with type variable`() =
        assertEquals(
            RescriptTypeAst.ReturnQuery(RescriptTypeAst.TypeVar("a")),
            parse("=> 'a"),
        )

    // ── Negative cases ──

    @Test
    fun `empty input is null`() = assertNull(parse(""))

    @Test
    fun `lone arrow is null`() = assertNull(parse("=>"))

    @Test
    fun `unbalanced angle is null`() = assertNull(parse("option<int"))

    @Test
    fun `record syntax is unsupported`() = assertNull(parse("{name: string}"))

    @Test
    fun `polymorphic variant syntax is unsupported`() = assertNull(parse("[#Foo | #Bar]"))

    @Test
    fun `trailing junk is null`() = assertNull(parse("int garbage"))

    @Test
    fun `missing return after arrow is null`() = assertNull(parse("int =>"))

    @Test
    fun `lone apostrophe is null`() = assertNull(parse("'"))
}
