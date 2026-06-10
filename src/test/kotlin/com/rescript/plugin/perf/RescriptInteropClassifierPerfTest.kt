package com.rescript.plugin.perf

import com.rescript.plugin.interop.RescriptInteropClassifier
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Smoke benchmark for [RescriptInteropClassifier.classify].
 *
 * Sweeps 10000 representative lines through the classifier and
 * asserts the cumulative cost stays below `BASELINE_MS * SLACK_FACTOR`.
 * BASELINE_MS is the expected cost on a 2024+ developer machine and
 * should ratchet downward as the implementation improves; raise it
 * only with an explicit justification in the commit message.
 */
class RescriptInteropClassifierPerfTest {
    private val iterations = 10_000

    @Test
    fun `classify 10000 lines finishes within bound`() {
        val samples =
            listOf(
                "let value = 1 + 2",
                "external alert: string => unit = \"alert\"",
                "@bs.send external setTitle: (window, string) => unit = \"setTitle\"",
                "let coerce = payload => Obj.magic(payload)",
                "%raw(\"console.log('hi')\")",
                "@bs.module(\"chalk\") external chalk: chalk = \"default\"",
            )
        // Warmup: classloader + JIT compilation otherwise dominates the
        // first invocation and would mask real algorithmic regressions.
        for (s in samples) RescriptInteropClassifier.classify(s)
        val started = System.nanoTime()
        var matched = 0
        for (i in 0 until iterations) {
            val line = samples[i % samples.size]
            if (RescriptInteropClassifier.classify(line) != null) matched++
        }
        val elapsedMs = (System.nanoTime() - started) / 1_000_000
        val ratio = elapsedMs.toDouble() / BASELINE_MS
        println(
            "perf[RescriptInteropClassifierPerfTest] elapsed=${elapsedMs}ms " +
                "baseline=${BASELINE_MS}ms ratio=${"%.2f".format(ratio)} limit=${TIME_LIMIT_MS}ms",
        )
        assertTrue(
            elapsedMs < TIME_LIMIT_MS,
            "elapsed=${elapsedMs}ms exceeded baseline*slack=${TIME_LIMIT_MS}ms " +
                "(baseline=${BASELINE_MS}ms, slack=${SLACK_FACTOR}x)",
        )
        // Sanity: every sample except the first ("let value = …") matches,
        // so 5/6 of the iterations should classify.
        assertTrue(matched > iterations / 2, "expected most samples to match, matched=$matched")
    }

    companion object {
        private const val BASELINE_MS = 200L
        private const val SLACK_FACTOR = 2.5
        private const val TIME_LIMIT_MS = (BASELINE_MS * SLACK_FACTOR).toLong()
    }
}
