package com.rescript.plugin.perf

import com.rescript.plugin.interop.RescriptInteropClassifier
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Smoke benchmark for [RescriptInteropClassifier.classify].
 *
 * Sweeps 10000 representative lines through the classifier and
 * asserts the cumulative cost stays below a generous upper bound.
 */
class RescriptInteropClassifierPerfTest {
    private val iterations = 10_000
    private val timeLimitMs = 500L

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
        val started = System.nanoTime()
        var matched = 0
        for (i in 0 until iterations) {
            val line = samples[i % samples.size]
            if (RescriptInteropClassifier.classify(line) != null) matched++
        }
        val elapsedMs = (System.nanoTime() - started) / 1_000_000
        assertTrue(elapsedMs < timeLimitMs, "classify sweep took ${elapsedMs}ms (limit ${timeLimitMs}ms)")
        // Sanity: every sample except the first ("let value = …") matches,
        // so 5/6 of the iterations should classify.
        assertTrue(matched > iterations / 2, "expected most samples to match, matched=$matched")
    }
}
