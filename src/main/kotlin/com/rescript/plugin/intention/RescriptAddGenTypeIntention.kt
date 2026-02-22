package com.rescript.plugin.intention

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.rescript.plugin.lang.psi.RescriptElementTypes
import com.rescript.plugin.lang.psi.RescriptFile
import com.rescript.plugin.lang.psi.RescriptPsiUtils

/**
 * Intention action that adds a @genType annotation to the current declaration.
 * Available on let, type, and module declarations that don't already have @genType.
 */
class RescriptAddGenTypeIntention : PsiElementBaseIntentionAction() {
    override fun getText(): String = "Add @genType annotation"

    override fun getFamilyName(): String = "Add @genType annotation"

    override fun isAvailable(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ): Boolean {
        if (element.containingFile !is RescriptFile) return false
        val declaration = findParentDeclaration(element) ?: return false
        return !hasGenTypeAnnotation(declaration)
    }

    override fun invoke(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ) {
        val declaration = findParentDeclaration(element) ?: return
        val document = editor?.document ?: return
        val offset = declaration.textRange.startOffset
        val lineNumber = document.getLineNumber(offset)
        val lineStartOffset = document.getLineStartOffset(lineNumber)
        val indent =
            document.getText(
                com.intellij.openapi.util
                    .TextRange(lineStartOffset, offset),
            )
        document.insertString(offset, "@genType\n$indent")
    }

    companion object {
        /**
         * Walks up the PSI tree to find the enclosing annotatable declaration.
         *
         * @param element the starting PSI element
         * @return the enclosing declaration, or null if not inside one
         */
        fun findParentDeclaration(element: PsiElement): PsiElement? =
            RescriptPsiUtils.findEnclosingDeclaration(element, RescriptPsiUtils.ANNOTATION_ELIGIBLE_TYPES)

        fun hasGenTypeAnnotation(declaration: PsiElement): Boolean {
            var prev = declaration.prevSibling
            while (prev != null) {
                if (prev.node?.elementType == RescriptElementTypes.ANNOTATION) {
                    val text = prev.text
                    if (text == "@genType" || text.startsWith("@genType(")) return true
                }
                // Skip whitespace/EOL between annotation and declaration
                if (prev.text.isNotBlank() && prev.node?.elementType != RescriptElementTypes.ANNOTATION) break
                prev = prev.prevSibling
            }
            return false
        }
    }
}
