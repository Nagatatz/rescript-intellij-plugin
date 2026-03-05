package com.rescript.plugin.paste

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptPasteAsJsxProcessorTest {
    @Test
    fun `processor can be instantiated`() {
        val processor = RescriptPasteAsJsxProcessor()
        assertNotNull(processor)
    }

    @Test
    fun `looksLikeHtml detects HTML tags`() {
        assertTrue(RescriptPasteAsJsxProcessor.looksLikeHtml("<div>hello</div>"))
        assertTrue(RescriptPasteAsJsxProcessor.looksLikeHtml("<img src=\"test.png\">"))
        assertTrue(RescriptPasteAsJsxProcessor.looksLikeHtml("<br>"))
    }

    @Test
    fun `looksLikeHtml rejects non-HTML`() {
        assertFalse(RescriptPasteAsJsxProcessor.looksLikeHtml("let x = 1"))
        assertFalse(RescriptPasteAsJsxProcessor.looksLikeHtml("a < b > c"))
    }

    @Test
    fun `convertHtmlToJsx renames class to className`() {
        val result = RescriptPasteAsJsxProcessor.convertHtmlToJsx("<div class=\"foo\">bar</div>")
        assertTrue(result.contains("className=\"foo\""))
        assertFalse(result.contains(" class="))
    }

    @Test
    fun `convertHtmlToJsx renames for to htmlFor`() {
        val result = RescriptPasteAsJsxProcessor.convertHtmlToJsx("<label for=\"name\">Name</label>")
        assertTrue(result.contains("htmlFor=\"name\""))
    }

    @Test
    fun `convertHtmlToJsx renames onclick to onClick`() {
        val result = RescriptPasteAsJsxProcessor.convertHtmlToJsx("<button onclick=\"handler()\">Click</button>")
        assertTrue(result.contains("onClick="))
    }

    @Test
    fun `convertHtmlToJsx self-closes void elements`() {
        assertEquals("<br />", RescriptPasteAsJsxProcessor.convertHtmlToJsx("<br>"))
        assertEquals("<hr />", RescriptPasteAsJsxProcessor.convertHtmlToJsx("<hr>"))
    }

    @Test
    fun `convertHtmlToJsx self-closes img with attributes`() {
        val result = RescriptPasteAsJsxProcessor.convertHtmlToJsx("<img src=\"test.png\" alt=\"test\">")
        assertTrue(result.endsWith("/>"))
    }

    @Test
    fun `convertHtmlToJsx preserves already self-closed tags`() {
        assertEquals("<br />", RescriptPasteAsJsxProcessor.convertHtmlToJsx("<br />"))
    }

    @Test
    fun `convertHtmlToJsx converts style attributes`() {
        val result = RescriptPasteAsJsxProcessor.convertHtmlToJsx("<div style=\"color: red\">test</div>")
        assertTrue(result.contains("ReactDOM.Style.make"))
        assertTrue(result.contains("~color=\"red\""))
    }

    @Test
    fun `cssPropertyToCamelCase converts hyphenated properties`() {
        assertEquals("backgroundColor", RescriptPasteAsJsxProcessor.cssPropertyToCamelCase("background-color"))
        assertEquals("fontSize", RescriptPasteAsJsxProcessor.cssPropertyToCamelCase("font-size"))
        assertEquals("color", RescriptPasteAsJsxProcessor.cssPropertyToCamelCase("color"))
    }
}
