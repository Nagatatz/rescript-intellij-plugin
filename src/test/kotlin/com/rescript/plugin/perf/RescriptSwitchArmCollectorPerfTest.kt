package com.rescript.plugin.perf

import com.rescript.plugin.narrowing.RescriptSwitchArmCollector
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Smoke benchmark for [RescriptSwitchArmCollector.collect].
 *
 * Generates a 1000-line ReScript source with 50 switch expressions
 * (4 arms each) and asserts the collector finishes within
 * `BASELINE_MS * SLACK_FACTOR`. Ratchet BASELINE_MS downward over time;
 * raise it only with an explicit justification.
 */
class RescriptSwitchArmCollectorPerfTest {
    private val armsPerSwitch = 4
    private val switchCount = 50

    @Test
    fun `collect 200 arms across 50 switches finishes within bound`() {
        val source = generateSource()
        // Warmup: classloader + JIT compilation otherwise dominates the
        // first invocation and would mask real algorithmic regressions.
        RescriptSwitchArmCollector.collect(source)
        val started = System.nanoTime()
        val arms = RescriptSwitchArmCollector.collect(source)
        val elapsedMs = (System.nanoTime() - started) / 1_000_000
        val ratio = elapsedMs.toDouble() / BASELINE_MS
        println(
            "perf[RescriptSwitchArmCollectorPerfTest] elapsed=${elapsedMs}ms " +
                "baseline=${BASELINE_MS}ms ratio=${"%.2f".format(ratio)} limit=${TIME_LIMIT_MS}ms",
        )
        assertEquals(armsPerSwitch * switchCount, arms.size, "expected ${armsPerSwitch * switchCount} arms")
        assertTrue(
            elapsedMs < TIME_LIMIT_MS,
            "elapsed=${elapsedMs}ms exceeded baseline*slack=${TIME_LIMIT_MS}ms " +
                "(baseline=${BASELINE_MS}ms, slack=${SLACK_FACTOR}x)",
        )
    }

    companion object {
        private const val BASELINE_MS = 80L
        private const val SLACK_FACTOR = 2.5
        private const val TIME_LIMIT_MS = (BASELINE_MS * SLACK_FACTOR).toLong()
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
