package com.rescript.plugin.errorlens

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptTypeDiffComputerTest {
    @Test
    fun `tokenizeType splits simple type`() {
        val tokens = RescriptTypeDiffComputer.tokenizeType("int")
        assertEquals(listOf("int"), tokens)
    }

    @Test
    fun `tokenizeType splits function type`() {
        val tokens = RescriptTypeDiffComputer.tokenizeType("int => string")
        assertEquals(listOf("int", " ", "=>", " ", "string"), tokens)
    }

    @Test
    fun `tokenizeType splits tuple type`() {
        val tokens = RescriptTypeDiffComputer.tokenizeType("(int, string)")
        assertEquals(listOf("(", "int", ",", " ", "string", ")"), tokens)
    }

    @Test
    fun `tokenizeType splits generic type`() {
        val tokens = RescriptTypeDiffComputer.tokenizeType("array<int>")
        assertEquals(listOf("array", "<", "int", ">"), tokens)
    }

    @Test
    fun `tokenizeType handles empty string`() {
        val tokens = RescriptTypeDiffComputer.tokenizeType("")
        assertTrue(tokens.isEmpty())
    }

    @Test
    fun `computeDiff marks identical types as not different`() {
        val (expected, actual) = RescriptTypeDiffComputer.computeDiff("int", "int")
        assertEquals(1, expected.size)
        assertEquals(1, actual.size)
        assertFalse(expected[0].isDifferent)
        assertFalse(actual[0].isDifferent)
    }

    @Test
    fun `computeDiff marks different types`() {
        val (expected, actual) = RescriptTypeDiffComputer.computeDiff("int", "string")
        assertEquals(1, expected.size)
        assertEquals(1, actual.size)
        assertTrue(expected[0].isDifferent)
        assertTrue(actual[0].isDifferent)
        assertEquals("int", expected[0].text)
        assertEquals("string", actual[0].text)
    }

    @Test
    fun `computeDiff handles partially different function types`() {
        val (expected, actual) =
            RescriptTypeDiffComputer.computeDiff(
                "int => string",
                "int => bool",
            )
        // Both start with "int" " " "=>" " " but end differently
        assertFalse(expected[0].isDifferent) // "int" matches
        assertTrue(expected[4].isDifferent) // "string" vs "bool"
    }

    @Test
    fun `computeDiff handles different length types`() {
        val (expected, actual) =
            RescriptTypeDiffComputer.computeDiff(
                "int",
                "(int, string)",
            )
        assertTrue(expected.isNotEmpty())
        assertTrue(actual.isNotEmpty())
        // The first tokens differ: "int" vs "("
        assertTrue(expected[0].isDifferent)
    }

    @Test
    fun `formatWithMarkers wraps different segments`() {
        val segments =
            listOf(
                RescriptTypeDiffComputer.TypeSegment("int", false),
                RescriptTypeDiffComputer.TypeSegment(" ", false),
                RescriptTypeDiffComputer.TypeSegment("=>", false),
                RescriptTypeDiffComputer.TypeSegment(" ", false),
                RescriptTypeDiffComputer.TypeSegment("string", true),
            )
        assertEquals("int => [string]", RescriptTypeDiffComputer.formatWithMarkers(segments))
    }

    @Test
    fun `formatWithMarkers with no differences`() {
        val segments =
            listOf(
                RescriptTypeDiffComputer.TypeSegment("int", false),
            )
        assertEquals("int", RescriptTypeDiffComputer.formatWithMarkers(segments))
    }

    @Test
    fun `formatWithMarkers with all different`() {
        val segments =
            listOf(
                RescriptTypeDiffComputer.TypeSegment("string", true),
            )
        assertEquals("[string]", RescriptTypeDiffComputer.formatWithMarkers(segments))
    }

    @Test
    fun `TypeSegment data class stores values`() {
        val segment = RescriptTypeDiffComputer.TypeSegment("int", true)
        assertEquals("int", segment.text)
        assertTrue(segment.isDifferent)
    }
}
