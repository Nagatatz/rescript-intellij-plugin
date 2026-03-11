package com.rescript.plugin.lang

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

/**
 * Performance benchmark tests for the ReScript lexer and parser.
 *
 * Measures tokenization time on large fixture files to ensure lexer performance
 * stays within acceptable bounds (< 100ms for 2000+ line files).
 */
class RescriptPerformanceBenchmarkTest {
    companion object {
        // Maximum allowed time in milliseconds for lexing a large file
        private const val MAX_LEXER_TIME_MS = 100L

        // Number of warmup iterations before measuring
        private const val WARMUP_ITERATIONS = 3

        // Number of measured iterations
        private const val MEASURE_ITERATIONS = 5
    }

    private fun tokenize(input: String): Int {
        val lexer = RescriptLexer()
        lexer.start(input)
        var count = 0
        while (lexer.tokenType != null) {
            count++
            lexer.advance()
        }
        return count
    }

    @Test
    fun `lexer performance on large file stays within threshold`() {
        val fixture = File("src/test/testData/lexer/LargeFile.res")
        assertTrue(fixture.exists(), "LargeFile.res fixture must exist")

        val content = fixture.readText()
        val lineCount = content.lines().size
        assertTrue(lineCount >= 2000, "LargeFile.res should have at least 2000 lines, has $lineCount")

        // Warmup
        repeat(WARMUP_ITERATIONS) {
            tokenize(content)
        }

        // Measure
        val times = mutableListOf<Long>()
        repeat(MEASURE_ITERATIONS) {
            val start = System.nanoTime()
            val tokenCount = tokenize(content)
            val elapsed = (System.nanoTime() - start) / 1_000_000
            times.add(elapsed)
            assertTrue(tokenCount > 0, "Should produce tokens")
        }

        val median = times.sorted()[times.size / 2]
        assertTrue(
            median <= MAX_LEXER_TIME_MS,
            "Lexer median time ($median ms) exceeds threshold ($MAX_LEXER_TIME_MS ms) " +
                "for $lineCount-line file. All times: $times",
        )
    }

    @Test
    fun `lexer processes all fixture files without errors`() {
        val testDataDir = File("src/test/testData/lexer")
        assertTrue(testDataDir.exists() && testDataDir.isDirectory, "lexer testData directory must exist")

        val fixtures = testDataDir.listFiles { f -> f.extension == "res" } ?: emptyArray()
        assertTrue(fixtures.isNotEmpty(), "Should have at least one lexer fixture")

        fixtures.forEach { fixture ->
            val content = fixture.readText()
            val tokenCount = tokenize(content)
            assertTrue(
                tokenCount > 0,
                "Fixture ${fixture.name} should produce tokens",
            )
        }
    }
}
