package com.rescript.plugin.generate

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.rescript.plugin.lang.psi.RescriptElementTypes

/**
 * Generate action that creates a `switch` expression with exhaustive pattern-match arms
 * from a variant type declaration at the caret.
 *
 * Parses the enclosing TYPE_DECLARATION using [RescriptTypeDeclarationParser],
 * generates a switch body with one arm per constructor, and inserts it after the type.
 *
 * @see RescriptGenerateActionUtil for shared editor context logic
 */
class RescriptGenerateSwitchAction :
    RescriptBaseGenerateAction(
        "Switch Arms",
        "Generate switch arms from variant type",
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

        if (result !is TypeShape.Variant) return

        val switchText = generateSwitchText(result.constructors)

        WriteCommandAction.runWriteCommandAction(e.project) {
            val insertOffset = typeDecl.textRange.endOffset
            editor.document.insertString(insertOffset, "\n\n$switchText")
            editor.caretModel.moveToOffset(insertOffset + 2)
        }
    }

    override fun update(e: AnActionEvent) {
        if (!RescriptGenerateActionUtil.isInsideDeclaration(e, RescriptElementTypes.TYPE_DECLARATION)) {
            e.presentation.isEnabled = false
            return
        }

        // Additionally check that the type is a variant (has constructors)
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
        e.presentation.isEnabled = result is TypeShape.Variant
    }

    companion object {
        fun generateSwitchText(constructors: List<VariantConstructor>): String =
            buildString {
                appendLine("switch value {")
                for (constructor in constructors) {
                    if (constructor.payload != null) {
                        appendLine("| ${constructor.name}(_) => todo")
                    } else {
                        appendLine("| ${constructor.name} => todo")
                    }
                }
                append("}")
            }
    }
}
