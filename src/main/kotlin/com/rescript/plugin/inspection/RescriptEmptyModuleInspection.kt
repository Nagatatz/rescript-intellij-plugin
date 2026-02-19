package com.rescript.plugin.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.rescript.plugin.lang.RescriptTokenTypes
import com.rescript.plugin.lang.psi.RescriptElementTypes
import com.rescript.plugin.lang.psi.RescriptFile

/**
 * Local inspection that flags empty module declarations (modules with braces but no declarations).
 *
 * Skips module aliases (`module X = Y`). Checks nested modules recursively.
 * Provides a quick-fix to remove the empty module and any preceding annotation.
 */
class RescriptEmptyModuleInspection : LocalInspectionTool() {
    companion object {
        private val DECLARATION_TYPES =
            setOf(
                RescriptElementTypes.LET_DECLARATION,
                RescriptElementTypes.TYPE_DECLARATION,
                RescriptElementTypes.MODULE_DECLARATION,
                RescriptElementTypes.EXTERNAL_DECLARATION,
                RescriptElementTypes.OPEN_STATEMENT,
                RescriptElementTypes.INCLUDE_STATEMENT,
                RescriptElementTypes.EXCEPTION_DECLARATION,
            )
    }

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
    ): PsiElementVisitor =
        object : PsiElementVisitor() {
            override fun visitFile(file: PsiFile) {
                if (file !is RescriptFile) return
                checkModules(file, holder)
            }
        }

    private fun checkModules(
        scope: PsiElement,
        holder: ProblemsHolder,
    ) {
        val modules = scope.children.filter { it.node?.elementType == RescriptElementTypes.MODULE_DECLARATION }

        for (module in modules) {
            val hasBrace = module.children.any { it.node?.elementType == RescriptTokenTypes.LBRACE }
            if (!hasBrace) continue // Module alias (module X = Y), skip

            val hasDeclaration = module.children.any { it.node?.elementType in DECLARATION_TYPES }
            if (!hasDeclaration) {
                holder.registerProblem(
                    module,
                    "Empty module declaration",
                    RemoveEmptyModuleQuickFix(),
                )
            }

            // Check nested modules recursively
            checkModules(module, holder)
        }
    }

    /** Quick fix that removes the empty module declaration and any preceding annotation. */
    private class RemoveEmptyModuleQuickFix : LocalQuickFix {
        override fun getFamilyName(): String = "Remove empty module"

        override fun applyFix(
            project: Project,
            descriptor: ProblemDescriptor,
        ) {
            val module = descriptor.psiElement ?: return

            // Also remove preceding annotation if present
            val prev = module.prevSibling
            if (prev != null && prev.node?.elementType == RescriptElementTypes.ANNOTATION) {
                prev.delete()
            }

            module.delete()
        }
    }
}
