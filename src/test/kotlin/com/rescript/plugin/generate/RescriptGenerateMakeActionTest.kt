package com.rescript.plugin.generate

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** Tests for [RescriptGenerateMakeAction] static generation logic. */
class RescriptGenerateMakeActionTest {
    @Test
    fun `generateMakeFunction with simple fields`() {
        val fields =
            listOf(
                RecordField("name", "string", false),
                RecordField("age", "int", false),
            )
        val result = RescriptGenerateMakeAction.generateMakeFunction("person", fields)
        assertEquals("let make = (~name, ~age): person => {name: name, age: age}", result)
    }

    @Test
    fun `generateMakeFunction with optional fields`() {
        val fields =
            listOf(
                RecordField("name", "string", false),
                RecordField("email", "option<string>", false),
            )
        val result = RescriptGenerateMakeAction.generateMakeFunction("user", fields)
        assertEquals("let make = (~name, ~email=?): user => {name: name, email: email}", result)
    }

    @Test
    fun `generateMakeFunction with type t omits return type`() {
        val fields =
            listOf(
                RecordField("x", "int", false),
                RecordField("y", "int", false),
            )
        val result = RescriptGenerateMakeAction.generateMakeFunction("t", fields)
        assertEquals("let make = (~x, ~y) => {x: x, y: y}", result)
    }

    @Test
    fun `generateMakeFunction with mutable field`() {
        val fields =
            listOf(
                RecordField("count", "int", true),
            )
        val result = RescriptGenerateMakeAction.generateMakeFunction("counter", fields)
        assertEquals("let make = (~count): counter => {count: count}", result)
    }

    @Test
    fun `generateMakeFunction with all optional fields`() {
        val fields =
            listOf(
                RecordField("a", "option<int>", false),
                RecordField("b", "option<string>", false),
            )
        val result = RescriptGenerateMakeAction.generateMakeFunction("config", fields)
        assertEquals("let make = (~a=?, ~b=?): config => {a: a, b: b}", result)
    }

    @Test
    fun `isOptionalType detects option types`() {
        assertTrue(RescriptGenerateMakeAction.isOptionalType("option<int>"))
        assertTrue(RescriptGenerateMakeAction.isOptionalType("option<string>"))
        assertTrue(RescriptGenerateMakeAction.isOptionalType("option"))
    }

    @Test
    fun `isOptionalType rejects non-option types`() {
        assertFalse(RescriptGenerateMakeAction.isOptionalType("int"))
        assertFalse(RescriptGenerateMakeAction.isOptionalType("string"))
        assertFalse(RescriptGenerateMakeAction.isOptionalType("array<option<int>>"))
    }
}
