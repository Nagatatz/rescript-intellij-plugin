package com.rescript.plugin.generate

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import com.rescript.plugin.lang.psi.RescriptFile

/**
 * Shared utility methods for ReScript Generate actions.
 *
 * Provides common logic for finding enclosing PSI declarations and
 * updating action presentation state, eliminating duplication between
 * [RescriptGenerateModuleTypeAction] and [RescriptGenerateSwitchAction].
 */
object RescriptGenerateActionUtil {
    /**
     * Finds the enclosing PSI element of the given [elementType] at the current caret position.
     *
     * @param e the action event providing editor and PSI context
     * @param elementType the target element type to search for in the PSI tree
     * @return the enclosing PSI element, or null if not found or context is invalid
     */
    fun findEnclosingDeclaration(
        e: AnActionEvent,
        elementType: IElementType,
    ): PsiElement? {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return null
        val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return null
        if (psiFile !is RescriptFile) return null

        val offset = editor.caretModel.offset
        val element = psiFile.findElementAt(offset) ?: return null

        return PsiTreeUtil.findFirstParent(element) {
            it.node?.elementType == elementType
        }
    }

    /**
     * Checks whether the caret is inside a declaration of the given [elementType]
     * and returns true if so. Returns false and disables the action otherwise.
     *
     * @param e the action event providing editor and PSI context
     * @param elementType the target element type to check for
     * @return true if the caret is inside a matching declaration
     */
    fun isInsideDeclaration(
        e: AnActionEvent,
        elementType: IElementType,
    ): Boolean {
        val psiFile = e.getData(CommonDataKeys.PSI_FILE)
        val editor = e.getData(CommonDataKeys.EDITOR)

        if (psiFile !is RescriptFile || editor == null) {
            e.presentation.isEnabledAndVisible = false
            return false
        }

        val offset = editor.caretModel.offset
        val element = psiFile.findElementAt(offset)
        val declaration =
            element?.let { el ->
                PsiTreeUtil.findFirstParent(el) {
                    it.node?.elementType == elementType
                }
            }

        return declaration != null
    }
}
