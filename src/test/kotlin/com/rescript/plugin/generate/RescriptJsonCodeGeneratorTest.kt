package com.rescript.plugin.generate

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptJsonCodeGeneratorTest {
    // --- Encoder name ---

    @Test
    fun `encoderName capitalizes type name`() {
        assertEquals("encodeUser", RescriptJsonCodeGenerator.encoderName("user"))
    }

    @Test
    fun `encoderName for type t omits type name`() {
        assertEquals("encode", RescriptJsonCodeGenerator.encoderName("t"))
    }

    @Test
    fun `decoderName capitalizes type name`() {
        assertEquals("decodeUser", RescriptJsonCodeGenerator.decoderName("user"))
    }

    @Test
    fun `decoderName for type t omits type name`() {
        assertEquals("decode", RescriptJsonCodeGenerator.decoderName("t"))
    }

    // --- Encode expressions ---

    @Test
    fun `encodeExpression for string`() {
        assertEquals(
            "String(v.name)",
            RescriptJsonCodeGenerator.encodeExpression(RescriptJsonType.StringType, "v.name"),
        )
    }

    @Test
    fun `encodeExpression for int`() {
        assertEquals(
            "Number(v.age->Int.toFloat)",
            RescriptJsonCodeGenerator.encodeExpression(RescriptJsonType.IntType, "v.age"),
        )
    }

    @Test
    fun `encodeExpression for float`() {
        assertEquals(
            "Number(v.score)",
            RescriptJsonCodeGenerator.encodeExpression(RescriptJsonType.FloatType, "v.score"),
        )
    }

    @Test
    fun `encodeExpression for bool`() {
        assertEquals(
            "Boolean(v.active)",
            RescriptJsonCodeGenerator.encodeExpression(RescriptJsonType.BoolType, "v.active"),
        )
    }

    @Test
    fun `encodeExpression for option`() {
        val optString = RescriptJsonType.OptionType(RescriptJsonType.StringType)
        assertEquals(
            "v.email->Option.mapOr(Null, v => String(v))",
            RescriptJsonCodeGenerator.encodeExpression(optString, "v.email"),
        )
    }

    @Test
    fun `encodeExpression for array`() {
        val arrString = RescriptJsonType.ArrayType(RescriptJsonType.StringType)
        assertEquals(
            "Array(v.tags->Array.map(v => String(v)))",
            RescriptJsonCodeGenerator.encodeExpression(arrString, "v.tags"),
        )
    }

    @Test
    fun `encodeExpression for unknown type includes TODO`() {
        val unknown = RescriptJsonType.UnknownType("Date.t")
        val result = RescriptJsonCodeGenerator.encodeExpression(unknown, "v.date")
        assertTrue(result.contains("TODO"))
        assertTrue(result.contains("Date.t"))
    }

    @Test
    fun `encodeExpression for nested option of array`() {
        val optArr = RescriptJsonType.OptionType(RescriptJsonType.ArrayType(RescriptJsonType.IntType))
        val result = RescriptJsonCodeGenerator.encodeExpression(optArr, "v.ids")
        assertTrue(result.contains("Option.mapOr"))
        assertTrue(result.contains("Array.map"))
    }

    // --- Decode inline expressions ---

    @Test
    fun `decodeInlineExpression for string`() {
        val result = RescriptJsonCodeGenerator.decodeInlineExpression(RescriptJsonType.StringType)
        assertTrue(result.contains("String(v)"))
        assertTrue(result.contains("Some(v)"))
    }

    @Test
    fun `decodeInlineExpression for int`() {
        val result = RescriptJsonCodeGenerator.decodeInlineExpression(RescriptJsonType.IntType)
        assertTrue(result.contains("Number(v)"))
        assertTrue(result.contains("Int.fromFloat"))
    }

    @Test
    fun `decodeInlineExpression for float`() {
        val result = RescriptJsonCodeGenerator.decodeInlineExpression(RescriptJsonType.FloatType)
        assertTrue(result.contains("Number(v)"))
        assertTrue(result.contains("Some(v)"))
    }

    @Test
    fun `decodeInlineExpression for bool`() {
        val result = RescriptJsonCodeGenerator.decodeInlineExpression(RescriptJsonType.BoolType)
        assertTrue(result.contains("Boolean(v)"))
    }

    @Test
    fun `decodeInlineExpression for option includes Null handling`() {
        val optString = RescriptJsonType.OptionType(RescriptJsonType.StringType)
        val result = RescriptJsonCodeGenerator.decodeInlineExpression(optString)
        assertTrue(result.contains("Null"))
        assertTrue(result.contains("Some(None)"))
    }

    @Test
    fun `decodeInlineExpression for array includes reduce`() {
        val arrInt = RescriptJsonType.ArrayType(RescriptJsonType.IntType)
        val result = RescriptJsonCodeGenerator.decodeInlineExpression(arrInt)
        assertTrue(result.contains("Array(arr)"))
        assertTrue(result.contains("Array.reduce"))
    }

    @Test
    fun `decodeInlineExpression for unknown type includes TODO`() {
        val unknown = RescriptJsonType.UnknownType("customType")
        val result = RescriptJsonCodeGenerator.decodeInlineExpression(unknown)
        assertTrue(result.contains("TODO"))
        assertTrue(result.contains("customType"))
    }

    // --- Record encoder generation ---

    @Test
    fun `generateEncoder for simple record`() {
        val fields =
            listOf(
                RecordField("name", "string", false),
                RecordField("age", "int", false),
            )
        val shape = TypeShape.Record(fields)
        val result = RescriptJsonCodeGenerator.generateEncoder("user", shape)
        assertNotNull(result)
        assertTrue(result!!.contains("let encodeUser"))
        assertTrue(result.contains("v: user"))
        assertTrue(result.contains("JSON.t"))
        assertTrue(result.contains("(\"name\", String(v.name))"))
        assertTrue(result.contains("(\"age\", Number(v.age->Int.toFloat))"))
    }

    @Test
    fun `generateEncoder for record with option field`() {
        val fields =
            listOf(
                RecordField("name", "string", false),
                RecordField("email", "option<string>", false),
            )
        val shape = TypeShape.Record(fields)
        val result = RescriptJsonCodeGenerator.generateEncoder("user", shape)
        assertNotNull(result)
        assertTrue(result!!.contains("Option.mapOr"))
    }

    @Test
    fun `generateEncoder for empty record returns null`() {
        val shape = TypeShape.Record(emptyList())
        assertNull(RescriptJsonCodeGenerator.generateEncoder("empty", shape))
    }

    @Test
    fun `generateEncoder for type t uses encode function name`() {
        val fields = listOf(RecordField("x", "int", false))
        val shape = TypeShape.Record(fields)
        val result = RescriptJsonCodeGenerator.generateEncoder("t", shape)
        assertNotNull(result)
        assertTrue(result!!.contains("let encode ="))
        assertTrue(result.contains("v: t"))
    }

    @Test
    fun `generateEncoder returns null for Unknown shape`() {
        assertNull(RescriptJsonCodeGenerator.generateEncoder("foo", TypeShape.Unknown))
    }

    // --- Record decoder generation ---

    @Test
    fun `generateDecoder for simple record`() {
        val fields =
            listOf(
                RecordField("name", "string", false),
                RecordField("age", "int", false),
            )
        val shape = TypeShape.Record(fields)
        val result = RescriptJsonCodeGenerator.generateDecoder("user", shape)
        assertNotNull(result)
        assertTrue(result!!.contains("let decodeUser"))
        assertTrue(result.contains("option<user>"))
        assertTrue(result.contains("Object(dict)"))
        assertTrue(result.contains("Dict.get(\"name\")"))
        assertTrue(result.contains("Dict.get(\"age\")"))
        assertTrue(result.contains("Some({name: name, age: age})"))
    }

    @Test
    fun `generateDecoder for record with option field includes Null handling`() {
        val fields =
            listOf(
                RecordField("email", "option<string>", false),
            )
        val shape = TypeShape.Record(fields)
        val result = RescriptJsonCodeGenerator.generateDecoder("config", shape)
        assertNotNull(result)
        assertTrue(result!!.contains("Null"))
        assertTrue(result.contains("Some(None)"))
    }

    @Test
    fun `generateDecoder for empty record returns null`() {
        val shape = TypeShape.Record(emptyList())
        assertNull(RescriptJsonCodeGenerator.generateDecoder("empty", shape))
    }

    @Test
    fun `generateDecoder returns null for Unknown shape`() {
        assertNull(RescriptJsonCodeGenerator.generateDecoder("foo", TypeShape.Unknown))
    }

    // --- Variant encoder generation ---

    @Test
    fun `generateEncoder for simple enum variant`() {
        val constructors =
            listOf(
                VariantConstructor("Red", null),
                VariantConstructor("Green", null),
                VariantConstructor("Blue", null),
            )
        val shape = TypeShape.Variant(constructors)
        val result = RescriptJsonCodeGenerator.generateEncoder("color", shape)
        assertNotNull(result)
        assertTrue(result!!.contains("let encodeColor"))
        assertTrue(result.contains("String(\"Red\")"))
        assertTrue(result.contains("String(\"Green\")"))
        assertTrue(result.contains("String(\"Blue\")"))
    }

    @Test
    fun `generateEncoder for tagged union variant`() {
        val constructors =
            listOf(
                VariantConstructor("Success", "string"),
                VariantConstructor("Error", "int"),
            )
        val shape = TypeShape.Variant(constructors)
        val result = RescriptJsonCodeGenerator.generateEncoder("status", shape)
        assertNotNull(result)
        assertTrue(result!!.contains("let encodeStatus"))
        assertTrue(result.contains("(\"tag\", String(\"Success\"))"))
        assertTrue(result.contains("(\"tag\", String(\"Error\"))"))
        assertTrue(result.contains("(\"_0\","))
    }

    @Test
    fun `generateEncoder for variant with no-payload and payload constructors`() {
        val constructors =
            listOf(
                VariantConstructor("None", null),
                VariantConstructor("Some", "string"),
            )
        val shape = TypeShape.Variant(constructors)
        val result = RescriptJsonCodeGenerator.generateEncoder("myOption", shape)
        assertNotNull(result)
        // Mixed variant uses tagged union encoding
        assertTrue(result!!.contains("(\"tag\", String(\"None\"))"))
        assertTrue(result.contains("(\"tag\", String(\"Some\"))"))
    }

    @Test
    fun `generateEncoder for variant with multiple payloads`() {
        val constructors =
            listOf(
                VariantConstructor("Rect", "float, float"),
            )
        val shape = TypeShape.Variant(constructors)
        val result = RescriptJsonCodeGenerator.generateEncoder("shape", shape)
        assertNotNull(result)
        assertTrue(result!!.contains("(\"_0\","))
        assertTrue(result.contains("(\"_1\","))
    }

    @Test
    fun `generateEncoder for empty variant returns null`() {
        val shape = TypeShape.Variant(emptyList())
        assertNull(RescriptJsonCodeGenerator.generateEncoder("empty", shape))
    }

    // --- Variant decoder generation ---

    @Test
    fun `generateDecoder for simple enum variant`() {
        val constructors =
            listOf(
                VariantConstructor("Red", null),
                VariantConstructor("Green", null),
                VariantConstructor("Blue", null),
            )
        val shape = TypeShape.Variant(constructors)
        val result = RescriptJsonCodeGenerator.generateDecoder("color", shape)
        assertNotNull(result)
        assertTrue(result!!.contains("let decodeColor"))
        assertTrue(result.contains("option<color>"))
        assertTrue(result.contains("String(\"Red\")"))
        assertTrue(result.contains("Some(Red)"))
    }

    @Test
    fun `generateDecoder for tagged union variant`() {
        val constructors =
            listOf(
                VariantConstructor("Success", "string"),
                VariantConstructor("Error", "int"),
            )
        val shape = TypeShape.Variant(constructors)
        val result = RescriptJsonCodeGenerator.generateDecoder("status", shape)
        assertNotNull(result)
        assertTrue(result!!.contains("let decodeStatus"))
        assertTrue(result.contains("Dict.get(\"tag\")"))
        assertTrue(result.contains("String(\"Success\")"))
        assertTrue(result.contains("String(\"Error\")"))
    }

    @Test
    fun `generateDecoder for empty variant returns null`() {
        val shape = TypeShape.Variant(emptyList())
        assertNull(RescriptJsonCodeGenerator.generateDecoder("empty", shape))
    }

    // --- generateBoth ---

    @Test
    fun `generateBoth returns both encoder and decoder`() {
        val fields =
            listOf(
                RecordField("name", "string", false),
                RecordField("age", "int", false),
            )
        val shape = TypeShape.Record(fields)
        val result = RescriptJsonCodeGenerator.generateBoth("user", shape)
        assertNotNull(result)
        assertTrue(result!!.contains("let encodeUser"))
        assertTrue(result.contains("let decodeUser"))
    }

    @Test
    fun `generateBoth returns null for empty record`() {
        val shape = TypeShape.Record(emptyList())
        assertNull(RescriptJsonCodeGenerator.generateBoth("empty", shape))
    }

    @Test
    fun `generateBoth returns null for Unknown shape`() {
        assertNull(RescriptJsonCodeGenerator.generateBoth("foo", TypeShape.Unknown))
    }

    // --- splitPayloadTypes ---

    @Test
    fun `splitPayloadTypes single type`() {
        assertEquals(listOf("string"), RescriptJsonCodeGenerator.splitPayloadTypes("string"))
    }

    @Test
    fun `splitPayloadTypes multiple types`() {
        assertEquals(
            listOf("string", "int"),
            RescriptJsonCodeGenerator.splitPayloadTypes("string, int"),
        )
    }

    @Test
    fun `splitPayloadTypes with generic types`() {
        assertEquals(
            listOf("string", "array<int>"),
            RescriptJsonCodeGenerator.splitPayloadTypes("string, array<int>"),
        )
    }

    @Test
    fun `splitPayloadTypes with nested generics`() {
        assertEquals(
            listOf("option<array<string>>", "int"),
            RescriptJsonCodeGenerator.splitPayloadTypes("option<array<string>>, int"),
        )
    }

    @Test
    fun `splitPayloadTypes empty string`() {
        assertEquals(emptyList<String>(), RescriptJsonCodeGenerator.splitPayloadTypes(""))
    }

    // --- decodeFieldExpression ---

    @Test
    fun `decodeFieldExpression for string`() {
        val result = RescriptJsonCodeGenerator.decodeFieldExpression(RescriptJsonType.StringType, "name")
        assertTrue(result.contains("Dict.get(\"name\")"))
        assertTrue(result.contains("String(v)"))
    }

    @Test
    fun `decodeFieldExpression for option includes Null case`() {
        val optString = RescriptJsonType.OptionType(RescriptJsonType.StringType)
        val result = RescriptJsonCodeGenerator.decodeFieldExpression(optString, "email")
        assertTrue(result.contains("Null"))
        assertTrue(result.contains("Some(None)"))
    }

    // --- End-to-end: parse and generate ---

    @Test
    fun `end-to-end record parse and generate`() {
        val declText = "type user = {name: string, age: int, email: option<string>}"
        val shape = RescriptTypeDeclarationParser.parse(declText)
        val typeName = RescriptTypeDeclarationParser.extractTypeName(declText)!!

        val result = RescriptJsonCodeGenerator.generateBoth(typeName, shape)
        assertNotNull(result)
        assertTrue(result!!.contains("let encodeUser"))
        assertTrue(result.contains("let decodeUser"))
    }

    @Test
    fun `end-to-end simple enum variant parse and generate`() {
        val declText = "type color = Red | Green | Blue"
        val shape = RescriptTypeDeclarationParser.parse(declText)
        val typeName = RescriptTypeDeclarationParser.extractTypeName(declText)!!

        val result = RescriptJsonCodeGenerator.generateBoth(typeName, shape)
        assertNotNull(result)
        assertTrue(result!!.contains("let encodeColor"))
        assertTrue(result.contains("let decodeColor"))
        assertTrue(result.contains("String(\"Red\")"))
    }

    @Test
    fun `end-to-end tagged union variant parse and generate`() {
        val declText = "type status = Success(string) | Error(int)"
        val shape = RescriptTypeDeclarationParser.parse(declText)
        val typeName = RescriptTypeDeclarationParser.extractTypeName(declText)!!

        val result = RescriptJsonCodeGenerator.generateBoth(typeName, shape)
        assertNotNull(result)
        assertTrue(result!!.contains("let encodeStatus"))
        assertTrue(result.contains("let decodeStatus"))
        assertTrue(result.contains("tag"))
    }
}
