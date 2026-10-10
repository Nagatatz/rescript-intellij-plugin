package com.rescript.plugin.editor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class RescriptTypedHandlerTest {
    private val handler = RescriptTypedHandler()

    @Test
    fun `extracts simple html tag name`() {
        val text = "<div>"
        assertEquals("div", handler.extractJsxTagName(text, text.lastIndex))
    }

    @Test
    fun `extracts component tag name`() {
        val text = "<Button>"
        assertEquals("Button", handler.extractJsxTagName(text, text.lastIndex))
    }

    @Test
    fun `extracts module-qualified tag name`() {
        val text = "<Module.Component>"
        assertEquals("Module.Component", handler.extractJsxTagName(text, text.lastIndex))
    }

    @Test
    fun `returns null for closing tag`() {
        val text = "</div>"
        assertNull(handler.extractJsxTagName(text, text.lastIndex))
    }

    @Test
    fun `returns null when no opening angle bracket`() {
        val text = "div>"
        assertNull(handler.extractJsxTagName(text, text.lastIndex))
    }

    @Test
    fun `extracts tag with attributes`() {
        val text = "<div className=\"test\">"
        assertEquals("div", handler.extractJsxTagName(text, text.lastIndex))
    }

    @Test
    fun `extracts tag with JSX expression attribute`() {
        val text = "<div onClick={handler}>"
        assertEquals("div", handler.extractJsxTagName(text, text.lastIndex))
    }

    @Test
    fun `returns null for self-closing angle`() {
        // This tests < not followed by a letter
        val text = "<>"
        assertNull(handler.extractJsxTagName(text, text.lastIndex))
    }

    @Test
    fun `extracts tag with underscore in name`() {
        val text = "<my_component>"
        assertEquals("my_component", handler.extractJsxTagName(text, text.lastIndex))
    }

    @Test
    fun `extracts deeply nested module tag`() {
        val text = "<A.B.C>"
        assertEquals("A.B.C", handler.extractJsxTagName(text, text.lastIndex))
    }

    @Test
    fun `does not close tags for operators following a type argument`() {
        for (suffix in listOf("values->", "x =>", "a >")) {
            val text = "let values: array<string> = []\n$suffix"
            assertNull(handler.extractJsxTagName(text, text.lastIndex))
        }
    }

    @Test
    fun `does not close tags for arrows inside unfinished JSX attributes`() {
        for (arrow in listOf("->", "=>")) {
            val text = "<div onClick={value $arrow"
            assertNull(handler.extractJsxTagName(text, text.lastIndex))
        }
    }

    @Test
    fun `extracts tags with arrows and comparisons inside nested attributes`() {
        for (attribute in listOf("_ => ()", "value->convert", "if a > b {a} else {b}")) {
            val text = "<Comp.Sub onClick={$attribute}>"
            assertEquals("Comp.Sub", handler.extractJsxTagName(text, text.lastIndex))
        }
    }
}
