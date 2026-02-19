package com.rescript.plugin.generate

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptTypeDeclarationParserTest {
    @Test
    fun `parse simple variant`() {
        val result = RescriptTypeDeclarationParser.parse("type color = Red | Green | Blue")
        assertTrue(result is TypeShape.Variant)
        val variant = result as TypeShape.Variant
        assertEquals(3, variant.constructors.size)
        assertEquals("Red", variant.constructors[0].name)
        assertNull(variant.constructors[0].payload)
        assertEquals("Green", variant.constructors[1].name)
        assertEquals("Blue", variant.constructors[2].name)
    }

    @Test
    fun `parse variant with payloads`() {
        val result = RescriptTypeDeclarationParser.parse("type shape = Circle(float) | Rectangle(float, float)")
        assertTrue(result is TypeShape.Variant)
        val variant = result as TypeShape.Variant
        assertEquals(2, variant.constructors.size)
        assertEquals("Circle", variant.constructors[0].name)
        assertEquals("float", variant.constructors[0].payload)
        assertEquals("Rectangle", variant.constructors[1].name)
        assertEquals("float, float", variant.constructors[1].payload)
    }

    @Test
    fun `parse variant with leading pipe`() {
        val result =
            RescriptTypeDeclarationParser.parse(
                """type color =
                  | Red
                  | Green
                  | Blue""",
            )
        assertTrue(result is TypeShape.Variant)
        val variant = result as TypeShape.Variant
        assertEquals(3, variant.constructors.size)
    }

    @Test
    fun `parse variant starting with uppercase no pipe`() {
        val result = RescriptTypeDeclarationParser.parse("type t = Some(int)")
        assertTrue(result is TypeShape.Variant)
        val variant = result as TypeShape.Variant
        assertEquals(1, variant.constructors.size)
        assertEquals("Some", variant.constructors[0].name)
        assertEquals("int", variant.constructors[0].payload)
    }

    @Test
    fun `parse record`() {
        val result = RescriptTypeDeclarationParser.parse("type user = {name: string, age: int}")
        assertTrue(result is TypeShape.Record)
        val record = result as TypeShape.Record
        assertEquals(2, record.fields.size)
        assertEquals("name", record.fields[0].name)
        assertEquals("string", record.fields[0].typeAnnotation)
        assertEquals(false, record.fields[0].isMutable)
        assertEquals("age", record.fields[1].name)
        assertEquals("int", record.fields[1].typeAnnotation)
    }

    @Test
    fun `parse record with mutable field`() {
        val result = RescriptTypeDeclarationParser.parse("type t = {mutable count: int}")
        assertTrue(result is TypeShape.Record)
        val record = result as TypeShape.Record
        assertEquals(1, record.fields.size)
        assertEquals("count", record.fields[0].name)
        assertEquals("int", record.fields[0].typeAnnotation)
        assertEquals(true, record.fields[0].isMutable)
    }

    @Test
    fun `parse empty record`() {
        val result = RescriptTypeDeclarationParser.parse("type t = {}")
        assertTrue(result is TypeShape.Record)
        val record = result as TypeShape.Record
        assertEquals(0, record.fields.size)
    }

    @Test
    fun `parse unknown type alias`() {
        val result = RescriptTypeDeclarationParser.parse("type t = string")
        assertTrue(result is TypeShape.Unknown)
    }

    @Test
    fun `parse returns Unknown for no equals sign`() {
        val result = RescriptTypeDeclarationParser.parse("type t")
        assertTrue(result is TypeShape.Unknown)
    }

    @Test
    fun `extractTypeName returns name`() {
        assertEquals("color", RescriptTypeDeclarationParser.extractTypeName("type color = Red | Green"))
    }

    @Test
    fun `extractTypeName returns name with spaces`() {
        assertEquals("user", RescriptTypeDeclarationParser.extractTypeName("  type user = {name: string}"))
    }

    @Test
    fun `extractTypeName returns null for invalid`() {
        assertNull(RescriptTypeDeclarationParser.extractTypeName("let x = 1"))
    }

    @Test
    fun `parse mixed variant with payload and without`() {
        val result = RescriptTypeDeclarationParser.parse("type result = Ok(string) | Error")
        assertTrue(result is TypeShape.Variant)
        val variant = result as TypeShape.Variant
        assertEquals(2, variant.constructors.size)
        assertEquals("Ok", variant.constructors[0].name)
        assertEquals("string", variant.constructors[0].payload)
        assertEquals("Error", variant.constructors[1].name)
        assertNull(variant.constructors[1].payload)
    }

    @Test
    fun `parse record with multiple fields and spaces`() {
        val result = RescriptTypeDeclarationParser.parse("type config = { host: string, port: int, debug: bool }")
        assertTrue(result is TypeShape.Record)
        val record = result as TypeShape.Record
        assertEquals(3, record.fields.size)
        assertEquals("host", record.fields[0].name)
        assertEquals("port", record.fields[1].name)
        assertEquals("debug", record.fields[2].name)
    }
}
