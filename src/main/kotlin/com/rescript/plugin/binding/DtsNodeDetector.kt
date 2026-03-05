package com.rescript.plugin.binding

import com.intellij.openapi.diagnostic.logger
import com.rescript.plugin.settings.RescriptProjectSettings
import com.rescript.plugin.util.RescriptSecurityUtils
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit

/**
 * Detects Node.js and the TypeScript package required for `.d.ts` parsing.
 *
 * Searches for `node_modules/typescript` in the project directory and parent
 * directories (monorepo support), and resolves the Node.js executable from
 * project settings or system PATH.
 *
 * @see DtsParserProcess for invoking the parser
 * @see com.rescript.plugin.lsp.RescriptLspDetector for similar detection pattern
 */
object DtsNodeDetector {
    private val LOG = logger<DtsNodeDetector>()

    /**
     * Finds the path to the TypeScript package directory.
     *
     * Searches `node_modules/typescript` starting from the given base path,
     * then walks up parent directories for monorepo layouts.
     *
     * @param basePath the starting directory to search from
     * @return the path to the typescript package, or null if not found
     */
    fun findTypeScriptPath(basePath: String?): String? {
        if (basePath == null) return null
        val base = Path.of(basePath)

        // Check project root
        findTsInNodeModules(base)?.let { return it }

        // Walk up parent directories (monorepo support)
        var dir = base.parent
        var depth = 0
        while (dir != null && depth < RescriptSecurityUtils.MAX_PARENT_TRAVERSAL_DEPTH) {
            findTsInNodeModules(dir)?.let { return it }
            dir = dir.parent
            depth++
        }
        return null
    }

    /**
     * Resolves the Node.js executable path.
     *
     * Uses the custom path from project settings if configured, otherwise
     * defaults to "node" (assumes it's on PATH).
     *
     * @param settings the project settings, or null to use default
     * @return the Node.js executable path
     */
    fun resolveNodePath(settings: RescriptProjectSettings?): String {
        val customPath = settings?.nodePath?.takeIf { it.isNotEmpty() }
        return customPath ?: "node"
    }

    /**
     * Checks whether Node.js is available by attempting to run `node --version`.
     *
     * @param nodePath the Node.js executable path
     * @return true if Node.js responds successfully
     */
    fun isNodeAvailable(nodePath: String): Boolean =
        try {
            val proc = ProcessBuilder(nodePath, "--version").redirectErrorStream(true).start()
            val output =
                proc.inputStream
                    .bufferedReader()
                    .readLine()
                    ?.trim() ?: ""
            val completed = proc.waitFor(RescriptSecurityUtils.PROCESS_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            if (!completed) {
                proc.destroyForcibly()
                return false
            }
            val exitCode = proc.exitValue()
            exitCode == 0 && output.startsWith("v")
        } catch (e: Exception) {
            LOG.debug("Node.js availability check failed for '$nodePath'", e)
            false
        }

    /**
     * Checks whether TypeScript is available at the given path.
     *
     * @param tsPath the path returned by [findTypeScriptPath]
     * @return true if the typescript package directory exists with a package.json
     */
    fun isTypeScriptAvailable(tsPath: String?): Boolean {
        if (tsPath == null) return false
        val dir = Path.of(tsPath)
        return Files.isDirectory(dir) && Files.isRegularFile(dir.resolve("package.json"))
    }

    private fun findTsInNodeModules(base: Path): String? {
        val tsDir = base.resolve("node_modules/typescript")
        return if (Files.isDirectory(tsDir)) tsDir.toString() else null
    }
}
