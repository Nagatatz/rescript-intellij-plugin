package com.rescript.plugin.completion

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Tests for [RescriptCompletionWeigher] utility methods.
 *
 * Note: Full weigher integration tests require IntelliJ Platform test fixtures.
 * Tests here focus on the static classification logic.
 */
class RescriptCompletionWeigherTest {
    @Test
    fun `isKeyword returns true for ReScript keywords`() {
        assertTrue(RescriptCompletionWeigher.isKeyword("let"))
        assertTrue(RescriptCompletionWeigher.isKeyword("switch"))
        assertTrue(RescriptCompletionWeigher.isKeyword("module"))
        assertTrue(RescriptCompletionWeigher.isKeyword("type"))
        assertTrue(RescriptCompletionWeigher.isKeyword("if"))
    }

    @Test
    fun `isKeyword returns false for non-keywords`() {
        assertFalse(RescriptCompletionWeigher.isKeyword("myFunction"))
        assertFalse(RescriptCompletionWeigher.isKeyword("Array"))
        assertFalse(RescriptCompletionWeigher.isKeyword(""))
    }

    @Test
    fun `computeWeight gives keywords lowest weight`() {
        val weight = RescriptCompletionWeigher.computeWeight("let", null, false)
        assertEquals(-10, weight)
    }

    @Test
    fun `computeWeight gives local variables highest weight`() {
        val weight = RescriptCompletionWeigher.computeWeight("myVar", null, true)
        assertEquals(10, weight)
    }

    @Test
    fun `computeWeight gives shorter names bonus`() {
        val shortWeight = RescriptCompletionWeigher.computeWeight("map", null, false)
        val longWeight = RescriptCompletionWeigher.computeWeight("veryLongFunctionName", null, false)
        assertTrue(shortWeight > longWeight)
    }

    @Test
    fun `computeWeight gives lowercase-starting names bonus`() {
        val lowerWeight = RescriptCompletionWeigher.computeWeight("myFunc", null, false)
        val upperWeight = RescriptCompletionWeigher.computeWeight("MyModule", null, false)
        assertTrue(lowerWeight > upperWeight)
    }

    @Test
    fun `computeWeight handles empty string without crashing`() {
        val weight = RescriptCompletionWeigher.computeWeight("", null, false)
        assertEquals(3, weight) // length <= 5 bonus only, no case bonus
    }
}
