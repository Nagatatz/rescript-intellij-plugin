package com.rescript.plugin.lsp

import com.intellij.openapi.project.Project
import com.rescript.plugin.settings.RescriptProjectSettings
import com.rescript.plugin.util.RescriptPaths
import java.nio.file.Files
import java.nio.file.Path

/**
 * Resolves the [RescriptWorkspaceLayout] for an IntelliJ project.
 *
 * The discovery proceeds through a short pipeline so monorepo layouts and
 * single-package layouts both produce a consistent list of package roots:
 *
 *  1. **Manual override** — `RescriptProjectSettings.packageRoots` (relative paths
 *     under the project base) are taken at face value.
 *  2. **Workspace files** — `pnpm-workspace.yaml` and `package.json#workspaces`
 *     are parsed and their globs expanded against the base directory.
 *  3. **Depth-limited scan** — when no workspace files yield a match, the base
 *     directory is walked up to a small depth looking for `rescript.json` /
 *     `bsconfig.json`.
 *  4. **Parent walk** — finally the legacy upward walk is consulted in case the
 *     IDE was opened on a sub-directory of a single-package project.
 *
 * Each layer short-circuits the next as soon as it produces at least one hit.
 * The return value is empty for projects that do not contain any ReScript
 * configuration.
 *
 * @see RescriptWorkspaceLayout
 * @see RescriptGlobExpander
 * @see RescriptWorkspaceFileParser
 */
object RescriptWorkspaceDiscovery {
    /** Maximum directory depth searched during the fallback recursive scan. */
    const val MAX_SCAN_DEPTH = 4

    /**
     * Discovers the layout of [project] using the project base path and the
     * persisted manual override list from settings.
     *
     * @param project the active IntelliJ project
     * @return the discovered layout, or [RescriptWorkspaceLayout.EMPTY]
     */
    fun discover(project: Project): RescriptWorkspaceLayout {
        val basePath = project.basePath ?: return RescriptWorkspaceLayout.EMPTY
        val overrides =
            try {
                RescriptProjectSettings.getInstance(project).packageRoots
            } catch (_: Exception) {
                emptyList()
            }
        return discover(basePath, overrides)
    }

    /**
     * Discovers the layout for [basePath] without consulting any persisted overrides.
     *
     * Equivalent to [discover] when [project] is unavailable (e.g., from
     * background utilities operating on a path string only).
     *
     * @param basePath the project base path, or null
     * @return the discovered layout, or [RescriptWorkspaceLayout.EMPTY]
     */
    fun discover(basePath: String?): RescriptWorkspaceLayout = discover(basePath, emptyList())

    /**
     * Core discovery routine combining all layers.
     *
     * @param basePath the project base path, or null
     * @param overrides relative paths from the manual override setting
     * @return the discovered layout, or [RescriptWorkspaceLayout.EMPTY]
     */
    fun discover(
        basePath: String?,
        overrides: List<String>,
    ): RescriptWorkspaceLayout {
        if (basePath == null) return RescriptWorkspaceLayout.EMPTY
        val base = Path.of(basePath).toAbsolutePath().normalize()
        if (!Files.isDirectory(base)) return RescriptWorkspaceLayout.EMPTY

        // Layer 1: manual override
        val manual = resolveOverrides(base, overrides)
        if (manual.isNotEmpty()) return RescriptWorkspaceLayout(manual)

        // Layer 2: workspace files (pnpm / package.json#workspaces)
        val workspaceRoots = expandWorkspaceFiles(base)
        if (workspaceRoots.isNotEmpty()) {
            // Always include the base itself when it has its own config alongside the workspace
            val withBase = if (hasConfigFile(base)) listOf(base) + workspaceRoots else workspaceRoots
            return RescriptWorkspaceLayout(withBase.distinct())
        }

        // Layer 3: depth-limited scan
        val scanned = scanDepthLimited(base)
        if (scanned.isNotEmpty()) return RescriptWorkspaceLayout(scanned)

        // Layer 4: parent walk fallback (preserves legacy behaviour for sub-directory opens)
        val ancestor = findAncestorWithConfig(base)
        if (ancestor != null) return RescriptWorkspaceLayout(listOf(ancestor))

        return RescriptWorkspaceLayout.EMPTY
    }

    /**
     * Resolves each [overrides] entry against [base] and keeps only entries
     * pointing to directories that actually contain a ReScript config file
     * and stay inside [base].
     */
    private fun resolveOverrides(
        base: Path,
        overrides: List<String>,
    ): List<Path> =
        overrides
            .asSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull { rel ->
                val resolved =
                    try {
                        base.resolve(rel).toAbsolutePath().normalize()
                    } catch (_: Exception) {
                        return@mapNotNull null
                    }
                if (!resolved.startsWith(base)) return@mapNotNull null
                if (!Files.isDirectory(resolved)) return@mapNotNull null
                if (!hasConfigFile(resolved)) return@mapNotNull null
                resolved
            }.toList()
            .distinct()

    private fun expandWorkspaceFiles(base: Path): List<Path> {
        val globs = RescriptWorkspaceFileParser.readGlobs(base)
        if (globs.isEmpty()) return emptyList()
        return globs
            .asSequence()
            .flatMap { RescriptGlobExpander.expand(base, it).asSequence() }
            .filter { Files.isDirectory(it) }
            .filter { it.startsWith(base) }
            .filter { hasConfigFile(it) }
            .toList()
            .distinct()
    }

    /**
     * Walks [base] up to [MAX_SCAN_DEPTH] directories deep, collecting the
     * shallowest directories that contain a ReScript config file.
     *
     * Subtrees of an already-matched directory are skipped so that nested
     * config files are treated as belonging to their parent package.
     */
    private fun scanDepthLimited(base: Path): List<Path> {
        val result = mutableListOf<Path>()
        if (hasConfigFile(base)) {
            result.add(base)
            return result
        }
        scan(base, 1, result)
        return result.distinct()
    }

    private fun scan(
        dir: Path,
        depth: Int,
        out: MutableList<Path>,
    ) {
        if (depth > MAX_SCAN_DEPTH) return
        val children =
            try {
                Files.list(dir).use { stream ->
                    stream.filter { Files.isDirectory(it) }.toList()
                }
            } catch (_: Exception) {
                return
            }
        for (child in children) {
            val name = child.fileName?.toString() ?: continue
            if (name in RescriptGlobExpander.EXCLUDED_DIRS) continue
            if (hasConfigFile(child)) {
                out.add(child)
                continue // do not descend into a matched package
            }
            scan(child, depth + 1, out)
        }
    }

    /**
     * Walks parent directories of [base] up to a bounded depth and returns the
     * first ancestor that contains a ReScript config file, or null.
     */
    private fun findAncestorWithConfig(base: Path): Path? {
        var dir: Path? = base.parent
        var depth = 0
        while (dir != null && depth < com.rescript.plugin.util.RescriptSecurityUtils.MAX_PARENT_TRAVERSAL_DEPTH) {
            if (hasConfigFile(dir)) return dir
            dir = dir.parent
            depth++
        }
        return null
    }

    /** Returns whether [dir] directly contains `rescript.json` or `bsconfig.json`. */
    fun hasConfigFile(dir: Path): Boolean = RescriptPaths.CONFIG_FILE_NAMES.any { Files.isRegularFile(dir.resolve(it)) }
}
