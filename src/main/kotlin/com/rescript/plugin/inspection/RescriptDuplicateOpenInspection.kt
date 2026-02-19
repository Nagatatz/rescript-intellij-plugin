package com.rescript.plugin.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.rescript.plugin.imports.RescriptImportUtil
import com.rescript.plugin.lang.psi.RescriptElementTypes
import com.rescript.plugin.lang.psi.RescriptFile

/**
 * Local inspection that detects duplicate `open` statements within the same scope.
 *
 * Checks both file-level and nested module scopes recursively.
 * Provides a quick-fix to remove the duplicate `open` statement.
 */
class RescriptDuplicateOpenInspection : LocalInspectionTool() {
    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
    ): PsiElementVisitor =
        object : PsiElementVisitor() {
            override fun visitFile(file: PsiFile) {
                if (file !is RescriptFile) return
                checkScope(file, holder)
            }
        }

    private fun checkScope(
        scope: PsiElement,
        holder: ProblemsHolder,
    ) {
        val openStatements = scope.children.filter { it.node?.elementType == RescriptElementTypes.OPEN_STATEMENT }
        val seen = mutableSetOf<String>()

        for (openStmt in openStatements) {
            val modulePath = RescriptImportUtil.extractModulePath(openStmt)
            if (modulePath.isNotEmpty()) {
                if (!seen.add(modulePath)) {
                    holder.registerProblem(
                        openStmt,
                        "Duplicate open statement: '$modulePath'",
                        RemoveDuplicateOpenQuickFix(),
                    )
                }
            }
        }

        // Check nested module scopes recursively
        val modules = scope.children.filter { it.node?.elementType == RescriptElementTypes.MODULE_DECLARATION }
        for (module in modules) {
            checkScope(module, holder)
        }
    }

    /** Quick fix that removes the duplicate `open` statement from the source. */
    private class RemoveDuplicateOpenQuickFix : LocalQuickFix {
        override fun getFamilyName(): String = "Remove duplicate open"

        override fun applyFix(
            project: Project,
            descriptor: ProblemDescriptor,
        ) {
            descriptor.psiElement?.delete()
        }
    }
}
