package com.rescript.plugin.util

import com.intellij.openapi.project.Project

/**
 * Sanitizes arbitrary message strings before they are shown in user-facing UI
 * (error dialogs, notifications) so that absolute filesystem paths never leak.
 *
 * The IntelliJ security guideline forbids exposing absolute paths in user-visible
 * messages. External process output (npm/pnpm stderr, exception messages) frequently
 * embeds absolute paths that reveal the user's home directory or full project location.
 * This object rewrites those paths to relative or masked markers.
 *
 * The core logic is a pure, deterministic function that takes the home directory and
 * project base path as parameters, making it unit-testable without an IntelliJ fixture.
 *
 * @see RescriptSecurityUtils for other security-related validation helpers
 */
object RescriptMessageSanitizer {
    /** Marker substituted for the project base path. */
    private const val PROJECT_MARKER = "<project>"

    /**
     * Matches a leading absolute unix-style path token so any residual absolute path
     * (e.g. `/Users/alice/acme/node_modules/.staging`) is collapsed to its basename.
     * Anchored to a boundary (start of line or whitespace) and requires at least two
     * path segments to avoid mangling normal prose that merely contains a slash.
     */
    private val ABSOLUTE_PATH_PATTERN = Regex("""(?<=^|\s)(/[^\s/]+(?:/[^\s/]+)+)""")

    /**
     * Sanitizes a message for display within the context of a project.
     *
     * Delegates to the pure [sanitize] overload using the current user's home directory
     * and the project's base path.
     *
     * @param project the current project, or null if none is available
     * @param message the raw message that may contain absolute paths
     * @return the message with absolute paths stripped or masked
     */
    fun sanitize(
        project: Project?,
        message: String,
    ): String = sanitize(message, System.getProperty("user.home"), project?.basePath)

    /**
     * Pure sanitization routine that strips absolute-path prefixes from a message.
     *
     * Applies the following rewrites to every line (multi-line stderr is handled):
     * 1. Replaces occurrences of [projectBasePath] with [PROJECT_MARKER].
     * 2. Replaces the [homeDir] prefix with `~`.
     * 3. Collapses any remaining absolute unix-style path to its basename as a
     *    conservative fallback, so home-revealing paths are removed even when they
     *    match neither the project nor the home prefix.
     *
     * @param message the raw message that may contain absolute paths
     * @param homeDir the user's home directory, or null if unavailable
     * @param projectBasePath the project base path, or null if unavailable
     * @return the sanitized message with absolute paths removed or masked
     */
    internal fun sanitize(
        message: String,
        homeDir: String?,
        projectBasePath: String?,
    ): String {
        // Normalize the prefixes once so trailing separators do not defeat matching.
        val normalizedBase = projectBasePath?.trimEnd('/')?.takeIf { it.isNotEmpty() }
        val normalizedHome = homeDir?.trimEnd('/')?.takeIf { it.isNotEmpty() }

        return message
            .lineSequence()
            .joinToString("\n") { line -> sanitizeLine(line, normalizedHome, normalizedBase) }
    }

    /**
     * Applies all path-masking rewrites to a single line.
     *
     * @param line the source line
     * @param normalizedHome the trimmed home directory, or null
     * @param normalizedBase the trimmed project base path, or null
     * @return the sanitized line
     */
    private fun sanitizeLine(
        line: String,
        normalizedHome: String?,
        normalizedBase: String?,
    ): String {
        var result = line

        // Project base path first: it is usually more specific (often nested under home).
        if (normalizedBase != null) {
            result = result.replace(normalizedBase, PROJECT_MARKER)
        }
        if (normalizedHome != null) {
            result = result.replace(normalizedHome, "~")
        }

        // Fallback: collapse any residual absolute path to its basename.
        result =
            ABSOLUTE_PATH_PATTERN.replace(result) { match ->
                match.value.substringAfterLast('/')
            }

        return result
    }
}
