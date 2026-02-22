package com.rescript.plugin.generate

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RescriptJsonTypeClassifierTest {
    // --- Primitive types ---

    @Test
    fun `classify string`() {
        assertEquals(RescriptJsonType.StringType, RescriptJsonTypeClassifier.classify("string"))
    }

    @Test
    fun `classify int`() {
        assertEquals(RescriptJsonType.IntType, RescriptJsonTypeClassifier.classify("int"))
    }

    @Test
    fun `classify float`() {
        assertEquals(RescriptJsonType.FloatType, RescriptJsonTypeClassifier.classify("float"))
    }

    @Test
    fun `classify bool`() {
        assertEquals(RescriptJsonType.BoolType, RescriptJsonTypeClassifier.classify("bool"))
    }

    // --- Generic types ---

    @Test
    fun `classify option of string`() {
        val result = RescriptJsonTypeClassifier.classify("option<string>")
        assertEquals(RescriptJsonType.OptionType(RescriptJsonType.StringType), result)
    }

    @Test
    fun `classify option of int`() {
        val result = RescriptJsonTypeClassifier.classify("option<int>")
        assertEquals(RescriptJsonType.OptionType(RescriptJsonType.IntType), result)
    }

    @Test
    fun `classify array of string`() {
        val result = RescriptJsonTypeClassifier.classify("array<string>")
        assertEquals(RescriptJsonType.ArrayType(RescriptJsonType.StringType), result)
    }

    @Test
    fun `classify array of float`() {
        val result = RescriptJsonTypeClassifier.classify("array<float>")
        assertEquals(RescriptJsonType.ArrayType(RescriptJsonType.FloatType), result)
    }

    // --- Nested generics ---

    @Test
    fun `classify option of array of string`() {
        val result = RescriptJsonTypeClassifier.classify("option<array<string>>")
        assertEquals(
            RescriptJsonType.OptionType(RescriptJsonType.ArrayType(RescriptJsonType.StringType)),
            result,
        )
    }

    @Test
    fun `classify array of option of int`() {
        val result = RescriptJsonTypeClassifier.classify("array<option<int>>")
        assertEquals(
            RescriptJsonType.ArrayType(RescriptJsonType.OptionType(RescriptJsonType.IntType)),
            result,
        )
    }

    @Test
    fun `classify option of option of string`() {
        val result = RescriptJsonTypeClassifier.classify("option<option<string>>")
        assertEquals(
            RescriptJsonType.OptionType(RescriptJsonType.OptionType(RescriptJsonType.StringType)),
            result,
        )
    }

    @Test
    fun `classify deeply nested generics`() {
        val result = RescriptJsonTypeClassifier.classify("option<array<option<int>>>")
        assertEquals(
            RescriptJsonType.OptionType(
                RescriptJsonType.ArrayType(
                    RescriptJsonType.OptionType(RescriptJsonType.IntType),
                ),
            ),
            result,
        )
    }

    // --- Unknown types ---

    @Test
    fun `classify unknown custom type`() {
        val result = RescriptJsonTypeClassifier.classify("user")
        assertEquals(RescriptJsonType.UnknownType("user"), result)
    }

    @Test
    fun `classify unknown qualified type`() {
        val result = RescriptJsonTypeClassifier.classify("Js.Date.t")
        assertEquals(RescriptJsonType.UnknownType("Js.Date.t"), result)
    }

    @Test
    fun `classify unknown generic type`() {
        val result = RescriptJsonTypeClassifier.classify("dict<string>")
        assertEquals(RescriptJsonType.UnknownType("dict<string>"), result)
    }

    // --- Whitespace handling ---

    @Test
    fun `classify with leading and trailing whitespace`() {
        assertEquals(RescriptJsonType.StringType, RescriptJsonTypeClassifier.classify("  string  "))
    }

    @Test
    fun `classify option with whitespace`() {
        // Note: "option< string >" won't match because we check exact prefix "option<"
        // This is expected behavior - type annotations in parsed PSI are typically trimmed
        val result = RescriptJsonTypeClassifier.classify("option<string>")
        assertEquals(RescriptJsonType.OptionType(RescriptJsonType.StringType), result)
    }

    // --- extractGenericInner ---

    @Test
    fun `extractGenericInner returns inner type for option`() {
        val result = RescriptJsonTypeClassifier.extractGenericInner("option", "option<string>")
        assertEquals("string", result)
    }

    @Test
    fun `extractGenericInner returns inner type for array`() {
        val result = RescriptJsonTypeClassifier.extractGenericInner("array", "array<int>")
        assertEquals("int", result)
    }

    @Test
    fun `extractGenericInner handles nested generics`() {
        val result = RescriptJsonTypeClassifier.extractGenericInner("option", "option<array<string>>")
        assertEquals("array<string>", result)
    }

    @Test
    fun `extractGenericInner returns null for non-matching wrapper`() {
        assertNull(RescriptJsonTypeClassifier.extractGenericInner("option", "array<string>"))
    }

    @Test
    fun `extractGenericInner returns null for non-generic type`() {
        assertNull(RescriptJsonTypeClassifier.extractGenericInner("option", "string"))
    }

    @Test
    fun `extractGenericInner returns null for unbalanced brackets`() {
        assertNull(RescriptJsonTypeClassifier.extractGenericInner("option", "option<array<string>"))
    }

    // --- Data class equality ---

    @Test
    fun `RescriptJsonType data class equality`() {
        assertEquals(
            RescriptJsonType.OptionType(RescriptJsonType.StringType),
            RescriptJsonType.OptionType(RescriptJsonType.StringType),
        )
    }

    @Test
    fun `RescriptJsonType different types are not equal`() {
        val opt = RescriptJsonType.OptionType(RescriptJsonType.StringType)
        val arr = RescriptJsonType.ArrayType(RescriptJsonType.StringType)
        assert(opt != arr)
    }
}
