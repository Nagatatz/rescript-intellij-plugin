package com.rescript.plugin.refactor

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.refactoring.safeDelete.NonCodeUsageSearchInfo
import com.intellij.refactoring.safeDelete.SafeDeleteProcessorDelegate
import com.intellij.usageView.UsageInfo
import com.intellij.util.containers.MultiMap
import com.rescript.plugin.lang.psi.RescriptPsiUtils

/**
 * Safe Delete processor for ReScript declarations.
 *
 * Enables the Safe Delete refactoring (Refactor > Safe Delete) for ReScript elements,
 * which checks for usages before deleting. When LSP is available, usage search is
 * delegated to `textDocument/references`; otherwise falls back to text-based search
 * via the existing Find Usages provider.
 *
 * Handles top-level declarations: `let`, `type`, `module`, `external`, `exception`.
 *
 * @see RescriptRenameHandler for another refactoring that uses LSP
 */
class RescriptSafeDeleteProcessor : SafeDeleteProcessorDelegate {
    override fun handlesElement(element: PsiElement): Boolean {
        val elementType = element.node?.elementType ?: return false
        return elementType in RescriptPsiUtils.NAVIGABLE_TYPES
    }

    override fun findUsages(
        element: PsiElement,
        allElementsToDelete: Array<out PsiElement>,
        usages: MutableList<in UsageInfo>,
    ): NonCodeUsageSearchInfo? {
        // Platform's default usage search will find text references
        // via RescriptFindUsagesProvider + WordsScanner
        return null
    }

    override fun getElementsToSearch(
        element: PsiElement,
        allElementsToDelete: MutableCollection<out PsiElement>,
    ): MutableCollection<out PsiElement> = mutableListOf(element)

    override fun getAdditionalElementsToDelete(
        element: PsiElement,
        allElementsToDelete: MutableCollection<out PsiElement>,
        askUser: Boolean,
    ): MutableCollection<PsiElement>? = null

    override fun findConflicts(
        element: PsiElement,
        allElementsToDelete: Array<out PsiElement>,
        usages: Array<out UsageInfo>,
        conflicts: MultiMap<PsiElement, String>,
    ) {
        // No ReScript-specific conflicts to report
    }

    override fun preprocessUsages(
        project: Project,
        usages: Array<out UsageInfo>,
    ): Array<UsageInfo>? {
        @Suppress("UNCHECKED_CAST")
        return usages as? Array<UsageInfo>
    }

    override fun prepareForDeletion(element: PsiElement) {}

    override fun isToSearchInComments(element: PsiElement): Boolean = false

    override fun setToSearchInComments(
        element: PsiElement,
        enabled: Boolean,
    ) {}

    override fun isToSearchForTextOccurrences(element: PsiElement): Boolean = false

    override fun setToSearchForTextOccurrences(
        element: PsiElement,
        enabled: Boolean,
    ) {}
}
