package com.rescript.plugin.intention

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.rescript.plugin.lang.RescriptTokenTypes
import com.rescript.plugin.lang.psi.RescriptPsiUtils

/**
 * Intention action that adds an underscore prefix to an identifier to suppress
 * unused variable warnings. Available on lowercase identifiers inside `let`
 * bindings that don't already start with `_`.
 *
 * In ReScript, prefixing a binding name with `_` signals to the compiler that
 * the value is intentionally unused.
 *
 * @see RescriptAddIgnoreIntention for discarding expression results
 */
class RescriptAddUnderscorePrefixIntention : RescriptBaseIntention() {
    override fun getText(): String = "Add _ prefix to suppress unused warning"

    override fun isAvailableInRescript(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ): Boolean {
        val tokenType = element.node?.elementType ?: return false

        // Must be a lowercase identifier
        if (tokenType != RescriptTokenTypes.LIDENT) return false

        // Must not already start with _
        if (element.text.startsWith("_")) return false

        // Must be inside a let or external declaration
        return hasParentDeclaration(element)
    }

    override fun invoke(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ) {
        val document = editor?.document ?: return
        val offset = element.textRange.startOffset
        document.insertString(offset, "_")
    }

    companion object {
        /**
         * Checks whether the element is inside a let or external declaration.
         *
         * @param element the PSI element to check
         * @return true if a parent declaration is found
         */
        fun hasParentDeclaration(element: PsiElement): Boolean =
            RescriptPsiUtils.isInsideDeclaration(element, RescriptPsiUtils.BINDING_TYPES)
    }
}
