package com.rescript.plugin.navigation

import com.intellij.navigation.ChooseByNameContributorEx
import com.intellij.navigation.NavigationItem
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.util.Processor
import com.intellij.util.indexing.FindSymbolParameters
import com.intellij.util.indexing.IdFilter
import com.rescript.plugin.indexing.RescriptNameIndex
import com.rescript.plugin.lang.psi.RescriptDeclarationPsiElement

/**
 * Contributes ReScript symbols to the Go to Symbol dialog (Cmd+Option+O).
 *
 * Uses [RescriptNameIndex] for fast O(log n) stub-based symbol lookup,
 * replacing the previous approach of scanning all files and walking PSI trees.
 */
class RescriptSymbolContributor : ChooseByNameContributorEx {
    override fun processNames(
        processor: Processor<in String>,
        scope: GlobalSearchScope,
        filter: IdFilter?,
    ) {
        StubIndex.getInstance().processAllKeys(RescriptNameIndex.KEY, processor, scope, filter)
    }

    override fun processElementsWithName(
        name: String,
        processor: Processor<in NavigationItem>,
        parameters: FindSymbolParameters,
    ) {
        StubIndex.getInstance().processElements(
            RescriptNameIndex.KEY,
            name,
            parameters.project,
            parameters.searchScope,
            RescriptDeclarationPsiElement::class.java,
            processor,
        )
    }
}
