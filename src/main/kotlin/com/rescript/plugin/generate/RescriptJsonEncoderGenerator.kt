package com.rescript.plugin.generate
import com.rescript.plugin.lang.RecordField
import com.rescript.plugin.lang.TypeShape
import com.rescript.plugin.lang.VariantConstructor

/**
 * Generates JSON encoder functions for ReScript record and variant types.
 *
 * Produces encoder functions using `@rescript/core` compatible JSON module
 * constructors (String, Number, Boolean, Null, Object, Array).
 *
 * @see RescriptJsonCodeGenerator
 * @see RescriptJsonDecoderGenerator
 */
internal object RescriptJsonEncoderGenerator {
    /**
     * Generates a JSON encoder function for the given type shape.
     *
     * @param typeName the name of the ReScript type
     * @param shape the parsed type shape
     * @return the generated encoder function text, or null if unsupported
     */
    fun generateEncoder(
        typeName: String,
        shape: TypeShape,
    ): String? =
        RescriptJsonCodeGenerator.dispatchByShape(
            shape,
            onRecord = { fields -> generateRecordEncoder(typeName, fields) },
            onVariant = { constructors -> generateVariantEncoder(typeName, constructors) },
        )

    /**
     * Generates an encode expression for a given JSON type and source expression.
     *
     * @param jsonType the classified type
     * @param expr the source expression to encode (e.g., `"v.name"`, `"v0"`)
     * @return the ReScript encode expression
     */
    fun encodeExpression(
        jsonType: RescriptJsonType,
        expr: String,
    ): String =
        when (jsonType) {
            is RescriptJsonType.StringType -> {
                "String($expr)"
            }

            is RescriptJsonType.IntType -> {
                "Number($expr->Int.toFloat)"
            }

            is RescriptJsonType.FloatType -> {
                "Number($expr)"
            }

            is RescriptJsonType.BoolType -> {
                "Boolean($expr)"
            }

            is RescriptJsonType.OptionType -> {
                val innerEncode = encodeExpression(jsonType.inner, "v")
                "$expr->Option.mapOr(Null, v => $innerEncode)"
            }

            is RescriptJsonType.ArrayType -> {
                val innerEncode = encodeExpression(jsonType.inner, "v")
                "Array($expr->Array.map(v => $innerEncode))"
            }

            is RescriptJsonType.UnknownType -> {
                "/* TODO: encode ${jsonType.raw} */ Null"
            }
        }

    // --- Record encoder ---

    private fun generateRecordEncoder(
        typeName: String,
        fields: List<RecordField>,
    ): String? {
        if (fields.isEmpty()) return null

        val funcName = RescriptJsonCodeGenerator.encoderName(typeName)
        val entries =
            RescriptJsonCodeGenerator
                .mapFieldsWithType(fields) { field, jsonType ->
                    val encodeExpr = encodeExpression(jsonType, "v.${field.name}")
                    "(\"${field.name}\", $encodeExpr)"
                }.joinToString(",\n      ")

        return buildString {
            append("let $funcName = (v: $typeName): JSON.t =>\n")
            append("  Object(\n")
            append("    Dict.fromArray([\n")
            append("      $entries,\n")
            append("    ]),\n")
            append("  )")
        }
    }

    // --- Variant encoder ---

    private fun generateVariantEncoder(
        typeName: String,
        constructors: List<VariantConstructor>,
    ): String? {
        val funcName = RescriptJsonCodeGenerator.encoderName(typeName)
        return RescriptJsonCodeGenerator.dispatchVariantKind(
            constructors,
            onSimpleEnum = { generateSimpleEnumEncoder(funcName, typeName, constructors) },
            onTaggedUnion = { generateTaggedUnionEncoder(funcName, typeName, constructors) },
        )
    }

    private fun generateSimpleEnumEncoder(
        funcName: String,
        typeName: String,
        constructors: List<VariantConstructor>,
    ): String {
        val arms =
            constructors.joinToString("\n") { ctor ->
                "  | ${ctor.name} => String(\"${ctor.name}\")"
            }

        return buildString {
            append("let $funcName = (v: $typeName): JSON.t =>\n")
            append("  switch v {\n")
            append(arms)
            append("\n")
            append("  }")
        }
    }

    private fun generateTaggedUnionEncoder(
        funcName: String,
        typeName: String,
        constructors: List<VariantConstructor>,
    ): String {
        val arms =
            constructors.joinToString("\n") { ctor ->
                if (ctor.payload == null) {
                    "  | ${ctor.name} => Object(Dict.fromArray([(\"tag\", String(\"${ctor.name}\"))]))"
                } else {
                    val classifiedTypes = RescriptJsonCodeGenerator.classifyPayloadTypes(ctor.payload)
                    val payloadBindings = classifiedTypes.indices.joinToString(", ") { i -> "v$i" }
                    val payloadEntries =
                        classifiedTypes
                            .mapIndexed { i, (_, jsonType) ->
                                "(\"_$i\", ${encodeExpression(jsonType, "v$i")})"
                            }.joinToString(", ")

                    "  | ${ctor.name}($payloadBindings) =>\n" +
                        "    Object(Dict.fromArray([(\"tag\", String(\"${ctor.name}\")), $payloadEntries]))"
                }
            }

        return buildString {
            append("let $funcName = (v: $typeName): JSON.t =>\n")
            append("  switch v {\n")
            append(arms)
            append("\n")
            append("  }")
        }
    }
}
