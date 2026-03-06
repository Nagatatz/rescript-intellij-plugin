package com.rescript.plugin.util

/**
 * Shared regex patterns used across multiple components.
 *
 * Centralizes commonly reused patterns to avoid duplication
 * and unnecessary Regex object instantiation. Each pattern is
 * compiled once and reused for the lifetime of the plugin.
 *
 * @see com.rescript.plugin.refactor.RescriptNamesValidator
 * @see com.rescript.plugin.refactor.RescriptExtractVariableUtil
 * @see com.rescript.plugin.generate.RescriptTypeDeclarationParser
 * @see com.rescript.plugin.lsp.RescriptLspSignatureParser
 */
object RescriptRegexPatterns {
    /** Matches a valid ReScript lowercase identifier (lident). */
    @JvmField
    val LIDENT = Regex("^[a-z_][a-zA-Z0-9_']*$")

    /** Matches a valid ReScript uppercase identifier (uident). */
    @JvmField
    val UIDENT = Regex("^[A-Z][a-zA-Z0-9_']*$")

    /** Splits text by one or more whitespace characters. */
    @JvmField
    val WHITESPACE = Regex("""\s+""")

    // ── open statement patterns ──────────────────────────────────

    /** Matches top-level `open ModuleName` statements without a capture group. */
    @JvmField
    val OPEN_STATEMENT = Regex("""(?m)^open\s+\S+""")

    /** Captures the module name from `open ModuleName` statements. */
    @JvmField
    val OPEN_MODULE_CAPTURE = Regex("""(?m)^open\s+(\S+)""")

    /** Strict line-level match for `open ModuleName` with capture group (allows leading whitespace). */
    @JvmField
    val OPEN_MODULE_STRICT = Regex("""^\s*open\s+([A-Z][\w.]*)\s*$""", RegexOption.MULTILINE)

    /** Tests whether a line starts with an `open` statement (allows leading whitespace). */
    @JvmField
    val OPEN_LINE_TEST = Regex("""^\s*open\s""", RegexOption.MULTILINE)

    // ── include statement patterns ────────────────────────────────

    /** Captures the module name from `include ModuleName` statements. */
    @JvmField
    val INCLUDE_MODULE_CAPTURE = Regex("""^include\s+([A-Z][\w.]*)""")

    // ── variant constructor patterns ──────────────────────────────

    /** Matches a variant constructor with optional payload: `Name` or `Name(payload)`. */
    @JvmField
    val CONSTRUCTOR_WITH_PAYLOAD = Regex("""^([A-Z]\w*)(?:\((.+)\))?\s*$""")

    // ── labeled parameter patterns ────────────────────────────────

    /** Extracts the name from a labeled parameter: `~name`. */
    @JvmField
    val LABELED_PARAM_NAME = Regex("""~(\w+)""")
}
