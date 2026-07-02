package com.rescript.plugin.intention

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

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
    fun `parseSwitchCases does not split on pipe-forward in body`() {
        val switchBlock =
            """
            switch x {
            | A => list |> map
            | B => list |> filter
            }
            """.trimIndent()

        val cases = RescriptMergeSwitchCasesIntention.parseSwitchCases(switchBlock)
        assertEquals(2, cases.size)
        assertEquals("A", cases[0].pattern)
        assertEquals("list |> map", cases[0].body)
        assertEquals("B", cases[1].pattern)
        assertEquals("list |> filter", cases[1].body)
    }

    @Test
    fun `parseSwitchCases keeps a single-line or-pattern as one arm`() {
        // `| A | B => 1` is an or-pattern: the inner `|` before `=>` must stay
        // inside the pattern, not spawn a separate (bodyless) arm that drops A.
        val switchBlock =
            """
            switch x {
            | A | B => 1
            | C => 2
            }
            """.trimIndent()

        val cases = RescriptMergeSwitchCasesIntention.parseSwitchCases(switchBlock)
        assertEquals(2, cases.size)
        assertEquals("A | B", cases[0].pattern)
        assertEquals("1", cases[0].body)
        assertEquals("C", cases[1].pattern)
        assertEquals("2", cases[1].body)
    }

    @Test
    fun `parseSwitchCases does not split on logical or in body`() {
        val switchBlock =
            """
            switch x {
            | A => a || b
            | B => c
            }
            """.trimIndent()

        val cases = RescriptMergeSwitchCasesIntention.parseSwitchCases(switchBlock)
        assertEquals(2, cases.size)
        assertEquals("a || b", cases[0].body)
        assertEquals("c", cases[1].body)
    }

    @Test
    fun `parseSwitchCases keeps nested switch as a single arm body`() {
        val switchBlock =
            """
            switch x {
            | A =>
              switch y {
              | C => 1
              | D => 2
              }
            | B => 0
            }
            """.trimIndent()

        val cases = RescriptMergeSwitchCasesIntention.parseSwitchCases(switchBlock)
        // The inner `|` of the nested switch must not create extra outer arms.
        assertEquals(2, cases.size)
        assertEquals("A", cases[0].pattern)
        assertTrue(cases[0].body.contains("switch y"))
        assertTrue(cases[0].body.contains("| C => 1"))
        assertTrue(cases[0].body.contains("| D => 2"))
        assertEquals("B", cases[1].pattern)
        assertEquals("0", cases[1].body)
    }

    @Test
    fun `parseSwitchCases splits a multi-line arm body correctly`() {
        val switchBlock =
            """
            switch x {
            | A => {
                let v = 1
                v + 1
              }
            | B => 0
            }
            """.trimIndent()

        val cases = RescriptMergeSwitchCasesIntention.parseSwitchCases(switchBlock)
        assertEquals(2, cases.size)
        assertEquals("A", cases[0].pattern)
        assertTrue(cases[0].body.contains("let v = 1"))
        assertTrue(cases[0].body.trimEnd().endsWith("}"))
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
