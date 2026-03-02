package com.rescript.plugin.paste

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptPasteAsRescriptProcessorTest {
    @Test
    fun `looksLikeJavaScript detects const declaration`() {
        assertTrue(RescriptPasteAsRescriptProcessor.looksLikeJavaScript("const x = 42;"))
    }

    @Test
    fun `looksLikeJavaScript detects function declaration`() {
        assertTrue(RescriptPasteAsRescriptProcessor.looksLikeJavaScript("function foo() { return 1; }"))
    }

    @Test
    fun `looksLikeJavaScript detects var declaration`() {
        assertTrue(RescriptPasteAsRescriptProcessor.looksLikeJavaScript("var x = 'hello';"))
    }

    @Test
    fun `looksLikeJavaScript rejects ReScript code`() {
        assertFalse(RescriptPasteAsRescriptProcessor.looksLikeJavaScript("let x = 42"))
    }

    @Test
    fun `looksLikeJavaScript rejects empty string`() {
        assertFalse(RescriptPasteAsRescriptProcessor.looksLikeJavaScript(""))
    }

    @Test
    fun `convertLine converts const to let`() {
        assertEquals("let x = 42", RescriptPasteAsRescriptProcessor.convertLine("const x = 42;"))
    }

    @Test
    fun `convertLine converts var to let`() {
        assertEquals("let x = 42", RescriptPasteAsRescriptProcessor.convertLine("var x = 42;"))
    }

    @Test
    fun `convertLine converts triple equals`() {
        assertEquals("x == y", RescriptPasteAsRescriptProcessor.convertLine("x === y"))
    }

    @Test
    fun `convertLine converts not triple equals`() {
        assertEquals("x != y", RescriptPasteAsRescriptProcessor.convertLine("x !== y"))
    }

    @Test
    fun `convertLine converts console log`() {
        assertEquals("Js.log(x)", RescriptPasteAsRescriptProcessor.convertLine("console.log(x)"))
    }

    @Test
    fun `convertLine converts null to None`() {
        assertEquals("let x = None", RescriptPasteAsRescriptProcessor.convertLine("const x = null;"))
    }

    @Test
    fun `convertLine strips semicolons`() {
        assertEquals("let x = 1", RescriptPasteAsRescriptProcessor.convertLine("let x = 1;"))
    }

    @Test
    fun `convertJsToRescript converts multiple lines`() {
        val js =
            """
            const x = 42;
            console.log(x);
            """.trimIndent()
        val result = RescriptPasteAsRescriptProcessor.convertJsToRescript(js)
        assertTrue(result.contains("let x = 42"))
        assertTrue(result.contains("Js.log(x)"))
    }

    @Test
    fun `convertLine converts function declaration`() {
        val result = RescriptPasteAsRescriptProcessor.convertLine("function add(a, b) {")
        assertTrue(result.contains("let add = (a, b) => {"))
    }

    @Test
    fun `convertLine preserves comments`() {
        assertEquals("// this is a comment", RescriptPasteAsRescriptProcessor.convertLine("// this is a comment"))
    }
}
