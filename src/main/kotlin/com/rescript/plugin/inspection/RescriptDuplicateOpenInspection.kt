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
            val modulePath = extractModulePath(openStmt)
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

    private fun extractModulePath(openStmt: PsiElement): String {
        val tokens =
            buildList {
                var child = openStmt.firstChild
                var pastOpen = false
                while (child != null) {
                    val type = child.node?.elementType
                    if (type == RescriptTokenTypes.OPEN) {
                        pastOpen = true
                    } else if (pastOpen && (type == RescriptTokenTypes.UIDENT || type == RescriptTokenTypes.DOT)) {
                        add(child.text)
                    }
                    child = child.nextSibling
                }
            }
        return tokens.joinToString("")
    }

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
