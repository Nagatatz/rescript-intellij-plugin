package com.rescript.plugin.hierarchy

import com.intellij.ide.hierarchy.HierarchyNodeDescriptor
import com.intellij.ide.util.treeView.NodeDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer
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
    // Own smart pointer to the wrapped element. SmartElementDescriptor.getPsiElement()
    // was reclassified @ApiStatus.Internal in 2026.2, so we expose a non-internal
    // accessor instead of calling the inherited internal method.
    private val elementPointer: SmartPsiElementPointer<PsiElement> =
        SmartPointerManager.getInstance(project).createSmartPsiElementPointer(element)

    /**
     * Non-internal replacement for `SmartElementDescriptor.getPsiElement()`.
     *
     * Backed by a [SmartPsiElementPointer] so it survives PSI reparses and
     * returns null once the wrapped element is invalidated.
     */
    val rescriptElement: PsiElement?
        get() = elementPointer.element

    override fun update(): Boolean {
        val changed = super.update()
        val element = rescriptElement ?: return changed

        val name = RescriptPsiUtils.extractName(element)
        val icon = RescriptPsiUtils.getIcon(element)

        myHighlightedText.beginning.addText(name)
        if (icon != null) {
            this.icon = icon
        }

        return changed
    }
}
