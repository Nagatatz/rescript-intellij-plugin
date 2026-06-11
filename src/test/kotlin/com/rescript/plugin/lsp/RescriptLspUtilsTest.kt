package com.rescript.plugin.lsp

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Tests for the LSP communication utilities.
 *
 * Only the pure URI conversion is testable headlessly; server lookup
 * and hover retrieval need a live LSP session. Parsing of hover and
 * diagnostic payloads is covered by [RescriptLspSignatureParserTest]
 * and [RescriptLspDiagnosticParserTest].
 */
class RescriptLspUtilsTest {
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
}
