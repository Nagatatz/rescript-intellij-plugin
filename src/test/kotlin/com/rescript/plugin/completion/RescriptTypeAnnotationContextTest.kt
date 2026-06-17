package com.rescript.plugin.completion

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Unit tests for the pure caret-context detector used by record/variant
 * placeholder completion.
 */
class RescriptTypeAnnotationContextTest {
    private fun detect(textBeforeCaret: String) = RescriptTypeAnnotationContext.detectExpectedType(textBeforeCaret)

    @Test
    fun `detects simple record type at value position`() {
        val d = detect("let p: person = ")
        assertEquals("person", d?.typeName)
        assertFalse(d!!.afterOpenBrace)
    }

    @Test
    fun `detects with no trailing space after equals`() {
        val d = detect("let p: person =")
        assertEquals("person", d?.typeName)
    }

    @Test
    fun `strips type arguments to head identifier`() {
        assertEquals("option", detect("let x: option<int> = ")?.typeName)
        assertEquals("array", detect("let xs: array<option<int>> = ")?.typeName)
    }

    @Test
    fun `keeps first segment of dotted type name`() {
        assertEquals("Belt", detect("let m: Belt.Map.t = ")?.typeName)
    }

    @Test
    fun `sets afterOpenBrace when brace already typed`() {
        val d = detect("let p: person = {")
        assertEquals("person", d?.typeName)
        assertTrue(d!!.afterOpenBrace)
    }

    @Test
    fun `allows a partial identifier prefix after equals`() {
        val d = detect("let c: color = R")
        assertEquals("color", d?.typeName)
        assertFalse(d!!.afterOpenBrace)
    }

    @Test
    fun `allows rec keyword binding`() {
        assertEquals("tree", detect("let rec t: tree = ")?.typeName)
    }

    @Test
    fun `returns null without type annotation`() {
        assertNull(detect("let x = "))
    }

    @Test
    fun `returns null when not a let binding`() {
        assertNull(detect("module M: T = "))
        assertNull(detect("x: person = "))
    }

    @Test
    fun `returns null for complex value tail`() {
        assertNull(detect("let x: int = 1 + "))
        assertNull(detect("let x: int = foo("))
    }

    @Test
    fun `returns null for destructuring binding`() {
        assertNull(detect("let (a, b): pair = "))
        assertNull(detect("let {x}: rec = "))
    }

    @Test
    fun `detects nested let inside expression`() {
        val d = detect("let outer = {\n  let p: person = ")
        assertEquals("person", d?.typeName)
    }

    @Test
    fun `returns null for empty input`() {
        assertNull(detect(""))
        assertNull(detect("   "))
    }

    @Test
    fun `returns null for tuple type annotation`() {
        assertNull(detect("let p: (int, string) = "))
    }

    @Test
    fun `returns null for function type annotation`() {
        // The labeled-arg colon lives inside parens, so the annotation colon is
        // still located, but the head of `(~x: int) => int` is `(` so no record
        // or variant head identifier can be extracted.
        assertNull(detect("let f: (~x: int) => int = "))
    }
}
