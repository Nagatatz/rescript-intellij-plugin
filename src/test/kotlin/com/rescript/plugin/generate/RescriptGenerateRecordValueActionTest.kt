package com.rescript.plugin.generate

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.rescript.plugin.lang.RecordField
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/** Tests for [RescriptGenerateRecordValueAction] static generation logic. */
class RescriptGenerateRecordValueActionTest {
    @Test
    fun `getActionUpdateThread returns BGT`() {
        assertEquals(ActionUpdateThread.BGT, RescriptGenerateRecordValueAction().getActionUpdateThread())
    }

    @Test
    fun `generateRecordValue with simple fields`() {
        val fields =
            listOf(
                RecordField("name", "string", false),
                RecordField("age", "int", false),
            )
        val result = RescriptGenerateRecordValueAction.generateRecordValue("person", fields)
        assertEquals(
            """
            let value: person = {
              name: "",
              age: 0,
            }
            """.trimIndent(),
            result,
        )
    }

    @Test
    fun `generateRecordValue with type t omits type annotation`() {
        val fields =
            listOf(
                RecordField("x", "int", false),
                RecordField("y", "int", false),
            )
        val result = RescriptGenerateRecordValueAction.generateRecordValue("t", fields)
        assertEquals(
            """
            let value = {
              x: 0,
              y: 0,
            }
            """.trimIndent(),
            result,
        )
    }

    @Test
    fun `generateRecordValue with option fields`() {
        val fields =
            listOf(
                RecordField("email", "option<string>", false),
                RecordField("phone", "option<string>", false),
            )
        val result = RescriptGenerateRecordValueAction.generateRecordValue("contact", fields)
        assertEquals(
            """
            let value: contact = {
              email: None,
              phone: None,
            }
            """.trimIndent(),
            result,
        )
    }

    @Test
    fun `generateRecordValue with mixed types`() {
        val fields =
            listOf(
                RecordField("name", "string", false),
                RecordField("count", "int", true),
                RecordField("score", "float", false),
                RecordField("active", "bool", false),
                RecordField("tags", "array<string>", false),
                RecordField("items", "list<int>", false),
                RecordField("callback", "(int) => unit", false),
            )
        val result = RescriptGenerateRecordValueAction.generateRecordValue("config", fields)
        assertEquals(
            """
            let value: config = {
              name: "",
              count: 0,
              score: 0.0,
              active: false,
              tags: [],
              items: list{},
              callback: _ => todo,
            }
            """.trimIndent(),
            result,
        )
    }

    @Test
    fun `defaultValueForType returns correct defaults`() {
        assertEquals("\"\"", RescriptGenerateRecordValueAction.defaultValueForType("string"))
        assertEquals("0", RescriptGenerateRecordValueAction.defaultValueForType("int"))
        assertEquals("0.0", RescriptGenerateRecordValueAction.defaultValueForType("float"))
        assertEquals("false", RescriptGenerateRecordValueAction.defaultValueForType("bool"))
        assertEquals("None", RescriptGenerateRecordValueAction.defaultValueForType("option<int>"))
        assertEquals("[]", RescriptGenerateRecordValueAction.defaultValueForType("array<string>"))
        assertEquals("list{}", RescriptGenerateRecordValueAction.defaultValueForType("list<int>"))
        assertEquals("()", RescriptGenerateRecordValueAction.defaultValueForType("unit"))
        assertEquals("_ => todo", RescriptGenerateRecordValueAction.defaultValueForType("(int) => string"))
        assertEquals("todo", RescriptGenerateRecordValueAction.defaultValueForType("someCustomType"))
    }

    @Test
    fun `defaultValueForType handles whitespace`() {
        assertEquals("\"\"", RescriptGenerateRecordValueAction.defaultValueForType("  string  "))
        assertEquals("0", RescriptGenerateRecordValueAction.defaultValueForType(" int "))
    }
}
