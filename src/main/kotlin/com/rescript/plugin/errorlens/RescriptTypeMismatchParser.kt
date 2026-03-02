package com.rescript.plugin.errorlens

import com.rescript.plugin.util.RescriptRegexPatterns

/**
 * Parses ReScript compiler type mismatch error messages into structured data.
 *
 * Extracts expected and actual types from error messages produced by the
 * ReScript compiler, enabling the Error Lens to display them in a
 * structured format (e.g., "Expected: int, Actual: string").
 *
 * Supports multiple error message formats from the ReScript compiler:
 * - `This has type: X  Somewhere wanted: Y`
 * - `Type X is not compatible with type Y`
 * - `expected X but got Y`
 *
 * @see RescriptErrorLensRenderer for the visual display
 */
object RescriptTypeMismatchParser {
    /**
     * Represents a parsed type mismatch with expected and actual types.
     *
     * @property expected the expected type string
     * @property actual the actual (found) type string
     */
    data class TypeMismatch(
        val expected: String,
        val actual: String,
    )

    // Pattern: "This has type: X\n  Somewhere wanted: Y"
    // or: "This has type: X  Somewhere wanted: Y"
    private val THIS_HAS_TYPE_PATTERN =
        Regex("""This has type:\s*(.+?)\s*Somewhere wanted:\s*(.+?)\s*$""")

    // Pattern: "This has type: X\n  But somewhere wanted: Y"
    private val BUT_SOMEWHERE_WANTED_PATTERN =
        Regex("""This has type:\s*(.+?)\s*But somewhere wanted:\s*(.+?)\s*$""")

    // Pattern: "expected X but got Y"
    private val EXPECTED_BUT_GOT_PATTERN =
        Regex("""(?i)expected\s+(.+?)\s+but\s+got\s+(.+?)\s*$""")

    // Pattern: "Type X is not compatible with type Y"
    private val NOT_COMPATIBLE_PATTERN =
        Regex("""Type\s+(.+?)\s+is not compatible with type\s+(.+?)\s*$""")

    // Pattern: "This has type: X\n  It's expected to have type: Y"
    private val EXPECTED_TO_HAVE_PATTERN =
        Regex("""This has type:\s*(.+?)\s*It's expected to have type:\s*(.+?)\s*$""")

    /**
     * Attempts to parse a type mismatch from an error message.
     *
     * @param message the compiler error message
     * @return a [TypeMismatch] if the message contains type mismatch info, or null
     */
    fun parse(message: String): TypeMismatch? {
        // Normalize whitespace (newlines, multiple spaces → single space)
        val normalized = message.replace(RescriptRegexPatterns.WHITESPACE, " ").trim()

        // Try each pattern in order
        EXPECTED_TO_HAVE_PATTERN.find(normalized)?.let { match ->
            return TypeMismatch(
                actual = match.groupValues[1].trim(),
                expected = match.groupValues[2].trim(),
            )
        }

        BUT_SOMEWHERE_WANTED_PATTERN.find(normalized)?.let { match ->
            return TypeMismatch(
                actual = match.groupValues[1].trim(),
                expected = match.groupValues[2].trim(),
            )
        }

        THIS_HAS_TYPE_PATTERN.find(normalized)?.let { match ->
            return TypeMismatch(
                actual = match.groupValues[1].trim(),
                expected = match.groupValues[2].trim(),
            )
        }

        EXPECTED_BUT_GOT_PATTERN.find(normalized)?.let { match ->
            return TypeMismatch(
                expected = match.groupValues[1].trim(),
                actual = match.groupValues[2].trim(),
            )
        }

        NOT_COMPATIBLE_PATTERN.find(normalized)?.let { match ->
            return TypeMismatch(
                actual = match.groupValues[1].trim(),
                expected = match.groupValues[2].trim(),
            )
        }

        return null
    }

    /**
     * Formats a type mismatch as a structured display string.
     *
     * @param mismatch the parsed type mismatch
     * @return a formatted string like "Expected: int | Actual: string"
     */
    fun formatMismatch(mismatch: TypeMismatch): String = "Expected: ${mismatch.expected} | Actual: ${mismatch.actual}"
}
