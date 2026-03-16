package com.rescript.plugin.analysis

import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import com.intellij.openapi.diagnostic.logger
import com.rescript.plugin.util.RescriptSecurityUtils
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

/**
 * Detects the installed ReScript version to determine whether
 * `reanalyze-server` mode is supported (requires ReScript >= 12.1.0).
 *
 * Reads `node_modules/rescript/package.json` from the project or ancestor
 * directories, parses the `version` field using semver conventions, and
 * exposes [isServerModeSupported] for lifecycle decisions.
 *
 * @see RescriptReanalyzeServerService
 * @see RescriptReanalyzeAnnotator.findReanalyzeTool
 */
object RescriptReanalyzeVersionDetector {
    private val LOG = logger<RescriptReanalyzeVersionDetector>()

    /** Minimum ReScript version that ships `reanalyze-server` subcommand. */
    val MIN_SERVER_VERSION = Triple(12, 1, 0)

    // Matches semver: major.minor.patch with optional pre-release suffix
    // e.g. "12.1.0", "12.1.0-alpha.1", "13.0.0-rc.2"
    private val SEMVER_REGEX = Regex("""^(\d+)\.(\d+)\.(\d+)""")

    /**
     * Parses a semver version string into a (major, minor, patch) triple.
     *
     * Pre-release suffixes (e.g. `-alpha.1`) are ignored for comparison
     * purposes, since any `12.1.0-alpha.x` build includes the server command.
     *
     * @param version the version string to parse (e.g. "12.1.0-alpha.1")
     * @return a triple of (major, minor, patch), or null if parsing fails
     */
    fun parseSemver(version: String): Triple<Int, Int, Int>? {
        val match = SEMVER_REGEX.find(version.trim()) ?: return null
        return try {
            Triple(
                match.groupValues[1].toInt(),
                match.groupValues[2].toInt(),
                match.groupValues[3].toInt(),
            )
        } catch (e: NumberFormatException) {
            null
        }
    }

    /**
     * Compares two semver triples.
     *
     * @param version the version to check
     * @param minimum the minimum required version
     * @return true if [version] is greater than or equal to [minimum]
     */
    fun isVersionAtLeast(
        version: Triple<Int, Int, Int>,
        minimum: Triple<Int, Int, Int>,
    ): Boolean {
        if (version.first != minimum.first) return version.first > minimum.first
        if (version.second != minimum.second) return version.second > minimum.second
        return version.third >= minimum.third
    }

    /**
     * Reads the ReScript version from `node_modules/rescript/package.json`.
     *
     * Walks up from [projectBasePath] looking for the package, using the same
     * directory traversal strategy as [RescriptReanalyzeAnnotator.findReanalyzeTool].
     *
     * @param projectBasePath the project's base directory path
     * @return the version string (e.g. "12.1.0"), or null if not found
     */
    fun readRescriptVersion(projectBasePath: String): String? {
        var dir: Path? = Path.of(projectBasePath)
        var depth = 0
        while (dir != null && depth < RescriptSecurityUtils.MAX_PARENT_TRAVERSAL_DEPTH) {
            val packageJson = dir.resolve("node_modules/rescript/package.json")
            if (Files.isRegularFile(packageJson)) {
                return parseVersionFromPackageJson(packageJson)
            }
            dir = dir.parent
            depth++
        }
        return null
    }

    /**
     * Determines whether the ReScript installation at [projectBasePath] supports
     * `reanalyze-server` mode.
     *
     * @param projectBasePath the project's base directory path
     * @return true if ReScript >= 12.1.0 is installed
     */
    fun isServerModeSupported(projectBasePath: String): Boolean {
        val versionStr = readRescriptVersion(projectBasePath) ?: return false
        val version = parseSemver(versionStr) ?: return false
        return isVersionAtLeast(version, MIN_SERVER_VERSION)
    }

    /**
     * Reads and parses the `version` field from a package.json file.
     *
     * @param packageJsonPath path to the package.json file
     * @return the version string, or null if reading or parsing fails
     */
    internal fun parseVersionFromPackageJson(packageJsonPath: Path): String? =
        try {
            val content = Files.readString(packageJsonPath, Charsets.UTF_8)
            val jsonObj = JsonParser.parseString(content).asJsonObject
            jsonObj.get("version")?.asString
        } catch (e: IOException) {
            LOG.warn("Failed to read package.json at $packageJsonPath", e)
            null
        } catch (e: JsonParseException) {
            LOG.warn("Malformed JSON in package.json at $packageJsonPath", e)
            null
        } catch (e: IllegalStateException) {
            // Thrown by asJsonObject when the parsed element is not a JSON object
            LOG.warn("Unexpected JSON structure in package.json at $packageJsonPath", e)
            null
        }
}
