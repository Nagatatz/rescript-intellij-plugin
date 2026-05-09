package com.rescript.plugin.intention

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Unit tests for [RescriptConstructorOccurrenceClassifier.classifyAt].
 *
 * Each case feeds a snippet of ReScript source plus the offset of a
 * UIDENT to classify, and asserts the resulting kind. The fixtures
 * exercise the four classifier outcomes (CONSTRUCTOR / PATTERN /
 * MODULE_QUALIFIED_TAIL / OTHER) across the contexts the rename
 * intention has to recognise: switch arms, type declaration arms,
 * constructor invocations, module-qualified calls, JSX, type-position
 * names, and tokens inside string literals.
 */
class RescriptConstructorOccurrenceClassifierTest {
    private fun classify(
        source: String,
        marker: String = "Foo",
    ): ConstructorOccurrenceKind {
        val offset = source.indexOf(marker)
        check(offset >= 0) { "marker '$marker' not found in: $source" }
        return RescriptConstructorOccurrenceClassifier.classifyAt(source, offset)
    }

    // ── PATTERN: arm patterns ──

    @Test
    fun `switch arm with paren constructor is PATTERN`() =
        assertEquals(
            ConstructorOccurrenceKind.PATTERN,
            classify("switch x { | Foo(_) => 1 | _ => 0 }"),
        )

    @Test
    fun `switch arm with zero-arity constructor is PATTERN`() =
        assertEquals(
            ConstructorOccurrenceKind.PATTERN,
            classify("switch x { | Foo => 1 | _ => 0 }"),
        )

    @Test
    fun `type declaration arm is PATTERN`() =
        assertEquals(
            ConstructorOccurrenceKind.PATTERN,
            classify("type t = | Foo | Bar"),
        )

    @Test
    fun `type declaration arm with payload is PATTERN`() =
        assertEquals(
            ConstructorOccurrenceKind.PATTERN,
            classify("type t = | Foo(int) | Bar"),
        )

    @Test
    fun `or-pattern second arm is PATTERN`() =
        assertEquals(
            ConstructorOccurrenceKind.PATTERN,
            classify("switch x { | Bar | Foo => 1 }"),
        )

    // ── CONSTRUCTOR: invocation in expression position ──

    @Test
    fun `constructor invocation with paren is CONSTRUCTOR`() =
        assertEquals(
            ConstructorOccurrenceKind.CONSTRUCTOR,
            classify("let x = Foo(1)"),
        )

    @Test
    fun `zero-arity constructor after equals is CONSTRUCTOR`() =
        assertEquals(
            ConstructorOccurrenceKind.CONSTRUCTOR,
            classify("let x = Foo"),
        )

    @Test
    fun `constructor inside paren-grouped argument is CONSTRUCTOR`() =
        assertEquals(
            ConstructorOccurrenceKind.CONSTRUCTOR,
            classify("f((Foo, 1))"),
        )

    @Test
    fun `constructor inside list literal is CONSTRUCTOR`() =
        assertEquals(
            ConstructorOccurrenceKind.CONSTRUCTOR,
            classify("let xs = [Foo, Bar]"),
        )

    @Test
    fun `constructor as second arg is CONSTRUCTOR`() =
        assertEquals(
            ConstructorOccurrenceKind.CONSTRUCTOR,
            classify("f(1, Foo, 3)"),
        )

    @Test
    fun `constructor after arrow is CONSTRUCTOR`() =
        assertEquals(
            ConstructorOccurrenceKind.CONSTRUCTOR,
            classify("let f = () => Foo"),
        )

    // ── MODULE_QUALIFIED_TAIL ──

    @Test
    fun `module-qualified constructor invocation is MODULE_QUALIFIED_TAIL`() =
        assertEquals(
            ConstructorOccurrenceKind.MODULE_QUALIFIED_TAIL,
            classify("let x = Result.Foo(1)", marker = "Foo"),
        )

    @Test
    fun `module-qualified zero-arity is MODULE_QUALIFIED_TAIL`() =
        assertEquals(
            ConstructorOccurrenceKind.MODULE_QUALIFIED_TAIL,
            classify("let x = M.Foo", marker = "Foo"),
        )

    @Test
    fun `module-qualified pattern is MODULE_QUALIFIED_TAIL`() =
        assertEquals(
            ConstructorOccurrenceKind.MODULE_QUALIFIED_TAIL,
            classify("switch x { | M.Foo(_) => 1 | _ => 0 }", marker = "Foo"),
        )

    // ── OTHER: type position, JSX, comments, strings ──

    @Test
    fun `JSX element name is OTHER`() =
        assertEquals(
            ConstructorOccurrenceKind.OTHER,
            classify("let el = <Foo />"),
        )

    @Test
    fun `closing JSX tag is OTHER`() =
        assertEquals(
            ConstructorOccurrenceKind.OTHER,
            classify("let el = <span> </Foo>", marker = "Foo"),
        )

    @Test
    fun `type annotation that mentions a constructor name is OTHER`() =
        assertEquals(
            ConstructorOccurrenceKind.OTHER,
            classify("let x: Foo = bar"),
        )

    @Test
    fun `module declaration head is OTHER`() =
        assertEquals(
            ConstructorOccurrenceKind.OTHER,
            classify("module Foo = {}"),
        )

    @Test
    fun `inside string literal is OTHER`() =
        assertEquals(
            ConstructorOccurrenceKind.OTHER,
            classify("let s = \"Foo\""),
        )

    @Test
    fun `inside line comment is OTHER`() =
        assertEquals(
            ConstructorOccurrenceKind.OTHER,
            classify("let x = 1 // Foo"),
        )

    @Test
    fun `inside block comment is OTHER`() =
        assertEquals(
            ConstructorOccurrenceKind.OTHER,
            classify("let x = /* Foo */ 1"),
        )

    @Test
    fun `bare UIDENT mid-expression after dot-method-call is OTHER`() {
        // `arr->Foo` — pipe-chain method call, not a constructor.
        // We classify this as OTHER because the prev token is the
        // pipe arrow and the follow-up isn't `(`. False-positive
        // tolerance: a 0-ary `Foo` constructor used after `->` is
        // unusual; users can rename via Shift+F6 (LSP) instead.
        assertEquals(
            ConstructorOccurrenceKind.OTHER,
            classify("let x = arr->Foo"),
        )
    }

    // ── Edge cases ──

    @Test
    fun `offset outside file returns OTHER`() {
        val source = "let x = 1"
        assertEquals(
            ConstructorOccurrenceKind.OTHER,
            RescriptConstructorOccurrenceClassifier.classifyAt(source, source.length + 5),
        )
    }

    @Test
    fun `offset on whitespace returns OTHER`() {
        val source = "let x = Foo"
        // offset right after `=` and before `Foo` lands on the space
        val space = source.indexOf("= ") + 1
        assertEquals(
            ConstructorOccurrenceKind.OTHER,
            RescriptConstructorOccurrenceClassifier.classifyAt(source, space),
        )
    }

    @Test
    fun `offset on lowercase identifier returns OTHER`() =
        assertEquals(
            ConstructorOccurrenceKind.OTHER,
            classify("let foo = 1", marker = "foo"),
        )
}
