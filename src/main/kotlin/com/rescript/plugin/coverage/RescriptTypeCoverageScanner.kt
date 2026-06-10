package com.rescript.plugin.coverage

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.rescript.plugin.RescriptFileType
import com.rescript.plugin.lang.RescriptLexer
import com.rescript.plugin.lang.RescriptTokenTypes
import com.rescript.plugin.util.RescriptProjectFileScanner

/**
 * Walks every `.res` file in the project, splits its source into
 * file-top-level `let` declarations, and produces a [ProjectCoverage]
 * by feeding each one to [RescriptTypeCoverageClassifier].
 *
 * The scanner is split into a `scan(project)` IO entry point and a
 * pure `analyzeSource(text)` helper so the splitting heuristic can
 * be unit-tested without an IntelliJ Platform fixture. Module-internal
 * `let`s and let-inside-expression bindings are intentionally skipped
 * — only declarations at file depth 0 contribute to the count, mirroring
 * what `RescriptParser` itself models in the PSI tree.
 *
 * Hard-capped at [MAX_FILES] files; results past the cap are reflected
 * in [ProjectCoverage.truncated] so the panel can warn the user.
 */
object RescriptTypeCoverageScanner {
    /** Hard cap on files visited per scan. */
    const val MAX_FILES = 2_000

    /**
     * Scans every `.res` file in the project and produces a
     * [ProjectCoverage]. Iteration and safe file reading are delegated
     * to [RescriptProjectFileScanner]. Must be called off the EDT —
     * file contents are read inside a read action.
     */
    fun scan(project: Project): ProjectCoverage {
        val perFile = mutableListOf<FileCoverage>()
        val truncated =
            RescriptProjectFileScanner.scanFiles(
                project = project,
                fileTypes = listOf(RescriptFileType),
                shouldContinue = { perFile.size < MAX_FILES },
            ) { file, text ->
                perFile.add(analyzeSource(file, text))
            }
        return ProjectCoverage(files = perFile, truncated = truncated)
    }

    /**
     * Pure helper that splits [text] into file-top-level `let`
     * declarations and counts how many carry an explicit type
     * annotation. Exposed `internal` so unit tests can drive the
     * scanner without an IntelliJ fixture.
     */
    internal fun analyzeSource(
        file: VirtualFile,
        text: String,
    ): FileCoverage {
        val letRanges = topLevelLetRanges(text)
        var annotated = 0
        for (range in letRanges) {
            val slice = text.substring(range.first, range.second)
            if (RescriptTypeCoverageClassifier.classifyLet(slice) == LetCoverage.ANNOTATED) {
                annotated++
            }
        }
        return FileCoverage(file = file, totalLets = letRanges.size, annotatedLets = annotated)
    }

    /**
     * Returns `[start, end)` source ranges for each file-top-level
     * `let` declaration in [text]. A declaration starts at its `LET`
     * token and runs up to (but excluding) the next file-top-level
     * `LET` token, or to end-of-text. Module-internal lets and lets
     * appearing inside any `()`, `{}`, or `[]` are skipped.
     */
    private fun topLevelLetRanges(text: String): List<Pair<Int, Int>> {
        val starts = mutableListOf<Int>()
        val lexer = RescriptLexer()
        lexer.start(text)
        var depth = 0
        while (lexer.tokenType != null) {
            val type = lexer.tokenType!!
            when (type) {
                RescriptTokenTypes.LPAREN,
                RescriptTokenTypes.LBRACE,
                RescriptTokenTypes.LBRACKET,
                -> depth++

                RescriptTokenTypes.RPAREN,
                RescriptTokenTypes.RBRACE,
                RescriptTokenTypes.RBRACKET,
                -> depth = (depth - 1).coerceAtLeast(0)

                RescriptTokenTypes.LET -> if (depth == 0) starts.add(lexer.tokenStart)

                else -> if (isIgnorable(type)) Unit
            }
            lexer.advance()
        }
        if (starts.isEmpty()) return emptyList()
        val ranges = mutableListOf<Pair<Int, Int>>()
        for (i in starts.indices) {
            val from = starts[i]
            val to = if (i + 1 < starts.size) starts[i + 1] else text.length
            ranges.add(from to to)
        }
        return ranges
    }

    private fun isIgnorable(type: IElementType): Boolean =
        type == TokenType.WHITE_SPACE ||
            type == RescriptTokenTypes.EOL ||
            type == RescriptTokenTypes.SINGLE_COMMENT ||
            type == RescriptTokenTypes.MULTI_COMMENT
}
