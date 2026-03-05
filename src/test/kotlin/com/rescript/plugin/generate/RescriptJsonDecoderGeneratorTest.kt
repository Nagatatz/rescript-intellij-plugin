package com.rescript.plugin.generate

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptJsonDecoderGeneratorTest {
    // ── generateDecoder ──

    @Test
    fun generateDecoderForRecordType() {
        val shape =
            TypeShape.Record(
                listOf(
                    RecordField("name", "string", false),
                    RecordField("age", "int", false),
                ),
            )
        val result = RescriptJsonDecoderGenerator.generateDecoder("user", shape)
        assertNotNull(result)
        assertTrue("Should contain decoder function name", result!!.contains("decodeUser"))
        assertTrue("Should decode name field", result.contains("\"name\""))
        assertTrue("Should decode age field", result.contains("\"age\""))
        assertTrue("Should return option type", result.contains("option<user>"))
    }

    @Test
    fun generateDecoderForSimpleEnumVariant() {
        val shape =
            TypeShape.Variant(
                listOf(
                    VariantConstructor("Red", null),
                    VariantConstructor("Green", null),
                    VariantConstructor("Blue", null),
                ),
            )
        val result = RescriptJsonDecoderGenerator.generateDecoder("color", shape)
        assertNotNull(result)
        assertTrue("Should contain decoder function name", result!!.contains("decodeColor"))
        assertTrue("Should match Red string", result.contains("String(\"Red\")"))
        assertTrue("Should match Green string", result.contains("String(\"Green\")"))
        assertTrue("Should match Blue string", result.contains("String(\"Blue\")"))
    }

    @Test
    fun generateDecoderForTaggedUnionVariant() {
        val shape =
            TypeShape.Variant(
                listOf(
                    VariantConstructor("Circle", "float"),
                    VariantConstructor("Rect", "float, float"),
                ),
            )
        val result = RescriptJsonDecoderGenerator.generateDecoder("shape", shape)
        assertNotNull(result)
        assertTrue("Should use tag-based decoding", result!!.contains("\"tag\""))
        assertTrue("Should decode Circle", result.contains("\"Circle\""))
        assertTrue("Should decode Rect", result.contains("\"Rect\""))
    }

    @Test
    fun generateDecoderForUnknownTypeReturnsNull() {
        val result = RescriptJsonDecoderGenerator.generateDecoder("foo", TypeShape.Unknown)
        assertNull("Unknown type shape should return null", result)
    }

    @Test
    fun generateDecoderForEmptyRecordReturnsNull() {
        val shape = TypeShape.Record(emptyList())
        val result = RescriptJsonDecoderGenerator.generateDecoder("empty", shape)
        assertNull("Empty record should return null", result)
    }

    @Test
    fun generateDecoderForEmptyVariantReturnsNull() {
        val shape = TypeShape.Variant(emptyList())
        val result = RescriptJsonDecoderGenerator.generateDecoder("empty", shape)
        assertNull("Empty variant should return null", result)
    }

    @Test
    fun generateDecoderForTypeT() {
        val shape =
            TypeShape.Record(
                listOf(RecordField("value", "string", false)),
            )
        val result = RescriptJsonDecoderGenerator.generateDecoder("t", shape)
        assertNotNull(result)
        // Type "t" should produce "decode" (not "decodeT")
        assertTrue("Type t should produce 'decode' function name", result!!.contains("let decode ="))
    }

    // ── decodeFieldExpression ──

    @Test
    fun decodeFieldExpressionForStringType() {
        val expr = RescriptJsonDecoderGenerator.decodeFieldExpression(RescriptJsonType.StringType, "name")
        assertTrue("Should access dict field", expr.contains("dict->Dict.get(\"name\")"))
        assertTrue("Should decode string", expr.contains("String(v)"))
    }

    @Test
    fun decodeFieldExpressionForIntType() {
        val expr = RescriptJsonDecoderGenerator.decodeFieldExpression(RescriptJsonType.IntType, "age")
        assertTrue("Should decode int via Number", expr.contains("Number(v)"))
        assertTrue("Should convert to int", expr.contains("Int.fromFloat"))
    }

    @Test
    fun decodeFieldExpressionForOptionType() {
        val optionType = RescriptJsonType.OptionType(RescriptJsonType.StringType)
        val expr = RescriptJsonDecoderGenerator.decodeFieldExpression(optionType, "nickname")
        assertTrue("Should handle Null for option", expr.contains("Null"))
        assertTrue("Should handle Some wrapping", expr.contains("Some"))
    }

    // ── decodeInlineExpression ──

    @Test
    fun decodeInlineExpressionForString() {
        val expr = RescriptJsonDecoderGenerator.decodeInlineExpression(RescriptJsonType.StringType)
        assertEquals("switch v { | String(v) => Some(v) | _ => None }", expr)
    }

    @Test
    fun decodeInlineExpressionForInt() {
        val expr = RescriptJsonDecoderGenerator.decodeInlineExpression(RescriptJsonType.IntType)
        assertEquals("switch v { | Number(v) => Some(v->Int.fromFloat) | _ => None }", expr)
    }

    @Test
    fun decodeInlineExpressionForFloat() {
        val expr = RescriptJsonDecoderGenerator.decodeInlineExpression(RescriptJsonType.FloatType)
        assertEquals("switch v { | Number(v) => Some(v) | _ => None }", expr)
    }

    @Test
    fun decodeInlineExpressionForBool() {
        val expr = RescriptJsonDecoderGenerator.decodeInlineExpression(RescriptJsonType.BoolType)
        assertEquals("switch v { | Boolean(v) => Some(v) | _ => None }", expr)
    }

    @Test
    fun decodeInlineExpressionForArray() {
        val arrayType = RescriptJsonType.ArrayType(RescriptJsonType.StringType)
        val expr = RescriptJsonDecoderGenerator.decodeInlineExpression(arrayType)
        assertTrue("Should match Array pattern", expr.contains("| Array(arr)"))
        assertTrue("Should reduce array", expr.contains("Array.reduce"))
    }

    @Test
    fun decodeInlineExpressionForUnknownType() {
        val unknownType = RescriptJsonType.UnknownType("customType")
        val expr = RescriptJsonDecoderGenerator.decodeInlineExpression(unknownType)
        assertTrue("Should contain TODO comment", expr.contains("TODO"))
        assertTrue("Should reference the raw type", expr.contains("customType"))
        assertTrue("Should return None", expr.contains("None"))
    }

    @Test
    fun decodeInlineExpressionForNestedOption() {
        val nestedOption = RescriptJsonType.OptionType(RescriptJsonType.IntType)
        val expr = RescriptJsonDecoderGenerator.decodeInlineExpression(nestedOption)
        assertTrue("Should handle Null case", expr.contains("Null"))
        assertTrue("Should handle inner decode", expr.contains("Number(v)"))
    }
}
