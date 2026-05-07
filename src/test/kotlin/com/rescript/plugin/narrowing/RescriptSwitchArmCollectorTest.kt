package com.rescript.plugin.narrowing

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Verifies that [RescriptSwitchArmCollector] correctly identifies
 * arm boundaries across the most common ReScript pattern shapes
 * (option, result, list, polymorphic and custom variants), and that
 * it handles nested switches, or-patterns, and `when` guards.
 */
class RescriptSwitchArmCollectorTest {
    private fun summaries(source: String): List<String> =
        RescriptSwitchArmCollector.collect(source).map { it.patternSummary }

    @Test
    fun `option pattern produces Some and None summaries`() {
        val source =
            """
            let f = x =>
              switch x {
              | Some(v) => v
              | None => 0
              }
            """.trimIndent()
        assertEquals(listOf("Some(_)", "None"), summaries(source))
    }

    @Test
    fun `result pattern produces Ok and Error summaries`() {
        val source =
            """
            let g = r =>
              switch r {
              | Ok(v) => v
              | Error(_) => 0
              }
            """.trimIndent()
        assertEquals(listOf("Ok(_)", "Error(_)"), summaries(source))
    }

    @Test
    fun `list pattern produces head literal and underscore`() {
        val source =
            """
            let h = xs =>
              switch xs {
              | list{} => 0
              | list{head, ..._tail} => head
              }
            """.trimIndent()
        val arms = RescriptSwitchArmCollector.collect(source)
        assertEquals(2, arms.size)
        // patternSummary is the leading token text; both arms start with `list`
        assertEquals("list", arms[0].patternSummary)
    }

    @Test
    fun `polymorphic variant pattern is summarized`() {
        val source =
            """
            let p = v =>
              switch v {
              | #foo => 1
              | #bar(_) => 2
              }
            """.trimIndent()
        val arms = RescriptSwitchArmCollector.collect(source)
        assertEquals(2, arms.size)
        // POLY_VARIANT token text includes the leading hash; just verify non-empty
        assertTrue(arms[0].patternSummary.isNotEmpty())
        assertTrue(arms[1].patternSummary.isNotEmpty())
    }

    @Test
    fun `custom variant produces constructor summary`() {
        val source =
            """
            let c = s =>
              switch s {
              | Loading => 0
              | Loaded(_x) => 1
              | Failed(_e) => -1
              }
            """.trimIndent()
        assertEquals(listOf("Loading", "Loaded(_)", "Failed(_)"), summaries(source))
    }

    @Test
    fun `or pattern keeps only first arm pipe as boundary`() {
        val source =
            """
            let o = v =>
              switch v {
              | Some(_) | None => 0
              | _ => 1
              }
            """.trimIndent()
        // The or-pattern collapses into a single arm.
        val arms = RescriptSwitchArmCollector.collect(source)
        assertEquals(2, arms.size)
        assertEquals("Some(_)", arms[0].patternSummary)
    }

    @Test
    fun `when guard is excluded from pattern summary`() {
        val source =
            """
            let w = v =>
              switch v {
              | Some(x) when x > 0 => x
              | _ => 0
              }
            """.trimIndent()
        assertEquals(listOf("Some(_)", "_"), summaries(source))
    }

    @Test
    fun `nested switch is collected separately`() {
        val source =
            """
            let n = (a, b) =>
              switch a {
              | Some(x) =>
                switch b {
                | Ok(_) => x
                | Error(_) => 0
                }
              | None => 0
              }
            """.trimIndent()
        val arms = RescriptSwitchArmCollector.collect(source)
        // Outer 2 + inner 2 = 4 arms total.
        assertEquals(4, arms.size)
        assertEquals(setOf("Some(_)", "None", "Ok(_)", "Error(_)"), arms.map { it.patternSummary }.toSet())
    }

    @Test
    fun `unterminated arm without arrow is skipped`() {
        val source =
            """
            let u = x =>
              switch x {
              | Some(v)
              }
            """.trimIndent()
        val arms = RescriptSwitchArmCollector.collect(source)
        assertEquals(0, arms.size)
    }

    @Test
    fun `source without switch returns empty list`() {
        val source =
            """
            let f = x => x + 1
            module M = {
              let g = (a, b) => a + b
            }
            """.trimIndent()
        assertEquals(emptyList<String>(), summaries(source))
    }

    @Test
    fun `scrutinee range covers expression between switch and brace`() {
        val source =
            """
            let f = x =>
              switch x->Js.String.toUpperCase {
              | _ => x
              }
            """.trimIndent()
        val arm = RescriptSwitchArmCollector.collect(source).first()
        val scrutinee = source.substring(arm.scrutineeRange.startOffset, arm.scrutineeRange.endOffset)
        assertEquals("x->Js.String.toUpperCase", scrutinee.trim())
    }

    @Test
    fun `arrow offset points immediately after the arrow token`() {
        val source = "let f = x => switch x { | _ => 0 }"
        val arm = RescriptSwitchArmCollector.collect(source).first()
        // `=>` ends at position of the first character after the arrow.
        assertEquals("=>", source.substring(arm.arrowOffset - 2, arm.arrowOffset))
    }

    @Test
    fun `body end offset spans up to the next pipe or closing brace`() {
        val source =
            """
            let f = x =>
              switch x {
              | Some(v) => v + 1
              | None => 0
              }
            """.trimIndent()
        val arms = RescriptSwitchArmCollector.collect(source)
        val firstBody = source.substring(arms[0].arrowOffset, arms[0].bodyEndOffset)
        assertEquals("v + 1", firstBody.trim())
        val secondBody = source.substring(arms[1].arrowOffset, arms[1].bodyEndOffset)
        assertEquals("0", secondBody.trim())
    }

    @Test
    fun `nested switch keeps outer arm body end at outer boundary`() {
        val source =
            """
            let n = (a, b) =>
              switch a {
              | Some(_) =>
                switch b {
                | Ok(_) => 1
                | Error(_) => 2
                }
              | None => 0
              }
            """.trimIndent()
        val arms = RescriptSwitchArmCollector.collect(source)
        val outerSomeArm = arms.first { it.patternSummary == "Some(_)" }
        val outerSomeBody = source.substring(outerSomeArm.arrowOffset, outerSomeArm.bodyEndOffset)
        // The Some(_) arm body must include the entire inner switch.
        org.junit.jupiter.api.Assertions
            .assertTrue(outerSomeBody.contains("switch b {"))
        org.junit.jupiter.api.Assertions
            .assertTrue(outerSomeBody.contains("Error(_) => 2"))
    }
}
