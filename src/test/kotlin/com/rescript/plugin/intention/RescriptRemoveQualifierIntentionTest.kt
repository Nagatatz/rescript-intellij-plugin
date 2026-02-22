package com.rescript.plugin.intention

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Tests for [RescriptRemoveQualifierIntention] static helper methods. */
class RescriptRemoveQualifierIntentionTest {
    @Test
    fun `isModuleOpened returns true when module is opened`() {
        val text =
            """
            open Belt
            open Array
            let x = Array.map(arr, f)
            """.trimIndent()
        assertTrue(RescriptRemoveQualifierIntention.isModuleOpened(text, "Belt"))
        assertTrue(RescriptRemoveQualifierIntention.isModuleOpened(text, "Array"))
    }

    @Test
    fun `isModuleOpened returns false when module is not opened`() {
        val text =
            """
            open Belt
            let x = Array.map(arr, f)
            """.trimIndent()
        assertFalse(RescriptRemoveQualifierIntention.isModuleOpened(text, "Array"))
    }

    @Test
    fun `isModuleOpened returns false for partial name match`() {
        val text = "open BeltArray"
        assertFalse(RescriptRemoveQualifierIntention.isModuleOpened(text, "Belt"))
    }

    @Test
    fun `isModuleOpened returns false for empty text`() {
        assertFalse(RescriptRemoveQualifierIntention.isModuleOpened("", "Belt"))
    }

    @Test
    fun `isModuleOpened ignores indented opens`() {
        val text =
            """
            module M = {
              open Belt
            }
            """.trimIndent()
        // Indented "open Belt" should not match (starts with spaces, not ^)
        assertFalse(RescriptRemoveQualifierIntention.isModuleOpened(text, "Belt"))
    }

    @Test
    fun `intention text is correct`() {
        val intention = RescriptRemoveQualifierIntention()
        assertEquals("Remove redundant qualifier", intention.text)
        assertEquals("Remove redundant qualifier", intention.familyName)
    }
}
