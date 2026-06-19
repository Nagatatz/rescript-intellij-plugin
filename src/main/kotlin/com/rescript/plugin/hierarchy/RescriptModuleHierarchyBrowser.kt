package com.rescript.plugin.hierarchy

import com.intellij.ide.hierarchy.HierarchyBrowserBaseEx
import com.intellij.ide.hierarchy.HierarchyNodeDescriptor
import com.intellij.ide.hierarchy.HierarchyTreeStructure
import com.intellij.ide.util.treeView.NodeDescriptor
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.psi.PsiElement
import com.rescript.plugin.lang.psi.RescriptElementTypes
import com.rescript.plugin.lang.psi.RescriptFile
import com.rescript.plugin.lang.psi.RescriptPsiUtils
import javax.swing.JPanel
import javax.swing.JTree

/**
 * Hierarchy browser UI for ReScript modules, supporting two view types:
 * - **Module Nesting**: shows the nested module structure within a file
 * - **Module Dependencies**: shows modules referenced by `open`/`include` statements
 *
 * @see RescriptModuleHierarchyTreeStructure for the nesting tree builder
 * @see RescriptModuleDependencyTreeStructure for the dependency tree builder
 */
class RescriptModuleHierarchyBrowser(
    rootElement: PsiElement,
) : HierarchyBrowserBaseEx(rootElement.project, rootElement) {
    companion object {
        const val MODULE_NESTING_TYPE = "Module Nesting"
        const val MODULE_DEPENDENCIES_TYPE = "Module Dependencies"
    }

    override fun isApplicableElement(element: PsiElement): Boolean =
        element is RescriptFile ||
            element.node?.elementType == RescriptElementTypes.MODULE_DECLARATION

    override fun createTrees(trees: MutableMap<in String, in JTree>) {
        trees[MODULE_NESTING_TYPE] = createTree(false)
        trees[MODULE_DEPENDENCIES_TYPE] = createTree(false)
    }

    @Suppress("RedundantNullableReturnType")
    override fun getContentDisplayName(
        typeName: String,
        element: PsiElement,
    ): String? {
        if (element is RescriptFile) return element.name
        return RescriptPsiUtils.extractName(element)
    }

    override fun createHierarchyTreeStructure(
        typeName: String,
        psiElement: PsiElement,
    ): HierarchyTreeStructure? =
        when (typeName) {
            MODULE_NESTING_TYPE -> RescriptModuleHierarchyTreeStructure(myProject, psiElement)
            MODULE_DEPENDENCIES_TYPE -> RescriptModuleDependencyTreeStructure(myProject, psiElement)
            else -> null
        }

    override fun getElementFromDescriptor(descriptor: HierarchyNodeDescriptor): PsiElement? =
        (descriptor as? RescriptModuleHierarchyNodeDescriptor)?.rescriptElement

    override fun getPrevOccurenceActionNameImpl(): String = "Previous Module"

    override fun getNextOccurenceActionNameImpl(): String = "Next Module"

    override fun createLegendPanel(): JPanel? = null

    override fun getActionPlace(): String = "RescriptModuleHierarchy"

    @Suppress("RedundantNullableReturnType")
    override fun getComparator(): Comparator<NodeDescriptor<*>>? =
        Comparator { o1, o2 ->
            val n1 = o1.toString()
            val n2 = o2.toString()
            n1.compareTo(n2, ignoreCase = true)
        }

    override fun prependActions(actionGroup: DefaultActionGroup) {
        // No additional actions needed
    }
}
