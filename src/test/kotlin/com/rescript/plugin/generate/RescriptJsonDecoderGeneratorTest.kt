package com.rescript.plugin.generate

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

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
        assertTrue(result!!.contains("decodeUser"), "Should contain decoder function name")
        assertTrue(result.contains("\"name\""), "Should decode name field")
        assertTrue(result.contains("\"age\""), "Should decode age field")
        assertTrue(result.contains("option<user>"), "Should return option type")
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
        assertTrue(result!!.contains("decodeColor"), "Should contain decoder function name")
        assertTrue(result.contains("String(\"Red\")"), "Should match Red string")
        assertTrue(result.contains("String(\"Green\")"), "Should match Green string")
        assertTrue(result.contains("String(\"Blue\")"), "Should match Blue string")
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
        assertTrue(result!!.contains("\"tag\""), "Should use tag-based decoding")
        assertTrue(result.contains("\"Circle\""), "Should decode Circle")
        assertTrue(result.contains("\"Rect\""), "Should decode Rect")
    }

    @Test
    fun generateDecoderForUnknownTypeReturnsNull() {
        val result = RescriptJsonDecoderGenerator.generateDecoder("foo", TypeShape.Unknown)
        assertNull(result, "Unknown type shape should return null")
    }

    @Test
    fun generateDecoderForEmptyRecordReturnsNull() {
        val shape = TypeShape.Record(emptyList())
        val result = RescriptJsonDecoderGenerator.generateDecoder("empty", shape)
        assertNull(result, "Empty record should return null")
    }

    @Test
    fun generateDecoderForEmptyVariantReturnsNull() {
        val shape = TypeShape.Variant(emptyList())
        val result = RescriptJsonDecoderGenerator.generateDecoder("empty", shape)
        assertNull(result, "Empty variant should return null")
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
        assertTrue(result!!.contains("let decode ="), "Type t should produce 'decode' function name")
    }

    // ── decodeFieldExpression ──

    @Test
    fun decodeFieldExpressionForStringType() {
        val expr = RescriptJsonDecoderGenerator.decodeFieldExpression(RescriptJsonType.StringType, "name")
        assertTrue(expr.contains("dict->Dict.get(\"name\")"), "Should access dict field")
        assertTrue(expr.contains("String(v)"), "Should decode string")
    }

    @Test
    fun decodeFieldExpressionForIntType() {
        val expr = RescriptJsonDecoderGenerator.decodeFieldExpression(RescriptJsonType.IntType, "age")
        assertTrue(expr.contains("Number(v)"), "Should decode int via Number")
        assertTrue(expr.contains("Int.fromFloat"), "Should convert to int")
    }

    @Test
    fun decodeFieldExpressionForOptionType() {
        val optionType = RescriptJsonType.OptionType(RescriptJsonType.StringType)
        val expr = RescriptJsonDecoderGenerator.decodeFieldExpression(optionType, "nickname")
        assertTrue(expr.contains("Null"), "Should handle Null for option")
        assertTrue(expr.contains("Some"), "Should handle Some wrapping")
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
        assertTrue(expr.contains("| Array(arr)"), "Should match Array pattern")
        assertTrue(expr.contains("Array.reduce"), "Should reduce array")
    }

    @Test
    fun decodeInlineExpressionForUnknownType() {
        val unknownType = RescriptJsonType.UnknownType("customType")
        val expr = RescriptJsonDecoderGenerator.decodeInlineExpression(unknownType)
        assertTrue(expr.contains("TODO"), "Should contain TODO comment")
        assertTrue(expr.contains("customType"), "Should reference the raw type")
        assertTrue(expr.contains("None"), "Should return None")
    }

    @Test
    fun decodeInlineExpressionForNestedOption() {
        val nestedOption = RescriptJsonType.OptionType(RescriptJsonType.IntType)
        val expr = RescriptJsonDecoderGenerator.decodeInlineExpression(nestedOption)
        assertTrue(expr.contains("Null"), "Should handle Null case")
        assertTrue(expr.contains("Number(v)"), "Should handle inner decode")
    }
}
