package com.rescript.plugin.lsp

import com.intellij.openapi.project.Project
import com.rescript.plugin.settings.RescriptProjectSettings
import com.rescript.plugin.util.RescriptPaths
import java.nio.file.Files
import java.nio.file.Path

/**
 * Utility for detecting the ReScript Language Server and ReScript project configuration.
 *
 * Provides static methods to check whether `@rescript/language-server` is installed,
 * whether the current project is a ReScript project, and whether a custom LSP path
 * is configured in plugin settings. Follows the same `object` pattern as
 * [RescriptCliDetector][com.rescript.plugin.run.RescriptCliDetector].
 *
 * @see com.rescript.plugin.run.RescriptCliDetector
 * @see RescriptLspServerDescriptor
 */
object RescriptLspDetector {
    // Config file names that identify a ReScript project
    private val RESCRIPT_CONFIG_FILES = RescriptPaths.CONFIG_FILE_NAMES

    /**
     * Checks whether `@rescript/language-server` is available in node_modules.
     *
     * Walks up from [projectBasePath] so deeply-nested files can still locate a
     * hoisted `node_modules`. For monorepo-aware detection that descends into
     * workspace packages, prefer the [isLspAvailable] overload that takes a
     * [Project].
     *
     * @param projectBasePath the project's base path, or null
     * @return true if the language server directory exists
     */
    fun isLspAvailable(projectBasePath: String?): Boolean = findInAncestors(projectBasePath, ::hasLspInNodeModules)

    /**
     * Monorepo-aware variant that consults [RescriptWorkspaceDiscovery] to
     * inspect each detected package root in addition to the legacy upward walk.
     *
     * @param project the active IntelliJ project
     * @return true if the language server directory exists in any candidate location
     */
    fun isLspAvailable(project: Project): Boolean {
        val layout = RescriptWorkspaceDiscovery.discover(project)
        if (layout.lspPackageDirs().any { Files.isDirectory(it) }) return true
        return isLspAvailable(project.basePath)
    }

    /**
     * Checks whether the given path is a ReScript project by looking for
     * `rescript.json` or `bsconfig.json`.
     *
     * Walks up from the supplied path to support sub-directory opens. For
     * monorepo layouts that only declare ReScript inside workspace packages,
     * prefer the [isRescriptProject] overload that takes a [Project] so the
     * workspace-aware discovery pipeline can be used.
     *
     * @param projectBasePath the project's base path, or null
     * @return true if a ReScript config file is found
     */
    fun isRescriptProject(projectBasePath: String?): Boolean = findInAncestors(projectBasePath, ::hasRescriptConfig)

    /**
     * Monorepo-aware variant that delegates to [RescriptWorkspaceDiscovery] so
     * workspace-only ReScript projects (e.g., `packages/<name>/rescript.json`
     * with no root-level config) are recognised.
     *
     * @param project the active IntelliJ project
     * @return true if the project contains at least one ReScript package root
     */
    fun isRescriptProject(project: Project): Boolean = RescriptWorkspaceDiscovery.discover(project).isRescriptProject()

    /**
     * Walks from [projectBasePath] up through parent directories, returning true
     * as soon as [predicate] matches any directory.
     */
    private fun findInAncestors(
        projectBasePath: String?,
        predicate: (Path) -> Boolean,
    ): Boolean {
        if (projectBasePath == null) return false
        var dir: Path? = Path.of(projectBasePath)
        while (dir != null) {
            if (predicate(dir)) return true
            dir = dir.parent
        }
        return false
    }

    /**
     * Checks whether a custom LSP server path is configured in plugin settings.
     *
     * @param project the IntelliJ project
     * @return true if a non-empty LSP path is set
     */
    fun isLspConfigured(project: Project): Boolean {
        val settings = RescriptProjectSettings.getInstance(project)
        return settings.lspServerPath.isNotEmpty()
    }

    private fun hasLspInNodeModules(base: Path): Boolean {
        val lspDir = base.resolve(RescriptPaths.LSP_PACKAGE_DIR)
        return Files.isDirectory(lspDir)
    }

    private fun hasRescriptConfig(dir: Path): Boolean =
        RESCRIPT_CONFIG_FILES.any { Files.isRegularFile(dir.resolve(it)) }
}
