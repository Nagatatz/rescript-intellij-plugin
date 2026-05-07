package com.rescript.plugin.narrowing

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

/**
 * Verifies that [RescriptNarrowingPresenter.format] suppresses trivial
 * types, preserves informative ones, and truncates overly long
 * signatures so they fit on a single inlay line.
 */
class RescriptNarrowingPresenterTest {
    @Test
    fun `null hover returns null`() {
        assertNull(RescriptNarrowingPresenter.format(null, "Some(_)"))
    }

    @Test
    fun `blank hover returns null`() {
        assertNull(RescriptNarrowingPresenter.format("   ", "Some(_)"))
    }

    @Test
    fun `unit type is suppressed`() {
        assertNull(RescriptNarrowingPresenter.format("unit", "Some(_)"))
    }

    @Test
    fun `free type variable is suppressed`() {
        assertNull(RescriptNarrowingPresenter.format("'_a", "Some(_)"))
        assertNull(RescriptNarrowingPresenter.format("'a", "Some(_)"))
    }

    @Test
    fun `informative type is returned trimmed`() {
        assertEquals("int", RescriptNarrowingPresenter.format("  int  ", "Some(_)"))
    }

    @Test
    fun `multi-line type is collapsed to single line`() {
        assertEquals(
            "{name: string, age: int}",
            RescriptNarrowingPresenter.format("{name: string,\n  age: int}", "x"),
        )
    }

    @Test
    fun `long type is truncated with ellipsis`() {
        val long = "a".repeat(80)
        val out = RescriptNarrowingPresenter.format(long, "x")!!
        assertEquals(64, out.length)
        assertEquals('…', out.last())
    }

    @Test
    fun `fullText returns original trimmed`() {
        assertEquals("option<int>", RescriptNarrowingPresenter.fullText("  option<int>  "))
        assertNull(RescriptNarrowingPresenter.fullText(null))
        assertNull(RescriptNarrowingPresenter.fullText(""))
    }
}
