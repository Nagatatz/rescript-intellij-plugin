package com.rescript.plugin.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.rescript.plugin.lang.psi.RescriptFile

/**
 * Offers removal of local scalar ref boxes only when all uses are proven contents reads.
 * The [LocalInspectionTool] updates the initializer and reads together, refusing mutations,
 * escapes, shadowing and unsupported syntax through [RescriptRefRemovalPlanner].
 */
class RescriptMutabilityInspection : LocalInspectionTool() {
    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
    ): PsiElementVisitor =
        object : PsiElementVisitor() {
            override fun visitFile(file: PsiFile) {
                if (file !is RescriptFile) return
                for (plan in RescriptRefRemovalPlanner.plans(file.text)) {
                    val element = file.findElementAt(plan.bindingOffset) ?: continue
                    holder.registerProblem(
                        element,
                        "Local ref '${plan.name}' is only read — use a plain let binding",
                        RemoveRefQuickFix(plan, file),
                    )
                }
            }
        }

    /**
     * Applies a complete inspected source replacement as a single document operation.
     *
     * @param plan the immutable source proof and replacement
     * @param sourceFile the exact inspected file, preventing reuse against another file
     */
    private class RemoveRefQuickFix(
        private val plan: RescriptRefRemovalPlanner.Plan,
        private val sourceFile: PsiFile,
    ) : LocalQuickFix {
        override fun getFamilyName(): String = "Remove unnecessary ref"

        override fun getName(): String = "Remove unnecessary ref"

        override fun applyFix(
            project: Project,
            descriptor: ProblemDescriptor,
        ) {
            val file = descriptor.psiElement?.containingFile ?: return
            if (file !== sourceFile || !file.isValid) return
            val document = file.viewProvider.document ?: return
            val replacement = plan.replacementFor(document.text) ?: return
            document.replaceString(0, document.textLength, replacement)
        }
    }
}
