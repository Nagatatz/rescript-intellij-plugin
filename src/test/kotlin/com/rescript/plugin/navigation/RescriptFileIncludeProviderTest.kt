package com.rescript.plugin.navigation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptFileIncludeProviderTest {
    private val provider = RescriptFileIncludeProvider()

    @Test
    fun testProviderCanBeInstantiated() {
        assertNotNull(provider)
    }

    @Test
    fun testGetId() {
        assertEquals("rescript", provider.id)
    }

    // -- extractModuleNames tests --

    @Test
    fun testExtractSingleOpenStatement() {
        val text = "open Belt"
        val result = RescriptFileIncludeProvider.extractModuleNames(text)
        assertEquals(listOf("Belt"), result)
    }

    @Test
    fun testExtractDottedModulePath() {
        val text = "open Belt.Array"
        val result = RescriptFileIncludeProvider.extractModuleNames(text)
        assertEquals(listOf("Belt.Array"), result)
    }

    @Test
    fun testExtractMultipleOpenStatements() {
        val text =
            """
            open Belt
            open Js.Array2
            open ReactDOM
            """.trimIndent()
        val result = RescriptFileIncludeProvider.extractModuleNames(text)
        assertEquals(3, result.size)
        assertEquals("Belt", result[0])
        assertEquals("Js.Array2", result[1])
        assertEquals("ReactDOM", result[2])
    }

    @Test
    fun testExtractWithLeadingWhitespace() {
        val text = "  open Belt"
        val result = RescriptFileIncludeProvider.extractModuleNames(text)
        assertEquals(listOf("Belt"), result)
    }

    @Test
    fun testNoMatchForLowercaseModule() {
        val text = "open belt"
        val result = RescriptFileIncludeProvider.extractModuleNames(text)
        assertTrue(result.isEmpty())
    }

    @Test
    fun testNoMatchForNonOpenStatement() {
        val text = "let x = 1"
        val result = RescriptFileIncludeProvider.extractModuleNames(text)
        assertTrue(result.isEmpty())
    }

    @Test
    fun testNoMatchForEmptyText() {
        val result = RescriptFileIncludeProvider.extractModuleNames("")
        assertTrue(result.isEmpty())
    }

    @Test
    fun testExtractIgnoresCommentedOpen() {
        val text =
            """
            open Belt
            // open Commented
            let x = 1
            """.trimIndent()
        val result = RescriptFileIncludeProvider.extractModuleNames(text)
        // The regex matches lines starting with optional whitespace + "open",
        // so "// open Commented" won't match (starts with "//")
        assertEquals(1, result.size)
        assertEquals("Belt", result[0])
    }

    // -- moduleNameToFileName tests --

    @Test
    fun testSimpleModuleToFileName() {
        assertEquals("Belt.res", RescriptFileIncludeProvider.moduleNameToFileName("Belt"))
    }

    @Test
    fun testDottedModuleToFileName() {
        assertEquals("Array.res", RescriptFileIncludeProvider.moduleNameToFileName("Belt.Array"))
    }

    @Test
    fun testDeepModulePathToFileName() {
        assertEquals("Map.res", RescriptFileIncludeProvider.moduleNameToFileName("Belt.Map.String.Map"))
    }
}
