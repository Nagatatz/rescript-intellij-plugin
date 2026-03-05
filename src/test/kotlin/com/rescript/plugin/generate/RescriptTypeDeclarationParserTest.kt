package com.rescript.plugin.generate

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptTypeDeclarationParserTest {
    // --- parse: Variant ---

    @Test
    fun `parse variant with pipe separators`() {
        val result = RescriptTypeDeclarationParser.parse("type color = | Red | Green | Blue")
        assertTrue(result is TypeShape.Variant)
        val variant = result as TypeShape.Variant
        assertEquals(3, variant.constructors.size)
        assertEquals("Red", variant.constructors[0].name)
        assertEquals("Green", variant.constructors[1].name)
        assertEquals("Blue", variant.constructors[2].name)
    }

    @Test
    fun `parse variant with payload`() {
        val result = RescriptTypeDeclarationParser.parse("type option = | Some(int) | None")
        assertTrue(result is TypeShape.Variant)
        val variant = result as TypeShape.Variant
        assertEquals(2, variant.constructors.size)
        assertEquals("Some", variant.constructors[0].name)
        assertEquals("int", variant.constructors[0].payload)
        assertEquals("None", variant.constructors[1].name)
        assertNull(variant.constructors[1].payload)
    }

    @Test
    fun `parse variant without leading pipe`() {
        val result = RescriptTypeDeclarationParser.parse("type t = A | B")
        assertTrue(result is TypeShape.Variant)
        val variant = result as TypeShape.Variant
        assertEquals(2, variant.constructors.size)
    }

    @Test
    fun `parse single constructor variant`() {
        val result = RescriptTypeDeclarationParser.parse("type t = Foo")
        assertTrue(result is TypeShape.Variant)
        val variant = result as TypeShape.Variant
        assertEquals(1, variant.constructors.size)
        assertEquals("Foo", variant.constructors[0].name)
    }

    // --- parse: Record ---

    @Test
    fun `parse record with two fields`() {
        val result = RescriptTypeDeclarationParser.parse("type person = {name: string, age: int}")
        assertTrue(result is TypeShape.Record)
        val record = result as TypeShape.Record
        assertEquals(2, record.fields.size)
        assertEquals("name", record.fields[0].name)
        assertEquals("string", record.fields[0].typeAnnotation)
        assertFalse(record.fields[0].isMutable)
        assertEquals("age", record.fields[1].name)
        assertEquals("int", record.fields[1].typeAnnotation)
    }

    @Test
    fun `parse record with mutable field`() {
        val result = RescriptTypeDeclarationParser.parse("type counter = {mutable count: int}")
        assertTrue(result is TypeShape.Record)
        val record = result as TypeShape.Record
        assertEquals(1, record.fields.size)
        assertTrue(record.fields[0].isMutable)
        assertEquals("count", record.fields[0].name)
        assertEquals("int", record.fields[0].typeAnnotation)
    }

    @Test
    fun `parse empty record`() {
        val result = RescriptTypeDeclarationParser.parse("type t = {}")
        assertTrue(result is TypeShape.Record)
        val record = result as TypeShape.Record
        assertEquals(0, record.fields.size)
    }

    // --- parse: Unknown ---

    @Test
    fun `parse empty body returns Unknown`() {
        val result = RescriptTypeDeclarationParser.parse("type t =   ")
        assertEquals(TypeShape.Unknown, result)
    }

    @Test
    fun `parse no equals sign returns Unknown`() {
        val result = RescriptTypeDeclarationParser.parse("type t")
        assertEquals(TypeShape.Unknown, result)
    }

    @Test
    fun `parse lowercase body returns Unknown`() {
        val result = RescriptTypeDeclarationParser.parse("type t = int")
        assertEquals(TypeShape.Unknown, result)
    }

    // --- extractTypeName ---

    @Test
    fun `extractTypeName returns type name`() {
        assertEquals("foo", RescriptTypeDeclarationParser.extractTypeName("type foo = int"))
    }

    @Test
    fun `extractTypeName with extra spaces`() {
        assertEquals("bar", RescriptTypeDeclarationParser.extractTypeName("  type   bar = string"))
    }

    @Test
    fun `extractTypeName returns null for non-type`() {
        assertNull(RescriptTypeDeclarationParser.extractTypeName("let x = 1"))
    }

    @Test
    fun `extractTypeName returns null for empty string`() {
        assertNull(RescriptTypeDeclarationParser.extractTypeName(""))
    }

    // --- Additional parse edge cases ---

    @Test
    fun `parse variant with tuple payload`() {
        val result = RescriptTypeDeclarationParser.parse("type shape = | Circle(float) | Rect(float, float)")
        assertTrue(result is TypeShape.Variant)
        val variant = result as TypeShape.Variant
        assertEquals(2, variant.constructors.size)
        assertEquals("float", variant.constructors[0].payload)
        assertEquals("float, float", variant.constructors[1].payload)
    }

    @Test
    fun `parse variant with only pipes returns Unknown`() {
        val result = RescriptTypeDeclarationParser.parse("type t = | | |")
        // Empty constructor names after split will be filtered, resulting in no constructors
        assertEquals(TypeShape.Unknown, result)
    }

    @Test
    fun `parse record with newline-separated fields`() {
        val result = RescriptTypeDeclarationParser.parse("type t = {\n  name: string\n  age: int\n}")
        assertTrue(result is TypeShape.Record)
        val record = result as TypeShape.Record
        assertEquals(2, record.fields.size)
        assertEquals("name", record.fields[0].name)
        assertEquals("age", record.fields[1].name)
    }

    @Test
    fun `parse record with mutable and immutable fields`() {
        val result = RescriptTypeDeclarationParser.parse("type t = {name: string, mutable count: int}")
        assertTrue(result is TypeShape.Record)
        val record = result as TypeShape.Record
        assertEquals(2, record.fields.size)
        assertFalse(record.fields[0].isMutable)
        assertTrue(record.fields[1].isMutable)
    }

    // --- Data class tests ---

    @Test
    fun `VariantConstructor data class equality`() {
        val a = VariantConstructor("Foo", "int")
        val b = VariantConstructor("Foo", "int")
        assertEquals(a, b)
    }

    @Test
    fun `VariantConstructor data class with null payload`() {
        val c = VariantConstructor("Bar", null)
        assertEquals("Bar", c.name)
        assertNull(c.payload)
    }

    @Test
    fun `RecordField data class equality`() {
        val a = RecordField("name", "string", false)
        val b = RecordField("name", "string", false)
        assertEquals(a, b)
    }

    @Test
    fun `RecordField data class with mutable`() {
        val f = RecordField("count", "int", true)
        assertTrue(f.isMutable)
    }

    @Test
    fun `TypeShape Variant data class equality`() {
        val a = TypeShape.Variant(listOf(VariantConstructor("A", null)))
        val b = TypeShape.Variant(listOf(VariantConstructor("A", null)))
        assertEquals(a, b)
    }

    @Test
    fun `TypeShape Record data class equality`() {
        val a = TypeShape.Record(listOf(RecordField("x", "int", false)))
        val b = TypeShape.Record(listOf(RecordField("x", "int", false)))
        assertEquals(a, b)
    }

    @Test
    fun `TypeShape Unknown is singleton`() {
        assertEquals(TypeShape.Unknown, TypeShape.Unknown)
    }

    @Test
    fun `parse variant with trailing whitespace`() {
        val result = RescriptTypeDeclarationParser.parse("type t = A  |  B  ")
        assertTrue(result is TypeShape.Variant)
        val variant = result as TypeShape.Variant
        assertEquals(2, variant.constructors.size)
    }

    @Test
    fun `extractTypeName with type keyword at start`() {
        assertEquals("t", RescriptTypeDeclarationParser.extractTypeName("type t = int"))
    }
}
