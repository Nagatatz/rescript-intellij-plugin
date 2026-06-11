package com.rescript.plugin.generate

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.rescript.plugin.lang.RescriptTypeDeclarationParser
import com.rescript.plugin.lang.TypeShape
import com.rescript.plugin.lang.psi.RescriptElementTypes

/**
 * Generate action that creates JSON encoder and decoder functions from a type declaration.
 *
 * Parses the enclosing TYPE_DECLARATION using [RescriptTypeDeclarationParser],
 * classifies field/payload types via [RescriptJsonTypeClassifier], generates
 * encoder/decoder code via [RescriptJsonCodeGenerator], and inserts the result
 * after the type declaration.
 *
 * Supports record types and variant types (both simple enums and tagged unions).
 * Available in the Generate menu (Cmd+N / Alt+Insert) when the caret is inside
 * a type declaration with at least one field or constructor.
 *
 * @see RescriptGenerateGroup
 * @see RescriptJsonCodeGenerator
 */
class RescriptGenerateJsonCodecAction :
    RescriptBaseGenerateAction(
        "JSON Encoder/Decoder",
        "Generate JSON encoder and decoder from type",
    ) {
    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val typeDecl =
            RescriptGenerateActionUtil.findEnclosingDeclaration(
                e,
                RescriptElementTypes.TYPE_DECLARATION,
            ) ?: return

        val declText = typeDecl.text
        val shape = RescriptTypeDeclarationParser.parse(declText)
        val typeName = RescriptTypeDeclarationParser.extractTypeName(declText) ?: "t"

        val generatedCode = RescriptJsonCodeGenerator.generateBoth(typeName, shape) ?: return

        WriteCommandAction.runWriteCommandAction(e.project) {
            val insertOffset = typeDecl.textRange.endOffset
            editor.document.insertString(insertOffset, "\n\n$generatedCode")
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

        val shape = RescriptTypeDeclarationParser.parse(typeDecl.text)
        e.presentation.isEnabled = isValidShape(shape)
    }

    companion object {
        /**
         * Checks if the type shape is valid for JSON codec generation.
         *
         * @param shape the parsed type shape
         * @return true if the shape is a record with fields or a variant with constructors
         */
        fun isValidShape(shape: TypeShape): Boolean =
            when (shape) {
                is TypeShape.Record -> shape.fields.isNotEmpty()
                is TypeShape.Variant -> shape.constructors.isNotEmpty()
                is TypeShape.Unknown -> false
            }
    }
}
