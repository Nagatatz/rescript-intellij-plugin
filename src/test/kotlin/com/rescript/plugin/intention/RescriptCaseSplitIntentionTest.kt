package com.rescript.plugin.intention

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** Tests for [RescriptCaseSplitIntention] static helper methods. */
class RescriptCaseSplitIntentionTest {
    @Test
    fun `isInsideSwitchPattern returns true when inside pattern`() {
        val text = "  | x => body"
        // offset at 'x' (index 4)
        assertTrue(RescriptCaseSplitIntention.isInsideSwitchPattern(text, 4))
    }

    @Test
    fun `isInsideSwitchPattern returns false when after arrow`() {
        val text = "  | x => body"
        // offset at 'b' in body (index 9)
        assertFalse(RescriptCaseSplitIntention.isInsideSwitchPattern(text, 9))
    }

    @Test
    fun `isInsideSwitchPattern returns false when no pipe`() {
        val text = "  let x = 42"
        assertFalse(RescriptCaseSplitIntention.isInsideSwitchPattern(text, 6))
    }

    @Test
    fun `isInsideSwitchPattern returns false when no arrow`() {
        val text = "  | x"
        assertFalse(RescriptCaseSplitIntention.isInsideSwitchPattern(text, 4))
    }

    @Test
    fun `isInsideSwitchPattern works with multiline text`() {
        val text =
            """
            switch x {
            | A => 1
            | y => 2
            }
            """.trimIndent()
        // Find position of 'y' in "| y => 2"
        val yIndex = text.indexOf("| y") + 2
        assertTrue(RescriptCaseSplitIntention.isInsideSwitchPattern(text, yIndex))
    }

    @Test
    fun `intention text is correct`() {
        val intention = RescriptCaseSplitIntention()
        assertEquals("Split into constructor cases", intention.text)
        assertEquals("Split into constructor cases", intention.familyName)
    }
}
