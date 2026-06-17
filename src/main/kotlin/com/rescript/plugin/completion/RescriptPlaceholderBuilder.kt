package com.rescript.plugin.completion

import com.rescript.plugin.lang.RecordField
import com.rescript.plugin.lang.VariantConstructor

/**
 * A single variant-constructor completion candidate.
 *
 * @property lookupName the constructor name shown in the completion list
 * @property insertText the text inserted on acceptance (`Some(_)` or `None`)
 */
data class VariantPlaceholder(
    val lookupName: String,
    val insertText: String,
)

/**
 * Pure builder that turns a resolved type shape into placeholder insertion
 * strings: a single record literal skeleton, or one entry per variant
 * constructor.
 *
 * Field values are always rendered as `_` holes (no recursive expansion in
 * v1); mutable record fields are still listed since value construction does
 * not distinguish mutability.
 *
 * @see RescriptPlaceholderCompletionContributor for the editor-facing wrapper
 */
object RescriptPlaceholderBuilder {
    /**
     * Builds a record-literal skeleton such as `{ name: _, age: _ }`.
     *
     * @param fields the record's fields in declaration order
     * @return the skeleton string, or `null` if the record has no fields
     */
    fun buildRecordPlaceholder(fields: List<RecordField>): String? {
        if (fields.isEmpty()) return null
        return fields.joinToString(prefix = "{ ", postfix = " }", separator = ", ") { "${it.name}: _" }
    }

    /**
     * Builds one placeholder per variant constructor: `Name(_)` when the
     * constructor carries a payload, `Name` otherwise.
     *
     * @param constructors the variant's constructors in declaration order
     * @return placeholders in the same order; empty when there are none
     */
    fun buildVariantPlaceholders(constructors: List<VariantConstructor>): List<VariantPlaceholder> =
        constructors.map { ctor ->
            val insertText = if (ctor.payload != null) "${ctor.name}(_)" else ctor.name
            VariantPlaceholder(lookupName = ctor.name, insertText = insertText)
        }
}
