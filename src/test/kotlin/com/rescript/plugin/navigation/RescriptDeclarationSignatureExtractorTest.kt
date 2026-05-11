package com.rescript.plugin.navigation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

/**
 * Unit tests for the pure declaration extractor used by the type
 * signature search contributor. Cases lock down the small grammar
 * the extractor recognises (`let` / `let rec` / `external` / `type`
 * with an explicit `: T = …` annotation) and the depth-aware
 * binding-`=` finder that stops the signature before the body.
 */
class RescriptDeclarationSignatureExtractorTest {
    // ── parseDeclaration ─────────────────────────────────────────

    @Test
    fun `parseDeclaration finds let with arrow signature`() {
        val parsed =
            RescriptDeclarationSignatureExtractor.parseDeclaration(
                "let add: (int, int) => int = (a, b) => a + b",
            )
        assertNotNull(parsed)
        assertEquals("add", parsed!!.name)
        assertEquals("(int, int) => int", parsed.signatureText)
    }

    @Test
    fun `parseDeclaration finds let rec with annotation`() {
        val parsed =
            RescriptDeclarationSignatureExtractor.parseDeclaration(
                "let rec loop: int => unit = n => loop(n - 1)",
            )
        assertNotNull(parsed)
        assertEquals("loop", parsed!!.name)
        assertEquals("int => unit", parsed.signatureText)
    }

    @Test
    fun `parseDeclaration finds external annotation`() {
        val parsed =
            RescriptDeclarationSignatureExtractor.parseDeclaration(
                "external add: (int, int) => int = \"add\"",
            )
        assertNotNull(parsed)
        assertEquals("add", parsed!!.name)
        assertEquals("(int, int) => int", parsed.signatureText)
    }

    @Test
    fun `parseDeclaration returns null when annotation is missing`() {
        assertNull(RescriptDeclarationSignatureExtractor.parseDeclaration("let x = 42"))
    }

    @Test
    fun `parseDeclaration returns null for unrelated declaration`() {
        assertNull(RescriptDeclarationSignatureExtractor.parseDeclaration("module M = {}"))
    }

    @Test
    fun `parseDeclaration captures generic type signature`() {
        val parsed =
            RescriptDeclarationSignatureExtractor.parseDeclaration(
                "let map: (option<'a>, 'a => 'b) => option<'b> = (opt, f) => ...",
            )
        assertNotNull(parsed)
        assertEquals("map", parsed!!.name)
        assertEquals("(option<'a>, 'a => 'b) => option<'b>", parsed.signatureText)
    }

    @Test
    fun `parseDeclaration handles type alias`() {
        val parsed = RescriptDeclarationSignatureExtractor.parseDeclaration("type alias: int = ...")
        assertNotNull(parsed)
        assertEquals("alias", parsed!!.name)
        assertEquals("int", parsed.signatureText)
    }

    // ── findBindingEquals ────────────────────────────────────────

    @Test
    fun `findBindingEquals returns offset of depth-0 equals`() {
        // " int = 42" — first '=' is at index 5
        assertEquals(5, RescriptDeclarationSignatureExtractor.findBindingEquals(" int = 42"))
    }

    @Test
    fun `findBindingEquals skips arrows`() {
        // "(int) => int = body" — the '=' inside '=>' must be ignored
        val text = "(int) => int = body"
        assertEquals(text.indexOf("= body"), RescriptDeclarationSignatureExtractor.findBindingEquals(text))
    }

    @Test
    fun `findBindingEquals skips equals inside angle brackets`() {
        // option<int = nope> = body — first '=' is inside '<...>'
        val text = "option<int> = body"
        assertEquals(text.indexOf("= body"), RescriptDeclarationSignatureExtractor.findBindingEquals(text))
    }

    @Test
    fun `findBindingEquals returns -1 when no binding equals exists`() {
        assertEquals(-1, RescriptDeclarationSignatureExtractor.findBindingEquals("(int, int) => int"))
    }
}
