package com.rescript.plugin.editor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class RescriptCopyPastePreProcessorTest {
    private val processor = RescriptCopyPastePreProcessor()

    @Test
    fun `processor can be instantiated`() {
        assertNotNull(processor)
    }

    @Test
    fun `preprocessOnCopy returns null`() {
        assertNull(processor.preprocessOnCopy(null, null, null, null))
    }

    @Test
    fun `escapeForString escapes double quotes`() {
        assertEquals("hello \\\"world\\\"", RescriptCopyPastePreProcessor.escapeForString("hello \"world\""))
    }

    @Test
    fun `escapeForString escapes backslashes`() {
        assertEquals("path\\\\to\\\\file", RescriptCopyPastePreProcessor.escapeForString("path\\to\\file"))
    }

    @Test
    fun `escapeForString escapes newlines and tabs`() {
        assertEquals(
            "line1\\nline2\\tindented",
            RescriptCopyPastePreProcessor.escapeForString("line1\nline2\tindented"),
        )
    }

    @Test
    fun `escapeForString handles empty string`() {
        assertEquals("", RescriptCopyPastePreProcessor.escapeForString(""))
    }

    @Test
    fun `escapeForTemplateString escapes backticks`() {
        assertEquals("\\`template\\`", RescriptCopyPastePreProcessor.escapeForTemplateString("`template`"))
    }

    @Test
    fun `escapeForTemplateString escapes dollar signs`() {
        assertEquals("\\\$var", RescriptCopyPastePreProcessor.escapeForTemplateString("\$var"))
    }

    @Test
    fun `escapeForTemplateString escapes backslashes`() {
        assertEquals("path\\\\to", RescriptCopyPastePreProcessor.escapeForTemplateString("path\\to"))
    }
}
