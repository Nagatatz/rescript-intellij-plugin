package com.rescript.plugin.structure

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.NavigatablePsiElement
import com.rescript.plugin.lang.psi.RescriptPsiUtils

class RescriptStructureViewElement(
    private val element: NavigatablePsiElement,
) : StructureViewTreeElement,
    SortableTreeElement {
    override fun getValue(): Any = element

    override fun navigate(requestFocus: Boolean) = element.navigate(requestFocus)

    override fun canNavigate(): Boolean = element.canNavigate()

    override fun canNavigateToSource(): Boolean = element.canNavigateToSource()

    override fun getAlphaSortKey(): String = RescriptPsiUtils.extractName(element)

    override fun getPresentation(): ItemPresentation =
        PresentationData(RescriptPsiUtils.extractName(element), null, RescriptPsiUtils.getIcon(element), null)

    override fun getChildren(): Array<TreeElement> =
        element.children
            .filter { it.node?.elementType in RescriptPsiUtils.NAVIGABLE_TYPES }
            .mapNotNull { it as? NavigatablePsiElement }
            .map { RescriptStructureViewElement(it) }
            .toTypedArray()
}
