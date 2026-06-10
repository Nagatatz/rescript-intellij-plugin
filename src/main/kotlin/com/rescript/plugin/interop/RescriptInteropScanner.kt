package com.rescript.plugin.interop

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.rescript.plugin.RescriptFileType
import com.rescript.plugin.RescriptInterfaceFileType
import com.rescript.plugin.util.RescriptProjectFileScanner

/**
 * Walks every ReScript source file in [Project] and produces an
 * ordered list of [InteropEntry]s by delegating each candidate line
 * to [RescriptInteropClassifier].
 *
 * The scanner is split into a `scan(project)` IO entry point and a
 * pure `collectEntriesFromText(...)` helper so the heuristic chain
 * can be tested end-to-end without an IntelliJ Platform fixture.
 */
object RescriptInteropScanner {
    /** Soft cap per file. */
    internal const val MAX_PER_FILE = 50

    /** Soft cap across the whole project. */
    internal const val MAX_TOTAL = 500

    /** Truncated result returned to the panel. */
    data class Result(
        val entries: List<InteropEntry>,
        val truncated: Boolean,
    )

    /**
     * Returns interop entries from every `.res` and `.resi` file in the
     * project. Iteration and safe file reading are delegated to
     * [RescriptProjectFileScanner]. Must be called off the EDT — the
     * scan reads file contents inside a read action.
     */
    fun scan(project: Project): Result {
        val collected = mutableListOf<InteropEntry>()
        val truncated =
            RescriptProjectFileScanner.scanFiles(
                project = project,
                fileTypes = listOf(RescriptFileType, RescriptInterfaceFileType),
                shouldContinue = { collected.size < MAX_TOTAL },
            ) { file, text ->
                val perFileBudget = (MAX_TOTAL - collected.size).coerceAtMost(MAX_PER_FILE)
                collected.addAll(collectEntriesFromText(file, text, perFileBudget))
            }
        // Sort by risk severity (HIGH first), then file path for stable display.
        val sorted =
            collected.sortedWith(
                compareBy<InteropEntry> { it.risk.ordinal }.thenBy { it.file.path }.thenBy { it.lineNumber },
            )
        return Result(entries = sorted, truncated = truncated)
    }

    /**
     * Pure helper that scans [text] line-by-line for interop matches.
     * Exposed `internal` so unit tests can drive the scanner without
     * touching the IDE.
     *
     * @param file forwarded into the produced [InteropEntry]s
     * @param text full file contents
     * @param maxEntries upper bound on returned entries
     */
    internal fun collectEntriesFromText(
        file: VirtualFile,
        text: String,
        maxEntries: Int,
    ): List<InteropEntry> {
        if (maxEntries <= 0) return emptyList()
        val entries = mutableListOf<InteropEntry>()
        var lineStart = 0
        var lineNumber = 0
        for (i in 0..text.length) {
            if (i == text.length || text[i] == '\n') {
                lineNumber++
                if (lineStart < i) {
                    val line = text.substring(lineStart, i)
                    val classified = RescriptInteropClassifier.classify(line)
                    if (classified != null) {
                        val (kind, risk) = classified
                        entries.add(
                            InteropEntry(
                                file = file,
                                offset = lineStart,
                                lineNumber = lineNumber,
                                previewLine = line.trim(),
                                kind = kind,
                                risk = risk,
                            ),
                        )
                        if (entries.size >= maxEntries) return entries
                    }
                }
                lineStart = i + 1
            }
        }
        return entries
    }
}
