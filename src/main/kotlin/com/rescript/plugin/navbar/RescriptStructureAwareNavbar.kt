package com.rescript.plugin.navbar

import com.intellij.ide.navigationToolbar.StructureAwareNavBarModelExtension
import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import com.rescript.plugin.RescriptLanguage
import com.rescript.plugin.lang.psi.RescriptPsiUtils
import javax.swing.Icon

/**
 * Provides navigation bar support for ReScript files.
 *
 * Shows the hierarchy of enclosing declarations (let, type, module, etc.)
 * in the editor's navigation bar, delegating to [RescriptPsiUtils] for
 * name extraction and icon resolution. Extends [StructureAwareNavBarModelExtension]
 * to automatically leverage the existing Structure View model for tree traversal.
 */
class RescriptStructureAwareNavbar : StructureAwareNavBarModelExtension() {
    override val language: Language = RescriptLanguage

    override fun getPresentableText(obj: Any?): String? {
        val element = obj as? PsiElement ?: return null
        if (element.node?.elementType !in RescriptPsiUtils.NAVIGABLE_TYPES) return null
        return RescriptPsiUtils.extractName(element)
    }

    override fun getIcon(obj: Any?): Icon? {
        val element = obj as? PsiElement ?: return null
        return RescriptPsiUtils.getIcon(element)
    }
}
