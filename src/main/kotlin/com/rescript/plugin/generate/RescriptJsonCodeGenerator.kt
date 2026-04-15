package com.rescript.plugin.generate

/**
 * Generates JSON encoder and decoder functions from ReScript type declarations.
 *
 * Produces `@rescript/core` compatible code using the `JSON` module constructors
 * (`String`, `Number`, `Boolean`, `Null`, `Object`, `Array`). Supports record types
 * and variant types (both simple enums and tagged unions with payloads).
 *
 * Encoder generation is delegated to [RescriptJsonEncoderGenerator] and
 * decoder generation to [RescriptJsonDecoderGenerator].
 *
 * @see RescriptJsonTypeClassifier
 * @see RescriptGenerateJsonCodecAction
 * @see RescriptJsonEncoderGenerator
 * @see RescriptJsonDecoderGenerator
 */
object RescriptJsonCodeGenerator {
    /**
     * Generates both encoder and decoder functions for the given type.
     *
     * @param typeName the name of the ReScript type (e.g., `"user"`, `"color"`)
     * @param shape the parsed type shape from [RescriptTypeDeclarationParser]
     * @return the generated code containing both functions, or null if the shape is unsupported
     */
    fun generateBoth(
        typeName: String,
        shape: TypeShape,
    ): String? {
        val encoder = generateEncoder(typeName, shape) ?: return null
        val decoder = generateDecoder(typeName, shape) ?: return null
        return "$encoder\n\n$decoder"
    }

    /**
     * Generates a JSON encoder function for the given type.
     *
     * @param typeName the name of the ReScript type
     * @param shape the parsed type shape
     * @return the generated encoder function text, or null if unsupported
     */
    fun generateEncoder(
        typeName: String,
        shape: TypeShape,
    ): String? = RescriptJsonEncoderGenerator.generateEncoder(typeName, shape)

    /**
     * Generates a JSON decoder function for the given type.
     *
     * @param typeName the name of the ReScript type
     * @param shape the parsed type shape
     * @return the generated decoder function text, or null if unsupported
     */
    fun generateDecoder(
        typeName: String,
        shape: TypeShape,
    ): String? = RescriptJsonDecoderGenerator.generateDecoder(typeName, shape)

    /**
     * Builds the encoder function name from the type name.
     * Type `t` produces `encode`, others produce `encode` + capitalized name.
     */
    internal fun encoderName(typeName: String): String =
        if (typeName == "t") "encode" else "encode${capitalize(typeName)}"

    /**
     * Builds the decoder function name from the type name.
     * Type `t` produces `decode`, others produce `decode` + capitalized name.
     */
    internal fun decoderName(typeName: String): String =
        if (typeName == "t") "decode" else "decode${capitalize(typeName)}"

    private fun capitalize(s: String): String = s.replaceFirstChar { it.uppercase() }

    /**
     * Generates an encode expression for a given JSON type and source expression.
     *
     * @param jsonType the classified type
     * @param expr the source expression to encode (e.g., `"v.name"`, `"v0"`)
     * @return the ReScript encode expression
     */
    internal fun encodeExpression(
        jsonType: RescriptJsonType,
        expr: String,
    ): String = RescriptJsonEncoderGenerator.encodeExpression(jsonType, expr)

    /**
     * Generates a decode let-binding for a record field.
     *
     * @param jsonType the classified field type
     * @param fieldName the record field name
     * @return the let-binding expression for decoding the field
     */
    internal fun decodeFieldExpression(
        jsonType: RescriptJsonType,
        fieldName: String,
    ): String = RescriptJsonDecoderGenerator.decodeFieldExpression(jsonType, fieldName)

    /**
     * Generates an inline decode expression (switch on a JSON value `v`).
     *
     * @param jsonType the classified type
     * @return the inline decode switch expression
     */
    internal fun decodeInlineExpression(jsonType: RescriptJsonType): String =
        RescriptJsonDecoderGenerator.decodeInlineExpression(jsonType)

    /**
     * Maps record fields with their classified JSON types.
     *
     * Centralizes the [RescriptJsonTypeClassifier.classify] call so that
     * both encoder and decoder generators use a consistent type classification.
     *
     * @param fields the record fields to process
     * @param transform function receiving each field and its classified type
     * @return list of transformed results
     */
    internal fun <R> mapFieldsWithType(
        fields: List<RecordField>,
        transform: (field: RecordField, jsonType: RescriptJsonType) -> R,
    ): List<R> =
        fields.map { field ->
            val jsonType = RescriptJsonTypeClassifier.classify(field.typeAnnotation)
            transform(field, jsonType)
        }

    /**
     * Maps variant constructor payload types with their classified JSON types.
     *
     * Splits the payload string into individual types (respecting angle bracket
     * nesting) and classifies each one.
     *
     * @param payload the raw payload string (e.g., "string, int")
     * @return list of pairs of (type string, classified JSON type)
     */
    internal fun classifyPayloadTypes(payload: String): List<Pair<String, RescriptJsonType>> =
        splitPayloadTypes(payload).map { typeStr ->
            typeStr to RescriptJsonTypeClassifier.classify(typeStr)
        }

    /**
     * Checks whether all variant constructors have no payload (simple enum).
     *
     * @param constructors the variant constructors
     * @return true if all constructors are nullary (no payload)
     */
    internal fun isSimpleEnum(constructors: List<VariantConstructor>): Boolean = constructors.all { it.payload == null }

    /**
     * Dispatches by [TypeShape] to the appropriate generator branch.
     *
     * Encoder and decoder share an identical top-level shape dispatch so future
     * additions to [TypeShape] only need to be wired in here.
     *
     * @return the result from the matching branch, or null for [TypeShape.Unknown]
     */
    internal inline fun <T : Any> dispatchByShape(
        shape: TypeShape,
        onRecord: (List<RecordField>) -> T?,
        onVariant: (List<VariantConstructor>) -> T?,
    ): T? =
        when (shape) {
            is TypeShape.Record -> onRecord(shape.fields)
            is TypeShape.Variant -> onVariant(shape.constructors)
            is TypeShape.Unknown -> null
        }

    /**
     * Dispatches variant generation by enum shape (simple enum vs tagged union).
     *
     * Returns null when [constructors] is empty (nothing to generate).
     */
    internal inline fun <T : Any> dispatchVariantKind(
        constructors: List<VariantConstructor>,
        onSimpleEnum: () -> T,
        onTaggedUnion: () -> T,
    ): T? {
        if (constructors.isEmpty()) return null
        return if (isSimpleEnum(constructors)) onSimpleEnum() else onTaggedUnion()
    }

    /**
     * Splits a variant constructor payload into individual type strings.
     *
     * Respects angle bracket nesting so that `"string, array<int>"` splits
     * correctly into `["string", "array<int>"]`.
     *
     * @param payload the raw payload string (e.g., `"string, int"`)
     * @return list of individual type strings
     */
    internal fun splitPayloadTypes(payload: String): List<String> {
        val result = mutableListOf<String>()
        var depth = 0
        var current = StringBuilder()

        for (ch in payload) {
            when (ch) {
                '<' -> {
                    depth++
                    current.append(ch)
                }

                '>' -> {
                    depth--
                    current.append(ch)
                }

                ',' -> {
                    if (depth == 0) {
                        result.add(current.toString().trim())
                        current = StringBuilder()
                    } else {
                        current.append(ch)
                    }
                }

                else -> {
                    current.append(ch)
                }
            }
        }

        val last = current.toString().trim()
        if (last.isNotEmpty()) {
            result.add(last)
        }

        return result
    }
}
