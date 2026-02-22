package com.rescript.plugin.intention

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Tests for [RescriptMergeSwitchCasesIntention] static helper methods. */
class RescriptMergeSwitchCasesIntentionTest {
    @Test
    fun `parseSwitchCases extracts simple cases`() {
        val switchBlock =
            """
            switch x {
            | A => "hello"
            | B => "world"
            | C => "hello"
            }
            """.trimIndent()

        val cases = RescriptMergeSwitchCasesIntention.parseSwitchCases(switchBlock)
        assertEquals(3, cases.size)
        assertEquals("A", cases[0].pattern)
        assertEquals("\"hello\"", cases[0].body)
        assertEquals("B", cases[1].pattern)
        assertEquals("\"world\"", cases[1].body)
    }

    @Test
    fun `hasDuplicateBodies detects duplicates`() {
        val cases =
            listOf(
                RescriptMergeSwitchCasesIntention.SwitchCase("A", "body1"),
                RescriptMergeSwitchCasesIntention.SwitchCase("B", "body2"),
                RescriptMergeSwitchCasesIntention.SwitchCase("C", "body1"),
            )
        assertTrue(RescriptMergeSwitchCasesIntention.hasDuplicateBodies(cases))
    }

    @Test
    fun `hasDuplicateBodies returns false for unique bodies`() {
        val cases =
            listOf(
                RescriptMergeSwitchCasesIntention.SwitchCase("A", "body1"),
                RescriptMergeSwitchCasesIntention.SwitchCase("B", "body2"),
            )
        assertFalse(RescriptMergeSwitchCasesIntention.hasDuplicateBodies(cases))
    }

    @Test
    fun `hasDuplicateBodies excludes wildcard pattern`() {
        val cases =
            listOf(
                RescriptMergeSwitchCasesIntention.SwitchCase("A", "body1"),
                RescriptMergeSwitchCasesIntention.SwitchCase("_", "body1"),
            )
        assertFalse(RescriptMergeSwitchCasesIntention.hasDuplicateBodies(cases))
    }

    @Test
    fun `mergeCases combines cases with same body`() {
        val cases =
            listOf(
                RescriptMergeSwitchCasesIntention.SwitchCase("A", "body1"),
                RescriptMergeSwitchCasesIntention.SwitchCase("B", "body2"),
                RescriptMergeSwitchCasesIntention.SwitchCase("C", "body1"),
            )
        val merged = RescriptMergeSwitchCasesIntention.mergeCases(cases)
        assertEquals(2, merged.size)
        assertEquals("A | C", merged[0].pattern)
        assertEquals("body1", merged[0].body)
        assertEquals("B", merged[1].pattern)
    }

    @Test
    fun `mergeCases preserves wildcard pattern separately`() {
        val cases =
            listOf(
                RescriptMergeSwitchCasesIntention.SwitchCase("A", "body1"),
                RescriptMergeSwitchCasesIntention.SwitchCase("_", "body1"),
            )
        val merged = RescriptMergeSwitchCasesIntention.mergeCases(cases)
        assertEquals(2, merged.size)
        // Wildcard should be preserved separately
        assertTrue(merged.any { it.pattern == "_" })
        assertTrue(merged.any { it.pattern == "A" })
    }

    @Test
    fun `buildMergedSwitch generates correct output`() {
        val cases =
            listOf(
                RescriptMergeSwitchCasesIntention.SwitchCase("A | C", "body1"),
                RescriptMergeSwitchCasesIntention.SwitchCase("B", "body2"),
            )
        val result = RescriptMergeSwitchCasesIntention.buildMergedSwitch(cases)
        assertEquals("  | A | C => body1\n  | B => body2", result)
    }

    @Test
    fun `findSwitchBlock finds enclosing switch`() {
        val text =
            """
            let x = switch value {
            | A => 1
            | B => 2
            }
            """.trimIndent()

        val result = RescriptMergeSwitchCasesIntention.findSwitchBlock(text, 30)
        assertNotNull(result)
        assertTrue(result!!.startsWith("switch"))
    }

    @Test
    fun `findSwitchBlock returns null when not in switch`() {
        val text = "let x = 42"
        val result = RescriptMergeSwitchCasesIntention.findSwitchBlock(text, 5)
        assertNull(result)
    }
}
