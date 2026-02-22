package com.rescript.plugin.imports

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptImportUtilTest {
    // -- findOpenInsertOffset --

    @Test
    fun testFindOpenInsertOffsetAfterLastOpenStatement() {
        val text =
            """
            open Belt
            open Array
            let x = 42
            """.trimIndent()
        val offset = RescriptImportUtil.findOpenInsertOffset(text)
        assertEquals(text.indexOf("let x"), offset)
    }

    @Test
    fun testFindOpenInsertOffsetAfterCommentsWhenNoOpen() {
        val text =
            """
            // This is a comment

            let x = 42
            """.trimIndent()
        val offset = RescriptImportUtil.findOpenInsertOffset(text)
        assertEquals(text.indexOf("let x"), offset)
    }

    @Test
    fun testFindOpenInsertOffsetReturnsZeroForCodeAtStart() {
        val text = "let x = 42"
        val offset = RescriptImportUtil.findOpenInsertOffset(text)
        assertEquals(0, offset)
    }

    @Test
    fun testFindOpenInsertOffsetSingleOpenStatement() {
        val text =
            """
            open Belt
            let x = 42
            """.trimIndent()
        val offset = RescriptImportUtil.findOpenInsertOffset(text)
        assertEquals(text.indexOf("let x"), offset)
    }

    @Test
    fun testFindOpenInsertOffsetOpenAtEndOfFile() {
        val text = "open Belt"
        val offset = RescriptImportUtil.findOpenInsertOffset(text)
        assertEquals(text.length, offset)
    }

    // -- collectOpenModules --

    @Test
    fun testCollectOpenModulesReturnsModuleNames() {
        val text =
            """
            open Belt
            open Array
            let x = 42
            """.trimIndent()
        val modules = RescriptImportUtil.collectOpenModules(text)
        assertEquals(listOf("Belt", "Array"), modules)
    }

    @Test
    fun testCollectOpenModulesReturnsEmptyWhenNoOpens() {
        val modules = RescriptImportUtil.collectOpenModules("let x = 42")
        assertTrue(modules.isEmpty())
    }

    @Test
    fun testCollectOpenModulesHandlesNestedModules() {
        val text =
            """
            open Belt.Array
            open Js.String2
            """.trimIndent()
        val modules = RescriptImportUtil.collectOpenModules(text)
        assertEquals(listOf("Belt.Array", "Js.String2"), modules)
    }

    @Test
    fun testCollectOpenModulesIgnoresNonOpenLines() {
        val text =
            """
            // open Belt
            let open = "not a module"
            open Array
            """.trimIndent()
        val modules = RescriptImportUtil.collectOpenModules(text)
        assertEquals(listOf("Array"), modules)
    }

    // -- isModuleOpened --

    @Test
    fun testIsModuleOpenedReturnsTrueWhenOpened() {
        val text =
            """
            open Belt
            open Array
            let x = Array.map(arr, f)
            """.trimIndent()
        assertTrue(RescriptImportUtil.isModuleOpened(text, "Belt"))
        assertTrue(RescriptImportUtil.isModuleOpened(text, "Array"))
    }

    @Test
    fun testIsModuleOpenedReturnsFalseWhenNotOpened() {
        val text =
            """
            open Belt
            let x = Array.map(arr, f)
            """.trimIndent()
        assertFalse(RescriptImportUtil.isModuleOpened(text, "Array"))
    }

    @Test
    fun testIsModuleOpenedReturnsFalseForPartialMatch() {
        val text = "open BeltArray"
        assertFalse(RescriptImportUtil.isModuleOpened(text, "Belt"))
    }

    @Test
    fun testIsModuleOpenedReturnsFalseForEmptyText() {
        assertFalse(RescriptImportUtil.isModuleOpened("", "Belt"))
    }

    @Test
    fun testIsModuleOpenedIgnoresIndentedOpens() {
        val text =
            """
            module M = {
              open Belt
            }
            """.trimIndent()
        assertFalse(RescriptImportUtil.isModuleOpened(text, "Belt"))
    }
}
