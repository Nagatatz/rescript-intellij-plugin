package com.rescript.plugin.generate

/**
 * Generates JSON encoder and decoder functions from ReScript type declarations.
 *
 * Produces `@rescript/core` compatible code using the `JSON` module constructors
 * (`String`, `Number`, `Boolean`, `Null`, `Object`, `Array`). Supports record types
 * and variant types (both simple enums and tagged unions with payloads).
 *
 * @see RescriptJsonTypeClassifier
 * @see RescriptGenerateJsonCodecAction
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
    ): String? =
        when (shape) {
            is TypeShape.Record -> generateRecordEncoder(typeName, shape.fields)
            is TypeShape.Variant -> generateVariantEncoder(typeName, shape.constructors)
            is TypeShape.Unknown -> null
        }

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
    ): String? =
        when (shape) {
            is TypeShape.Record -> generateRecordDecoder(typeName, shape.fields)
            is TypeShape.Variant -> generateVariantDecoder(typeName, shape.constructors)
            is TypeShape.Unknown -> null
        }

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

    // --- Record encoder ---

    private fun generateRecordEncoder(
        typeName: String,
        fields: List<RecordField>,
    ): String? {
        if (fields.isEmpty()) return null

        val funcName = encoderName(typeName)
        val entries =
            fields.joinToString(",\n      ") { field ->
                val jsonType = RescriptJsonTypeClassifier.classify(field.typeAnnotation)
                val encodeExpr = encodeExpression(jsonType, "v.${field.name}")
                "(\"${field.name}\", $encodeExpr)"
            }

        return buildString {
            append("let $funcName = (v: $typeName): JSON.t =>\n")
            append("  Object(\n")
            append("    Dict.fromArray([\n")
            append("      $entries,\n")
            append("    ]),\n")
            append("  )")
        }
    }

    // --- Record decoder ---

    private fun generateRecordDecoder(
        typeName: String,
        fields: List<RecordField>,
    ): String? {
        if (fields.isEmpty()) return null

        val funcName = decoderName(typeName)

        // Build let bindings for each field
        val letBindings =
            fields.joinToString("\n") { field ->
                val jsonType = RescriptJsonTypeClassifier.classify(field.typeAnnotation)
                val decodeExpr = decodeFieldExpression(jsonType, field.name)
                "      $decodeExpr"
            }

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

    // --- Variant encoder ---

    private fun generateVariantEncoder(
        typeName: String,
        constructors: List<VariantConstructor>,
    ): String? {
        if (constructors.isEmpty()) return null

        val funcName = encoderName(typeName)
        val isSimpleEnum = constructors.all { it.payload == null }

        return if (isSimpleEnum) {
            generateSimpleEnumEncoder(funcName, typeName, constructors)
        } else {
            generateTaggedUnionEncoder(funcName, typeName, constructors)
        }
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
                    val payloadTypes = splitPayloadTypes(ctor.payload)
                    val payloadBindings = payloadTypes.indices.joinToString(", ") { i -> "v$i" }
                    val payloadEntries =
                        payloadTypes
                            .mapIndexed { i, typeStr ->
                                val jsonType = RescriptJsonTypeClassifier.classify(typeStr)
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

    // --- Variant decoder ---

    private fun generateVariantDecoder(
        typeName: String,
        constructors: List<VariantConstructor>,
    ): String? {
        if (constructors.isEmpty()) return null

        val funcName = decoderName(typeName)
        val isSimpleEnum = constructors.all { it.payload == null }

        return if (isSimpleEnum) {
            generateSimpleEnumDecoder(funcName, typeName, constructors)
        } else {
            generateTaggedUnionDecoder(funcName, typeName, constructors)
        }
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
                    val payloadTypes = splitPayloadTypes(ctor.payload)
                    val letBindings =
                        payloadTypes
                            .mapIndexed { i, typeStr ->
                                val jsonType = RescriptJsonTypeClassifier.classify(typeStr)
                                "        let v$i = dict->Dict.get(\"_$i\")->Option.flatMap(v =>\n" +
                                    "          ${decodeInlineExpression(jsonType)}\n" +
                                    "        )"
                            }.joinToString("\n")

                    val somePattern = payloadTypes.indices.joinToString(", ") { i -> "Some(v$i)" }
                    val ctorArgs = payloadTypes.indices.joinToString(", ") { i -> "v$i" }

                    "    | Some(String(\"${ctor.name}\")) => {\n" +
                        "$letBindings\n" +
                        "        switch (${payloadTypes.indices.joinToString(", ") { i -> "v$i" }}) {\n" +
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

    // --- Encode expressions ---

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
    ): String =
        when (jsonType) {
            is RescriptJsonType.StringType -> "String($expr)"
            is RescriptJsonType.IntType -> "Number($expr->Int.toFloat)"
            is RescriptJsonType.FloatType -> "Number($expr)"
            is RescriptJsonType.BoolType -> "Boolean($expr)"
            is RescriptJsonType.OptionType -> {
                val innerEncode = encodeExpression(jsonType.inner, "v")
                "$expr->Option.mapOr(Null, v => $innerEncode)"
            }
            is RescriptJsonType.ArrayType -> {
                val innerEncode = encodeExpression(jsonType.inner, "v")
                "Array($expr->Array.map(v => $innerEncode))"
            }
            is RescriptJsonType.UnknownType -> "/* TODO: encode ${jsonType.raw} */ Null"
        }

    // --- Decode expressions ---

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
    internal fun decodeInlineExpression(jsonType: RescriptJsonType): String =
        when (jsonType) {
            is RescriptJsonType.StringType ->
                "switch v { | String(v) => Some(v) | _ => None }"
            is RescriptJsonType.IntType ->
                "switch v { | Number(v) => Some(v->Int.fromFloat) | _ => None }"
            is RescriptJsonType.FloatType ->
                "switch v { | Number(v) => Some(v) | _ => None }"
            is RescriptJsonType.BoolType ->
                "switch v { | Boolean(v) => Some(v) | _ => None }"
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
            is RescriptJsonType.UnknownType ->
                "/* TODO: decode ${jsonType.raw} */ None"
        }

    // --- Utility ---

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
                else -> current.append(ch)
            }
        }

        val last = current.toString().trim()
        if (last.isNotEmpty()) {
            result.add(last)
        }

        return result
    }
}
