package com.rescript.plugin.intention

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.rescript.plugin.lang.psi.RescriptPsiUtils

/**
 * Intention action that appends `->ignore` to an expression to discard its result.
 *
 * Available when the caret is inside a `let` binding's right-hand side or on a
 * top-level expression whose result is unused. Skips expressions that already
 * end with `->ignore`.
 *
 * @see RescriptWrapWithIntention for a similar expression-wrapping intention
 */
class RescriptAddIgnoreIntention : RescriptBaseIntention() {
    override fun getText(): String = "Add ->ignore"

    override fun isAvailableInRescript(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ): Boolean {
        val declaration = findParentDeclaration(element) ?: return false

        // Check the declaration text doesn't already end with ->ignore
        val text = declaration.text
        return !text.trimEnd().endsWith("->ignore") && !text.trimEnd().endsWith("-> ignore")
    }

    override fun invoke(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ) {
        val declaration = findParentDeclaration(element) ?: return
        val document = editor?.document ?: return
        val endOffset = declaration.textRange.endOffset
        document.insertString(endOffset, "->ignore")
    }

    companion object {
        /**
         * Walks up the PSI tree to find the enclosing let or external declaration.
         *
         * @param element the starting PSI element
         * @return the enclosing declaration, or null if not inside one
         */
        fun findParentDeclaration(element: PsiElement): PsiElement? =
            RescriptPsiUtils.findEnclosingDeclaration(element, RescriptPsiUtils.BINDING_TYPES)

        /**
         * Checks whether the given text already ends with `->ignore`.
         *
         * @param text the declaration text to check
         * @return true if it already contains ->ignore at the end
         */
        fun alreadyHasIgnore(text: String): Boolean =
            text.trimEnd().endsWith("->ignore") || text.trimEnd().endsWith("-> ignore")
    }
}
