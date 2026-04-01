package com.rescript.plugin.intention

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.rescript.plugin.lang.psi.RescriptFile

/**
 * Abstract base class for ReScript intention actions that eliminates common boilerplate.
 *
 * Provides a default [getFamilyName] implementation delegating to [getText],
 * and wraps the [isAvailable] check with an automatic [RescriptFile] guard so that
 * subclasses only need to implement [isAvailableInRescript].
 *
 * @see PsiElementBaseIntentionAction
 */
abstract class RescriptBaseIntention : PsiElementBaseIntentionAction() {
    override fun getFamilyName(): String = text

    override fun isAvailable(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ): Boolean {
        if (element.containingFile !is RescriptFile) return false
        return isAvailableInRescript(project, editor, element)
    }

    /**
     * Checks whether this intention is available at the given PSI element position.
     *
     * Called only when the element is inside a [RescriptFile], so subclasses
     * do not need to check the file type.
     *
     * @param project the current project
     * @param editor the active editor, or null
     * @param element the PSI element at the caret position
     * @return true if the intention should be offered to the user
     */
    protected abstract fun isAvailableInRescript(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ): Boolean
}
