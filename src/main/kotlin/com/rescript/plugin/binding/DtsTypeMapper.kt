package com.rescript.plugin.binding

import com.rescript.plugin.binding.DtsJsonModel.ArrayType
import com.rescript.plugin.binding.DtsJsonModel.DtsType
import com.rescript.plugin.binding.DtsJsonModel.FunctionType
import com.rescript.plugin.binding.DtsJsonModel.IndexSignatureType
import com.rescript.plugin.binding.DtsJsonModel.IntersectionType
import com.rescript.plugin.binding.DtsJsonModel.NumericLiteralType
import com.rescript.plugin.binding.DtsJsonModel.ObjectLiteralType
import com.rescript.plugin.binding.DtsJsonModel.Parameter
import com.rescript.plugin.binding.DtsJsonModel.PrimitiveType
import com.rescript.plugin.binding.DtsJsonModel.ReferenceType
import com.rescript.plugin.binding.DtsJsonModel.StringLiteralType
import com.rescript.plugin.binding.DtsJsonModel.TupleType
import com.rescript.plugin.binding.DtsJsonModel.UnionType
import com.rescript.plugin.binding.DtsJsonModel.UnknownType

/**
 * Maps TypeScript types to ReScript type strings.
 *
 * Provides pure functions for converting [DtsType] nodes from the JSON model
 * into ReScript type annotation strings. Handles primitives, generics,
 * function types, unions (including nullable/optional detection), and
 * well-known reference types (Promise, Array, Map, Set, etc.).
 *
 * @see DtsJsonModel for the input type hierarchy
 * @see DtsToRescriptConverter for usage in code generation
 */
object DtsTypeMapper {
    // Well-known TypeScript → ReScript primitive mappings
    private val PRIMITIVE_MAP =
        mapOf(
            "string" to "string",
            "number" to "float",
            "boolean" to "bool",
            "void" to "unit",
            "undefined" to "unit",
            "null" to "Js.null",
            "never" to "unit",
            "any" to "JSON.t",
            "unknown" to "JSON.t",
            "object" to "JSON.t",
            "symbol" to "Symbol.t",
            "bigint" to "BigInt.t",
        )

    // Well-known generic reference types: TS name → (ReScript name, wrap format)
    private val REFERENCE_MAP =
        mapOf(
            "Array" to "array",
            "ReadonlyArray" to "array",
            "Promise" to "promise",
            "Date" to "Date.t",
            "RegExp" to "RegExp.t",
            "Map" to "Map.t",
            "ReadonlyMap" to "Map.t",
            "Set" to "Set.t",
            "ReadonlySet" to "Set.t",
            "WeakMap" to "WeakMap.t",
            "WeakSet" to "WeakSet.t",
            "Error" to "Exn.t",
            "Uint8Array" to "TypedArray.t",
            "Int32Array" to "TypedArray.t",
            "Float64Array" to "TypedArray.t",
            "ArrayBuffer" to "ArrayBuffer.t",
        )

    /**
     * Converts a [DtsType] to a ReScript type string.
     *
     * @param type the TypeScript type node to map
     * @return the ReScript type annotation string
     */
    fun mapType(type: DtsType): String =
        when (type) {
            is PrimitiveType -> mapPrimitive(type)
            is ReferenceType -> mapReference(type)
            is ArrayType -> "array<${mapType(type.elementType)}>"
            is TupleType -> mapTuple(type)
            is FunctionType -> mapFunction(type)
            is UnionType -> mapUnion(type)
            is IntersectionType -> mapIntersection(type)
            is ObjectLiteralType -> mapObjectLiteral(type)
            is StringLiteralType -> mapStringLiteral(type)
            is NumericLiteralType -> type.value
            is IndexSignatureType -> "Dict.t<${mapType(type.valueType)}>"
            is UnknownType -> if (type.text.isNotEmpty()) "/* ${type.text} */ JSON.t" else "JSON.t"
        }

    /**
     * Converts a parameter list to a ReScript function parameter type string.
     *
     * @param parameters the function parameters
     * @return the parameter portion of a function type (e.g., `(~name: string, ~age: float=?)`)
     */
    fun mapParameters(parameters: List<Parameter>): String {
        if (parameters.isEmpty()) return "unit"
        val parts =
            parameters.map { param ->
                val typeStr = mapType(param.type)
                val label = toReScriptIdentifier(param.name)
                if (param.optional) {
                    "(~$label: $typeStr=?)"
                } else {
                    "(~$label: $typeStr)"
                }
            }
        return if (parts.size == 1 && !parameters[0].optional) {
            // Single non-optional param: no labeled argument needed for simple cases
            mapType(parameters[0].type)
        } else {
            parts.joinToString(", ")
        }
    }

    /**
     * Checks if a union type is a string literal union (polymorphic variant).
     *
     * @param union the union type to check
     * @return true if all members are string literals
     */
    fun isStringLiteralUnion(union: UnionType): Boolean = union.types.all { it is StringLiteralType }

    /**
     * Converts a ReScript-unsafe identifier to a safe one.
     *
     * @param name the original identifier
     * @return a valid ReScript identifier (escaped with \\" if it's a keyword)
     */
    fun toReScriptIdentifier(name: String): String {
        val keywords =
            setOf(
                "and",
                "as",
                "assert",
                "catch",
                "constraint",
                "else",
                "exception",
                "external",
                "false",
                "for",
                "if",
                "in",
                "include",
                "lazy",
                "let",
                "module",
                "mutable",
                "of",
                "open",
                "rec",
                "switch",
                "true",
                "try",
                "type",
                "when",
                "while",
                "with",
            )
        return if (name in keywords) "\\\"$name\\\"" else name
    }

    /**
     * Converts a TypeScript name to a ReScript module-safe name (PascalCase).
     *
     * @param name the original name
     * @return PascalCase name suitable for a ReScript module
     */
    fun toModuleName(name: String): String = name.replaceFirstChar { it.uppercase() }

    private fun mapPrimitive(type: PrimitiveType): String = PRIMITIVE_MAP[type.name] ?: "JSON.t"

    private fun mapReference(type: ReferenceType): String {
        val rescriptName = REFERENCE_MAP[type.name]

        // No type arguments
        if (type.typeArguments.isEmpty()) {
            if (rescriptName != null) return rescriptName
            // Could be a user-defined type reference
            return "${type.name}.t"
        }

        // Special case: Record<string, T> → Dict.t<T>
        if (type.name == "Record" && type.typeArguments.size == 2) {
            return "Dict.t<${mapType(type.typeArguments[1])}>"
        }

        // Special case: Partial<T> → already handled as T with all optional fields
        if (type.name == "Partial" && type.typeArguments.size == 1) {
            return mapType(type.typeArguments[0])
        }

        // Generic reference with type arguments
        val args = type.typeArguments.joinToString(", ") { mapType(it) }
        val baseName = rescriptName ?: "${type.name}.t"
        return "$baseName<$args>"
    }

    private fun mapTuple(type: TupleType): String {
        if (type.elements.isEmpty()) return "unit"
        val elements = type.elements.joinToString(", ") { mapType(it) }
        return "($elements)"
    }

    private fun mapFunction(type: FunctionType): String {
        val params = mapParameters(type.parameters)
        val ret = mapType(type.returnType)
        return "$params => $ret"
    }

    private fun mapUnion(type: UnionType): String {
        // Check for nullable: T | null → Nullable.t<T>
        val nullFiltered = type.types.filter { it !is PrimitiveType || it.name != "null" }
        if (nullFiltered.size < type.types.size && nullFiltered.size == 1) {
            return "Nullable.t<${mapType(nullFiltered[0])}>"
        }

        // Check for optional: T | undefined → option<T>
        val undefinedFiltered = type.types.filter { it !is PrimitiveType || it.name != "undefined" }
        if (undefinedFiltered.size < type.types.size && undefinedFiltered.size == 1) {
            return "option<${mapType(undefinedFiltered[0])}>"
        }

        // Check for T | null | undefined → option<Nullable.t<T>>
        val bothFiltered =
            type.types.filter {
                it !is PrimitiveType || (it.name != "null" && it.name != "undefined")
            }
        if (bothFiltered.size == 1 && bothFiltered.size < type.types.size - 1) {
            return "option<Nullable.t<${mapType(bothFiltered[0])}>>"
        }

        // String literal union → polymorphic variant
        if (isStringLiteralUnion(type)) {
            return mapStringLiteralUnion(type)
        }

        // General union → fallback to JSON.t with comment
        val typeNames = type.types.joinToString(" | ") { describeType(it) }
        return "/* $typeNames */ JSON.t"
    }

    private fun mapIntersection(type: IntersectionType): String {
        val typeNames = type.types.joinToString(" & ") { describeType(it) }
        return "/* TODO: intersection $typeNames */ JSON.t"
    }

    private fun mapObjectLiteral(type: ObjectLiteralType): String {
        if (type.members.isEmpty()) return "JSON.t"
        val fields =
            type.members.joinToString(", ") { member ->
                val fieldName = toReScriptIdentifier(member.name)
                val fieldType = mapType(member.type)
                if (member.optional) {
                    "$fieldName?: $fieldType"
                } else {
                    "$fieldName: $fieldType"
                }
            }
        return "{$fields}"
    }

    private fun mapStringLiteral(type: StringLiteralType): String = "[#\"${type.value}\"]"

    /**
     * Maps a string literal union to a ReScript polymorphic variant type.
     */
    fun mapStringLiteralUnion(type: UnionType): String {
        val variants =
            type.types
                .filterIsInstance<StringLiteralType>()
                .joinToString(" | ") { "#\"${it.value}\"" }
        return "[$variants]"
    }

    /**
     * Produces a human-readable description of a type for comments.
     */
    private fun describeType(type: DtsType): String =
        when (type) {
            is PrimitiveType -> type.name
            is ReferenceType -> type.name + if (type.typeArguments.isNotEmpty()) "<...>" else ""
            is StringLiteralType -> "\"${type.value}\""
            is NumericLiteralType -> type.value
            is ArrayType -> "Array<...>"
            is FunctionType -> "(...) => ..."
            else -> "..."
        }
}
