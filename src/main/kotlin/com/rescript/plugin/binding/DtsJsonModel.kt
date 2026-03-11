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

    /** A function declaration with parameters and a return type. */
    data class FunctionDeclaration(
        override val name: String = "",
        override val exported: Boolean = true,
        val parameters: List<Parameter> = emptyList(),
        val returnType: DtsType = UnknownType(),
    ) : DtsDeclaration()

    /** An interface declaration with a list of member properties and methods. */
    data class InterfaceDeclaration(
        override val name: String = "",
        override val exported: Boolean = true,
        val members: List<Member> = emptyList(),
    ) : DtsDeclaration()

    /** A type alias declaration mapping a name to an underlying type. */
    data class TypeAliasDeclaration(
        override val name: String = "",
        override val exported: Boolean = true,
        val type: DtsType = UnknownType(),
    ) : DtsDeclaration()

    /** A variable (const/let) declaration with a type and mutability flag. */
    data class VariableDeclaration(
        override val name: String = "",
        override val exported: Boolean = true,
        val type: DtsType = UnknownType(),
        val isConst: Boolean = false,
    ) : DtsDeclaration()

    /** An enum declaration with named members, optionally string-valued. */
    data class EnumDeclaration(
        override val name: String = "",
        override val exported: Boolean = true,
        val members: List<EnumMember> = emptyList(),
        val isStringEnum: Boolean = false,
    ) : DtsDeclaration()

    /** A class declaration with constructors, methods, and properties. */
    data class ClassDeclaration(
        override val name: String = "",
        override val exported: Boolean = true,
        val constructors: List<ConstructorDecl> = emptyList(),
        val methods: List<MethodDecl> = emptyList(),
        val properties: List<Member> = emptyList(),
    ) : DtsDeclaration()

    // ── Supporting types ──────────────────────────────────────────────

    /** A function or method parameter with a name, type, and optional flag. */
    data class Parameter(
        val name: String = "",
        val type: DtsType = UnknownType(),
        val optional: Boolean = false,
    )

    /** An interface or object literal member (property or field). */
    data class Member(
        val name: String = "",
        val type: DtsType = UnknownType(),
        val optional: Boolean = false,
        val readonly: Boolean = false,
    )

    /** A single member within an enum declaration. */
    data class EnumMember(
        val name: String = "",
        val value: String? = null,
    )

    /** A class constructor declaration with its parameter list. */
    data class ConstructorDecl(
        val parameters: List<Parameter> = emptyList(),
    )

    /** A class method declaration with parameters and a return type. */
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

    /** A primitive type such as `string`, `number`, `boolean`, or `void`. */
    data class PrimitiveType(
        val name: String = "",
    ) : DtsType()

    /** A named type reference, optionally with generic type arguments. */
    data class ReferenceType(
        val name: String = "",
        val typeArguments: List<DtsType> = emptyList(),
    ) : DtsType()

    /** An array type wrapping a single element type. */
    data class ArrayType(
        val elementType: DtsType = UnknownType(),
    ) : DtsType()

    /** A tuple type containing a fixed-length list of element types. */
    data class TupleType(
        val elements: List<DtsType> = emptyList(),
    ) : DtsType()

    /** A function (callback) type with parameters and a return type. */
    data class FunctionType(
        val parameters: List<Parameter> = emptyList(),
        val returnType: DtsType = UnknownType(),
    ) : DtsType()

    /** A union type representing `A | B | C`. */
    data class UnionType(
        val types: List<DtsType> = emptyList(),
    ) : DtsType()

    /** An intersection type representing `A & B & C`. */
    data class IntersectionType(
        val types: List<DtsType> = emptyList(),
    ) : DtsType()

    /** An inline object literal type with named members. */
    data class ObjectLiteralType(
        val members: List<Member> = emptyList(),
    ) : DtsType()

    /** A string literal type representing a specific string value. */
    data class StringLiteralType(
        val value: String = "",
    ) : DtsType()

    /** A numeric literal type representing a specific number value. */
    data class NumericLiteralType(
        val value: String = "",
    ) : DtsType()

    /** An index signature type representing `{ [key: K]: V }`. */
    data class IndexSignatureType(
        val keyType: DtsType = PrimitiveType("string"),
        val valueType: DtsType = UnknownType(),
    ) : DtsType()

    /** A fallback type for unrecognized or unsupported TypeScript type nodes. */
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
