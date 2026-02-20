package com.rescript.plugin.editor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RescriptTypedHandlerTest {
    private val handler = RescriptTypedHandler()

    @Test
    fun `extracts simple html tag name`() {
        val text = "<div>"
        // gtIndex points to '>' at index 4
        assertEquals("div", handler.extractJsxTagName(text, 3))
    }

    @Test
    fun `extracts component tag name`() {
        val text = "<Button>"
        assertEquals("Button", handler.extractJsxTagName(text, 6))
    }

    @Test
    fun `extracts module-qualified tag name`() {
        val text = "<Module.Component>"
        assertEquals("Module.Component", handler.extractJsxTagName(text, 16))
    }

    @Test
    fun `returns null for closing tag`() {
        val text = "</div>"
        assertNull(handler.extractJsxTagName(text, 4))
    }

    @Test
    fun `returns null when no opening angle bracket`() {
        val text = "div>"
        assertNull(handler.extractJsxTagName(text, 3))
    }

    @Test
    fun `extracts tag with attributes`() {
        val text = "<div className=\"test\">"
        assertEquals("div", handler.extractJsxTagName(text, 20))
    }

    @Test
    fun `extracts tag with JSX expression attribute`() {
        val text = "<div onClick={handler}>"
        // '>' is at index 22; pass it so backward scan visits '}' at 21
        assertEquals("div", handler.extractJsxTagName(text, 22))
    }

    @Test
    fun `returns null for self-closing angle`() {
        // This tests < not followed by a letter
        val text = "<>"
        assertNull(handler.extractJsxTagName(text, 1))
    }

    @Test
    fun `extracts tag with underscore in name`() {
        val text = "<my_component>"
        assertEquals("my_component", handler.extractJsxTagName(text, 12))
    }

    @Test
    fun `extracts deeply nested module tag`() {
        val text = "<A.B.C>"
        assertEquals("A.B.C", handler.extractJsxTagName(text, 5))
    }
}
