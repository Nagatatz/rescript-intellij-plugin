package com.rescript.plugin.intention

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
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
    fun `findSplitTarget consumes full multi-line arm body`() {
        val text =
            """
            switch x {
            | y => {
                let a = 1
                a + 1
              }
            | z => 0
            }
            """.trimIndent()
        val offset = text.indexOf("| y") + 2 // on 'y'

        val target = RescriptCaseSplitIntention.findSplitTarget(text, offset)
        assertNotNull(target)
        // The entire block body (all physical lines) must be captured, so no
        // trailing lines are orphaned after the replacement.
        assertTrue(target!!.body.contains("let a = 1"))
        assertTrue(target.body.contains("a + 1"))
        assertTrue(target.body.trimEnd().endsWith("}"))

        // The replaced range spans the whole arm: from its `|` line to the
        // closing brace of the body block, not just the first physical line.
        val replaced = text.substring(target.replaceStart, target.replaceEnd)
        assertTrue(replaced.startsWith("| y"))
        assertTrue(replaced.trimEnd().endsWith("}"))
        // The next arm `| z` must remain untouched after the replaced range.
        assertTrue(text.substring(target.replaceEnd).contains("| z => 0"))
    }

    @Test
    fun `findSplitTarget captures single-line arm body`() {
        val text =
            """
            switch x {
            | y => 42
            | z => 0
            }
            """.trimIndent()
        val offset = text.indexOf("| y") + 2

        val target = RescriptCaseSplitIntention.findSplitTarget(text, offset)
        assertNotNull(target)
        assertEquals("42", target!!.body)
        assertEquals("| y => 42", text.substring(target.replaceStart, target.replaceEnd))
    }

    @Test
    fun `findSplitTarget returns null when offset is not in a pattern`() {
        val text =
            """
            switch x {
            | y => 42
            }
            """.trimIndent()
        // Offset inside the body (on '42'), not the pattern.
        val offset = text.indexOf("42")
        assertNull(RescriptCaseSplitIntention.findSplitTarget(text, offset))
    }

    @Test
    fun `intention text is correct`() {
        val intention = RescriptCaseSplitIntention()
        assertEquals("Split into constructor cases", intention.text)
        assertEquals("Split into constructor cases", intention.familyName)
    }
}
