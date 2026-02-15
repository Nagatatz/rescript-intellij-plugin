package com.rescript.plugin.breadcrumb

import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider
import com.rescript.plugin.RescriptLanguage
import com.rescript.plugin.lang.psi.RescriptPsiUtils
import javax.swing.Icon

class RescriptBreadcrumbsProvider : BreadcrumbsProvider {
    override fun getLanguages(): Array<Language> = arrayOf(RescriptLanguage)

    override fun acceptElement(element: PsiElement): Boolean =
        element.node?.elementType in RescriptPsiUtils.NAVIGABLE_TYPES

    override fun getElementInfo(element: PsiElement): String = RescriptPsiUtils.extractName(element)

    override fun getElementIcon(element: PsiElement): Icon? = RescriptPsiUtils.getIcon(element)

    override fun getElementTooltip(element: PsiElement): String? = RescriptPsiUtils.getElementDescription(element)
}
