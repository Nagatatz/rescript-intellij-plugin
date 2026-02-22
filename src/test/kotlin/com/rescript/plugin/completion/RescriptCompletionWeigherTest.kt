package com.rescript.plugin.completion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

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
}
