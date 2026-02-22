package com.rescript.plugin.generate

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.rescript.plugin.lang.psi.RescriptElementTypes

/**
 * Generate action that creates a `make` constructor function from a record type declaration.
 *
 * Parses the enclosing TYPE_DECLARATION using [RescriptTypeDeclarationParser],
 * generates a function with labeled parameters matching record fields,
 * and inserts it after the type declaration. Fields with `option<_>` type
 * receive optional parameter syntax (`=?`).
 *
 * @see RescriptGenerateGroup
 * @see RescriptTypeDeclarationParser
 */
class RescriptGenerateMakeAction : AnAction("Make Function", "Generate make function from record type", null) {
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
        val makeText = generateMakeFunction(typeName, result.fields)

        WriteCommandAction.runWriteCommandAction(e.project) {
            val insertOffset = typeDecl.textRange.endOffset
            editor.document.insertString(insertOffset, "\n\n$makeText")
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
         * Generates a `make` function from record fields.
         *
         * @param typeName the name of the record type
         * @param fields the list of record fields
         * @return the generated function text
         */
        fun generateMakeFunction(
            typeName: String,
            fields: List<RecordField>,
        ): String =
            buildString {
                val params =
                    fields.joinToString(", ") { field ->
                        val paramName = "~${field.name}"
                        if (isOptionalType(field.typeAnnotation)) {
                            "$paramName=?"
                        } else {
                            paramName
                        }
                    }

                val body =
                    fields.joinToString(", ") { field ->
                        "${field.name}: ${field.name}"
                    }

                // If type name is "t", omit return type annotation
                if (typeName == "t") {
                    append("let make = ($params) => {$body}")
                } else {
                    append("let make = ($params): $typeName => {$body}")
                }
            }

        /** Checks if the type is an `option<...>` type. */
        internal fun isOptionalType(typeAnnotation: String): Boolean {
            val trimmed = typeAnnotation.trim()
            return trimmed.startsWith("option<") || trimmed == "option"
        }
    }
}
