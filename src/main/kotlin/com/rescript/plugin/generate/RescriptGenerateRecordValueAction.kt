package com.rescript.plugin.generate

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.rescript.plugin.lang.RecordField
import com.rescript.plugin.lang.RescriptTypeDeclarationParser
import com.rescript.plugin.lang.TypeShape
import com.rescript.plugin.lang.psi.RescriptElementTypes

/**
 * Generate action that creates a record value with default field values
 * from a record type declaration.
 *
 * Parses the enclosing TYPE_DECLARATION using [RescriptTypeDeclarationParser],
 * generates a record literal with sensible default values for each field type,
 * and inserts it after the type declaration. Accessible via Cmd+N (Generate menu).
 *
 * @see RescriptGenerateGroup
 * @see RescriptTypeDeclarationParser
 */
class RescriptGenerateRecordValueAction :
    RescriptBaseGenerateAction(
        "Record Value",
        "Generate record value with default fields",
    ) {
    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val typeDecl =
            RescriptGenerateActionUtil.findEnclosingDeclaration(
                e,
                RescriptElementTypes.TYPE_DECLARATION,
            ) ?: return

        val declText = typeDecl.text
        val result = RescriptTypeDeclarationParser.parse(declText)

        if (result !is TypeShape.Record) return
        if (result.fields.isEmpty()) return

        val typeName = RescriptTypeDeclarationParser.extractTypeName(declText) ?: "t"
        val recordText = generateRecordValue(typeName, result.fields)

        WriteCommandAction.runWriteCommandAction(e.project) {
            val insertOffset = typeDecl.textRange.endOffset
            editor.document.insertString(insertOffset, "\n\n$recordText")
            editor.caretModel.moveToOffset(insertOffset + 2)
        }
    }

    override fun update(e: AnActionEvent) {
        if (!RescriptGenerateActionUtil.isInsideDeclaration(e, RescriptElementTypes.TYPE_DECLARATION)) {
            e.presentation.isEnabled = false
            return
        }

        val typeDecl =
            RescriptGenerateActionUtil.findEnclosingDeclaration(
                e,
                RescriptElementTypes.TYPE_DECLARATION,
            )
        if (typeDecl == null) {
            e.presentation.isEnabled = false
            return
        }

        val result = RescriptTypeDeclarationParser.parse(typeDecl.text)
        e.presentation.isEnabled = result is TypeShape.Record && result.fields.isNotEmpty()
    }

    companion object {
        /**
         * Generates a record value literal from record fields with default values.
         *
         * @param typeName the name of the record type
         * @param fields the list of record fields
         * @return the generated record value text
         */
        fun generateRecordValue(
            typeName: String,
            fields: List<RecordField>,
        ): String =
            buildString {
                val body =
                    fields.joinToString(",\n  ") { field ->
                        "${field.name}: ${defaultValueForType(field.typeAnnotation)}"
                    }

                // If type name is "t", omit type annotation
                if (typeName == "t") {
                    append("let value = {\n  $body,\n}")
                } else {
                    append("let value: $typeName = {\n  $body,\n}")
                }
            }

        /**
         * Returns a sensible default value for the given ReScript type annotation.
         *
         * @param typeAnnotation the type annotation string (e.g., "string", "option<int>")
         * @return the default value literal
         */
        internal fun defaultValueForType(typeAnnotation: String): String {
            val trimmed = typeAnnotation.trim()
            return when {
                trimmed == "string" -> "\"\""

                trimmed == "int" -> "0"

                trimmed == "float" -> "0.0"

                trimmed == "bool" -> "false"

                trimmed.startsWith("option") -> "None"

                trimmed.startsWith("array") -> "[]"

                trimmed.startsWith("list") -> "list{}"

                trimmed == "unit" -> "()"

                // Function types: (...) => ...
                trimmed.contains("=>") -> "_ => todo"

                else -> "todo"
            }
        }
    }
}
