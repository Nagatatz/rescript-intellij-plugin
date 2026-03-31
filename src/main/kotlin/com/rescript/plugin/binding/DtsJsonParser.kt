package com.rescript.plugin.binding

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

/**
 * Handles Gson deserialization of the JSON intermediate representation from dts-to-json.js.
 *
 * Contains custom type adapters for the sealed class hierarchies in [DtsJsonModel]
 * and provides [parse] to convert a JSON string into a [DtsJsonModel.DtsFile].
 *
 * @see DtsJsonModel for the data model
 * @see DtsParserProcess for invoking the Node.js parser
 */
object DtsJsonParser {
    /**
     * Deserializer for [DtsJsonModel.DtsType] that dispatches on the `"kind"` field.
     */
    private class DtsTypeDeserializer : JsonDeserializer<DtsJsonModel.DtsType> {
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext,
        ): DtsJsonModel.DtsType {
            val obj = json.asJsonObject
            return when (obj.get("kind")?.asString) {
                "primitive" -> {
                    context.deserialize(json, DtsJsonModel.PrimitiveType::class.java)
                }

                "reference" -> {
                    context.deserialize(json, DtsJsonModel.ReferenceType::class.java)
                }

                "array" -> {
                    context.deserialize(json, DtsJsonModel.ArrayType::class.java)
                }

                "tuple" -> {
                    context.deserialize(json, DtsJsonModel.TupleType::class.java)
                }

                "function" -> {
                    context.deserialize(json, DtsJsonModel.FunctionType::class.java)
                }

                "union" -> {
                    context.deserialize(json, DtsJsonModel.UnionType::class.java)
                }

                "intersection" -> {
                    context.deserialize(json, DtsJsonModel.IntersectionType::class.java)
                }

                "objectLiteral" -> {
                    context.deserialize(json, DtsJsonModel.ObjectLiteralType::class.java)
                }

                "stringLiteral" -> {
                    context.deserialize(json, DtsJsonModel.StringLiteralType::class.java)
                }

                "numericLiteral" -> {
                    context.deserialize(json, DtsJsonModel.NumericLiteralType::class.java)
                }

                "indexSignature" -> {
                    context.deserialize(json, DtsJsonModel.IndexSignatureType::class.java)
                }

                else -> {
                    val text = obj.get("text")?.asString ?: ""
                    DtsJsonModel.UnknownType(text)
                }
            }
        }
    }

    /**
     * Deserializer for [DtsJsonModel.DtsDeclaration] that dispatches on the `"kind"` field.
     */
    private class DtsDeclarationDeserializer : JsonDeserializer<DtsJsonModel.DtsDeclaration> {
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext,
        ): DtsJsonModel.DtsDeclaration {
            val obj = json.asJsonObject
            return when (obj.get("kind")?.asString) {
                "function" -> context.deserialize(json, DtsJsonModel.FunctionDeclaration::class.java)
                "interface" -> context.deserialize(json, DtsJsonModel.InterfaceDeclaration::class.java)
                "typeAlias" -> context.deserialize(json, DtsJsonModel.TypeAliasDeclaration::class.java)
                "variable" -> context.deserialize(json, DtsJsonModel.VariableDeclaration::class.java)
                "enum" -> context.deserialize(json, DtsJsonModel.EnumDeclaration::class.java)
                "class" -> context.deserialize(json, DtsJsonModel.ClassDeclaration::class.java)
                else -> DtsJsonModel.VariableDeclaration(name = "unknown", type = DtsJsonModel.UnknownType())
            }
        }
    }

    /**
     * Creates a Gson instance configured with custom type adapters for the DTS model.
     */
    fun createGson(): Gson =
        GsonBuilder()
            .registerTypeAdapter(DtsJsonModel.DtsType::class.java, DtsTypeDeserializer())
            .registerTypeAdapter(DtsJsonModel.DtsDeclaration::class.java, DtsDeclarationDeserializer())
            .create()

    /**
     * Parses a JSON string into a [DtsJsonModel.DtsFile].
     *
     * @param json the JSON string from dts-to-json.js
     * @return the parsed file model
     */
    fun parse(json: String): DtsJsonModel.DtsFile {
        val gson = createGson()
        return gson.fromJson(json, object : TypeToken<DtsJsonModel.DtsFile>() {}.type)
    }
}
