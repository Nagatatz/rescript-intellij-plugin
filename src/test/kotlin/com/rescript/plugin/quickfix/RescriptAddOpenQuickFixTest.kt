package com.rescript.plugin.quickfix

import com.rescript.plugin.imports.RescriptImportUtil
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/** Tests for [RescriptAddOpenQuickFix] and related import utilities. */
class RescriptAddOpenQuickFixTest {
    @Test
    fun `findOpenInsertOffset returns offset after last open statement`() {
        val text =
            """
            open Belt
            open Array
            let x = 42
            """.trimIndent()
        val offset = RescriptImportUtil.findOpenInsertOffset(text)
        // Should be right after "open Array\n"
        assertEquals(text.indexOf("let x"), offset)
    }

    @Test
    fun `findOpenInsertOffset returns offset after comments when no open`() {
        val text =
            """
            // This is a comment

            let x = 42
            """.trimIndent()
        val offset = RescriptImportUtil.findOpenInsertOffset(text)
        // Should be after the comment and blank line
        assertEquals(text.indexOf("let x"), offset)
    }

    @Test
    fun `findOpenInsertOffset returns 0 for file starting with code`() {
        val text = "let x = 42"
        val offset = RescriptImportUtil.findOpenInsertOffset(text)
        assertEquals(0, offset)
    }

    @Test
    fun `findOpenInsertOffset handles single open statement`() {
        val text =
            """
            open Belt
            let x = 42
            """.trimIndent()
        val offset = RescriptImportUtil.findOpenInsertOffset(text)
        assertEquals(text.indexOf("let x"), offset)
    }

    @Test
    fun `intention text is correct`() {
        val fix = RescriptAddOpenQuickFix()
        assertEquals("Add open statement", fix.text)
        assertEquals("Add open statement", fix.familyName)
    }
}
