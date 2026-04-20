package com.rescript.plugin.settings

import com.intellij.openapi.options.ConfigurationException
import com.rescript.plugin.util.RescriptSecurityUtils
import java.io.File

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
}
