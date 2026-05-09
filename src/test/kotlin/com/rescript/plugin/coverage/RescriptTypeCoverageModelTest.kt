package com.rescript.plugin.coverage

import com.intellij.mock.MockVirtualFile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Unit tests for [FileCoverage] and [ProjectCoverage] arithmetic.
 *
 * Aggregation is the part of the model that ships rendered numbers
 * to the panel, so any drift between [FileCoverage.coveragePercent]
 * and [ProjectCoverage.coveragePercent] is what the user sees first.
 */
class RescriptTypeCoverageModelTest {
    private fun mockFile(name: String) = MockVirtualFile(name)

    @Test
    fun `file with zero lets reports 100 percent (vacuously covered)`() {
        val fc = FileCoverage(mockFile("Empty.res"), totalLets = 0, annotatedLets = 0)
        assertEquals(100.0, fc.coveragePercent, 0.0001)
        assertEquals(0, fc.inferredLets)
    }

    @Test
    fun `file with all lets annotated reports 100 percent`() {
        val fc = FileCoverage(mockFile("F.res"), totalLets = 4, annotatedLets = 4)
        assertEquals(100.0, fc.coveragePercent, 0.0001)
        assertEquals(0, fc.inferredLets)
    }

    @Test
    fun `file with no annotations reports 0 percent`() {
        val fc = FileCoverage(mockFile("F.res"), totalLets = 7, annotatedLets = 0)
        assertEquals(0.0, fc.coveragePercent, 0.0001)
        assertEquals(7, fc.inferredLets)
    }

    @Test
    fun `file with mixed annotations reports proportional percent`() {
        val fc = FileCoverage(mockFile("F.res"), totalLets = 5, annotatedLets = 2)
        assertEquals(40.0, fc.coveragePercent, 0.0001)
        assertEquals(3, fc.inferredLets)
    }

    @Test
    fun `project aggregates totals and percent across files`() {
        val pc =
            ProjectCoverage(
                files =
                    listOf(
                        FileCoverage(mockFile("A.res"), totalLets = 10, annotatedLets = 5),
                        FileCoverage(mockFile("B.res"), totalLets = 4, annotatedLets = 4),
                        FileCoverage(mockFile("C.res"), totalLets = 6, annotatedLets = 0),
                    ),
            )
        assertEquals(20, pc.totalLets)
        assertEquals(9, pc.annotatedLets)
        assertEquals(45.0, pc.coveragePercent, 0.0001)
    }

    @Test
    fun `empty project reports 100 percent and zero counts`() {
        val pc = ProjectCoverage(files = emptyList())
        assertEquals(0, pc.totalLets)
        assertEquals(0, pc.annotatedLets)
        assertEquals(100.0, pc.coveragePercent, 0.0001)
    }

    @Test
    fun `truncated flag round-trips through the data class`() {
        val pc = ProjectCoverage(files = emptyList(), truncated = true)
        assertEquals(true, pc.truncated)
    }
}
