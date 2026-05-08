package com.rescript.plugin.settings

import com.intellij.openapi.options.ConfigurationException
import com.rescript.plugin.util.RescriptPaths
import com.rescript.plugin.util.RescriptSecurityUtils
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

/**
 * Validates user-provided ReScript settings paths before they are persisted.
 *
 * Extracted from [RescriptConfigurable] so that validation logic can be unit-tested
 * independently of the Swing UI. Each validator preserves the exact error message
 * wording produced by the original `apply()` flow.
 */
object RescriptSettingsValidator {
    /**
     * Validates the LSP server path.
     *
     * Empty paths are accepted (auto-detect). Non-empty paths must point to an
     * existing file; non-`.js` paths must additionally be executable.
     *
     * @throws ConfigurationException if the path is non-empty and invalid.
     */
    @Throws(ConfigurationException::class)
    fun validateLspPath(path: String) {
        if (path.isEmpty()) return
        if (!File(path).exists()) {
            throw ConfigurationException(
                "Language server path does not exist: $path. " +
                    "Leave blank to use the project's node_modules, " +
                    "or run 'npm install @rescript/language-server' to install it.",
            )
        }
        // Non-.js server paths must be executable binaries
        if (!path.endsWith(".js") && !RescriptSecurityUtils.isValidExecutable(path)) {
            throw ConfigurationException(
                "Language server path is not an executable file: $path. " +
                    "Ensure the file has execute permissions (chmod +x on Unix).",
            )
        }
    }

    /**
     * Validates the Node.js interpreter path.
     *
     * Empty paths are accepted (PATH lookup). Non-empty paths must point to an
     * existing and executable file.
     *
     * @throws ConfigurationException if the path is non-empty and invalid.
     */
    @Throws(ConfigurationException::class)
    fun validateNodePath(path: String) {
        if (path.isEmpty()) return
        if (!File(path).exists()) {
            throw ConfigurationException(
                "Node.js interpreter path does not exist: $path. " +
                    "Leave blank to auto-detect from PATH, or install Node.js from https://nodejs.org.",
            )
        }
        if (!RescriptSecurityUtils.isValidExecutable(path)) {
            throw ConfigurationException(
                "Node.js interpreter path is not an executable file: $path. " +
                    "Ensure the file has execute permissions (chmod +x on Unix).",
            )
        }
    }

    /**
     * Validates the ReScript compiler binary path.
     *
     * Empty paths are accepted (auto-detect). Non-empty paths must point to an
     * existing filesystem entry.
     *
     * @throws ConfigurationException if the path is non-empty and missing.
     */
    @Throws(ConfigurationException::class)
    fun validateRescriptBinaryPath(path: String) {
        if (path.isEmpty()) return
        if (!File(path).exists()) {
            throw ConfigurationException(
                "ReScript binary path does not exist: $path. " +
                    "Leave blank to auto-detect from node_modules, or run 'npm install rescript'.",
            )
        }
    }

    /**
     * Validates the ReScript platform directory path.
     *
     * Empty paths are accepted (default). Non-empty paths must point to an
     * existing filesystem entry.
     *
     * @throws ConfigurationException if the path is non-empty and missing.
     */
    @Throws(ConfigurationException::class)
    fun validatePlatformPath(path: String) {
        if (path.isEmpty()) return
        if (!File(path).exists()) {
            throw ConfigurationException(
                "Platform path does not exist: $path. " +
                    "Leave blank to use the default path, or verify the ReScript installation.",
            )
        }
    }

    /**
     * Validates the ReScript runtime directory path.
     *
     * Empty paths are accepted (default). Non-empty paths must point to an
     * existing filesystem entry.
     *
     * @throws ConfigurationException if the path is non-empty and missing.
     */
    @Throws(ConfigurationException::class)
    fun validateRuntimePath(path: String) {
        if (path.isEmpty()) return
        if (!File(path).exists()) {
            throw ConfigurationException(
                "Runtime path does not exist: $path. " +
                    "Leave blank to use the default path, or verify the ReScript installation.",
            )
        }
    }

    /**
     * Categorises every entry in [packageRoots] against [base] without throwing.
     *
     * Used by tests and by future UI affordances (e.g., a notice strip in the
     * settings panel). The Configurable itself does not block apply on
     * validation failures — invalid entries are silently filtered at discovery
     * time so the user can iterate without being locked out.
     *
     * @param base the project base directory entries are resolved against
     * @param packageRoots relative paths supplied by the user
     * @return one [PackageRootIssue] per problematic entry; an empty list when
     *         every entry resolves to a valid ReScript package root
     */
    fun validatePackageRoots(
        base: Path?,
        packageRoots: List<String>,
    ): List<PackageRootIssue> {
        if (base == null) return emptyList()
        val baseAbs = base.toAbsolutePath().normalize()
        val issues = mutableListOf<PackageRootIssue>()
        for (raw in packageRoots) {
            val entry = raw.trim()
            if (entry.isEmpty()) continue
            val resolved =
                try {
                    baseAbs.resolve(entry).toAbsolutePath().normalize()
                } catch (_: Exception) {
                    issues.add(PackageRootIssue(entry, "Path is malformed"))
                    continue
                }
            if (!resolved.startsWith(baseAbs)) {
                issues.add(PackageRootIssue(entry, "Path escapes the project base directory"))
                continue
            }
            if (!Files.isDirectory(resolved)) {
                issues.add(PackageRootIssue(entry, "Directory does not exist"))
                continue
            }
            val hasConfig =
                RescriptPaths.CONFIG_FILE_NAMES.any { Files.isRegularFile(resolved.resolve(it)) }
            if (!hasConfig) {
                issues.add(
                    PackageRootIssue(
                        entry,
                        "Directory does not contain rescript.json or bsconfig.json",
                    ),
                )
            }
        }
        return issues
    }
}

/**
 * Issue raised by [RescriptSettingsValidator.validatePackageRoots] for a single
 * misconfigured entry, suitable for diagnostic display or assertions.
 *
 * @param entry the offending raw entry as the user typed it
 * @param message human-readable description of the problem
 */
data class PackageRootIssue(
    val entry: String,
    val message: String,
)
