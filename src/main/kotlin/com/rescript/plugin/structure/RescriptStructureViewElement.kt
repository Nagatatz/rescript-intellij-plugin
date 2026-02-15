package com.rescript.plugin.structure

import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.rescript.plugin.RescriptIcons
import com.rescript.plugin.lang.RescriptTokenTypes
import com.rescript.plugin.lang.psi.RescriptElementTypes
import com.rescript.plugin.lang.psi.RescriptFile
import javax.swing.Icon

class RescriptStructureViewElement(
    private val element: NavigatablePsiElement,
) : StructureViewTreeElement,
    SortableTreeElement {
    override fun getValue(): Any = element

    override fun navigate(requestFocus: Boolean) = element.navigate(requestFocus)

    override fun canNavigate(): Boolean = element.canNavigate()

    override fun canNavigateToSource(): Boolean = element.canNavigateToSource()

    override fun getAlphaSortKey(): String = extractName(element)

    override fun getPresentation(): ItemPresentation =
        PresentationData(extractName(element), null, getIcon(element), null)

    override fun getChildren(): Array<TreeElement> =
        element.children
            .filter { it.node.elementType in NAVIGABLE_TYPES }
            .mapNotNull { it as? NavigatablePsiElement }
            .map { RescriptStructureViewElement(it) }
            .toTypedArray()

    companion object {
        private val NAVIGABLE_TYPES =
            setOf(
                RescriptElementTypes.LET_DECLARATION,
                RescriptElementTypes.TYPE_DECLARATION,
                RescriptElementTypes.MODULE_DECLARATION,
                RescriptElementTypes.EXTERNAL_DECLARATION,
                RescriptElementTypes.EXCEPTION_DECLARATION,
            )

        private val IDENTIFIER_TYPES =
            setOf<IElementType>(
                RescriptTokenTypes.LIDENT,
                RescriptTokenTypes.UIDENT,
                RescriptTokenTypes.UNDERSCORE,
            )

        private fun extractName(element: PsiElement): String {
            if (element is RescriptFile) return element.name

            val node = element.node ?: return "(unknown)"
            var child = node.firstChildNode
            var afterKeyword = false
            while (child != null) {
                if (RescriptTokenTypes.TOP_LEVEL_KEYWORDS.contains(child.elementType)) {
                    afterKeyword = true
                } else if (child.elementType == RescriptTokenTypes.REC) {
                    // skip 'rec' keyword
                } else if (afterKeyword && child.elementType in IDENTIFIER_TYPES) {
                    return child.text
                }
                child = child.treeNext
            }
            return "(anonymous)"
        }

        private fun getIcon(element: PsiElement): Icon? {
            if (element is RescriptFile) return RescriptIcons.FILE

            return when (element.node?.elementType) {
                RescriptElementTypes.LET_DECLARATION -> AllIcons.Nodes.Function
                RescriptElementTypes.TYPE_DECLARATION -> AllIcons.Nodes.Type
                RescriptElementTypes.MODULE_DECLARATION -> AllIcons.Nodes.Module
                RescriptElementTypes.EXTERNAL_DECLARATION -> AllIcons.Nodes.PluginJB
                RescriptElementTypes.EXCEPTION_DECLARATION -> AllIcons.Nodes.ExceptionClass
                else -> null
            }
        }
    }
}
