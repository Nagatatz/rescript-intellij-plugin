package com.rescript.plugin.generate

/**
 * Sealed hierarchy representing classified ReScript types for JSON codec generation.
 *
 * Maps type annotation strings (e.g., `"string"`, `"option<array<int>>"`) to
 * a structured representation used by [RescriptJsonCodeGenerator] to produce
 * appropriate encoder/decoder expressions.
 *
 * @see RescriptJsonTypeClassifier
 */
sealed class RescriptJsonType {
    /** ReScript `string` type, encoded as `JSON.String`. */
    data object StringType : RescriptJsonType()

    /** ReScript `int` type, encoded as `JSON.Number` via `Int.toFloat`. */
    data object IntType : RescriptJsonType()

    /** ReScript `float` type, encoded as `JSON.Number`. */
    data object FloatType : RescriptJsonType()

    /** ReScript `bool` type, encoded as `JSON.Boolean`. */
    data object BoolType : RescriptJsonType()

    /** ReScript `option<T>` type, encoded as the inner type or `JSON.Null`. */
    data class OptionType(
        val inner: RescriptJsonType,
    ) : RescriptJsonType()

    /** ReScript `array<T>` type, encoded as `JSON.Array`. */
    data class ArrayType(
        val inner: RescriptJsonType,
    ) : RescriptJsonType()

    /** An unrecognized type that requires manual encoder/decoder implementation. */
    data class UnknownType(
        val raw: String,
    ) : RescriptJsonType()
}

/**
 * Classifies ReScript type annotation strings into [RescriptJsonType] values.
 *
 * Handles primitive types (`string`, `int`, `float`, `bool`) and generic types
 * (`option<T>`, `array<T>`) with arbitrary nesting depth. Unrecognized types
 * are wrapped in [RescriptJsonType.UnknownType].
 *
 * @see RescriptJsonCodeGenerator
 */
object RescriptJsonTypeClassifier {
    /**
     * Classifies a ReScript type annotation string into a [RescriptJsonType].
     *
     * @param typeAnnotation the raw type annotation (e.g., `"string"`, `"option<array<int>>"`)
     * @return the classified type
     */
    fun classify(typeAnnotation: String): RescriptJsonType =
        when (val trimmed = typeAnnotation.trim()) {
            "string" -> RescriptJsonType.StringType
            "int" -> RescriptJsonType.IntType
            "float" -> RescriptJsonType.FloatType
            "bool" -> RescriptJsonType.BoolType
            else -> classifyGeneric(trimmed)
        }

    /**
     * Attempts to classify a generic type (e.g., `option<T>`, `array<T>`).
     * Falls back to [RescriptJsonType.UnknownType] if the type is not recognized.
     */
    private fun classifyGeneric(type: String): RescriptJsonType {
        val innerType = extractGenericInner("option", type)
        if (innerType != null) {
            return RescriptJsonType.OptionType(classify(innerType))
        }

        val arrayInner = extractGenericInner("array", type)
        if (arrayInner != null) {
            return RescriptJsonType.ArrayType(classify(arrayInner))
        }

        return RescriptJsonType.UnknownType(type)
    }

    /**
     * Extracts the inner type argument from a generic type annotation.
     *
     * Uses angle bracket depth tracking to correctly handle nested generics
     * (e.g., `option<array<string>>` extracts `array<string>`).
     *
     * @param wrapper the outer type name (e.g., `"option"`, `"array"`)
     * @param type the full type annotation string
     * @return the inner type string, or null if the type doesn't match the wrapper
     */
    internal fun extractGenericInner(
        wrapper: String,
        type: String,
    ): String? {
        if (!type.startsWith("$wrapper<") || !type.endsWith(">")) return null

        val inner = type.substring(wrapper.length + 1, type.length - 1)

        // Validate balanced angle brackets
        var depth = 0
        for (ch in inner) {
            when (ch) {
                '<' -> {
                    depth++
                }

                '>' -> {
                    depth--
                    if (depth < 0) return null
                }
            }
        }

        return if (depth == 0) inner else null
    }
}
