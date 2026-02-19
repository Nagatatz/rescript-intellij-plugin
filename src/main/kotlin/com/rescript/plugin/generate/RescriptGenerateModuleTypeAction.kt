package com.rescript.plugin.generate

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.rescript.plugin.lang.psi.RescriptElementTypes
import com.rescript.plugin.lang.psi.RescriptFile
import com.rescript.plugin.lang.psi.RescriptPsiUtils

/**
 * Generate action that creates a `module type` signature from the module declaration at the caret.
 *
 * Collects all `let`, `type`, `external`, and nested `module` declarations within the
 * target module and generates a type signature skeleton with placeholder types (`'a`).
 * The generated module type is inserted above the module declaration.
 */
class RescriptGenerateModuleTypeAction :
    AnAction("Module Type", "Generate module type signature from module", null) {
    @Suppress("DuplicatedCode")
    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return
        if (psiFile !is RescriptFile) return

        val offset = editor.caretModel.offset
        val element = psiFile.findElementAt(offset) ?: return

        // Find the enclosing MODULE_DECLARATION
        val moduleDecl =
            PsiTreeUtil.findFirstParent(element) {
                it.node?.elementType == RescriptElementTypes.MODULE_DECLARATION
            } ?: return

        val moduleName = RescriptPsiUtils.extractName(moduleDecl)
        val declarations = collectDeclarations(moduleDecl)
        val moduleTypeText = generateModuleTypeText(moduleName, declarations)

        WriteCommandAction.runWriteCommandAction(e.project) {
            val insertOffset = moduleDecl.textRange.startOffset
            editor.document.insertString(insertOffset, "$moduleTypeText\n\n")
        }
    }

    @Suppress("DuplicatedCode")
    override fun update(e: AnActionEvent) {
        val psiFile = e.getData(CommonDataKeys.PSI_FILE)
        val editor = e.getData(CommonDataKeys.EDITOR)

        if (psiFile !is RescriptFile || editor == null) {
            e.presentation.isEnabledAndVisible = false
            return
        }

        val offset = editor.caretModel.offset
        val element = psiFile.findElementAt(offset)
        val moduleDecl =
            element?.let { el ->
                PsiTreeUtil.findFirstParent(el) {
                    it.node?.elementType == RescriptElementTypes.MODULE_DECLARATION
                }
            }

        e.presentation.isEnabled = moduleDecl != null
    }

    companion object {
        data class Declaration(
            val kind: String,
            val name: String,
        )

        fun collectDeclarations(moduleDecl: PsiElement): List<Declaration> {
            val declarations = mutableListOf<Declaration>()

            for (child in moduleDecl.children) {
                val elementType = child.node?.elementType ?: continue
                val name = RescriptPsiUtils.extractName(child)

                when (elementType) {
                    RescriptElementTypes.LET_DECLARATION -> {
                        declarations.add(Declaration("let", name))
                    }
                    RescriptElementTypes.TYPE_DECLARATION -> {
                        declarations.add(Declaration("type", name))
                    }
                    RescriptElementTypes.EXTERNAL_DECLARATION -> {
                        declarations.add(Declaration("let", name))
                    }
                    RescriptElementTypes.MODULE_DECLARATION -> {
                        declarations.add(Declaration("module", name))
                    }
                }
            }

            return declarations
        }

        fun generateModuleTypeText(
            moduleName: String,
            declarations: List<Declaration>,
        ): String =
            buildString {
                appendLine("module type ${moduleName}Type = {")
                for (decl in declarations) {
                    when (decl.kind) {
                        "let" -> appendLine("  let ${decl.name}: 'a")
                        "type" -> appendLine("  type ${decl.name}")
                        "module" -> appendLine("  module ${decl.name}: {}")
                    }
                }
                append("}")
            }
    }
}
