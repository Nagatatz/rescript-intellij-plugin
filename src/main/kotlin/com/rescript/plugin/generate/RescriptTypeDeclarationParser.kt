package com.rescript.plugin.generate

/** A single constructor arm in a variant type declaration. */
data class VariantConstructor(
    val name: String,
    val payload: String?,
)

/** A single field in a record type declaration. */
data class RecordField(
    val name: String,
    val typeAnnotation: String,
    val isMutable: Boolean,
)

/** Sealed hierarchy representing the parsed shape of a ReScript type declaration. */
sealed class TypeShape {
    data class Variant(
        val constructors: List<VariantConstructor>,
    ) : TypeShape()

    data class Record(
        val fields: List<RecordField>,
    ) : TypeShape()

    data object Unknown : TypeShape()
}

/**
 * Text-based parser for ReScript type declarations.
 *
 * Extracts the shape (variant or record) from the raw text of a `type` declaration.
 * Used by code generation actions to produce switch arms or module type signatures
 * from existing type definitions.
 *
 * @see RescriptGenerateSwitchAction
 */
object RescriptTypeDeclarationParser {
    fun parse(declarationText: String): TypeShape {
        val body = extractBody(declarationText) ?: return TypeShape.Unknown
        val trimmed = body.trim()

        if (trimmed.isEmpty()) return TypeShape.Unknown

        // Record: starts with {
        if (trimmed.startsWith("{")) {
            return parseRecord(trimmed)
        }

        // Variant: contains | or starts with uppercase letter
        if (trimmed.contains("|") || trimmed.first().isUpperCase()) {
            return parseVariant(trimmed)
        }

        return TypeShape.Unknown
    }

    fun extractTypeName(declarationText: String): String? {
        val match = Regex("""^\s*type\s+(\w+)""").find(declarationText)
        return match?.groupValues?.get(1)
    }

    private fun extractBody(text: String): String? {
        val eqIndex = text.indexOf('=')
        if (eqIndex < 0) return null
        return text.substring(eqIndex + 1)
    }

    private fun parseVariant(text: String): TypeShape {
        val constructors = mutableListOf<VariantConstructor>()

        // Split by | and process each arm
        val arms =
            text.split("|").map { it.trim() }.filter { it.isNotEmpty() }

        for (arm in arms) {
            val constructor = parseConstructor(arm)
            if (constructor != null) {
                constructors.add(constructor)
            }
        }

        return if (constructors.isNotEmpty()) {
            TypeShape.Variant(constructors)
        } else {
            TypeShape.Unknown
        }
    }

    private fun parseConstructor(arm: String): VariantConstructor? {
        val trimmed = arm.trim()
        if (trimmed.isEmpty()) return null

        // Match: ConstructorName or ConstructorName(payload)
        val match = Regex("""^([A-Z]\w*)(?:\((.+)\))?\s*$""").find(trimmed)
        return if (match != null) {
            VariantConstructor(
                name = match.groupValues[1],
                payload = match.groupValues[2].ifEmpty { null },
            )
        } else {
            null
        }
    }

    private fun parseRecord(text: String): TypeShape {
        // Remove outer braces
        val inner =
            text.trimStart('{').trimEnd('}').trim()
        if (inner.isEmpty()) return TypeShape.Record(emptyList())

        val fields = mutableListOf<RecordField>()

        // Split by comma, handling potential newlines
        val parts = inner.split(",", "\n").map { it.trim() }.filter { it.isNotEmpty() }

        for (part in parts) {
            val field = parseField(part)
            if (field != null) {
                fields.add(field)
            }
        }

        return TypeShape.Record(fields)
    }

    private fun parseField(text: String): RecordField? {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return null

        // Match: mutable? fieldName: type
        val match = Regex("""^(mutable\s+)?(\w+)\s*:\s*(.+)$""").find(trimmed)
        return if (match != null) {
            RecordField(
                name = match.groupValues[2],
                typeAnnotation = match.groupValues[3].trim(),
                isMutable = match.groupValues[1].isNotEmpty(),
            )
        } else {
            null
        }
    }
}
