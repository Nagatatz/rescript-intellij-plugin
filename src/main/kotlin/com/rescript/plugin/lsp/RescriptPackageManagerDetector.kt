package com.rescript.plugin.lsp

import com.rescript.plugin.util.RescriptSecurityUtils
import java.nio.file.Files
import java.nio.file.Path

/**
 * Supported package managers with their install commands and lock file indicators.
 *
 * @param displayName human-readable name for UI display
 * @param installCommand the CLI command parts for installing a dev dependency
 * @param lockFileName the lock file that identifies this package manager
 */
enum class PackageManager(
    val displayName: String,
    val installCommand: List<String>,
    val lockFileName: String,
) {
    NPM("npm", listOf("npm", "install", "--save-dev"), "package-lock.json"),
    YARN("Yarn", listOf("yarn", "add", "--dev"), "yarn.lock"),
    PNPM("pnpm", listOf("pnpm", "add", "--save-dev"), "pnpm-lock.yaml"),
}

/**
 * Result of package manager detection, including the detected package manager
 * and the directory where the lock file was found (used as working directory for install).
 *
 * @param packageManager the detected package manager
 * @param workingDirectory the directory containing the lock file
 */
data class PackageManagerDetectionResult(
    val packageManager: PackageManager,
    val workingDirectory: String,
)

/**
 * Detects the package manager used by a project by searching for lock files.
 *
 * Searches in the project root directory and then walks up parent directories
 * to support monorepo layouts. Within each directory, lock files are checked
 * in priority order: pnpm-lock.yaml > yarn.lock > package-lock.json.
 *
 * If no lock file is found, defaults to NPM with the project root as working directory.
 *
 * @see PackageManager
 * @see PackageManagerDetectionResult
 */
object RescriptPackageManagerDetector {
    // Ordered by priority: pnpm > yarn > npm
    private val DETECTION_ORDER = listOf(PackageManager.PNPM, PackageManager.YARN, PackageManager.NPM)

    /**
     * Detects the package manager for the given project base path.
     *
     * @param projectBasePath the project's base path, or null
     * @return detection result with package manager and working directory;
     *         defaults to NPM + projectBasePath if no lock file is found,
     *         or NPM + "." if projectBasePath is null
     */
    fun detect(projectBasePath: String?): PackageManagerDetectionResult {
        val defaultDir = projectBasePath ?: "."

        if (projectBasePath == null) {
            return PackageManagerDetectionResult(PackageManager.NPM, defaultDir)
        }

        val basePath = Path.of(projectBasePath)

        // Check project root
        detectInDirectory(basePath)?.let { return it }

        // Walk up parent directories (monorepo support)
        var dir = basePath.parent
        var depth = 0
        while (dir != null && depth < RescriptSecurityUtils.MAX_PARENT_TRAVERSAL_DEPTH) {
            detectInDirectory(dir)?.let { return it }
            dir = dir.parent
            depth++
        }

        // Default to NPM with project root
        return PackageManagerDetectionResult(PackageManager.NPM, projectBasePath)
    }

    private fun detectInDirectory(dir: Path): PackageManagerDetectionResult? {
        for (pm in DETECTION_ORDER) {
            if (Files.isRegularFile(dir.resolve(pm.lockFileName))) {
                return PackageManagerDetectionResult(pm, dir.toString())
            }
        }
        return null
    }
}
