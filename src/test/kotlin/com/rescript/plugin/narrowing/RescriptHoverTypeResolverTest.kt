package com.rescript.plugin.narrowing

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

/**
 * Shape tests for the [RescriptHoverTypeResolver] functional interface:
 * SAM construction, null propagation, and the offset arg threading.
 * The production [RescriptHoverTypeResolver.Companion.forFile] factory is
 * exercised indirectly by [RescriptNarrowingHintProvider] tests because
 * it relies on an LSP server; here we cover only the pure SAM behaviour.
 */
class RescriptHoverTypeResolverTest {
    @Test
    fun `SAM lambda returns its mapped value`() {
        val resolver = RescriptHoverTypeResolver { offset -> "T@$offset" }
        assertEquals("T@0", resolver.resolveAt(0))
        assertEquals("T@42", resolver.resolveAt(42))
    }

    @Test
    fun `SAM lambda may return null`() {
        val resolver = RescriptHoverTypeResolver { null }
        assertNull(resolver.resolveAt(0))
    }

    @Test
    fun `SAM lambda receives the offset argument verbatim`() {
        val seen = mutableListOf<Int>()
        val resolver =
            RescriptHoverTypeResolver { offset ->
                seen += offset
                null
            }
        resolver.resolveAt(1)
        resolver.resolveAt(7)
        resolver.resolveAt(99)
        assertEquals(listOf(1, 7, 99), seen)
    }
}
