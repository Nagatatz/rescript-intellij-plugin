package com.rescript.plugin.binding

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

/**
 * Data model for the JSON intermediate representation produced by dts-to-json.js.
 *
 * Represents the structure of a parsed `.d.ts` file as a hierarchy of data classes
 * and sealed classes, deserialized from JSON using Gson with custom type adapters.
 *
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

    data class FunctionDeclaration(
        override val name: String = "",
        override val exported: Boolean = true,
        val parameters: List<Parameter> = emptyList(),
        val returnType: DtsType = UnknownType(),
    ) : DtsDeclaration()

    data class InterfaceDeclaration(
        override val name: String = "",
        override val exported: Boolean = true,
        val members: List<Member> = emptyList(),
    ) : DtsDeclaration()

    data class TypeAliasDeclaration(
        override val name: String = "",
        override val exported: Boolean = true,
        val type: DtsType = UnknownType(),
    ) : DtsDeclaration()

    data class VariableDeclaration(
        override val name: String = "",
        override val exported: Boolean = true,
        val type: DtsType = UnknownType(),
        val isConst: Boolean = false,
    ) : DtsDeclaration()

    data class EnumDeclaration(
        override val name: String = "",
        override val exported: Boolean = true,
        val members: List<EnumMember> = emptyList(),
        val isStringEnum: Boolean = false,
    ) : DtsDeclaration()

    data class ClassDeclaration(
        override val name: String = "",
        override val exported: Boolean = true,
        val constructors: List<ConstructorDecl> = emptyList(),
        val methods: List<MethodDecl> = emptyList(),
        val properties: List<Member> = emptyList(),
    ) : DtsDeclaration()

    // ── Supporting types ──────────────────────────────────────────────

    data class Parameter(
        val name: String = "",
        val type: DtsType = UnknownType(),
        val optional: Boolean = false,
    )

    data class Member(
        val name: String = "",
        val type: DtsType = UnknownType(),
        val optional: Boolean = false,
        val readonly: Boolean = false,
    )

    data class EnumMember(
        val name: String = "",
        val value: String? = null,
    )

    data class ConstructorDecl(
        val parameters: List<Parameter> = emptyList(),
    )

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

    data class PrimitiveType(
        val name: String = "",
    ) : DtsType()

    data class ReferenceType(
        val name: String = "",
        val typeArguments: List<DtsType> = emptyList(),
    ) : DtsType()

    data class ArrayType(
        val elementType: DtsType = UnknownType(),
    ) : DtsType()

    data class TupleType(
        val elements: List<DtsType> = emptyList(),
    ) : DtsType()

    data class FunctionType(
        val parameters: List<Parameter> = emptyList(),
        val returnType: DtsType = UnknownType(),
    ) : DtsType()

    data class UnionType(
        val types: List<DtsType> = emptyList(),
    ) : DtsType()

    data class IntersectionType(
        val types: List<DtsType> = emptyList(),
    ) : DtsType()

    data class ObjectLiteralType(
        val members: List<Member> = emptyList(),
    ) : DtsType()

    data class StringLiteralType(
        val value: String = "",
    ) : DtsType()

    data class NumericLiteralType(
        val value: String = "",
    ) : DtsType()

    data class IndexSignatureType(
        val keyType: DtsType = PrimitiveType("string"),
        val valueType: DtsType = UnknownType(),
    ) : DtsType()

    data class UnknownType(
        val text: String = "",
    ) : DtsType()

    // ── Gson setup ────────────────────────────────────────────────────

    /**
     * Deserializer for [DtsType] that dispatches on the `"kind"` field.
     */
    class DtsTypeDeserializer : JsonDeserializer<DtsType> {
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext,
        ): DtsType {
            val obj = json.asJsonObject
            return when (obj.get("kind")?.asString) {
                "primitive" -> context.deserialize(json, PrimitiveType::class.java)
                "reference" -> context.deserialize(json, ReferenceType::class.java)
                "array" -> context.deserialize(json, ArrayType::class.java)
                "tuple" -> context.deserialize(json, TupleType::class.java)
                "function" -> context.deserialize(json, FunctionType::class.java)
                "union" -> context.deserialize(json, UnionType::class.java)
                "intersection" -> context.deserialize(json, IntersectionType::class.java)
                "objectLiteral" -> context.deserialize(json, ObjectLiteralType::class.java)
                "stringLiteral" -> context.deserialize(json, StringLiteralType::class.java)
                "numericLiteral" -> context.deserialize(json, NumericLiteralType::class.java)
                "indexSignature" -> context.deserialize(json, IndexSignatureType::class.java)
                else -> {
                    val text = obj.get("text")?.asString ?: ""
                    UnknownType(text)
                }
            }
        }
    }

    /**
     * Deserializer for [DtsDeclaration] that dispatches on the `"kind"` field.
     */
    class DtsDeclarationDeserializer : JsonDeserializer<DtsDeclaration> {
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext,
        ): DtsDeclaration {
            val obj = json.asJsonObject
            return when (obj.get("kind")?.asString) {
                "function" -> context.deserialize(json, FunctionDeclaration::class.java)
                "interface" -> context.deserialize(json, InterfaceDeclaration::class.java)
                "typeAlias" -> context.deserialize(json, TypeAliasDeclaration::class.java)
                "variable" -> context.deserialize(json, VariableDeclaration::class.java)
                "enum" -> context.deserialize(json, EnumDeclaration::class.java)
                "class" -> context.deserialize(json, ClassDeclaration::class.java)
                else -> VariableDeclaration(name = "unknown", type = UnknownType())
            }
        }
    }

    /**
     * Creates a Gson instance configured with custom type adapters for this model.
     */
    fun createGson(): Gson =
        GsonBuilder()
            .registerTypeAdapter(DtsType::class.java, DtsTypeDeserializer())
            .registerTypeAdapter(DtsDeclaration::class.java, DtsDeclarationDeserializer())
            .create()

    /**
     * Parses a JSON string into a [DtsFile].
     *
     * @param json the JSON string from dts-to-json.js
     * @return the parsed file model
     */
    fun parse(json: String): DtsFile {
        val gson = createGson()
        return gson.fromJson(json, object : TypeToken<DtsFile>() {}.type)
    }
}
