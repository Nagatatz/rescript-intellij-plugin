package com.rescript.plugin.generate

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiElement
import com.rescript.plugin.lang.psi.RescriptElementTypes
import com.rescript.plugin.lang.psi.RescriptPsiUtils

/**
 * Generate action that creates a `module type` signature from the module declaration at the caret.
 *
 * Collects all `let`, `type`, `external`, and nested `module` declarations within the
 * target module and generates a type signature skeleton with placeholder types (`'a`).
 * The generated module type is inserted above the module declaration.
 *
 * @see RescriptGenerateActionUtil for shared editor context logic
 */
class RescriptGenerateModuleTypeAction :
    RescriptBaseGenerateAction("Module Type", "Generate module type signature from module") {
    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val moduleDecl =
            RescriptGenerateActionUtil.findEnclosingDeclaration(
                e,
                RescriptElementTypes.MODULE_DECLARATION,
            ) ?: return

        val moduleName = RescriptPsiUtils.extractName(moduleDecl)
        val declarations = collectDeclarations(moduleDecl)
        val moduleTypeText = generateModuleTypeText(moduleName, declarations)

        WriteCommandAction.runWriteCommandAction(e.project) {
            val insertOffset = moduleDecl.textRange.startOffset
            editor.document.insertString(insertOffset, "$moduleTypeText\n\n")
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled =
            RescriptGenerateActionUtil.isInsideDeclaration(
                e,
                RescriptElementTypes.MODULE_DECLARATION,
            )
    }

    companion object {
        /** Represents a single declaration (let, type, module) within a module body. */
        data class Declaration(
            val kind: String,
            val name: String,
        )

        /**
         * Collects let, type, external, and module declarations from a module body.
         *
         * @param moduleDecl the MODULE_DECLARATION PSI element to scan
         * @return list of declarations found within the module
         */
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

        /**
         * Generates a `module type` signature text from a module name and its declarations.
         *
         * @param moduleName the name of the module
         * @param declarations the declarations to include in the type signature
         * @return the generated module type text
         */
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
