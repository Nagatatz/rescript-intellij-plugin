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
}
