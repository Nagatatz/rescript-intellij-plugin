package com.rescript.plugin.lsp

import java.nio.file.Files
import java.nio.file.Path

/**
 * Expands a small subset of npm/pnpm workspace glob patterns into concrete directories.
 *
 * Supports the common forms `packages/STAR`, `apps/DOUBLESTAR`, plain segments, and
 * directory chains (the literal patterns being a single asterisk and two asterisks).
 * Negation patterns (`!packages/foo`) and brace expansion are intentionally not
 * supported — they are rare in real-world workspace configurations and the action
 * of matching them is taken over by Layer 4 (depth-limited fallback) when needed.
 *
 * Excludes well-known non-source directories (`node_modules`, `.git`,
 * `build`, `dist`, etc.) from wildcard traversal to keep the cost bounded.
 *
 * @see RescriptWorkspaceFileParser
 * @see RescriptWorkspaceDiscovery
 */
object RescriptGlobExpander {
    /** Maximum recursion depth allowed for double-asterisk segments. */
    private const val MAX_DOUBLE_STAR_DEPTH = 6

    private const val SINGLE_STAR = "*"
    private const val DOUBLE_STAR = "**"

    /** Directory names skipped by single- and double-asterisk traversal. */
    @JvmField
    val EXCLUDED_DIRS: Set<String> =
        setOf(
            "node_modules",
            ".git",
            ".hg",
            ".svn",
            "build",
            "dist",
            "lib",
            ".pnpm",
            ".bs",
            "target",
            "out",
            ".next",
            ".nuxt",
            ".cache",
            ".turbo",
            ".idea",
            ".vscode",
        )

    /**
     * Expands [glob] against [base] and returns directories matching the pattern.
     *
     * Patterns starting with `!` are skipped (negation not supported).
     * Patterns containing path separators are split on `/` and each segment is
     * matched against the next directory level; backslashes are normalised to
     * forward slashes.
     *
     * @param base the workspace root the glob is anchored against
     * @param glob the workspace glob pattern (e.g., a `packages/<single-asterisk>` form)
     * @return the list of directories that match, or an empty list
     */
    fun expand(
        base: Path,
        glob: String,
    ): List<Path> {
        val trimmed = glob.trim()
        if (trimmed.isEmpty() || trimmed.startsWith("!")) return emptyList()
        val normalized = trimmed.replace('\\', '/').trimStart('/').trimEnd('/')
        if (normalized.isEmpty()) return emptyList()
        val parts = normalized.split('/').filter { it.isNotEmpty() }
        if (parts.isEmpty()) return emptyList()
        val result = mutableListOf<Path>()
        walk(base, parts, 0, result)
        return result.distinct()
    }

    /**
     * Recursively walks [parts] starting from [current], collecting matching directories into [out].
     *
     * @param current the directory currently being explored
     * @param parts the glob pattern split into segments
     * @param idx the index of the segment to match next
     * @param out accumulator for matching directories
     */
    private fun walk(
        current: Path,
        parts: List<String>,
        idx: Int,
        out: MutableList<Path>,
    ) {
        if (idx == parts.size) {
            if (Files.isDirectory(current)) out.add(current.normalize())
            return
        }
        if (!Files.isDirectory(current)) return
        when (val segment = parts[idx]) {
            SINGLE_STAR -> {
                listChildren(current).forEach { child ->
                    if (!isExcluded(child)) walk(child, parts, idx + 1, out)
                }
            }

            DOUBLE_STAR -> {
                // Double-asterisk matches zero or more directories, with depth bounded
                walk(current, parts, idx + 1, out)
                walkDoubleStar(current, parts, idx, out, depth = 0)
            }

            else -> {
                val next = current.resolve(segment)
                if (Files.isDirectory(next) && !isExcluded(next)) {
                    walk(next, parts, idx + 1, out)
                }
            }
        }
    }

    /**
     * Recursively descends through every non-excluded subdirectory while keeping the
     * double-asterisk segment "open" so trailing pattern parts can match at any depth.
     *
     * @param current the directory currently being explored
     * @param parts the glob pattern split into segments
     * @param doubleStarIdx the index of the double-asterisk segment in [parts]
     * @param out accumulator for matching directories
     * @param depth current recursion depth (bounded by [MAX_DOUBLE_STAR_DEPTH])
     */
    private fun walkDoubleStar(
        current: Path,
        parts: List<String>,
        doubleStarIdx: Int,
        out: MutableList<Path>,
        depth: Int,
    ) {
        if (depth >= MAX_DOUBLE_STAR_DEPTH) return
        listChildren(current).forEach { child ->
            if (isExcluded(child)) return@forEach
            walk(child, parts, doubleStarIdx + 1, out)
            walkDoubleStar(child, parts, doubleStarIdx, out, depth + 1)
        }
    }

    private fun listChildren(dir: Path): List<Path> =
        try {
            Files.list(dir).use { stream ->
                stream
                    .filter { Files.isDirectory(it) }
                    .toList()
            }
        } catch (_: Exception) {
            emptyList()
        }

    private fun isExcluded(dir: Path): Boolean = dir.fileName?.toString() in EXCLUDED_DIRS
}
