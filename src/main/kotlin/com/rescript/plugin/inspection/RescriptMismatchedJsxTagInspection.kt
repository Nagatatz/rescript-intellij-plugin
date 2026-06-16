package com.rescript.plugin.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.rescript.plugin.lang.psi.RescriptElementTypes
import com.rescript.plugin.lang.psi.RescriptJsxTagPairUtil

/**
 * Flags JSX elements whose closing tag name does not match the opening tag name.
 *
 * Reports a warning on the closing tag range of any `JSX_ELEMENT` where both tag
 * names are present and differ (for example `<div></span>`), helping catch typos
 * before the compiler runs. Elements with a missing closing name, fragments, and
 * self-closing elements are never flagged.
 *
 * Implements [LocalInspectionTool]; registered via `localInspection` in plugin.xml.
 *
 * @see RescriptJsxTagPairUtil
 */
class RescriptMismatchedJsxTagInspection : LocalInspectionTool() {
    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
    ): PsiElementVisitor =
        object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                super.visitElement(element)
                if (element.node?.elementType != RescriptElementTypes.JSX_ELEMENT) return

                val names = RescriptJsxTagPairUtil.extractTagNames(element)
                val open = names.openName ?: return
                val close = names.closeName ?: return
                val closeRange = names.closeRange ?: return
                if (open == close) return

                // Register the problem relative to the element's own text range.
                val relativeRange = closeRange.shiftLeft(element.textRange.startOffset)
                holder.registerProblem(
                    element,
                    relativeRange,
                    "Closing tag '$close' does not match opening tag '$open'",
                )
            }
        }
}
