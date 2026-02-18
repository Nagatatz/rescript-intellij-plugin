package com.rescript.plugin.paste

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptPasteAsJsonActionTest {
    @Test
    fun `isLikelyJson detects object`() {
        assertTrue(RescriptPasteAsJsonAction.isLikelyJson("{\"key\": \"value\"}"))
    }

    @Test
    fun `isLikelyJson detects array`() {
        assertTrue(RescriptPasteAsJsonAction.isLikelyJson("[1, 2, 3]"))
    }

    @Test
    fun `isLikelyJson rejects non-json`() {
        assertFalse(RescriptPasteAsJsonAction.isLikelyJson("hello world"))
        assertFalse(RescriptPasteAsJsonAction.isLikelyJson(""))
    }

    @Test
    fun `isLikelyJson handles whitespace prefix`() {
        assertTrue(RescriptPasteAsJsonAction.isLikelyJson("  {\"key\": 1}"))
        assertTrue(RescriptPasteAsJsonAction.isLikelyJson("  [1]"))
    }

    @Test
    fun `converts string`() {
        val json =
            com.google.gson.JsonParser
                .parseString("\"hello\"")
        assertEquals("JSON.String(\"hello\")", RescriptPasteAsJsonAction.convertJsonToRescript(json))
    }

    @Test
    fun `converts integer`() {
        val json =
            com.google.gson.JsonParser
                .parseString("42")
        assertEquals("JSON.Number(42.)", RescriptPasteAsJsonAction.convertJsonToRescript(json))
    }

    @Test
    fun `converts float`() {
        val json =
            com.google.gson.JsonParser
                .parseString("3.14")
        assertEquals("JSON.Number(3.14)", RescriptPasteAsJsonAction.convertJsonToRescript(json))
    }

    @Test
    fun `converts boolean`() {
        val jsonTrue =
            com.google.gson.JsonParser
                .parseString("true")
        assertEquals("JSON.Boolean(true)", RescriptPasteAsJsonAction.convertJsonToRescript(jsonTrue))
        val jsonFalse =
            com.google.gson.JsonParser
                .parseString("false")
        assertEquals("JSON.Boolean(false)", RescriptPasteAsJsonAction.convertJsonToRescript(jsonFalse))
    }

    @Test
    fun `converts null`() {
        val json =
            com.google.gson.JsonParser
                .parseString("null")
        assertEquals("JSON.Null", RescriptPasteAsJsonAction.convertJsonToRescript(json))
    }

    @Test
    fun `converts array`() {
        val json =
            com.google.gson.JsonParser
                .parseString("[1, \"two\", true]")
        assertEquals(
            "JSON.Array([JSON.Number(1.), JSON.String(\"two\"), JSON.Boolean(true)])",
            RescriptPasteAsJsonAction.convertJsonToRescript(json),
        )
    }

    @Test
    fun `converts object`() {
        val json =
            com.google.gson.JsonParser
                .parseString("{\"name\": \"test\", \"age\": 25}")
        val result = RescriptPasteAsJsonAction.convertJsonToRescript(json)
        assertTrue(result.startsWith("JSON.Object(dict{"))
        assertTrue(result.contains("\"name\": JSON.String(\"test\")"))
        assertTrue(result.contains("\"age\": JSON.Number(25.)"))
    }

    @Test
    fun `converts nested structure`() {
        val json =
            com.google.gson.JsonParser
                .parseString("{\"items\": [1, 2]}")
        val result = RescriptPasteAsJsonAction.convertJsonToRescript(json)
        assertEquals(
            "JSON.Object(dict{\"items\": JSON.Array([JSON.Number(1.), JSON.Number(2.)])})",
            result,
        )
    }

    @Test
    fun `converts empty object`() {
        val json =
            com.google.gson.JsonParser
                .parseString("{}")
        assertEquals("JSON.Object(dict{})", RescriptPasteAsJsonAction.convertJsonToRescript(json))
    }

    @Test
    fun `converts empty array`() {
        val json =
            com.google.gson.JsonParser
                .parseString("[]")
        assertEquals("JSON.Array([])", RescriptPasteAsJsonAction.convertJsonToRescript(json))
    }

    @Test
    fun `escapes special characters in strings`() {
        val json =
            com.google.gson.JsonParser
                .parseString("\"hello\\nworld\\t!\"")
        assertEquals(
            "JSON.String(\"hello\\nworld\\t!\")",
            RescriptPasteAsJsonAction.convertJsonToRescript(json),
        )
    }
}
