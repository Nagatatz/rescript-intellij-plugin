package com.rescript.plugin.lsp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for [RescriptLspDiagnosticParser], verifying correct extraction
 * of structured diagnostic information from LSP diagnostic message strings.
 */
class RescriptLspDiagnosticParserTest {
    // ── Unresolved value ─────────────────────────────────────────────

    @Test
    fun `parseDiagnosticMessage detects unresolved value`() {
        val result = RescriptLspDiagnosticParser.parseDiagnosticMessage("The value myFunc can't be found")

        assertNotNull(result)
        assertEquals(RescriptLspDiagnosticParser.DiagnosticKind.UNRESOLVED_VALUE, result!!.kind)
        assertEquals("myFunc", result.identifier)
    }

    @Test
    fun `parseDiagnosticMessage detects unresolved value with single-char name`() {
        val result = RescriptLspDiagnosticParser.parseDiagnosticMessage("The value x can't be found")

        assertNotNull(result)
        assertEquals(RescriptLspDiagnosticParser.DiagnosticKind.UNRESOLVED_VALUE, result!!.kind)
        assertEquals("x", result.identifier)
    }

    @Test
    fun `parseDiagnosticMessage detects unresolved value with underscore`() {
        val result = RescriptLspDiagnosticParser.parseDiagnosticMessage("The value my_func can't be found")

        assertNotNull(result)
        assertEquals(RescriptLspDiagnosticParser.DiagnosticKind.UNRESOLVED_VALUE, result!!.kind)
        assertEquals("my_func", result.identifier)
    }

    @Test
    fun `parseDiagnosticMessage detects unresolved value case insensitive`() {
        val result = RescriptLspDiagnosticParser.parseDiagnosticMessage("the value myFunc CAN'T be found")

        assertNotNull(result)
        assertEquals(RescriptLspDiagnosticParser.DiagnosticKind.UNRESOLVED_VALUE, result!!.kind)
        assertEquals("myFunc", result.identifier)
    }

    @Test
    fun `parseDiagnosticMessage detects unresolved value with numeric suffix`() {
        val result = RescriptLspDiagnosticParser.parseDiagnosticMessage("The value item2 can't be found")

        assertNotNull(result)
        assertEquals(RescriptLspDiagnosticParser.DiagnosticKind.UNRESOLVED_VALUE, result!!.kind)
        assertEquals("item2", result.identifier)
    }

    // ── Unresolved module ────────────────────────────────────────────

    @Test
    fun `parseDiagnosticMessage detects unresolved module`() {
        val result = RescriptLspDiagnosticParser.parseDiagnosticMessage("The module or file MyModule can't be found")

        assertNotNull(result)
        assertEquals(RescriptLspDiagnosticParser.DiagnosticKind.UNRESOLVED_MODULE, result!!.kind)
        assertEquals("MyModule", result.identifier)
    }

    @Test
    fun `parseDiagnosticMessage detects unresolved module case insensitive`() {
        val result = RescriptLspDiagnosticParser.parseDiagnosticMessage("the module or file Belt CAN'T be found")

        assertNotNull(result)
        assertEquals(RescriptLspDiagnosticParser.DiagnosticKind.UNRESOLVED_MODULE, result!!.kind)
        assertEquals("Belt", result.identifier)
    }

    @Test
    fun `parseDiagnosticMessage detects unresolved module with underscore`() {
        val result = RescriptLspDiagnosticParser.parseDiagnosticMessage("The module or file My_Module can't be found")

        assertNotNull(result)
        assertEquals(RescriptLspDiagnosticParser.DiagnosticKind.UNRESOLVED_MODULE, result!!.kind)
        assertEquals("My_Module", result.identifier)
    }

    // ── Unknown / unrecognized messages ──────────────────────────────

    @Test
    fun `parseDiagnosticMessage returns null for unknown message`() {
        assertNull(RescriptLspDiagnosticParser.parseDiagnosticMessage("Some other error message"))
    }

    @Test
    fun `parseDiagnosticMessage returns null for empty string`() {
        assertNull(RescriptLspDiagnosticParser.parseDiagnosticMessage(""))
    }

    @Test
    fun `parseDiagnosticMessage returns null for partial match`() {
        assertNull(RescriptLspDiagnosticParser.parseDiagnosticMessage("The value"))
    }

    @Test
    fun `parseDiagnosticMessage returns null for type error message`() {
        assertNull(
            RescriptLspDiagnosticParser.parseDiagnosticMessage(
                "This has type int but is expected to have type string",
            ),
        )
    }

    @Test
    fun `parseDiagnosticMessage returns null for syntax error`() {
        assertNull(RescriptLspDiagnosticParser.parseDiagnosticMessage("Syntax error: unexpected token"))
    }

    // ── Embedded in longer messages ──────────────────────────────────

    @Test
    fun `parseDiagnosticMessage detects pattern in longer message`() {
        val result =
            RescriptLspDiagnosticParser.parseDiagnosticMessage(
                "Error: The value doSomething can't be found in the current scope",
            )

        assertNotNull(result)
        assertEquals(RescriptLspDiagnosticParser.DiagnosticKind.UNRESOLVED_VALUE, result!!.kind)
        assertEquals("doSomething", result.identifier)
    }

    // ── DiagnosticInfo data class ────────────────────────────────────

    @Test
    fun `DiagnosticInfo equality works correctly`() {
        val a =
            RescriptLspDiagnosticParser.DiagnosticInfo(
                RescriptLspDiagnosticParser.DiagnosticKind.UNRESOLVED_VALUE,
                "foo",
            )
        val b =
            RescriptLspDiagnosticParser.DiagnosticInfo(
                RescriptLspDiagnosticParser.DiagnosticKind.UNRESOLVED_VALUE,
                "foo",
            )
        assertEquals(a, b)
    }

    @Test
    fun `DiagnosticKind enum has expected values`() {
        val values = RescriptLspDiagnosticParser.DiagnosticKind.entries
        assertEquals(2, values.size)
        assertEquals(RescriptLspDiagnosticParser.DiagnosticKind.UNRESOLVED_VALUE, values[0])
        assertEquals(RescriptLspDiagnosticParser.DiagnosticKind.UNRESOLVED_MODULE, values[1])
    }
}
