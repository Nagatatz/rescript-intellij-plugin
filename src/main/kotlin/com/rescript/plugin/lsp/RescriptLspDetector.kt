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
     * Searches the project root directory and then walks up parent directories
     * to support monorepo layouts.
     *
     * @param projectBasePath the project's base path, or null
     * @return true if the language server directory exists
     */
    fun isLspAvailable(projectBasePath: String?): Boolean =
        findInAncestors(projectBasePath, ::hasLspInNodeModules)

    /**
     * Checks whether the given path is a ReScript project by looking for
     * `rescript.json` or `bsconfig.json`.
     *
     * Searches the project root directory and then walks up parent directories
     * to support monorepo layouts.
     *
     * @param projectBasePath the project's base path, or null
     * @return true if a ReScript config file is found
     */
    fun isRescriptProject(projectBasePath: String?): Boolean =
        findInAncestors(projectBasePath, ::hasRescriptConfig)

    /**
     * Walks from [projectBasePath] up through parent directories, returning true
     * as soon as [predicate] matches any directory.
     */
    private fun findInAncestors(projectBasePath: String?, predicate: (Path) -> Boolean): Boolean {
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
