package com.rescript.plugin.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.rescript.plugin.imports.RescriptOpenRemovalProof
import com.rescript.plugin.lang.psi.RescriptElementTypes
import com.rescript.plugin.lang.psi.RescriptFile

/**
 * Reports adjacent opens only when they resolve to the same proven local module.
 *
 * Reopening a module after another open or declaration is preserved. Unknown module
 * identity is never inferred from matching spelling. The quick fix repeats the proof.
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
        for (openStmt in RescriptOpenRemovalProof.findRedundantOpens(scope)) {
            val range = RescriptOpenRemovalProof.removalRange(openStmt) ?: continue
            holder.registerProblem(
                openStmt,
                TextRange(
                    range.startOffset - openStmt.textRange.startOffset,
                    range.endOffset - openStmt.textRange.startOffset,
                ),
                "Redundant open statement with an unchanged local module target",
                RemoveDuplicateOpenQuickFix(),
            )
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
            val element = descriptor.psiElement ?: return
            if (!element.isValid) return
            val file = element.containingFile ?: return
            val document = file.viewProvider.document ?: return
            if (!PsiDocumentManager.getInstance(project).isCommitted(document) || document.text != file.text) return
            val scope = element.parent ?: return
            if (element !in RescriptOpenRemovalProof.findRedundantOpens(scope)) return
            val range = RescriptOpenRemovalProof.removalRange(element) ?: return
            document.deleteString(range.startOffset, range.endOffset)
        }
    }
}
