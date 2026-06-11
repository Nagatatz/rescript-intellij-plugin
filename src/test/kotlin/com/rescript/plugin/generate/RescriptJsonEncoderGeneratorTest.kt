package com.rescript.plugin.generate

import com.rescript.plugin.lang.RecordField
import com.rescript.plugin.lang.TypeShape
import com.rescript.plugin.lang.VariantConstructor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptJsonEncoderGeneratorTest {
    // ── generateEncoder ──

    @Test
    fun generateEncoderForRecordType() {
        val shape =
            TypeShape.Record(
                listOf(
                    RecordField("name", "string", false),
                    RecordField("age", "int", false),
                ),
            )
        val result = RescriptJsonEncoderGenerator.generateEncoder("user", shape)
        assertNotNull(result)
        assertTrue(result!!.contains("encodeUser"), "Should contain encoder function name")
        assertTrue(result.contains("\"name\""), "Should encode name field")
        assertTrue(result.contains("\"age\""), "Should encode age field")
        assertTrue(result.contains("v: user"), "Should accept user type parameter")
    }

    @Test
    fun generateEncoderForSimpleEnumVariant() {
        val shape =
            TypeShape.Variant(
                listOf(
                    VariantConstructor("Red", null),
                    VariantConstructor("Green", null),
                    VariantConstructor("Blue", null),
                ),
            )
        val result = RescriptJsonEncoderGenerator.generateEncoder("color", shape)
        assertNotNull(result)
        assertTrue(result!!.contains("encodeColor"), "Should contain encoder function name")
        assertTrue(result.contains("| Red => String(\"Red\")"), "Should encode Red as String")
        assertTrue(result.contains("| Green => String(\"Green\")"), "Should encode Green as String")
        assertTrue(result.contains("| Blue => String(\"Blue\")"), "Should encode Blue as String")
    }

    @Test
    fun generateEncoderForTaggedUnionVariant() {
        val shape =
            TypeShape.Variant(
                listOf(
                    VariantConstructor("Circle", "float"),
                    VariantConstructor("Square", null),
                ),
            )
        val result = RescriptJsonEncoderGenerator.generateEncoder("shape", shape)
        assertNotNull(result)
        assertTrue(result!!.contains("\"tag\""), "Should use tag-based encoding")
        assertTrue(result.contains("Circle(v0)"), "Should encode Circle with payload")
        assertTrue(result.contains("| Square =>"), "Should encode Square without payload")
    }

    @Test
    fun generateEncoderForUnknownTypeReturnsNull() {
        val result = RescriptJsonEncoderGenerator.generateEncoder("foo", TypeShape.Unknown)
        assertNull(result, "Unknown type shape should return null")
    }

    @Test
    fun generateEncoderForEmptyRecordReturnsNull() {
        val shape = TypeShape.Record(emptyList())
        val result = RescriptJsonEncoderGenerator.generateEncoder("empty", shape)
        assertNull(result, "Empty record should return null")
    }

    @Test
    fun generateEncoderForEmptyVariantReturnsNull() {
        val shape = TypeShape.Variant(emptyList())
        val result = RescriptJsonEncoderGenerator.generateEncoder("empty", shape)
        assertNull(result, "Empty variant should return null")
    }

    @Test
    fun generateEncoderForTypeT() {
        val shape =
            TypeShape.Record(
                listOf(RecordField("value", "string", false)),
            )
        val result = RescriptJsonEncoderGenerator.generateEncoder("t", shape)
        assertNotNull(result)
        assertTrue(result!!.contains("let encode ="), "Type t should produce 'encode' function name")
    }

    // ── encodeExpression ──

    @Test
    fun encodeExpressionForString() {
        val expr = RescriptJsonEncoderGenerator.encodeExpression(RescriptJsonType.StringType, "v.name")
        assertEquals("String(v.name)", expr)
    }

    @Test
    fun encodeExpressionForInt() {
        val expr = RescriptJsonEncoderGenerator.encodeExpression(RescriptJsonType.IntType, "v.age")
        assertEquals("Number(v.age->Int.toFloat)", expr)
    }

    @Test
    fun encodeExpressionForFloat() {
        val expr = RescriptJsonEncoderGenerator.encodeExpression(RescriptJsonType.FloatType, "v.score")
        assertEquals("Number(v.score)", expr)
    }

    @Test
    fun encodeExpressionForBool() {
        val expr = RescriptJsonEncoderGenerator.encodeExpression(RescriptJsonType.BoolType, "v.active")
        assertEquals("Boolean(v.active)", expr)
    }

    @Test
    fun encodeExpressionForOptionType() {
        val optionType = RescriptJsonType.OptionType(RescriptJsonType.StringType)
        val expr = RescriptJsonEncoderGenerator.encodeExpression(optionType, "v.nickname")
        assertTrue(expr.contains("Option.mapOr"), "Should use Option.mapOr")
        assertTrue(expr.contains("Null"), "Should use Null for None")
        assertTrue(expr.contains("String(v)"), "Should encode inner as String")
    }

    @Test
    fun encodeExpressionForArrayType() {
        val arrayType = RescriptJsonType.ArrayType(RescriptJsonType.IntType)
        val expr = RescriptJsonEncoderGenerator.encodeExpression(arrayType, "v.ids")
        assertTrue(expr.startsWith("Array("), "Should wrap in Array")
        assertTrue(expr.contains("Array.map"), "Should map over elements")
        assertTrue(expr.contains("Number(v->Int.toFloat)"), "Should convert inner to Number")
    }

    @Test
    fun encodeExpressionForUnknownType() {
        val unknownType = RescriptJsonType.UnknownType("customType")
        val expr = RescriptJsonEncoderGenerator.encodeExpression(unknownType, "v.data")
        assertTrue(expr.contains("TODO"), "Should contain TODO comment")
        assertTrue(expr.contains("customType"), "Should reference the raw type")
        assertTrue(expr.contains("Null"), "Should fallback to Null")
    }

    @Test
    fun encodeExpressionForNestedOptionArray() {
        val nestedType = RescriptJsonType.OptionType(RescriptJsonType.ArrayType(RescriptJsonType.StringType))
        val expr = RescriptJsonEncoderGenerator.encodeExpression(nestedType, "v.tags")
        assertTrue(expr.contains("Option.mapOr"), "Should handle option wrapping")
        assertTrue(expr.contains("Array("), "Should handle array encoding")
    }
}
