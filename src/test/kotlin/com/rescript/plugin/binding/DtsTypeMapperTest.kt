package com.rescript.plugin.binding

import com.rescript.plugin.binding.DtsJsonModel.ArrayType
import com.rescript.plugin.binding.DtsJsonModel.FunctionType
import com.rescript.plugin.binding.DtsJsonModel.IndexSignatureType
import com.rescript.plugin.binding.DtsJsonModel.IntersectionType
import com.rescript.plugin.binding.DtsJsonModel.NumericLiteralType
import com.rescript.plugin.binding.DtsJsonModel.ObjectLiteralType
import com.rescript.plugin.binding.DtsJsonModel.Parameter
import com.rescript.plugin.binding.DtsJsonModel.PrimitiveType
import com.rescript.plugin.binding.DtsJsonModel.ReferenceType
import com.rescript.plugin.binding.DtsJsonModel.StringLiteralType
import com.rescript.plugin.binding.DtsJsonModel.TupleType
import com.rescript.plugin.binding.DtsJsonModel.UnionType
import com.rescript.plugin.binding.DtsJsonModel.UnknownType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DtsTypeMapperTest {
    // ── Primitive mappings ────────────────────────────────────────────

    @Test
    fun `map string`() {
        assertEquals("string", DtsTypeMapper.mapType(PrimitiveType("string")))
    }

    @Test
    fun `map number`() {
        assertEquals("float", DtsTypeMapper.mapType(PrimitiveType("number")))
    }

    @Test
    fun `map boolean`() {
        assertEquals("bool", DtsTypeMapper.mapType(PrimitiveType("boolean")))
    }

    @Test
    fun `map void`() {
        assertEquals("unit", DtsTypeMapper.mapType(PrimitiveType("void")))
    }

    @Test
    fun `map undefined`() {
        assertEquals("unit", DtsTypeMapper.mapType(PrimitiveType("undefined")))
    }

    @Test
    fun `map any`() {
        assertEquals("JSON.t", DtsTypeMapper.mapType(PrimitiveType("any")))
    }

    @Test
    fun `map unknown`() {
        assertEquals("JSON.t", DtsTypeMapper.mapType(PrimitiveType("unknown")))
    }

    @Test
    fun `map never`() {
        assertEquals("unit", DtsTypeMapper.mapType(PrimitiveType("never")))
    }

    @Test
    fun `map symbol`() {
        assertEquals("Symbol.t", DtsTypeMapper.mapType(PrimitiveType("symbol")))
    }

    @Test
    fun `map bigint`() {
        assertEquals("BigInt.t", DtsTypeMapper.mapType(PrimitiveType("bigint")))
    }

    @Test
    fun `map object`() {
        assertEquals("JSON.t", DtsTypeMapper.mapType(PrimitiveType("object")))
    }

    @Test
    fun `map unrecognized primitive`() {
        assertEquals("JSON.t", DtsTypeMapper.mapType(PrimitiveType("something")))
    }

    // ── Array ─────────────────────────────────────────────────────────

    @Test
    fun `map array type`() {
        assertEquals("array<string>", DtsTypeMapper.mapType(ArrayType(PrimitiveType("string"))))
    }

    @Test
    fun `map nested array type`() {
        assertEquals(
            "array<array<float>>",
            DtsTypeMapper.mapType(ArrayType(ArrayType(PrimitiveType("number")))),
        )
    }

    // ── Tuple ─────────────────────────────────────────────────────────

    @Test
    fun `map tuple type`() {
        val tuple = TupleType(listOf(PrimitiveType("string"), PrimitiveType("number")))
        assertEquals("(string, float)", DtsTypeMapper.mapType(tuple))
    }

    @Test
    fun `map empty tuple`() {
        assertEquals("unit", DtsTypeMapper.mapType(TupleType(emptyList())))
    }

    @Test
    fun `map single element tuple`() {
        assertEquals("(string)", DtsTypeMapper.mapType(TupleType(listOf(PrimitiveType("string")))))
    }

    // ── Function ──────────────────────────────────────────────────────

    @Test
    fun `map function type`() {
        val fn =
            FunctionType(
                parameters = listOf(Parameter("x", PrimitiveType("number"))),
                returnType = PrimitiveType("string"),
            )
        assertEquals("float => string", DtsTypeMapper.mapType(fn))
    }

    @Test
    fun `map function with no params`() {
        val fn = FunctionType(parameters = emptyList(), returnType = PrimitiveType("void"))
        assertEquals("unit => unit", DtsTypeMapper.mapType(fn))
    }

    @Test
    fun `map function with multiple params`() {
        val fn =
            FunctionType(
                parameters =
                    listOf(
                        Parameter("a", PrimitiveType("string")),
                        Parameter("b", PrimitiveType("number")),
                    ),
                returnType = PrimitiveType("boolean"),
            )
        assertEquals("(~a: string), (~b: float) => bool", DtsTypeMapper.mapType(fn))
    }

    // ── Reference types ───────────────────────────────────────────────

    @Test
    fun `map Promise reference`() {
        val ref = ReferenceType("Promise", listOf(PrimitiveType("string")))
        assertEquals("promise<string>", DtsTypeMapper.mapType(ref))
    }

    @Test
    fun `map Date reference`() {
        assertEquals("Date.t", DtsTypeMapper.mapType(ReferenceType("Date")))
    }

    @Test
    fun `map RegExp reference`() {
        assertEquals("RegExp.t", DtsTypeMapper.mapType(ReferenceType("RegExp")))
    }

    @Test
    fun `map Map reference`() {
        val ref = ReferenceType("Map", listOf(PrimitiveType("string"), PrimitiveType("number")))
        assertEquals("Map.t<string, float>", DtsTypeMapper.mapType(ref))
    }

    @Test
    fun `map Set reference`() {
        val ref = ReferenceType("Set", listOf(PrimitiveType("string")))
        assertEquals("Set.t<string>", DtsTypeMapper.mapType(ref))
    }

    @Test
    fun `map Error reference`() {
        assertEquals("Exn.t", DtsTypeMapper.mapType(ReferenceType("Error")))
    }

    @Test
    fun `map Record reference`() {
        val ref = ReferenceType("Record", listOf(PrimitiveType("string"), PrimitiveType("number")))
        assertEquals("Dict.t<float>", DtsTypeMapper.mapType(ref))
    }

    @Test
    fun `map Array reference with type argument`() {
        val ref = ReferenceType("Array", listOf(PrimitiveType("number")))
        assertEquals("array<float>", DtsTypeMapper.mapType(ref))
    }

    @Test
    fun `map custom reference without type args`() {
        assertEquals("MyLib.t", DtsTypeMapper.mapType(ReferenceType("MyLib")))
    }

    @Test
    fun `map custom reference with type args`() {
        val ref = ReferenceType("Container", listOf(PrimitiveType("string")))
        assertEquals("Container.t<string>", DtsTypeMapper.mapType(ref))
    }

    @Test
    fun `map Partial reference`() {
        val ref = ReferenceType("Partial", listOf(ReferenceType("MyType")))
        assertEquals("MyType.t", DtsTypeMapper.mapType(ref))
    }

    // ── Union types ───────────────────────────────────────────────────

    @Test
    fun `map nullable union`() {
        val union = UnionType(listOf(PrimitiveType("string"), PrimitiveType("null")))
        assertEquals("Nullable.t<string>", DtsTypeMapper.mapType(union))
    }

    @Test
    fun `map optional union`() {
        val union = UnionType(listOf(PrimitiveType("number"), PrimitiveType("undefined")))
        assertEquals("option<float>", DtsTypeMapper.mapType(union))
    }

    @Test
    fun `map nullable and undefined union`() {
        val union =
            UnionType(
                listOf(PrimitiveType("string"), PrimitiveType("null"), PrimitiveType("undefined")),
            )
        assertEquals("option<Nullable.t<string>>", DtsTypeMapper.mapType(union))
    }

    @Test
    fun `map string literal union`() {
        val union =
            UnionType(
                listOf(
                    StringLiteralType("a"),
                    StringLiteralType("b"),
                    StringLiteralType("c"),
                ),
            )
        assertEquals("[#\"a\" | #\"b\" | #\"c\"]", DtsTypeMapper.mapType(union))
    }

    @Test
    fun `map general union falls back to JSON_t`() {
        val union = UnionType(listOf(PrimitiveType("string"), PrimitiveType("number")))
        val result = DtsTypeMapper.mapType(union)
        assertTrue(result.contains("JSON.t"))
        assertTrue(result.contains("string"))
        assertTrue(result.contains("number"))
    }

    // ── Intersection ──────────────────────────────────────────────────

    @Test
    fun `map intersection type`() {
        val intersection = IntersectionType(listOf(ReferenceType("A"), ReferenceType("B")))
        val result = DtsTypeMapper.mapType(intersection)
        assertTrue(result.contains("TODO: intersection"))
        assertTrue(result.contains("JSON.t"))
    }

    // ── Object literal ────────────────────────────────────────────────

    @Test
    fun `map object literal`() {
        val obj =
            ObjectLiteralType(
                listOf(
                    DtsJsonModel.Member("name", PrimitiveType("string")),
                    DtsJsonModel.Member("age", PrimitiveType("number"), optional = true),
                ),
            )
        assertEquals("{name: string, age?: float}", DtsTypeMapper.mapType(obj))
    }

    @Test
    fun `map empty object literal`() {
        assertEquals("JSON.t", DtsTypeMapper.mapType(ObjectLiteralType(emptyList())))
    }

    // ── String literal ────────────────────────────────────────────────

    @Test
    fun `map string literal`() {
        assertEquals("[#\"hello\"]", DtsTypeMapper.mapType(StringLiteralType("hello")))
    }

    // ── Numeric literal ───────────────────────────────────────────────

    @Test
    fun `map numeric literal`() {
        assertEquals("42", DtsTypeMapper.mapType(NumericLiteralType("42")))
    }

    // ── Index signature ───────────────────────────────────────────────

    @Test
    fun `map index signature`() {
        val idx = IndexSignatureType(PrimitiveType("string"), PrimitiveType("number"))
        assertEquals("Dict.t<float>", DtsTypeMapper.mapType(idx))
    }

    // ── Unknown type ──────────────────────────────────────────────────

    @Test
    fun `map unknown type with text`() {
        val result = DtsTypeMapper.mapType(UnknownType("SomeComplexType"))
        assertTrue(result.contains("SomeComplexType"))
        assertTrue(result.contains("JSON.t"))
    }

    @Test
    fun `map unknown type without text`() {
        assertEquals("JSON.t", DtsTypeMapper.mapType(UnknownType()))
    }

    // ── Parameters ────────────────────────────────────────────────────

    @Test
    fun `mapParameters with empty list`() {
        assertEquals("unit", DtsTypeMapper.mapParameters(emptyList()))
    }

    @Test
    fun `mapParameters with single param`() {
        val params = listOf(Parameter("x", PrimitiveType("number")))
        assertEquals("float", DtsTypeMapper.mapParameters(params))
    }

    @Test
    fun `mapParameters with optional param`() {
        val params = listOf(Parameter("x", PrimitiveType("number"), optional = true))
        assertEquals("(~x: float=?)", DtsTypeMapper.mapParameters(params))
    }

    @Test
    fun `mapParameters with multiple params`() {
        val params =
            listOf(
                Parameter("a", PrimitiveType("string")),
                Parameter("b", PrimitiveType("number")),
            )
        assertEquals("(~a: string), (~b: float)", DtsTypeMapper.mapParameters(params))
    }

    // ── Utility ───────────────────────────────────────────────────────

    @Test
    fun `isStringLiteralUnion returns true for all string literals`() {
        val union = UnionType(listOf(StringLiteralType("a"), StringLiteralType("b")))
        assertTrue(DtsTypeMapper.isStringLiteralUnion(union))
    }

    @Test
    fun `isStringLiteralUnion returns false for mixed types`() {
        val union = UnionType(listOf(StringLiteralType("a"), PrimitiveType("number")))
        assertFalse(DtsTypeMapper.isStringLiteralUnion(union))
    }

    @Test
    fun `toReScriptIdentifier escapes keywords`() {
        assertEquals("\\\"type\\\"", DtsTypeMapper.toReScriptIdentifier("type"))
        assertEquals("\\\"let\\\"", DtsTypeMapper.toReScriptIdentifier("let"))
        assertEquals("\\\"module\\\"", DtsTypeMapper.toReScriptIdentifier("module"))
    }

    @Test
    fun `toReScriptIdentifier passes through non-keywords`() {
        assertEquals("name", DtsTypeMapper.toReScriptIdentifier("name"))
        assertEquals("myFunc", DtsTypeMapper.toReScriptIdentifier("myFunc"))
    }

    @Test
    fun `toModuleName capitalizes first letter`() {
        assertEquals("Foo", DtsTypeMapper.toModuleName("foo"))
        assertEquals("Already", DtsTypeMapper.toModuleName("Already"))
    }
}
