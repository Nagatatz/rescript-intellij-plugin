package com.rescript.plugin.navbar

import com.intellij.ide.navigationToolbar.AbstractNavBarModelExtension
import com.intellij.psi.PsiElement
import com.rescript.plugin.RescriptLanguage
import com.rescript.plugin.lang.psi.RescriptPsiUtils
import javax.swing.Icon

/**
 * Provides navigation bar support for ReScript files.
 *
 * Shows the hierarchy of enclosing declarations (let, type, module, etc.) in
 * the editor's navigation bar. Implements [AbstractNavBarModelExtension] (the
 * `com.intellij.navbar` extension point); 2026.2 removed the former
 * `StructureAwareNavBarModelExtension` convenience subclass, so the enclosing
 * declaration breadcrumb is rebuilt here with a PSI parent walk via
 * [RescriptPsiUtils.findEnclosingDeclaration] rather than the removed
 * Structure View machinery.
 *
 * Each method guards on [RescriptLanguage] because the navbar extension point
 * is consulted for elements of every language; non-ReScript elements are left
 * to the platform's default navbar extension.
 *
 * @see RescriptPsiUtils for name extraction and icon resolution
 */
class RescriptStructureAwareNavbar : AbstractNavBarModelExtension() {
    override fun getPresentableText(obj: Any?): String? {
        val element = obj as? PsiElement ?: return null
        if (element.language != RescriptLanguage) return null
        if (element.node?.elementType !in RescriptPsiUtils.NAVIGABLE_TYPES) return null
        return RescriptPsiUtils.extractName(element)
    }

    override fun getIcon(obj: Any?): Icon? {
        val element = obj as? PsiElement ?: return null
        if (element.language != RescriptLanguage) return null
        return RescriptPsiUtils.getIcon(element)
    }

    /**
     * Returns the nearest enclosing ReScript declaration so the navbar can
     * build the breadcrumb chain. Falls back to the platform's default parent
     * resolution for non-ReScript elements and for top-level declarations that
     * have no enclosing declaration.
     */
    override fun getParent(psiElement: PsiElement): PsiElement? {
        if (psiElement.language != RescriptLanguage) return super.getParent(psiElement)
        return RescriptPsiUtils.findEnclosingDeclaration(
            psiElement,
            RescriptPsiUtils.NAVIGABLE_TYPES,
            includeElement = false,
        ) ?: super.getParent(psiElement)
    }
}
