package com.rescript.plugin.coverage

import com.intellij.mock.MockVirtualFile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Unit tests for [RescriptTypeCoverageScanner.analyzeSource].
 *
 * These exercise the file-level splitting heuristic and the
 * aggregation arithmetic without an IntelliJ Platform fixture: the
 * scanner's IO entry point ([RescriptTypeCoverageScanner.scan]) is
 * exercised end-to-end by the run-IDE smoke when present, but the
 * splitting / counting logic that drives every column of the panel
 * is fully covered here.
 */
class RescriptTypeCoverageScannerTest {
    private fun mock(name: String) = MockVirtualFile(name)

    @Test
    fun `empty file returns 0 of 0 lets`() {
        val fc = RescriptTypeCoverageScanner.analyzeSource(mock("Empty.res"), "")
        assertEquals(0, fc.totalLets)
        assertEquals(0, fc.annotatedLets)
    }

    @Test
    fun `file with only comments returns 0 of 0 lets`() {
        val source =
            """
            // a comment
            /* block */
            """.trimIndent()
        val fc = RescriptTypeCoverageScanner.analyzeSource(mock("C.res"), source)
        assertEquals(0, fc.totalLets)
    }

    @Test
    fun `single annotated let counts as 1 of 1`() {
        val fc = RescriptTypeCoverageScanner.analyzeSource(mock("S.res"), "let x: int = 5\n")
        assertEquals(1, fc.totalLets)
        assertEquals(1, fc.annotatedLets)
    }

    @Test
    fun `single inferred let counts as 0 of 1`() {
        val fc = RescriptTypeCoverageScanner.analyzeSource(mock("S.res"), "let x = 5\n")
        assertEquals(1, fc.totalLets)
        assertEquals(0, fc.annotatedLets)
    }

    @Test
    fun `mixed annotated and inferred lets count separately`() {
        val source =
            """
            let a: int = 1
            let b = 2
            let c: string = "x"
            let d = (x: int) => x + 1
            """.trimIndent()
        val fc = RescriptTypeCoverageScanner.analyzeSource(mock("M.res"), source)
        assertEquals(4, fc.totalLets)
        assertEquals(2, fc.annotatedLets)
    }

    @Test
    fun `let inside module body is skipped`() {
        // The file-level scanner only counts depth-0 lets; the inner
        // `let y` is at depth 1 because of the surrounding `{}` and
        // therefore must not contribute. The module declaration itself
        // begins with `module`, not `let`, so it produces no entry
        // either — total lets is zero.
        val source =
            """
            module M = {
              let y: int = 5
            }
            """.trimIndent()
        val fc = RescriptTypeCoverageScanner.analyzeSource(mock("Mod.res"), source)
        assertEquals(0, fc.totalLets)
    }

    @Test
    fun `let inside expression body is skipped`() {
        val source =
            """
            let outer = {
              let inner = 1
              inner + 2
            }
            """.trimIndent()
        val fc = RescriptTypeCoverageScanner.analyzeSource(mock("E.res"), source)
        // Only `outer` is at depth 0; `inner` lives inside `{ ... }`
        assertEquals(1, fc.totalLets)
        assertEquals(0, fc.annotatedLets)
    }

    @Test
    fun `let rec annotated counts as annotated`() {
        val source = "let rec loop: int => unit = n => loop(n - 1)\n"
        val fc = RescriptTypeCoverageScanner.analyzeSource(mock("R.res"), source)
        assertEquals(1, fc.totalLets)
        assertEquals(1, fc.annotatedLets)
    }

    @Test
    fun `parameter annotation alone keeps the binding inferred`() {
        val source = "let f = (x: int, y: int) => x + y\n"
        val fc = RescriptTypeCoverageScanner.analyzeSource(mock("P.res"), source)
        assertEquals(1, fc.totalLets)
        assertEquals(0, fc.annotatedLets)
    }

    @Test
    fun `realistic file with five lets reports proportional counts`() {
        // 3 annotated, 2 inferred → 60%
        val source =
            """
            // file header

            let userId: int = 42
            let userName: string = "alice"
            let computed = userId + 1
            let toString: int => string = Int.toString
            let pipeline = arr->Array.reduce(0, (acc, x) => acc + x)
            """.trimIndent()
        val fc = RescriptTypeCoverageScanner.analyzeSource(mock("Real.res"), source)
        assertEquals(5, fc.totalLets)
        assertEquals(3, fc.annotatedLets)
        assertEquals(60.0, fc.coveragePercent, 0.0001)
    }
}
