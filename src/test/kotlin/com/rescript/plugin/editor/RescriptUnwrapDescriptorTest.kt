package com.rescript.plugin.editor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptUnwrapDescriptorTest {
    private val descriptor = RescriptUnwrapDescriptor()

    // ── findWrapperRange ─────────────────────────────────────────

    @Test
    fun `findWrapperRange finds Some wrapper`() {
        val text = "let x = Some(value)"
        val result = descriptor.findWrapperRange(text, 13, "Some")
        assertNotNull(result)
        assertEquals(8, result!!.first)
        assertEquals(19, result.second)
    }

    @Test
    fun `findWrapperRange finds Ok wrapper`() {
        val text = "let x = Ok(value)"
        val result = descriptor.findWrapperRange(text, 11, "Ok")
        assertNotNull(result)
        assertEquals(8, result!!.first)
        assertEquals(17, result.second)
    }

    @Test
    fun `findWrapperRange finds Error wrapper`() {
        val text = "let x = Error(value)"
        val result = descriptor.findWrapperRange(text, 14, "Error")
        assertNotNull(result)
        assertEquals(8, result!!.first)
        assertEquals(20, result.second)
    }

    @Test
    fun `findWrapperRange returns null when no wrapper`() {
        val text = "let x = value"
        assertNull(descriptor.findWrapperRange(text, 10, "Some"))
    }

    @Test
    fun `findWrapperRange handles nested wrappers`() {
        val text = "Some(Ok(x))"
        val result = descriptor.findWrapperRange(text, 5, "Some")
        assertNotNull(result)
        assertEquals(0, result!!.first)
        assertEquals(11, result.second)
    }

    // ── findBraceRange ───────────────────────────────────────────

    @Test
    fun `findBraceRange finds enclosing braces`() {
        val text = "{ body }"
        val result = descriptor.findBraceRange(text, 3)
        assertNotNull(result)
        assertEquals(0, result!!.first)
        assertEquals(8, result.second)
    }

    @Test
    fun `findBraceRange returns null when no enclosing brace`() {
        val text = "let x = 1"
        assertNull(descriptor.findBraceRange(text, 5))
    }

    @Test
    fun `findBraceRange skips inner matched braces`() {
        val text = "{ { inner } outer }"
        val result = descriptor.findBraceRange(text, 15)
        assertNotNull(result)
        assertEquals(0, result!!.first)
        assertEquals(19, result.second)
    }

    // ── findIfRange ──────────────────────────────────────────────

    @Test
    fun `findIfRange finds if block`() {
        val text = "if (cond) { body }"
        val result = descriptor.findIfRange(text, 12)
        assertNotNull(result)
        assertEquals(0, result!!.outerStart)
        assertEquals(18, result.outerEnd)
    }

    @Test
    fun `findIfRange returns null for plain code`() {
        val text = "let x = 1"
        assertNull(descriptor.findIfRange(text, 5))
    }

    // ── findSwitchBodyRange ──────────────────────────────────────

    @Test
    fun `findSwitchBodyRange finds switch block`() {
        val text = "switch x { | A => 1 }"
        val result = descriptor.findSwitchBodyRange(text, 15)
        assertNotNull(result)
        assertEquals(0, result!!.outerStart)
        assertEquals(21, result.outerEnd)
    }

    @Test
    fun `findSwitchBodyRange returns null for plain code`() {
        assertNull(descriptor.findSwitchBodyRange("let x = 1", 5))
    }

    // ── findTryRange ─────────────────────────────────────────────

    @Test
    fun `findTryRange finds try-catch block`() {
        val text = "try { expr } catch { | Exn.Error(_) => () }"
        val result = descriptor.findTryRange(text, 7)
        assertNotNull(result)
        assertEquals(0, result!!.outerStart)
        assertEquals(43, result.outerEnd)
    }

    @Test
    fun `findTryRange returns null for plain code`() {
        assertNull(descriptor.findTryRange("let x = 1", 5))
    }

    // ── showOptionsDialog / shouldTryToRestoreCaretPosition ──────

    @Test
    fun `showOptionsDialog returns true`() {
        assertTrue(descriptor.showOptionsDialog())
    }

    @Test
    fun `shouldTryToRestoreCaretPosition returns true`() {
        assertTrue(descriptor.shouldTryToRestoreCaretPosition())
    }
}
