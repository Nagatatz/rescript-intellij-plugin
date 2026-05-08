package com.rescript.plugin.perf

import com.rescript.plugin.narrowing.RescriptSwitchArmCollector
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Smoke benchmark for [RescriptSwitchArmCollector.collect].
 *
 * Generates a 1000-line ReScript source with 50 switch expressions
 * (4 arms each) and asserts the collector finishes within a generous
 * upper bound. The bound is set to ~10× the typical local M-series
 * runtime so CI noise does not flake the test.
 */
class RescriptSwitchArmCollectorPerfTest {
    private val armsPerSwitch = 4
    private val switchCount = 50
    private val timeLimitMs = 200L

    @Test
    fun `collect 200 arms across 50 switches finishes within bound`() {
        val source = generateSource()
        val started = System.nanoTime()
        val arms = RescriptSwitchArmCollector.collect(source)
        val elapsedMs = (System.nanoTime() - started) / 1_000_000
        assertEquals(armsPerSwitch * switchCount, arms.size, "expected ${armsPerSwitch * switchCount} arms")
        assertTrue(elapsedMs < timeLimitMs, "collect took ${elapsedMs}ms (limit ${timeLimitMs}ms)")
    }

    private fun generateSource(): String =
        buildString {
            for (i in 0 until switchCount) {
                appendLine("let f$i = (x: option<int>) =>")
                appendLine("  switch x {")
                appendLine("  | Some(v) => v + 1")
                appendLine("  | None => 0")
                appendLine("  | Some(v) when v > 100 => v / 2")
                appendLine("  | _ => -1")
                appendLine("  }")
                appendLine()
            }
        }
}
