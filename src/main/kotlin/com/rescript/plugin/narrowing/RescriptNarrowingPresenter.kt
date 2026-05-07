package com.rescript.plugin.narrowing

/**
 * Formats the type string returned by LSP hover into the short label
 * displayed by the type-narrowing inlay hint, alongside the original
 * pattern summary.
 *
 * Filters out trivial types (e.g. `unit`, free type variables) where
 * showing a hint would only add visual noise without conveying any
 * narrowing information.
 */
object RescriptNarrowingPresenter {
    /** Maximum number of characters shown inline before truncation. */
    private const val MAX_LENGTH = 64

    /**
     * Type strings that the visualizer treats as uninformative and
     * suppresses (returning `null` from [format]).
     *
     * - Free type variables like `'_a` indicate a still-unsolved row,
     *   not a narrowed type.
     * - `unit` and `unknown` carry no narrowing value either.
     */
    private val TRIVIAL_TYPES = setOf("unit", "unknown")

    private val FREE_TYPE_VAR = Regex("^'[_a-zA-Z][_a-zA-Z0-9]*$")

    /**
     * Returns the inline hint text for the given LSP hover result, or
     * `null` if no hint should be displayed.
     *
     * @param narrowedType the type string extracted from LSP hover, or
     *   null when LSP returned nothing
     * @param patternSummary short pattern label produced by
     *   [RescriptSwitchArmCollector.collect]; reserved for future use
     *   (currently ignored, kept in signature so callers don't change
     *   when we start prefixing with the pattern)
     * @return the formatted hint, or `null` when the type is trivial
     *   or unavailable
     */
    @Suppress("UNUSED_PARAMETER")
    fun format(
        narrowedType: String?,
        patternSummary: String,
    ): String? {
        if (narrowedType.isNullOrBlank()) return null
        val trimmed = narrowedType.trim()
        if (trimmed in TRIVIAL_TYPES) return null
        if (FREE_TYPE_VAR.matches(trimmed)) return null
        val singleLine = trimmed.replace(Regex("\\s+"), " ")
        return if (singleLine.length > MAX_LENGTH) {
            singleLine.substring(0, MAX_LENGTH - 1) + "…"
        } else {
            singleLine
        }
    }

    /**
     * Returns the full type text for the tooltip / "show full" action.
     * Always returns the unmodified hover string when present (with
     * leading/trailing whitespace stripped). Returns `null` when no
     * type information is available.
     */
    fun fullText(narrowedType: String?): String? {
        if (narrowedType.isNullOrBlank()) return null
        return narrowedType.trim()
    }
}
