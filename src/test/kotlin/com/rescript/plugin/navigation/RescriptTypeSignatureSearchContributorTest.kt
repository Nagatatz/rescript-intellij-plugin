package com.rescript.plugin.navigation

import com.intellij.mock.MockVirtualFile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

/**
 * Unit tests for the helpers exposed on
 * [RescriptTypeSignatureSearchContributor]'s companion. The end-to-end
 * Search Everywhere flow needs an IntelliJ Platform fixture and is
 * verified by hand in `runIde`; these cases lock down the small set
 * of pure helpers that actually drive the signature extraction and
 * display formatting.
 */
class RescriptTypeSignatureSearchContributorTest {
    // ── parseDeclaration ──

    @Test
    fun `parseDeclaration finds let with arrow signature`() {
        val parsed =
            RescriptTypeSignatureSearchContributor.parseDeclaration(
                "let add: (int, int) => int = (a, b) => a + b",
            )
        assertNotNull(parsed)
        assertEquals("add", parsed!!.name)
        assertEquals("(int, int) => int", parsed.signatureText)
    }

    @Test
    fun `parseDeclaration finds let rec with annotation`() {
        val parsed =
            RescriptTypeSignatureSearchContributor.parseDeclaration(
                "let rec loop: int => unit = n => loop(n - 1)",
            )
        assertNotNull(parsed)
        assertEquals("loop", parsed!!.name)
        assertEquals("int => unit", parsed.signatureText)
    }

    @Test
    fun `parseDeclaration finds external annotation`() {
        val parsed =
            RescriptTypeSignatureSearchContributor.parseDeclaration(
                "external add: (int, int) => int = \"add\"",
            )
        assertNotNull(parsed)
        assertEquals("add", parsed!!.name)
        assertEquals("(int, int) => int", parsed.signatureText)
    }

    @Test
    fun `parseDeclaration returns null when annotation is missing`() {
        assertNull(RescriptTypeSignatureSearchContributor.parseDeclaration("let x = 42"))
    }

    @Test
    fun `parseDeclaration returns null for unrelated declaration`() {
        assertNull(RescriptTypeSignatureSearchContributor.parseDeclaration("module M = {}"))
    }

    @Test
    fun `parseDeclaration captures generic type signature`() {
        val parsed =
            RescriptTypeSignatureSearchContributor.parseDeclaration(
                "let map: (option<'a>, 'a => 'b) => option<'b> = (opt, f) => ...",
            )
        assertNotNull(parsed)
        assertEquals("map", parsed!!.name)
        assertEquals("(option<'a>, 'a => 'b) => option<'b>", parsed.signatureText)
    }

    @Test
    fun `parseDeclaration handles type alias`() {
        val parsed = RescriptTypeSignatureSearchContributor.parseDeclaration("type alias: int = ...")
        assertNotNull(parsed)
        assertEquals("alias", parsed!!.name)
        assertEquals("int", parsed.signatureText)
    }

    // ── relativeOf ──

    @Test
    fun `relativeOf strips a matching base path prefix`() {
        val file = MockVirtualFile("Foo.res")
        // MockVirtualFile path is the file's name (no base directory).
        // Use the file's actual path as the base so the helper has
        // something concrete to strip.
        assertEquals("", RescriptTypeSignatureSearchContributor.relativeOf(file.path, file))
    }

    @Test
    fun `relativeOf returns full path when base path missing`() {
        val file = MockVirtualFile("Foo.res")
        // path is unanchored — falls through to `file.path`.
        assertEquals(file.path, RescriptTypeSignatureSearchContributor.relativeOf(null, file))
    }
}
