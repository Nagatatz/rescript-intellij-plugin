package com.rescript.plugin.coverage

import com.intellij.openapi.vfs.VirtualFile

/**
 * Whether a single top-level `let` binding carries an explicit type
 * annotation on the binding name, or relies entirely on inference.
 *
 * Parameter-only annotations (e.g. `let f = (x: int) => x`) are
 * intentionally classified as [INFERRED] in v1: the binding itself
 * has no public type signature, so consumers reading the file have
 * to fall back to inference / hover. A future revision may surface a
 * separate "param-annotated" tier.
 */
enum class LetCoverage {
    /** A `:` token appears at depth 0 between the binding name and `=`. */
    ANNOTATED,

    /** No depth-0 `:` between the binding name and `=`. */
    INFERRED,
}

/**
 * Aggregated annotation counts for a single ReScript source file.
 *
 * Coverage is reported as a fraction of [annotatedLets] over
 * [totalLets]; files with zero `let` declarations are reported as
 * 100% so they don't drag the project average down (a file with no
 * bindings has nothing to annotate).
 *
 * @property file the underlying virtual file the panel jumps to on
 *   double-click; only the path is displayed in the table
 * @property totalLets total number of top-level `let` declarations
 *   the scanner found in [file]
 * @property annotatedLets subset of [totalLets] classified as
 *   [LetCoverage.ANNOTATED]
 */
data class FileCoverage(
    val file: VirtualFile,
    val totalLets: Int,
    val annotatedLets: Int,
) {
    /** Lets that fell back to inference. */
    val inferredLets: Int get() = totalLets - annotatedLets

    /**
     * Coverage percentage in [0.0, 100.0]. Returns 100.0 when there
     * are no lets in the file (vacuously fully covered).
     */
    val coveragePercent: Double
        get() = if (totalLets == 0) 100.0 else annotatedLets.toDouble() * 100.0 / totalLets
}

/**
 * Coverage roll-up across every scanned file.
 *
 * @property files per-file results in scan order; the panel sorts
 *   independently for display
 * @property truncated true when the scanner hit its file budget and
 *   skipped the tail of the project
 */
data class ProjectCoverage(
    val files: List<FileCoverage>,
    val truncated: Boolean = false,
) {
    /** Sum of all file [FileCoverage.totalLets]. */
    val totalLets: Int get() = files.sumOf { it.totalLets }

    /** Sum of all file [FileCoverage.annotatedLets]. */
    val annotatedLets: Int get() = files.sumOf { it.annotatedLets }

    /**
     * Project-wide coverage percentage in [0.0, 100.0]. Returns 100.0
     * when no lets exist anywhere (empty / fully-vacuous project).
     */
    val coveragePercent: Double
        get() = if (totalLets == 0) 100.0 else annotatedLets.toDouble() * 100.0 / totalLets
}
