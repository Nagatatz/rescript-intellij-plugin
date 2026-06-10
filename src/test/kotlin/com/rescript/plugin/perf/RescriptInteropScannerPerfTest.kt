package com.rescript.plugin.perf

import com.intellij.testFramework.LightVirtualFile
import com.rescript.plugin.interop.InteropEntry
import com.rescript.plugin.interop.RescriptInteropScanner
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Smoke benchmark for [RescriptInteropScanner.collectEntriesFromText].
 *
 * Builds a synthetic ~100KB ReScript file (1000 lines × ~100 chars)
 * laced with interop matches and asserts the line walker completes
 * within `BASELINE_MS * SLACK_FACTOR`. Ratchet BASELINE_MS downward
 * over time; only raise it with an explicit justification.
 */
class RescriptInteropScannerPerfTest {
    private val lineCount = 1000

    @Test
    fun `collectEntriesFromText on a 100KB file finishes within bound`() {
        val source = generateSource()
        val file = LightVirtualFile("Bench.res")
        // Warmup: classloader + JIT compilation otherwise dominates the
        // first invocation and would mask real algorithmic regressions.
        RescriptInteropScanner.collectEntriesFromText(file, source, maxEntries = Int.MAX_VALUE)
        val started = System.nanoTime()
        val entries: List<InteropEntry> =
            RescriptInteropScanner.collectEntriesFromText(file, source, maxEntries = Int.MAX_VALUE)
        val elapsedMs = (System.nanoTime() - started) / 1_000_000
        val ratio = elapsedMs.toDouble() / BASELINE_MS
        println(
            "perf[RescriptInteropScannerPerfTest] elapsed=${elapsedMs}ms " +
                "baseline=${BASELINE_MS}ms ratio=${"%.2f".format(ratio)} limit=${TIME_LIMIT_MS}ms",
        )
        assertTrue(
            elapsedMs < TIME_LIMIT_MS,
            "elapsed=${elapsedMs}ms exceeded baseline*slack=${TIME_LIMIT_MS}ms " +
                "(baseline=${BASELINE_MS}ms, slack=${SLACK_FACTOR}x)",
        )
        assertTrue(entries.isNotEmpty(), "expected at least some interop matches in the synthetic source")
    }

    companion object {
        private const val BASELINE_MS = 200L
        private const val SLACK_FACTOR = 2.5
        private const val TIME_LIMIT_MS = (BASELINE_MS * SLACK_FACTOR).toLong()
    }

    private fun generateSource(): String =
        buildString {
            for (i in 0 until lineCount) {
                when (i % 5) {
                    0 -> appendLine("// padding comment line $i ${"x".repeat(80)}")
                    1 -> appendLine("let value$i = 1 + 2 + ${"a".repeat(60)}")
                    2 -> appendLine("external alert$i: string => unit = \"alert$i\"")
                    3 -> appendLine("let coerce$i = payload => Obj.magic(payload)")
                    4 -> appendLine("@bs.send external setTitle$i: (window, string) => unit = \"setTitle$i\"")
                }
            }
        }
}
