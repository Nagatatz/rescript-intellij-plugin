package com.rescript.plugin.navigation

import com.intellij.ide.actions.searcheverywhere.FoundItemDescriptor
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributor
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributorFactory
import com.intellij.ide.actions.searcheverywhere.WeightedSearchEverywhereContributor
import com.intellij.ide.util.gotoByName.GotoFileCellRenderer
import com.intellij.navigation.NavigationItem
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Processor
import com.rescript.plugin.RescriptFileType
import com.rescript.plugin.RescriptInterfaceFileType
import com.rescript.plugin.lang.psi.RescriptFile
import com.rescript.plugin.lang.psi.RescriptPsiUtils
import javax.swing.ListCellRenderer

/**
 * Provides ReScript symbols in the Search Everywhere dialog (Shift+Shift).
 *
 * Surfaces top-level declarations (let, type, module, external, exception)
 * from all ReScript files in the project in the unified search UI.
 *
 * @see RescriptSymbolContributor
 * @see SearchEverywhereContributor
 */
class RescriptSearchEverywhereContributor(
    event: AnActionEvent,
) : WeightedSearchEverywhereContributor<Any> {
    private val myProject = event.getData(CommonDataKeys.PROJECT)

    override fun getSearchProviderId(): String = "RescriptSearchEverywhereContributor"

    override fun getGroupName(): String = "ReScript"

    override fun getSortWeight(): Int = 150

    override fun isShownInSeparateTab(): Boolean = false

    override fun showInFindResults(): Boolean = false

    override fun fetchWeightedElements(
        pattern: String,
        progressIndicator: ProgressIndicator,
        consumer: Processor<in FoundItemDescriptor<Any>>,
    ) {
        if (pattern.isBlank()) return

        val project = myProject ?: return
        val scope = GlobalSearchScope.projectScope(project)
        val psiManager = PsiManager.getInstance(project)
        val lowercasePattern = pattern.lowercase()

        ApplicationManager.getApplication().runReadAction {
            for (fileType in listOf(RescriptFileType, RescriptInterfaceFileType)) {
                for (vf in com.intellij.psi.search.FileTypeIndex
                    .getFiles(fileType, scope)) {
                    progressIndicator.checkCanceled()
                    val psiFile = psiManager.findFile(vf) as? RescriptFile ?: continue
                    collectMatchingElements(psiFile, lowercasePattern, consumer)
                }
            }
        }
    }

    override fun processSelectedItem(
        selected: Any,
        modifiers: Int,
        searchText: String,
    ): Boolean {
        if (selected is NavigationItem) {
            selected.navigate(true)
            return true
        }
        return false
    }

    override fun getElementsRenderer(): ListCellRenderer<in Any> = GotoFileCellRenderer(0)

    override fun getDataForItem(
        element: Any,
        dataId: String,
    ): Any? = null

    override fun dispose() {}

    private fun collectMatchingElements(
        file: RescriptFile,
        pattern: String,
        consumer: Processor<in FoundItemDescriptor<Any>>,
    ) {
        for (child in file.children) {
            val elementType = child.node?.elementType ?: continue
            if (elementType !in RescriptPsiUtils.NAVIGABLE_TYPES) continue

            if (child is NavigationItem) {
                val name = child.name?.lowercase() ?: continue
                if (name.contains(pattern)) {
                    val weight = if (name.startsWith(pattern)) 2 else 1
                    consumer.process(FoundItemDescriptor(child, weight))
                }
            }

            // Check nested elements (e.g., declarations inside modules)
            collectMatchingChildElements(child, pattern, consumer)
        }
    }

    private fun collectMatchingChildElements(
        parent: com.intellij.psi.PsiElement,
        pattern: String,
        consumer: Processor<in FoundItemDescriptor<Any>>,
    ) {
        for (child in parent.children) {
            val elementType = child.node?.elementType ?: continue
            if (elementType in RescriptPsiUtils.NAVIGABLE_TYPES && child is NavigationItem) {
                val name = child.name?.lowercase() ?: continue
                if (name.contains(pattern)) {
                    val weight = if (name.startsWith(pattern)) 2 else 1
                    consumer.process(FoundItemDescriptor(child, weight))
                }
            }
            collectMatchingChildElements(child, pattern, consumer)
        }
    }

    companion object {
        /** Pattern for splitting identifiers at camelCase boundaries (before uppercase letters). */
        private val CAMEL_CASE_SPLIT_PATTERN = Regex("(?=[A-Z])")

        /**
         * Checks if a symbol name matches a search pattern.
         *
         * Supports exact match, prefix match, case-insensitive substring match,
         * and camelCase hump matching (e.g., "mLFN" matches "myLongFunctionName").
         *
         * @param name the symbol name to test
         * @param pattern the search pattern
         * @return true if the name matches the pattern
         */
        internal fun matchesPattern(
            name: String,
            pattern: String,
        ): Boolean {
            if (pattern.isEmpty()) return true
            val lowerName = name.lowercase()
            val lowerPattern = pattern.lowercase()

            // Exact or substring match (case-insensitive)
            if (lowerName.contains(lowerPattern)) return true

            // CamelCase hump matching: extract uppercase letters from name and compare
            val humps =
                buildString {
                    for (ch in name) {
                        if (ch.isUpperCase()) append(ch)
                    }
                }
            if (humps.isNotEmpty() && humps.lowercase().contains(lowerPattern.lowercase())) return true

            // CamelCase hump matching: match initials of camelCase segments
            val segments = name.split(CAMEL_CASE_SPLIT_PATTERN).filter { it.isNotEmpty() }
            if (segments.size >= pattern.length) {
                val initials = segments.joinToString("") { it.take(1) }
                if (initials.lowercase().startsWith(lowerPattern)) return true
            }

            return false
        }
    }

    /**
     * Factory for creating [RescriptSearchEverywhereContributor] instances.
     */
    class Factory : SearchEverywhereContributorFactory<Any> {
        override fun createContributor(initEvent: AnActionEvent): SearchEverywhereContributor<Any> =
            RescriptSearchEverywhereContributor(initEvent)
    }
}
