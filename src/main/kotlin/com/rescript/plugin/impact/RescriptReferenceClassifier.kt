package com.rescript.plugin.impact

/**
 * Classifies how a single reference site uses a target type, using a
 * small text-only heuristic. Pure function so it can be unit-tested
 * without an IDE fixture.
 *
 * The heuristic looks at the non-whitespace context immediately
 * preceding (and, where useful, following) the reference offset.
 * Because it works on raw text it cannot resolve aliases or follow
 * `open` statements; ambiguous cases fall through to
 * [TypeRefKind.UNKNOWN].
 */
object RescriptReferenceClassifier {
    /**
     * Returns the [TypeRefKind] for the reference whose first character
     * sits at [offset] in [source].
     *
     * @param source the full file text
     * @param offset zero-based offset of the first character of the
     *   identifier reference
     */
    fun classify(
        source: String,
        offset: Int,
    ): TypeRefKind {
        if (offset !in 0..source.length) return TypeRefKind.UNKNOWN
        val before = previousMeaningfulChar(source, offset)
        if (before != null) {
            when (before) {
                ':' -> {
                    return TypeRefKind.TYPE_REF
                }

                '.' -> {
                    return TypeRefKind.FIELD_ACCESS
                }

                '|' -> {
                    // Distinguish a constructor arm (`| Some(_) =>`) from a
                    // pattern in a destructure (`| {field}` etc.). For now we
                    // tag everything starting with `|` as PATTERN — the
                    // following lookahead promotes it to CONSTRUCTOR when the
                    // identifier starts with an uppercase letter (variant
                    // constructor convention) and is followed by `(`.
                    return promoteByLookahead(source, offset)
                }
            }
        }
        // No structural prefix — fall back to lookahead-based classification
        // so that `Some(x)` stand-alone calls still register as constructors.
        return promoteByLookahead(source, offset).takeIf { it != TypeRefKind.PATTERN } ?: TypeRefKind.UNKNOWN
    }

    private fun promoteByLookahead(
        source: String,
        offset: Int,
    ): TypeRefKind {
        val firstChar = source.getOrNull(offset) ?: return TypeRefKind.PATTERN
        if (firstChar.isUpperCase()) {
            // Walk to the end of the identifier and peek for `(`.
            var i = offset
            while (i < source.length && (source[i].isLetterOrDigit() || source[i] == '_')) i++
            // Skip insignificant whitespace.
            while (i < source.length && source[i].isWhitespace()) i++
            if (i < source.length && source[i] == '(') return TypeRefKind.CONSTRUCTOR
            return TypeRefKind.CONSTRUCTOR
        }
        return TypeRefKind.PATTERN
    }

    /**
     * Returns the most recent non-whitespace character before [offset],
     * or `null` if [offset] is the start of the file. Comments and
     * string literals are treated as opaque text, the same heuristic
     * limit as the rest of the impact preview.
     */
    private fun previousMeaningfulChar(
        source: String,
        offset: Int,
    ): Char? {
        var i = offset - 1
        while (i >= 0 && source[i].isWhitespace()) i--
        return if (i < 0) null else source[i]
    }
}
