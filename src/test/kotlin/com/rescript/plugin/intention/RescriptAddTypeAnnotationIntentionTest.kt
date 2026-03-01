package com.rescript.plugin.intention

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptAddTypeAnnotationIntentionTest {
    @Test
    fun `insertTypeAnnotation adds type to simple binding`() {
        val result =
            RescriptAddTypeAnnotationIntention.insertTypeAnnotation(
                "let count = 0",
                "int",
            )
        assertEquals("let count: int = 0", result)
    }

    @Test
    fun `insertTypeAnnotation adds type to function binding`() {
        val result =
            RescriptAddTypeAnnotationIntention.insertTypeAnnotation(
                "let add = (a, b) => a + b",
                "(int, int) => int",
            )
        assertEquals("let add: (int, int) => int = (a, b) => a + b", result)
    }

    @Test
    fun `insertTypeAnnotation returns null for already annotated`() {
        val result =
            RescriptAddTypeAnnotationIntention.insertTypeAnnotation(
                "let count: int = 0",
                "int",
            )
        assertNull(result)
    }

    @Test
    fun `insertTypeAnnotation returns null for non-let line`() {
        val result =
            RescriptAddTypeAnnotationIntention.insertTypeAnnotation(
                "type t = int",
                "int",
            )
        assertNull(result)
    }

    @Test
    fun `insertTypeAnnotation preserves indentation`() {
        val result =
            RescriptAddTypeAnnotationIntention.insertTypeAnnotation(
                "  let value = expr",
                "string",
            )
        assertEquals("  let value: string = expr", result)
    }

    @Test
    fun `extractTypeFromHover extracts from let pattern`() {
        val result =
            RescriptAddTypeAnnotationIntention.extractTypeFromHover(
                "let count: int",
            )
        assertEquals("int", result)
    }

    @Test
    fun `extractTypeFromHover extracts from function signature`() {
        val result =
            RescriptAddTypeAnnotationIntention.extractTypeFromHover(
                "let add: (int, int) => int",
            )
        assertEquals("(int, int) => int", result)
    }

    @Test
    fun `extractTypeFromHover returns raw text as fallback`() {
        val result = RescriptAddTypeAnnotationIntention.extractTypeFromHover("int")
        assertEquals("int", result)
    }

    @Test
    fun `extractTypeFromHover returns null for empty string`() {
        val result = RescriptAddTypeAnnotationIntention.extractTypeFromHover("")
        assertNull(result)
    }

    @Test
    fun `LET_WITHOUT_TYPE_PATTERN matches simple let`() {
        assertTrue(
            RescriptAddTypeAnnotationIntention.LET_WITHOUT_TYPE_PATTERN.containsMatchIn("let x = 42"),
        )
    }

    @Test
    fun `LET_WITH_TYPE_PATTERN matches annotated let`() {
        assertTrue(
            RescriptAddTypeAnnotationIntention.LET_WITH_TYPE_PATTERN.containsMatchIn("let x: int = 42"),
        )
    }

    @Test
    fun `intention can be instantiated`() {
        assertNotNull(RescriptAddTypeAnnotationIntention())
    }

    @Test
    fun `getText returns correct text`() {
        assertEquals("Add type annotation", RescriptAddTypeAnnotationIntention().text)
    }
}
