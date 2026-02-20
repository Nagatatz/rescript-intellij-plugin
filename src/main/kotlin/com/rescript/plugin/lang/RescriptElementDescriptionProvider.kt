package com.rescript.plugin.lang

import com.intellij.psi.ElementDescriptionLocation
import com.intellij.psi.ElementDescriptionProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.usageView.UsageViewLongNameLocation
import com.intellij.usageView.UsageViewShortNameLocation
import com.intellij.usageView.UsageViewTypeLocation
import com.rescript.plugin.lang.psi.RescriptElementTypes
import com.rescript.plugin.lang.psi.RescriptFile
import com.rescript.plugin.lang.psi.RescriptPsiUtils

/**
 * Provides human-readable descriptions of ReScript PSI elements for IDE UI surfaces.
 *
 * Used in refactoring dialogs, Find Usages result headers, and other places
 * where a user-friendly element description is needed. Returns type labels
 * (e.g., "function", "type", "module") and element names based on the PSI structure.
 *
 * @see RescriptPsiUtils for name extraction logic
 * @see RescriptFindUsagesProvider for related usage-search support
 */
class RescriptElementDescriptionProvider : ElementDescriptionProvider {
    override fun getElementDescription(
        element: PsiElement,
        location: ElementDescriptionLocation,
    ): String? {
        if (element is RescriptFile) {
            return when (location) {
                is UsageViewTypeLocation -> "file"
                else -> element.name
            }
        }

        val elementType = element.node?.elementType ?: return null
        if (elementType !in RescriptPsiUtils.NAVIGABLE_TYPES &&
            elementType != RescriptElementTypes.OPEN_STATEMENT &&
            elementType != RescriptElementTypes.INCLUDE_STATEMENT
        ) {
            return null
        }

        val name = RescriptPsiUtils.extractName(element)

        return when (location) {
            is UsageViewTypeLocation -> getTypeDescription(elementType)
            is UsageViewShortNameLocation -> name
            is UsageViewLongNameLocation -> "${getTypeDescription(elementType)} '$name'"
            else -> name
        }
    }

    /**
     * Maps an element type to a human-readable type label for display in UI.
     *
     * @param elementType the PSI element type to describe
     * @return a short label such as "function", "type", or "module"
     */
    private fun getTypeDescription(elementType: IElementType): String =
        when (elementType) {
            RescriptElementTypes.LET_DECLARATION -> "function"
            RescriptElementTypes.TYPE_DECLARATION -> "type"
            RescriptElementTypes.MODULE_DECLARATION -> "module"
            RescriptElementTypes.EXTERNAL_DECLARATION -> "external"
            RescriptElementTypes.EXCEPTION_DECLARATION -> "exception"
            RescriptElementTypes.OPEN_STATEMENT -> "open statement"
            RescriptElementTypes.INCLUDE_STATEMENT -> "include statement"
            else -> "element"
        }
}
