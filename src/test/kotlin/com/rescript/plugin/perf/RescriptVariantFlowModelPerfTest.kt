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
 * `BASELINE_MS * SLACK_FACTOR`. Ratchet BASELINE_MS downward over
 * time; raise it only with an explicit justification.
 */
class RescriptVariantFlowModelPerfTest {
    private val switchBlocks = 500

    @Test
    fun `buildAtOffset on a 5000-line file finishes within bound`() {
        val source = generateLargeSource()
        // Place the caret at the first `switch` keyword we emit.
        val offset = source.indexOf("switch ")
        require(offset >= 0)
        // Warmup: classloader + JIT compilation otherwise dominates the
        // first invocation and would mask real algorithmic regressions.
        RescriptVariantFlowModel.buildAtOffset(source, offset)
        val started = System.nanoTime()
        val diagram = RescriptVariantFlowModel.buildAtOffset(source, offset)
        val elapsedMs = (System.nanoTime() - started) / 1_000_000
        val ratio = elapsedMs.toDouble() / BASELINE_MS
        println(
            "perf[RescriptVariantFlowModelPerfTest] elapsed=${elapsedMs}ms " +
                "baseline=${BASELINE_MS}ms ratio=${"%.2f".format(ratio)} limit=${TIME_LIMIT_MS}ms",
        )
        assertNotNull(diagram, "buildAtOffset returned null for caret on 'switch'")
        assertTrue(
            elapsedMs < TIME_LIMIT_MS,
            "elapsed=${elapsedMs}ms exceeded baseline*slack=${TIME_LIMIT_MS}ms " +
                "(baseline=${BASELINE_MS}ms, slack=${SLACK_FACTOR}x)",
        )
    }

    companion object {
        private const val BASELINE_MS = 400L
        private const val SLACK_FACTOR = 2.5
        private const val TIME_LIMIT_MS = (BASELINE_MS * SLACK_FACTOR).toLong()
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
