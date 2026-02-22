package com.rescript.plugin.intention

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.rescript.plugin.lang.psi.RescriptFile
import com.rescript.plugin.lang.psi.RescriptPsiUtils

/**
 * Intention action that removes a declaration from the interface file.
 *
 * When invoked on a top-level declaration in a `.resi` file, deletes
 * the declaration from the interface, effectively making the corresponding
 * implementation in `.res` private (non-exported).
 *
 * @see RescriptAddToInterfaceIntention for the reverse operation
 */
class RescriptRemoveFromInterfaceIntention : PsiElementBaseIntentionAction() {
    override fun getText(): String = "Remove declaration from interface"

    override fun getFamilyName(): String = "Remove declaration from interface"

    override fun isAvailable(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ): Boolean {
        if (element.containingFile !is RescriptFile) return false
        val virtualFile = element.containingFile.virtualFile ?: return false
        if (virtualFile.extension != "resi") return false

        // Check that cursor is on a top-level declaration
        val declaration =
            RescriptPsiUtils.findEnclosingDeclaration(element, RescriptPsiUtils.NAVIGABLE_TYPES)
                ?: return false
        val name = RescriptPsiUtils.extractName(declaration)
        return name != "(anonymous)" && name != "(unknown)"
    }

    override fun invoke(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ) {
        val document = editor?.document ?: return
        val declaration =
            RescriptPsiUtils.findEnclosingDeclaration(element, RescriptPsiUtils.NAVIGABLE_TYPES)
                ?: return

        val startOffset = declaration.textRange.startOffset
        var endOffset = declaration.textRange.endOffset

        // Also remove trailing newline if present
        val text = document.text
        if (endOffset < text.length && text[endOffset] == '\n') {
            endOffset++
        }

        document.deleteString(startOffset, endOffset)
    }
}
