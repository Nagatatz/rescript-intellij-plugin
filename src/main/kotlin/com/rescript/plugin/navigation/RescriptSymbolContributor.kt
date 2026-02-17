package com.rescript.plugin.navigation

import com.intellij.navigation.ChooseByNameContributorEx
import com.intellij.navigation.NavigationItem
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Processor
import com.intellij.util.indexing.FindSymbolParameters
import com.intellij.util.indexing.IdFilter
import com.rescript.plugin.RescriptFileType
import com.rescript.plugin.RescriptInterfaceFileType
import com.rescript.plugin.lang.psi.RescriptElementTypes
import com.rescript.plugin.lang.psi.RescriptFile
import com.rescript.plugin.lang.psi.RescriptPsiUtils

class RescriptSymbolContributor : ChooseByNameContributorEx {
    override fun processNames(
        processor: Processor<in String>,
        scope: GlobalSearchScope,
        filter: IdFilter?,
    ) {
        val project = scope.project ?: return
        val psiManager = PsiManager.getInstance(project)

        for (fileType in listOf(RescriptFileType, RescriptInterfaceFileType)) {
            for (vf in FileTypeIndex.getFiles(fileType, scope)) {
                val psiFile = psiManager.findFile(vf) as? RescriptFile ?: continue
                collectNames(psiFile, processor)
            }
        }
    }

    override fun processElementsWithName(
        name: String,
        processor: Processor<in NavigationItem>,
        parameters: FindSymbolParameters,
    ) {
        val project = parameters.project
        val scope = parameters.searchScope
        val psiManager = PsiManager.getInstance(project)

        for (fileType in listOf(RescriptFileType, RescriptInterfaceFileType)) {
            for (vf in FileTypeIndex.getFiles(fileType, scope)) {
                val psiFile = psiManager.findFile(vf) as? RescriptFile ?: continue
                collectElements(psiFile, name, processor)
            }
        }
    }

    private fun collectNames(
        element: com.intellij.psi.PsiElement,
        processor: Processor<in String>,
    ) {
        for (child in element.children) {
            val type = child.node?.elementType ?: continue
            if (type in RescriptPsiUtils.NAVIGABLE_TYPES) {
                val symbolName = RescriptPsiUtils.extractName(child)
                if (symbolName != "(anonymous)" && symbolName != "(unknown)") {
                    processor.process(symbolName)
                }
                if (type == RescriptElementTypes.MODULE_DECLARATION) {
                    collectNames(child, processor)
                }
            }
        }
    }

    private fun collectElements(
        element: com.intellij.psi.PsiElement,
        name: String,
        processor: Processor<in NavigationItem>,
    ) {
        for (child in element.children) {
            val type = child.node?.elementType ?: continue
            if (type in RescriptPsiUtils.NAVIGABLE_TYPES) {
                if (RescriptPsiUtils.extractName(child) == name && child is NavigatablePsiElement) {
                    processor.process(child)
                }
                if (type == RescriptElementTypes.MODULE_DECLARATION) {
                    collectElements(child, name, processor)
                }
            }
        }
    }
}
