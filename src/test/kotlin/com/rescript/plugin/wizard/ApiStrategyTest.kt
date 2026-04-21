package com.rescript.plugin.wizard

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ApiStrategyTest {
    @Test
    fun `exposes rest and graphql entries`() {
        val names = ApiStrategy.entries.map { it.name }
        assertTrue(names.contains("REST"))
        assertTrue(names.contains("GRAPHQL"))
        assertEquals(2, ApiStrategy.entries.size)
    }

    @Test
    fun `display names are human-facing labels`() {
        assertEquals("REST", ApiStrategy.REST.displayName)
        assertEquals("GraphQL", ApiStrategy.GRAPHQL.displayName)
    }

    @Test
    fun `variantKey is lowercase name`() {
        assertEquals("rest", ApiStrategy.REST.variantKey())
        assertEquals("graphql", ApiStrategy.GRAPHQL.variantKey())
    }

    @Test
    fun `toString mirrors the display name for Swing renderers`() {
        assertEquals("REST", ApiStrategy.REST.toString())
        assertEquals("GraphQL", ApiStrategy.GRAPHQL.toString())
    }
}
