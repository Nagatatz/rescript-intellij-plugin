package com.rescript.plugin.util

/**
 * Centralizes hardcoded path constants used across the plugin for
 * locating ReScript tooling, configuration files, and node_modules artifacts.
 *
 * Consolidating these paths in one place avoids duplication and makes it
 * easy to update paths when the upstream ReScript toolchain changes.
 *
 * @see RescriptFileUtil for `.res`/`.resi` extension utilities
 */
object RescriptPaths {
    // ── Config file names ──────────────────────────────────────────────

    /** Primary ReScript project configuration file. */
    const val RESCRIPT_JSON = "rescript.json"

    /** Legacy BuckleScript configuration file (ReScript < 9). */
    const val BSCONFIG_JSON = "bsconfig.json"

    /** Set of all recognized ReScript config file names. */
    @JvmField
    val CONFIG_FILE_NAMES: Set<String> = setOf(RESCRIPT_JSON, BSCONFIG_JSON)

    // ── node_modules directory segments ────────────────────────────────

    /** Relative path to the npm binary directory. */
    const val NODE_MODULES_BIN = "node_modules/.bin"

    /** Path segment for the `@rescript/language-server` package directory. */
    const val LSP_PACKAGE_DIR = "node_modules/@rescript/language-server"

    /** Relative path to the LSP CLI entry-point JS file. */
    const val LSP_CLI_JS = "node_modules/@rescript/language-server/out/cli.js"

    /** LSP server binary name inside `node_modules/.bin/`. */
    const val LSP_BIN_NAME = "rescript-language-server"

    /** Relative path to the ReScript package.json (used for version detection). */
    const val RESCRIPT_PACKAGE_JSON = "node_modules/rescript/package.json"

    /** Relative path to the `rescript-tools` native executable (Unix). */
    const val RESCRIPT_TOOLS = "node_modules/rescript/rescript-tools"

    /** Relative path to the `rescript-tools.exe` native executable (Windows). */
    const val RESCRIPT_TOOLS_EXE = "node_modules/rescript/rescript-tools.exe"

    /** Binary name for `rescript-tools` inside `node_modules/.bin/`. */
    const val RESCRIPT_TOOLS_BIN_NAME = "rescript-tools"

    // ── Excluded path segments (filtering) ─────────────────────────────

    /**
     * Path segments that identify directories excluded from problem highlighting,
     * dead-code analysis, and similar inspections.
     */
    @JvmField
    val EXCLUDED_PATH_SEGMENTS: List<String> =
        listOf(
            "/node_modules/",
            "/lib/bs/",
            "/lib/ocaml/",
        )

    /** Path segment used to detect files inside any `node_modules/` directory. */
    const val NODE_MODULES_SEGMENT = "/node_modules/"
}
