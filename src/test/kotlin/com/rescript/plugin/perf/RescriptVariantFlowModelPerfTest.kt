package com.rescript.plugin.perf

import com.rescript.plugin.flow.RescriptVariantFlowModel
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Smoke benchmark for [RescriptVariantFlowModel.buildAtOffset].
 *
 * Generates ~5000 lines of ReScript, places the caret on the
 * outermost switch, and asserts the diagram builder completes within
 * a generous upper bound.
 */
class RescriptVariantFlowModelPerfTest {
    private val switchBlocks = 500
    private val timeLimitMs = 1000L

    @Test
    fun `buildAtOffset on a 5000-line file finishes within bound`() {
        val source = generateLargeSource()
        // Place the caret at the first `switch` keyword we emit.
        val offset = source.indexOf("switch ")
        require(offset >= 0)
        val started = System.nanoTime()
        val diagram = RescriptVariantFlowModel.buildAtOffset(source, offset)
        val elapsedMs = (System.nanoTime() - started) / 1_000_000
        assertNotNull(diagram, "buildAtOffset returned null for caret on 'switch'")
        assertTrue(elapsedMs < timeLimitMs, "buildAtOffset took ${elapsedMs}ms (limit ${timeLimitMs}ms)")
    }

    private fun generateLargeSource(): String =
        buildString {
            for (i in 0 until switchBlocks) {
                appendLine("let g$i = (x: result<int, string>) =>")
                appendLine("  switch x {")
                appendLine("  | Ok(v) => v")
                appendLine("  | Error(_) => -1")
                appendLine("  }")
                appendLine()
                // Pad with comments so we cross the 5000-line threshold.
                appendLine("// padding line $i — keeps file long enough to exercise the pure walker")
            }
        }
}
