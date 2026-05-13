package com.rescript.plugin.narrowing

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Drives [RescriptNarrowingHintProvider.buildHints] end-to-end through
 * its three collaborators (collector, resolver stub, presenter) to
 * ensure the pipeline behaves correctly without a live LSP.
 *
 * The actual rendering through `InlayHintsSink` and `PresentationFactory`
 * is exercised only at runtime and falls under the IntelliJ Platform
 * fixture exemption documented in tasklist.md.
 */
class RescriptNarrowingHintProviderTest {
    private fun resolverReturning(result: String?): RescriptHoverTypeResolver =
        RescriptHoverTypeResolver { _ -> result }

    @Test
    fun `produces a hint per arm when LSP returns a type`() {
        // Use an or-pattern-free fixture with no bindings so we can
        // assert exactly one hint per arm (the arrow-anchored hint).
        val source =
            """
            let f = x =>
              switch x {
              | "a" => 1
              | _ => 0
              }
            """.trimIndent()
        val hints = RescriptNarrowingHintProvider.buildHints(source, resolverReturning("option<int>"))
        assertEquals(2, hints.size)
        assertTrue(hints.all { it.displayText == "option<int>" })
    }

    @Test
    fun `skips arms when LSP returns null`() {
        val source =
            """
            let f = x =>
              switch x {
              | Some(_) => 1
              | None => 0
              }
            """.trimIndent()
        assertEquals(
            emptyList<RescriptNarrowingHintProvider.HintEntry>(),
            RescriptNarrowingHintProvider.buildHints(source, resolverReturning(null)),
        )
    }

    @Test
    fun `skips arms with trivial types`() {
        val source =
            """
            let f = x =>
              switch x {
              | _ => ()
              }
            """.trimIndent()
        assertEquals(
            emptyList<RescriptNarrowingHintProvider.HintEntry>(),
            RescriptNarrowingHintProvider.buildHints(source, resolverReturning("unit")),
        )
    }

    @Test
    fun `truncates long types in display`() {
        val source =
            """
            let f = x =>
              switch x {
              | _ => 0
              }
            """.trimIndent()
        val long = "ReallyLongType".repeat(20)
        val hints = RescriptNarrowingHintProvider.buildHints(source, resolverReturning(long))
        assertEquals(1, hints.size)
        // 64-char cap from the presenter; final character must be the ellipsis sentinel.
        assertEquals(64, hints.first().displayText.length)
    }

    @Test
    fun `respects the per-file arm cap`() {
        val arms =
            (1..(RescriptNarrowingHintProvider.MAX_ARMS_PER_FILE + 1))
                .joinToString("\n") { "| C$it => 0" }
        val source =
            """
            let f = x =>
              switch x {
              $arms
              }
            """.trimIndent()
        assertEquals(
            emptyList<RescriptNarrowingHintProvider.HintEntry>(),
            RescriptNarrowingHintProvider.buildHints(source, resolverReturning("int")),
        )
    }

    @Test
    fun `anchors hints at arrow offsets reported by collector`() {
        // Use a fixture with no bindings (only `_`) so each arm
        // produces exactly the arrow hint and the comparison stays
        // exact.
        val source =
            """
            let f = x =>
              switch x {
              | Some(_) => 1
              | None => 0
              }
            """.trimIndent()
        val arms = RescriptSwitchArmCollector.collect(source)
        val hints = RescriptNarrowingHintProvider.buildHints(source, resolverReturning("int"))
        assertEquals(arms.map { it.arrowOffset }, hints.map { it.offset })
    }

    // ── Phase 2: binding-anchored hints ────────────────────────────

    @Test
    fun `emits a binding hint after each pattern variable`() {
        val source =
            """
            let f = x =>
              switch x {
              | Some(v) => v
              | None => 0
              }
            """.trimIndent()
        val hints = RescriptNarrowingHintProvider.buildHints(source, resolverReturning("int"))
        val arms = RescriptSwitchArmCollector.collect(source)
        // Expected: 1 arrow hint per arm (2) + 1 binding hint for `v` = 3
        assertEquals(3, hints.size)
        val someArm = arms.first { it.patternSummary == "Some(_)" }
        val bindingOffset = someArm.bindingOffsets.single()
        assertTrue(
            hints.any { it.offset == bindingOffset && it.displayText == "int" },
            "expected a binding hint at offset $bindingOffset",
        )
    }

    @Test
    fun `binding hints are skipped when LSP returns a trivial type`() {
        val source =
            """
            let f = x =>
              switch x {
              | Some(v) => v
              }
            """.trimIndent()
        // resolver returns unit for everything → both arrow and
        // binding hints filtered by presenter
        assertEquals(
            emptyList<RescriptNarrowingHintProvider.HintEntry>(),
            RescriptNarrowingHintProvider.buildHints(source, resolverReturning("unit")),
        )
    }

    @Test
    fun `multi-arg constructor produces one binding hint per binding`() {
        val source =
            """
            let f = x =>
              switch x {
              | Pair(left, right) => left + right
              }
            """.trimIndent()
        val hints = RescriptNarrowingHintProvider.buildHints(source, resolverReturning("int"))
        // 1 arrow hint + 2 binding hints
        assertEquals(3, hints.size)
    }

    @Test
    fun `bare LIDENT catch-all arm produces only an arrow hint`() {
        val source =
            """
            let f = x =>
              switch x {
              | Some(_) => 1
              | other => 0
              }
            """.trimIndent()
        val hints = RescriptNarrowingHintProvider.buildHints(source, resolverReturning("int"))
        // Two arrow hints; no extra binding hint for `other`.
        assertEquals(2, hints.size)
    }
}
