package com.rescript.plugin.generate

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.util.PsiTreeUtil
import com.rescript.plugin.lang.psi.RescriptElementTypes
import com.rescript.plugin.lang.psi.RescriptFile

/**
 * Generate action that creates a `switch` expression with exhaustive pattern-match arms
 * from a variant type declaration at the caret.
 *
 * Parses the enclosing TYPE_DECLARATION using [RescriptTypeDeclarationParser],
 * generates a switch body with one arm per constructor, and inserts it after the type.
 */
class RescriptGenerateSwitchAction : AnAction("Switch Arms", "Generate switch arms from variant type", null) {
    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return
        if (psiFile !is RescriptFile) return

        val offset = editor.caretModel.offset
        val element = psiFile.findElementAt(offset) ?: return

        // Find the enclosing TYPE_DECLARATION
        val typeDecl =
            PsiTreeUtil.findFirstParent(element) {
                it.node?.elementType == RescriptElementTypes.TYPE_DECLARATION
            } ?: return

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
        val psiFile = e.getData(CommonDataKeys.PSI_FILE)
        val editor = e.getData(CommonDataKeys.EDITOR)

        if (psiFile !is RescriptFile || editor == null) {
            e.presentation.isEnabledAndVisible = false
            return
        }

        val offset = editor.caretModel.offset
        val element = psiFile.findElementAt(offset)
        val typeDecl =
            element?.let { el ->
                PsiTreeUtil.findFirstParent(el) {
                    it.node?.elementType == RescriptElementTypes.TYPE_DECLARATION
                }
            }

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
