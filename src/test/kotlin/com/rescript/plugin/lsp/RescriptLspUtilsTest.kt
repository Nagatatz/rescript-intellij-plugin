package com.rescript.plugin.lsp

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptLspUtilsTest {
    // --- lspUriToVfsUrl ---

    @Test
    fun `lspUriToVfsUrl normalizes file URI with triple slash`() {
        val result = RescriptLspUtils.lspUriToVfsUrl("file:///Users/test/src/App.res")
        assertEquals("file:///Users/test/src/App.res", result)
    }

    @Test
    fun `lspUriToVfsUrl handles encoded characters in URI`() {
        val result = RescriptLspUtils.lspUriToVfsUrl("file:///Users/test/my%20project/App.res")
        assertEquals("file:///Users/test/my project/App.res", result)
    }

    @Test
    fun `lspUriToVfsUrl returns original string for invalid URI`() {
        val result = RescriptLspUtils.lspUriToVfsUrl("not a valid uri :::")
        assertEquals("not a valid uri :::", result)
    }

    @Test
    fun `lspUriToVfsUrl handles bare path`() {
        // A bare absolute path like "/Users/test/App.res" is a valid URI
        val result = RescriptLspUtils.lspUriToVfsUrl("/Users/test/App.res")
        assertEquals("file:///Users/test/App.res", result)
    }

    // --- parseSignatureLabels ---

    @Test
    fun `parseSignatureLabels extracts labeled params`() {
        val sig = "(~name: string, ~age: int, unit) => person"
        val result = RescriptLspUtils.parseSignatureLabels(sig)

        assertEquals(2, result.size)
        assertEquals("name", result[0].name)
        assertEquals("string", result[0].type)
        assertFalse(result[0].isOptional)
        assertEquals("age", result[1].name)
        assertEquals("int", result[1].type)
        assertFalse(result[1].isOptional)
    }

    @Test
    fun `parseSignatureLabels handles optional params`() {
        val sig = "(~name: string, ~age: int=?, unit) => person"
        val result = RescriptLspUtils.parseSignatureLabels(sig)

        assertEquals(2, result.size)
        assertEquals("name", result[0].name)
        assertFalse(result[0].isOptional)
        assertEquals("age", result[1].name)
        assertTrue(result[1].isOptional)
    }

    @Test
    fun `parseSignatureLabels returns empty for no labels`() {
        val sig = "(string, int) => bool"
        val result = RescriptLspUtils.parseSignatureLabels(sig)

        assertEquals(0, result.size)
    }

    @Test
    fun `parseSignatureLabels handles complex types`() {
        val sig = "(~items: array<string>, ~callback: (int, string) => unit) => unit"
        val result = RescriptLspUtils.parseSignatureLabels(sig)

        assertEquals(2, result.size)
        assertEquals("items", result[0].name)
        assertEquals("array<string>", result[0].type)
        assertEquals("callback", result[1].name)
    }

    @Test
    fun `parseVariantConstructors handles option type`() {
        val result = RescriptLspUtils.parseVariantConstructors("option<int>")

        assertEquals(2, result.size)
        assertEquals("Some", result[0].name)
        assertTrue(result[0].hasPayload)
        assertEquals("None", result[1].name)
        assertFalse(result[1].hasPayload)
    }

    @Test
    fun `parseVariantConstructors handles result type`() {
        val result = RescriptLspUtils.parseVariantConstructors("result<int, string>")

        assertEquals(2, result.size)
        assertEquals("Ok", result[0].name)
        assertTrue(result[0].hasPayload)
        assertEquals("Error", result[1].name)
        assertTrue(result[1].hasPayload)
    }

    @Test
    fun `parseVariantConstructors handles inline variants`() {
        val result = RescriptLspUtils.parseVariantConstructors("Red | Green | Blue")

        assertEquals(3, result.size)
        assertEquals("Red", result[0].name)
        assertFalse(result[0].hasPayload)
        assertEquals("Green", result[1].name)
        assertEquals("Blue", result[2].name)
    }

    @Test
    fun `parseVariantConstructors handles constructors with payload`() {
        val result = RescriptLspUtils.parseVariantConstructors("Some(int) | None")

        assertEquals(2, result.size)
        assertEquals("Some", result[0].name)
        assertTrue(result[0].hasPayload)
        assertEquals("None", result[1].name)
        assertFalse(result[1].hasPayload)
    }

    @Test
    fun `parseDiagnosticMessage detects unresolved value`() {
        val result = RescriptLspUtils.parseDiagnosticMessage("The value myFunc can't be found")

        assertNotNull(result)
        assertEquals(RescriptLspUtils.DiagnosticKind.UNRESOLVED_VALUE, result!!.kind)
        assertEquals("myFunc", result.identifier)
    }

    @Test
    fun `parseDiagnosticMessage detects unresolved module`() {
        val result = RescriptLspUtils.parseDiagnosticMessage("The module or file MyModule can't be found")

        assertNotNull(result)
        assertEquals(RescriptLspUtils.DiagnosticKind.UNRESOLVED_MODULE, result!!.kind)
        assertEquals("MyModule", result.identifier)
    }

    @Test
    fun `parseDiagnosticMessage returns null for unknown message`() {
        val result = RescriptLspUtils.parseDiagnosticMessage("Some other error message")

        assertNull(result)
    }

    @Test
    fun `extractParenContent handles simple case`() {
        val result = RescriptLspUtils.extractParenContent("foo(bar, baz)")

        assertEquals("bar, baz", result)
    }

    @Test
    fun `extractParenContent handles nested parens`() {
        val result = RescriptLspUtils.extractParenContent("foo(bar(1), baz)")

        assertEquals("bar(1), baz", result)
    }

    @Test
    fun `extractParenContent returns null without parens`() {
        val result = RescriptLspUtils.extractParenContent("no parens here")

        assertNull(result)
    }

    @Test
    fun `splitByComma handles simple case`() {
        val result = RescriptLspUtils.splitByComma("a, b, c")

        assertEquals(3, result.size)
        assertEquals("a", result[0].trim())
        assertEquals("b", result[1].trim())
        assertEquals("c", result[2].trim())
    }

    @Test
    fun `splitByComma respects nested brackets`() {
        val result = RescriptLspUtils.splitByComma("array<int, string>, bool")

        assertEquals(2, result.size)
        assertEquals("array<int, string>", result[0].trim())
        assertEquals("bool", result[1].trim())
    }
}
