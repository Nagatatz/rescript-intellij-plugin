package com.rescript.plugin.generate

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptGenerateModuleTypeActionTest {
    @Test
    fun `generates module type with let declarations`() {
        val declarations =
            listOf(
                RescriptGenerateModuleTypeAction.Companion.Declaration("let", "x"),
                RescriptGenerateModuleTypeAction.Companion.Declaration("let", "y"),
            )
        val result = RescriptGenerateModuleTypeAction.generateModuleTypeText("Foo", declarations)
        assertTrue(result.contains("module type FooType = {"))
        assertTrue(result.contains("let x: 'a"))
        assertTrue(result.contains("let y: 'a"))
        assertTrue(result.endsWith("}"))
    }

    @Test
    fun `generates module type with type declarations`() {
        val declarations =
            listOf(
                RescriptGenerateModuleTypeAction.Companion.Declaration("type", "t"),
                RescriptGenerateModuleTypeAction.Companion.Declaration("type", "config"),
            )
        val result = RescriptGenerateModuleTypeAction.generateModuleTypeText("Foo", declarations)
        assertTrue(result.contains("type t"))
        assertTrue(result.contains("type config"))
    }

    @Test
    fun `generates module type with module declarations`() {
        val declarations =
            listOf(
                RescriptGenerateModuleTypeAction.Companion.Declaration("module", "Sub"),
            )
        val result = RescriptGenerateModuleTypeAction.generateModuleTypeText("Foo", declarations)
        assertTrue(result.contains("module Sub: {}"))
    }

    @Test
    fun `generates module type with mixed declarations`() {
        val declarations =
            listOf(
                RescriptGenerateModuleTypeAction.Companion.Declaration("type", "t"),
                RescriptGenerateModuleTypeAction.Companion.Declaration("let", "make"),
                RescriptGenerateModuleTypeAction.Companion.Declaration("let", "toString"),
                RescriptGenerateModuleTypeAction.Companion.Declaration("module", "Internal"),
            )
        val result = RescriptGenerateModuleTypeAction.generateModuleTypeText("MyModule", declarations)
        assertTrue(result.contains("module type MyModuleType = {"))
        assertTrue(result.contains("type t"))
        assertTrue(result.contains("let make: 'a"))
        assertTrue(result.contains("let toString: 'a"))
        assertTrue(result.contains("module Internal: {}"))
    }

    @Test
    fun `generates empty module type for no declarations`() {
        val declarations = emptyList<RescriptGenerateModuleTypeAction.Companion.Declaration>()
        val result = RescriptGenerateModuleTypeAction.generateModuleTypeText("Empty", declarations)
        val expected =
            buildString {
                appendLine("module type EmptyType = {")
                append("}")
            }
        assertEquals(expected, result)
    }

    @Test
    fun `module type name appends Type suffix`() {
        val result =
            RescriptGenerateModuleTypeAction.generateModuleTypeText(
                "Parser",
                emptyList(),
            )
        assertTrue(result.contains("module type ParserType = {"))
    }

    @Test
    fun `Declaration data class holds kind and name`() {
        val decl = RescriptGenerateModuleTypeAction.Companion.Declaration("let", "x")
        assertEquals("let", decl.kind)
        assertEquals("x", decl.name)
    }
}
