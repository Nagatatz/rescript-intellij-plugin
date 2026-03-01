package com.rescript.plugin.errorlens

/**
 * Computes character-level diffs between two ReScript type strings
 * for visual highlighting in the Error Lens.
 *
 * Tokenizes type strings at structural boundaries (`<`, `>`, `(`, `)`,
 * `=>`, `,`, spaces) and compares tokens pairwise to identify which
 * segments differ between expected and actual types.
 *
 * @see RescriptErrorLensRenderer for rendering diff segments
 * @see RescriptTypeMismatchParser for extracting types from error messages
 */
object RescriptTypeDiffComputer {
    /**
     * A segment of a type string with a flag indicating whether it
     * differs from the corresponding segment in the other type.
     *
     * @property text the text content of this segment
     * @property isDifferent true if this segment differs from the other type
     */
    data class TypeSegment(
        val text: String,
        val isDifferent: Boolean,
    )

    // Delimiters that mark token boundaries in type strings
    private val DELIMITER_PATTERN = Regex("""(=>|[<>(,)\s])""")

    /**
     * Tokenizes a ReScript type string into individual tokens.
     *
     * Splits at structural delimiters while preserving the delimiters
     * as separate tokens. Whitespace is collapsed to single spaces.
     *
     * @param typeStr the type string to tokenize
     * @return list of tokens (empty strings filtered out)
     */
    internal fun tokenizeType(typeStr: String): List<String> {
        val tokens = mutableListOf<String>()
        var remaining = typeStr.trim()

        while (remaining.isNotEmpty()) {
            val match = DELIMITER_PATTERN.find(remaining)
            if (match == null) {
                // No more delimiters, add the rest as a token
                tokens.add(remaining)
                break
            }

            // Add text before the delimiter
            if (match.range.first > 0) {
                tokens.add(remaining.substring(0, match.range.first))
            }

            // Add the delimiter itself (collapse whitespace)
            val delimiter = match.value
            if (delimiter.isBlank()) {
                tokens.add(" ")
            } else {
                tokens.add(delimiter)
            }

            remaining = remaining.substring(match.range.last + 1)
        }

        return tokens.filter { it.isNotEmpty() }
    }

    /**
     * Computes the diff between two type strings.
     *
     * Tokenizes both types, then compares tokens pairwise.
     * Tokens at the same position that are equal are marked as
     * not different; tokens that differ or are extra are marked as different.
     *
     * @param expected the expected type string
     * @param actual the actual type string
     * @return a pair of (expectedSegments, actualSegments) with diff annotations
     */
    internal fun computeDiff(
        expected: String,
        actual: String,
    ): Pair<List<TypeSegment>, List<TypeSegment>> {
        val expectedTokens = tokenizeType(expected)
        val actualTokens = tokenizeType(actual)

        val maxLen = maxOf(expectedTokens.size, actualTokens.size)

        val expectedSegments = mutableListOf<TypeSegment>()
        val actualSegments = mutableListOf<TypeSegment>()

        for (i in 0 until maxLen) {
            val expectedToken = expectedTokens.getOrNull(i)
            val actualToken = actualTokens.getOrNull(i)

            if (expectedToken != null && actualToken != null) {
                val isDifferent = expectedToken != actualToken
                expectedSegments.add(TypeSegment(expectedToken, isDifferent))
                actualSegments.add(TypeSegment(actualToken, isDifferent))
            } else if (expectedToken != null) {
                expectedSegments.add(TypeSegment(expectedToken, true))
            } else if (actualToken != null) {
                actualSegments.add(TypeSegment(actualToken, true))
            }
        }

        return expectedSegments to actualSegments
    }

    /**
     * Formats diff segments into a single string with markers around different parts.
     *
     * Different segments are wrapped in `[...]` brackets for text-mode display.
     *
     * @param segments the list of typed segments with diff annotations
     * @return the formatted string with diff markers
     */
    internal fun formatWithMarkers(segments: List<TypeSegment>): String =
        segments.joinToString("") { segment ->
            if (segment.isDifferent) "[${segment.text}]" else segment.text
        }
}
