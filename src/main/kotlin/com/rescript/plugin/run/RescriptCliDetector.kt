package com.rescript.plugin.run

import com.rescript.plugin.util.RescriptPaths
import com.rescript.plugin.util.RescriptSecurityUtils
import java.nio.file.Files
import java.nio.file.Path

/**
 * Detects the `rescript` CLI binary in the project's `node_modules/.bin/` directory.
 *
 * Searches in the following order:
 * 1. Working directory's `node_modules/.bin/`
 * 2. Project base path's `node_modules/.bin/`
 * 3. Parent directories (monorepo support)
 */
object RescriptCliDetector {
    private const val BIN_NAME = "rescript"

    /**
     * Finds the `rescript` CLI executable path, or `null` if not found.
     *
     * @param workingDirectory the run configuration's working directory
     * @param projectBasePath the project's base path (fallback)
     */
    fun findCli(
        workingDirectory: String?,
        projectBasePath: String?,
    ): String? =
        findFromDirectory(workingDirectory)
            ?: findFromDirectory(projectBasePath)
            ?: findInParentDirectories(projectBasePath)

    private fun findFromDirectory(directory: String?): String? {
        if (directory == null) return null
        return findInNodeModulesBin(Path.of(directory))
    }

    private fun findInParentDirectories(basePath: String?): String? {
        var dir = basePath?.let { Path.of(it).parent }
        var depth = 0
        while (dir != null && depth < RescriptSecurityUtils.MAX_PARENT_TRAVERSAL_DEPTH) {
            findInNodeModulesBin(dir)?.let { return it }
            dir = dir.parent
            depth++
        }
        return null
    }

    private fun findInNodeModulesBin(base: Path): String? {
        val bin = base.resolve("${RescriptPaths.NODE_MODULES_BIN}/$BIN_NAME")
        if (Files.isExecutable(bin)) return bin.toString()
        return null
    }
}
