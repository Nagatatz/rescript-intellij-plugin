package com.rescript.plugin.quickfix

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Tests for [RescriptQualifyReferenceQuickFix] static helper methods. */
class RescriptQualifyReferenceQuickFixTest {
    @Test
    fun `collectOpenModules returns module names from open statements`() {
        val text =
            """
            open Belt
            open Array
            let x = 42
            """.trimIndent()
        val modules = RescriptQualifyReferenceQuickFix.collectOpenModules(text)
        assertEquals(listOf("Belt", "Array"), modules)
    }

    @Test
    fun `collectOpenModules returns empty list when no opens`() {
        val text = "let x = 42"
        val modules = RescriptQualifyReferenceQuickFix.collectOpenModules(text)
        assertTrue(modules.isEmpty())
    }

    @Test
    fun `collectOpenModules handles nested module opens`() {
        val text =
            """
            open Belt.Array
            open Js.String2
            """.trimIndent()
        val modules = RescriptQualifyReferenceQuickFix.collectOpenModules(text)
        assertEquals(listOf("Belt.Array", "Js.String2"), modules)
    }

    @Test
    fun `collectOpenModules ignores non-open lines`() {
        val text =
            """
            // open Belt
            let open = "not a module"
            open Array
            """.trimIndent()
        val modules = RescriptQualifyReferenceQuickFix.collectOpenModules(text)
        assertEquals(listOf("Array"), modules)
    }

    @Test
    fun `intention text is correct`() {
        val fix = RescriptQualifyReferenceQuickFix()
        assertEquals("Qualify with module name", fix.text)
        assertEquals("Qualify with module name", fix.familyName)
    }
}
