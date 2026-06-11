package com.rescript.plugin.generate
import com.rescript.plugin.lang.RecordField
import com.rescript.plugin.lang.TypeShape
import com.rescript.plugin.lang.VariantConstructor

/**
 * Generates JSON decoder functions for ReScript record and variant types.
 *
 * Produces decoder functions that pattern-match on JSON.t values and return
 * option types for safe decoding.
 *
 * @see RescriptJsonCodeGenerator
 * @see RescriptJsonEncoderGenerator
 */
internal object RescriptJsonDecoderGenerator {
    /**
     * Generates a JSON decoder function for the given type shape.
     *
     * @param typeName the name of the ReScript type
     * @param shape the parsed type shape
     * @return the generated decoder function text, or null if unsupported
     */
    fun generateDecoder(
        typeName: String,
        shape: TypeShape,
    ): String? =
        RescriptJsonCodeGenerator.dispatchByShape(
            shape,
            onRecord = { fields -> generateRecordDecoder(typeName, fields) },
            onVariant = { constructors -> generateVariantDecoder(typeName, constructors) },
        )

    /**
     * Generates a decode let-binding for a record field.
     *
     * @param jsonType the classified field type
     * @param fieldName the record field name
     * @return the let-binding expression for decoding the field
     */
    fun decodeFieldExpression(
        jsonType: RescriptJsonType,
        fieldName: String,
    ): String {
        val baseExpr = "dict->Dict.get(\"$fieldName\")"

        return when (jsonType) {
            is RescriptJsonType.OptionType -> {
                val innerDecode = decodeInlineExpression(jsonType.inner)
                "let $fieldName = $baseExpr->Option.flatMap(v =>\n" +
                    "        switch v { | Null => Some(None) | _ => $innerDecode->Option.map(v => Some(v)) }\n" +
                    "      )"
            }

            else -> {
                val decode = decodeInlineExpression(jsonType)
                "let $fieldName = $baseExpr->Option.flatMap(v =>\n" +
                    "        $decode\n" +
                    "      )"
            }
        }
    }

    /**
     * Generates an inline decode expression (switch on a JSON value `v`).
     *
     * @param jsonType the classified type
     * @return the inline decode switch expression
     */
    fun decodeInlineExpression(jsonType: RescriptJsonType): String =
        when (jsonType) {
            is RescriptJsonType.StringType -> {
                "switch v { | String(v) => Some(v) | _ => None }"
            }

            is RescriptJsonType.IntType -> {
                "switch v { | Number(v) => Some(v->Int.fromFloat) | _ => None }"
            }

            is RescriptJsonType.FloatType -> {
                "switch v { | Number(v) => Some(v) | _ => None }"
            }

            is RescriptJsonType.BoolType -> {
                "switch v { | Boolean(v) => Some(v) | _ => None }"
            }

            is RescriptJsonType.OptionType -> {
                val innerDecode = decodeInlineExpression(jsonType.inner)
                "switch v { | Null => Some(None) | _ => ($innerDecode)->Option.map(v => Some(v)) }"
            }

            is RescriptJsonType.ArrayType -> {
                val innerDecode = decodeInlineExpression(jsonType.inner)
                "switch v {\n" +
                    "          | Array(arr) =>\n" +
                    "            arr->Array.reduce(Some([]), (acc, v) =>\n" +
                    "              switch (acc, $innerDecode) {\n" +
                    "              | (Some(arr), Some(v)) => Some(Array.concat(arr, [v]))\n" +
                    "              | _ => None\n" +
                    "              }\n" +
                    "            )\n" +
                    "          | _ => None\n" +
                    "        }"
            }

            is RescriptJsonType.UnknownType -> {
                "/* TODO: decode ${jsonType.raw} */ None"
            }
        }

    // --- Record decoder ---

    private fun generateRecordDecoder(
        typeName: String,
        fields: List<RecordField>,
    ): String? {
        if (fields.isEmpty()) return null

        val funcName = RescriptJsonCodeGenerator.decoderName(typeName)

        // Build let bindings for each field
        val letBindings =
            RescriptJsonCodeGenerator
                .mapFieldsWithType(fields) { field, jsonType ->
                    val decodeExpr = decodeFieldExpression(jsonType, field.name)
                    "      $decodeExpr"
                }.joinToString("\n")

        // Build tuple pattern for the switch
        val tuplePattern = fields.joinToString(", ") { field -> field.name }
        val somePattern = fields.joinToString(", ") { field -> "Some(${field.name})" }
        val recordBody = fields.joinToString(", ") { field -> "${field.name}: ${field.name}" }

        return buildString {
            append("let $funcName = (json: JSON.t): option<$typeName> =>\n")
            append("  switch json {\n")
            append("  | Object(dict) => {\n")
            append(letBindings)
            append("\n")
            append("      switch ($tuplePattern) {\n")
            append("      | ($somePattern) => Some({$recordBody})\n")
            append("      | _ => None\n")
            append("      }\n")
            append("    }\n")
            append("  | _ => None\n")
            append("  }")
        }
    }

    // --- Variant decoder ---

    private fun generateVariantDecoder(
        typeName: String,
        constructors: List<VariantConstructor>,
    ): String? {
        val funcName = RescriptJsonCodeGenerator.decoderName(typeName)
        return RescriptJsonCodeGenerator.dispatchVariantKind(
            constructors,
            onSimpleEnum = { generateSimpleEnumDecoder(funcName, typeName, constructors) },
            onTaggedUnion = { generateTaggedUnionDecoder(funcName, typeName, constructors) },
        )
    }

    private fun generateSimpleEnumDecoder(
        funcName: String,
        typeName: String,
        constructors: List<VariantConstructor>,
    ): String {
        val arms =
            constructors.joinToString("\n") { ctor ->
                "  | String(\"${ctor.name}\") => Some(${ctor.name})"
            }

        return buildString {
            append("let $funcName = (json: JSON.t): option<$typeName> =>\n")
            append("  switch json {\n")
            append(arms)
            append("\n")
            append("  | _ => None\n")
            append("  }")
        }
    }

    private fun generateTaggedUnionDecoder(
        funcName: String,
        typeName: String,
        constructors: List<VariantConstructor>,
    ): String {
        val arms =
            constructors.joinToString("\n") { ctor ->
                if (ctor.payload == null) {
                    "    | Some(String(\"${ctor.name}\")) => Some(${ctor.name})"
                } else {
                    val classifiedTypes = RescriptJsonCodeGenerator.classifyPayloadTypes(ctor.payload)
                    val letBindings =
                        classifiedTypes
                            .mapIndexed { i, (_, jsonType) ->
                                "        let v$i = dict->Dict.get(\"_$i\")->Option.flatMap(v =>\n" +
                                    "          ${decodeInlineExpression(jsonType)}\n" +
                                    "        )"
                            }.joinToString("\n")

                    val somePattern = classifiedTypes.indices.joinToString(", ") { i -> "Some(v$i)" }
                    val ctorArgs = classifiedTypes.indices.joinToString(", ") { i -> "v$i" }

                    "    | Some(String(\"${ctor.name}\")) => {\n" +
                        "$letBindings\n" +
                        "        switch (${classifiedTypes.indices.joinToString(", ") { i -> "v$i" }}) {\n" +
                        "        | ($somePattern) => Some(${ctor.name}($ctorArgs))\n" +
                        "        | _ => None\n" +
                        "        }\n" +
                        "      }"
                }
            }

        return buildString {
            append("let $funcName = (json: JSON.t): option<$typeName> =>\n")
            append("  switch json {\n")
            append("  | Object(dict) =>\n")
            append("    switch dict->Dict.get(\"tag\") {\n")
            append(arms)
            append("\n")
            append("    | _ => None\n")
            append("    }\n")
            append("  | _ => None\n")
            append("  }")
        }
    }
}
