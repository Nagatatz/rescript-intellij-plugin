package com.rescript.plugin.navigation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for [RescriptSearchEverywhereContributor] utility methods.
 *
 * Note: Full integration tests require IntelliJ Platform test fixtures.
 * Tests here focus on the static helper methods.
 */
class RescriptSearchEverywhereContributorTest {
    @Test
    fun `matchesPattern with exact match returns true`() {
        assertTrue(RescriptSearchEverywhereContributor.matchesPattern("myFunction", "myFunction"))
    }

    @Test
    fun `matchesPattern with prefix match returns true`() {
        assertTrue(RescriptSearchEverywhereContributor.matchesPattern("myFunction", "myFunc"))
    }

    @Test
    fun `matchesPattern case insensitive returns true`() {
        assertTrue(RescriptSearchEverywhereContributor.matchesPattern("MyModule", "mymod"))
    }

    @Test
    fun `matchesPattern with no match returns false`() {
        assertFalse(RescriptSearchEverywhereContributor.matchesPattern("myFunction", "xyz"))
    }

    @Test
    fun `matchesPattern with empty pattern returns true`() {
        assertTrue(RescriptSearchEverywhereContributor.matchesPattern("anything", ""))
    }

    @Test
    fun `matchesPattern with camelCase hump matching`() {
        assertTrue(RescriptSearchEverywhereContributor.matchesPattern("myLongFunctionName", "mLFN"))
    }
}
