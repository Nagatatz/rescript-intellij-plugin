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
 * within a generous upper bound.
 */
class RescriptInteropScannerPerfTest {
    private val lineCount = 1000
    private val timeLimitMs = 500L

    @Test
    fun `collectEntriesFromText on a 100KB file finishes within bound`() {
        val source = generateSource()
        val file = LightVirtualFile("Bench.res")
        val started = System.nanoTime()
        val entries: List<InteropEntry> =
            RescriptInteropScanner.collectEntriesFromText(file, source, maxEntries = Int.MAX_VALUE)
        val elapsedMs = (System.nanoTime() - started) / 1_000_000
        assertTrue(elapsedMs < timeLimitMs, "collectEntriesFromText took ${elapsedMs}ms (limit ${timeLimitMs}ms)")
        assertTrue(entries.isNotEmpty(), "expected at least some interop matches in the synthetic source")
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
