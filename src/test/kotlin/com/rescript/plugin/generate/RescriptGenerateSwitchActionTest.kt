package com.rescript.plugin.generate

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptGenerateSwitchActionTest {
    @Test
    fun `generates switch for simple variant`() {
        val constructors =
            listOf(
                VariantConstructor("Red", null),
                VariantConstructor("Green", null),
                VariantConstructor("Blue", null),
            )
        val result = RescriptGenerateSwitchAction.generateSwitchText(constructors)
        assertTrue(result.contains("switch value {"))
        assertTrue(result.contains("| Red => todo"))
        assertTrue(result.contains("| Green => todo"))
        assertTrue(result.contains("| Blue => todo"))
        assertTrue(result.endsWith("}"))
    }

    @Test
    fun `generates switch with payload wildcard`() {
        val constructors =
            listOf(
                VariantConstructor("Circle", "float"),
                VariantConstructor("Rectangle", "float, float"),
            )
        val result = RescriptGenerateSwitchAction.generateSwitchText(constructors)
        assertTrue(result.contains("| Circle(_) => todo"))
        assertTrue(result.contains("| Rectangle(_) => todo"))
    }

    @Test
    fun `generates switch with mixed constructors`() {
        val constructors =
            listOf(
                VariantConstructor("Ok", "string"),
                VariantConstructor("Error", null),
            )
        val result = RescriptGenerateSwitchAction.generateSwitchText(constructors)
        assertTrue(result.contains("| Ok(_) => todo"))
        assertTrue(result.contains("| Error => todo"))
    }

    @Test
    fun `generates switch for single constructor`() {
        val constructors =
            listOf(
                VariantConstructor("Value", "int"),
            )
        val result = RescriptGenerateSwitchAction.generateSwitchText(constructors)
        val expected =
            buildString {
                appendLine("switch value {")
                appendLine("| Value(_) => todo")
                append("}")
            }
        assertEquals(expected, result)
    }

    @Test
    fun `switch text starts with switch and ends with brace`() {
        val constructors =
            listOf(
                VariantConstructor("A", null),
                VariantConstructor("B", null),
            )
        val result = RescriptGenerateSwitchAction.generateSwitchText(constructors)
        assertTrue(result.startsWith("switch value {"))
        assertTrue(result.endsWith("}"))
    }

    @Test
    fun `end to end parse and generate`() {
        val typeDecl = "type color = Red | Green | Blue"
        val parsed = RescriptTypeDeclarationParser.parse(typeDecl)
        assertTrue(parsed is TypeShape.Variant)
        val variant = parsed as TypeShape.Variant
        val result = RescriptGenerateSwitchAction.generateSwitchText(variant.constructors)
        assertTrue(result.contains("| Red => todo"))
        assertTrue(result.contains("| Green => todo"))
        assertTrue(result.contains("| Blue => todo"))
    }
}
