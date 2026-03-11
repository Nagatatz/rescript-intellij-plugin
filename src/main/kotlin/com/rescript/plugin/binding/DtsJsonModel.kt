package com.rescript.plugin.binding

/**
 * Data model for the JSON intermediate representation produced by dts-to-json.js.
 *
 * Represents the structure of a parsed `.d.ts` file as a hierarchy of data classes
 * and sealed classes. Deserialization logic is in [DtsJsonParser].
 *
 * @see DtsJsonParser for Gson deserialization
 * @see DtsToRescriptConverter for code generation from this model
 * @see DtsParserProcess for invoking the Node.js parser
 */
object DtsJsonModel {
    /**
     * Top-level result from parsing a `.d.ts` file.
     *
     * @param fileName the original file name
     * @param moduleName the inferred module name
     * @param declarations the list of parsed declarations
     * @param errors any parsing errors encountered
     */
    data class DtsFile(
        val fileName: String = "",
        val moduleName: String = "",
        val declarations: List<DtsDeclaration> = emptyList(),
        val errors: List<String> = emptyList(),
    )

    // ── Declarations ──────────────────────────────────────────────────

    /**
     * Sealed hierarchy representing a top-level declaration in a `.d.ts` file.
     * Discriminated by the `"kind"` field in JSON.
     */
    sealed class DtsDeclaration {
        abstract val name: String
        abstract val exported: Boolean
    }

    /** A top-level function declaration parsed from a `.d.ts` file. */
    data class FunctionDeclaration(
        override val name: String = "",
        override val exported: Boolean = true,
        val parameters: List<Parameter> = emptyList(),
        val returnType: DtsType = UnknownType(),
    ) : DtsDeclaration()

    /** A TypeScript interface declaration with its member list. */
    data class InterfaceDeclaration(
        override val name: String = "",
        override val exported: Boolean = true,
        val members: List<Member> = emptyList(),
    ) : DtsDeclaration()

    /** A TypeScript type alias declaration (`type X = ...`). */
    data class TypeAliasDeclaration(
        override val name: String = "",
        override val exported: Boolean = true,
        val type: DtsType = UnknownType(),
    ) : DtsDeclaration()

    /** A top-level variable or constant declaration from a `.d.ts` file. */
    data class VariableDeclaration(
        override val name: String = "",
        override val exported: Boolean = true,
        val type: DtsType = UnknownType(),
        val isConst: Boolean = false,
    ) : DtsDeclaration()

    /** A TypeScript enum declaration with its members. */
    data class EnumDeclaration(
        override val name: String = "",
        override val exported: Boolean = true,
        val members: List<EnumMember> = emptyList(),
        val isStringEnum: Boolean = false,
    ) : DtsDeclaration()

    /** A TypeScript class declaration with constructors, methods, and properties. */
    data class ClassDeclaration(
        override val name: String = "",
        override val exported: Boolean = true,
        val constructors: List<ConstructorDecl> = emptyList(),
        val methods: List<MethodDecl> = emptyList(),
        val properties: List<Member> = emptyList(),
    ) : DtsDeclaration()

    // ── Supporting types ──────────────────────────────────────────────

    /** A function or method parameter with name, type, and optionality. */
    data class Parameter(
        val name: String = "",
        val type: DtsType = UnknownType(),
        val optional: Boolean = false,
    )

    /** An interface or object member (property or method signature). */
    data class Member(
        val name: String = "",
        val type: DtsType = UnknownType(),
        val optional: Boolean = false,
        val readonly: Boolean = false,
    )

    /** A single member of a TypeScript enum with an optional literal value. */
    data class EnumMember(
        val name: String = "",
        val value: String? = null,
    )

    /** A class constructor declaration with its parameter list. */
    data class ConstructorDecl(
        val parameters: List<Parameter> = emptyList(),
    )

    /** A class method declaration with name, parameters, and return type. */
    data class MethodDecl(
        val name: String = "",
        val parameters: List<Parameter> = emptyList(),
        val returnType: DtsType = UnknownType(),
    )

    // ── Type nodes ────────────────────────────────────────────────────

    /**
     * Sealed hierarchy representing a TypeScript type node.
     * Discriminated by the `"kind"` field in JSON.
     */
    sealed class DtsType

    /** A primitive TypeScript type such as `string`, `number`, or `boolean`. */
    data class PrimitiveType(
        val name: String = "",
    ) : DtsType()

    /** A named type reference, optionally with generic type arguments. */
    data class ReferenceType(
        val name: String = "",
        val typeArguments: List<DtsType> = emptyList(),
    ) : DtsType()

    /** A TypeScript array type (`T[]`). */
    data class ArrayType(
        val elementType: DtsType = UnknownType(),
    ) : DtsType()

    /** A TypeScript tuple type (`[A, B, ...]`). */
    data class TupleType(
        val elements: List<DtsType> = emptyList(),
    ) : DtsType()

    /** A TypeScript function type (`(params) => returnType`). */
    data class FunctionType(
        val parameters: List<Parameter> = emptyList(),
        val returnType: DtsType = UnknownType(),
    ) : DtsType()

    /** A TypeScript union type (`A | B`). */
    data class UnionType(
        val types: List<DtsType> = emptyList(),
    ) : DtsType()

    /** A TypeScript intersection type (`A & B`). */
    data class IntersectionType(
        val types: List<DtsType> = emptyList(),
    ) : DtsType()

    /** A TypeScript object literal type (`{ key: Type }`). */
    data class ObjectLiteralType(
        val members: List<Member> = emptyList(),
    ) : DtsType()

    /** A string literal type (e.g., `"click"`). */
    data class StringLiteralType(
        val value: String = "",
    ) : DtsType()

    /** A numeric literal type (e.g., `42`). */
    data class NumericLiteralType(
        val value: String = "",
    ) : DtsType()

    /** A TypeScript index signature type (`{ [key: K]: V }`). */
    data class IndexSignatureType(
        val keyType: DtsType = PrimitiveType("string"),
        val valueType: DtsType = UnknownType(),
    ) : DtsType()

    /** Fallback for unrecognized or unsupported TypeScript type nodes. */
    data class UnknownType(
        val text: String = "",
    ) : DtsType()

    /**
     * Parses a JSON string into a [DtsFile].
     * Delegates to [DtsJsonParser] for Gson deserialization.
     *
     * @param json the JSON string from dts-to-json.js
     * @return the parsed file model
     */
    fun parse(json: String): DtsFile = DtsJsonParser.parse(json)
}
