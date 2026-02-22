package com.rescript.plugin.util

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import java.io.File

/**
 * Centralized security validation functions for the ReScript plugin.
 *
 * Provides input validation for executable paths, project scope checks,
 * package name sanitization, and HTML escaping. Used across navigation,
 * configuration, and process execution code to prevent path traversal,
 * command injection, and HTML injection vulnerabilities.
 */
object RescriptSecurityUtils {
    /** Maximum depth for parent directory traversal to prevent unbounded filesystem walks. */
    const val MAX_PARENT_TRAVERSAL_DEPTH = 10

    /** Default timeout in seconds for external process execution. */
    const val PROCESS_TIMEOUT_SECONDS = 30L

    /**
     * Validates that a path points to an existing, executable file.
     *
     * @param path the file system path to validate
     * @return true if the path is a non-empty string pointing to an existing executable file
     */
    fun isValidExecutable(path: String): Boolean {
        if (path.isBlank()) return false
        val file = File(path)
        return file.isFile && file.canExecute()
    }

    /**
     * Checks whether a virtual file is located within the project directory tree.
     *
     * Prevents LSP server responses from directing file operations outside the project scope.
     *
     * @param project the current project
     * @param file the virtual file to check
     * @return true if the file is under the project root directory
     */
    fun isWithinProject(
        project: Project,
        file: VirtualFile,
    ): Boolean {
        val projectDir = project.guessProjectDir() ?: return false
        return VfsUtil.isAncestor(projectDir, file, false)
    }

    /**
     * Validates a package name for safe use in file path construction.
     *
     * Allows alphanumeric characters, `@`, `/`, `-`, `.`, and `_` which covers
     * standard npm package names including scoped packages (e.g., `@rescript/core`).
     *
     * @param name the package name to validate
     * @return true if the name contains only safe characters
     */
    fun isValidPackageName(name: String): Boolean {
        if (name.isBlank()) return false
        // Reject path traversal sequences (..)
        if (name.contains("..")) return false
        // Match npm package name rules: alphanumeric, @, /, -, ., _
        return VALID_PACKAGE_NAME_PATTERN.matches(name)
    }

    /**
     * Escapes HTML entities in text to prevent HTML injection in documentation rendering.
     *
     * Delegates to IntelliJ's [StringUtil.escapeXmlEntities] for reliable escaping
     * of `<`, `>`, `&`, `"`, and `'`.
     *
     * @param text the raw text to escape
     * @return the HTML-safe escaped text
     */
    fun escapeHtml(text: String): String = StringUtil.escapeXmlEntities(text)

    // Match: alphanumeric, @, /, -, ., _ (covers scoped npm packages like @scope/name)
    private val VALID_PACKAGE_NAME_PATTERN = Regex("^[a-zA-Z0-9@/\\-._]+$")
}
