package com.rescript.plugin.hierarchy

import com.intellij.ide.hierarchy.HierarchyNodeDescriptor
import com.intellij.ide.util.treeView.NodeDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.rescript.plugin.lang.psi.RescriptPsiUtils

/**
 * Node descriptor for module hierarchy tree nodes.
 *
 * Renders each node with the module name and icon extracted via [RescriptPsiUtils].
 */
class RescriptModuleHierarchyNodeDescriptor(
    project: Project,
    parentDescriptor: NodeDescriptor<*>?,
    element: PsiElement,
    isBase: Boolean,
) : HierarchyNodeDescriptor(project, parentDescriptor, element, isBase) {
    override fun update(): Boolean {
        val changed = super.update()
        val element = psiElement ?: return changed

        val name = RescriptPsiUtils.extractName(element)
        val icon = RescriptPsiUtils.getIcon(element)

        myHighlightedText.beginning.addText(name)
        if (icon != null) {
            this.icon = icon
        }

        return changed
    }
}
