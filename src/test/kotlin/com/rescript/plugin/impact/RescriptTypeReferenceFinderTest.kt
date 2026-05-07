package com.rescript.plugin.impact

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Tests the pure helpers exported by [RescriptTypeReferenceFinder].
 * The IntelliJ Platform `PsiSearchHelper` integration falls under the
 * IDE-fixture exemption documented in tasklist.md; the line/preview
 * helper is exercised directly here.
 */
class RescriptTypeReferenceFinderTest {
    @Test
    fun `lineAndPreview returns 1-based line and trimmed source line`() {
        val source = "let a = 1\nlet b: t = 2\nlet c = 3\n"
        val offset = source.indexOf("t = 2")
        val (line, preview) = RescriptTypeReferenceFinder.lineAndPreview(source, offset)
        assertEquals(2, line)
        assertEquals("let b: t = 2", preview)
    }

    @Test
    fun `lineAndPreview handles offset on first line`() {
        val source = "type t = int\nlet x: t = 1"
        val (line, preview) = RescriptTypeReferenceFinder.lineAndPreview(source, 5)
        assertEquals(1, line)
        assertEquals("type t = int", preview)
    }

    @Test
    fun `lineAndPreview handles end-of-file offset`() {
        val source = "let a = 1"
        val (line, preview) = RescriptTypeReferenceFinder.lineAndPreview(source, source.length)
        assertEquals(1, line)
        assertEquals("let a = 1", preview)
    }

    @Test
    fun `lineAndPreview returns 0 and blank for negative offset`() {
        val (line, preview) = RescriptTypeReferenceFinder.lineAndPreview("anything", -1)
        assertEquals(0, line)
        assertEquals("", preview)
    }

    @Test
    fun `lineAndPreview trims leading whitespace`() {
        val source = "let a = 1\n    let b: t = 2"
        val offset = source.indexOf("t = 2")
        val (_, preview) = RescriptTypeReferenceFinder.lineAndPreview(source, offset)
        assertEquals("let b: t = 2", preview)
    }
}
