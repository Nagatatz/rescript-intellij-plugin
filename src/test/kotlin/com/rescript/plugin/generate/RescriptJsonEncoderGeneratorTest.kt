package com.rescript.plugin.generate

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

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
        assertTrue("Should contain encoder function name", result!!.contains("encodeUser"))
        assertTrue("Should encode name field", result.contains("\"name\""))
        assertTrue("Should encode age field", result.contains("\"age\""))
        assertTrue("Should accept user type parameter", result.contains("v: user"))
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
        assertTrue("Should contain encoder function name", result!!.contains("encodeColor"))
        assertTrue("Should encode Red as String", result.contains("| Red => String(\"Red\")"))
        assertTrue("Should encode Green as String", result.contains("| Green => String(\"Green\")"))
        assertTrue("Should encode Blue as String", result.contains("| Blue => String(\"Blue\")"))
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
        assertTrue("Should use tag-based encoding", result!!.contains("\"tag\""))
        assertTrue("Should encode Circle with payload", result.contains("Circle(v0)"))
        assertTrue("Should encode Square without payload", result.contains("| Square =>"))
    }

    @Test
    fun generateEncoderForUnknownTypeReturnsNull() {
        val result = RescriptJsonEncoderGenerator.generateEncoder("foo", TypeShape.Unknown)
        assertNull("Unknown type shape should return null", result)
    }

    @Test
    fun generateEncoderForEmptyRecordReturnsNull() {
        val shape = TypeShape.Record(emptyList())
        val result = RescriptJsonEncoderGenerator.generateEncoder("empty", shape)
        assertNull("Empty record should return null", result)
    }

    @Test
    fun generateEncoderForEmptyVariantReturnsNull() {
        val shape = TypeShape.Variant(emptyList())
        val result = RescriptJsonEncoderGenerator.generateEncoder("empty", shape)
        assertNull("Empty variant should return null", result)
    }

    @Test
    fun generateEncoderForTypeT() {
        val shape =
            TypeShape.Record(
                listOf(RecordField("value", "string", false)),
            )
        val result = RescriptJsonEncoderGenerator.generateEncoder("t", shape)
        assertNotNull(result)
        assertTrue("Type t should produce 'encode' function name", result!!.contains("let encode ="))
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
        assertTrue("Should use Option.mapOr", expr.contains("Option.mapOr"))
        assertTrue("Should use Null for None", expr.contains("Null"))
        assertTrue("Should encode inner as String", expr.contains("String(v)"))
    }

    @Test
    fun encodeExpressionForArrayType() {
        val arrayType = RescriptJsonType.ArrayType(RescriptJsonType.IntType)
        val expr = RescriptJsonEncoderGenerator.encodeExpression(arrayType, "v.ids")
        assertTrue("Should wrap in Array", expr.startsWith("Array("))
        assertTrue("Should map over elements", expr.contains("Array.map"))
        assertTrue("Should convert inner to Number", expr.contains("Number(v->Int.toFloat)"))
    }

    @Test
    fun encodeExpressionForUnknownType() {
        val unknownType = RescriptJsonType.UnknownType("customType")
        val expr = RescriptJsonEncoderGenerator.encodeExpression(unknownType, "v.data")
        assertTrue("Should contain TODO comment", expr.contains("TODO"))
        assertTrue("Should reference the raw type", expr.contains("customType"))
        assertTrue("Should fallback to Null", expr.contains("Null"))
    }

    @Test
    fun encodeExpressionForNestedOptionArray() {
        val nestedType = RescriptJsonType.OptionType(RescriptJsonType.ArrayType(RescriptJsonType.StringType))
        val expr = RescriptJsonEncoderGenerator.encodeExpression(nestedType, "v.tags")
        assertTrue("Should handle option wrapping", expr.contains("Option.mapOr"))
        assertTrue("Should handle array encoding", expr.contains("Array("))
    }
}
