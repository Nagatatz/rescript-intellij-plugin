package com.rescript.plugin.navigation

import com.intellij.mock.MockVirtualFile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Unit tests for the helpers exposed on
 * [RescriptTypeSignatureSearchContributor]'s companion. The end-to-end
 * Search Everywhere flow needs an IntelliJ Platform fixture and is
 * verified by hand in `runIde`; these cases lock down the small
 * `relativeOf` path-display helper. Declaration-text parsing is owned
 * by [RescriptDeclarationSignatureExtractor] and tested separately in
 * `RescriptDeclarationSignatureExtractorTest`.
 */
class RescriptTypeSignatureSearchContributorTest {
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
