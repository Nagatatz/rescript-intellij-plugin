package com.rescript.plugin.hierarchy.call

import com.intellij.ide.hierarchy.HierarchyBrowserBaseEx
import com.intellij.ide.hierarchy.HierarchyNodeDescriptor
import com.intellij.ide.hierarchy.HierarchyTreeStructure
import com.intellij.ide.util.treeView.NodeDescriptor
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.psi.PsiElement
import com.rescript.plugin.lang.psi.RescriptPsiUtils
import javax.swing.JPanel
import javax.swing.JTree

/**
 * Hierarchy browser UI for ReScript function call hierarchy, supporting two view types:
 * - **Callers**: shows functions that call the selected function
 * - **Callees**: shows functions that the selected function calls
 *
 * Extends [HierarchyBrowserBaseEx] to provide the standard IntelliJ hierarchy browser experience.
 *
 * @see RescriptCallerTreeStructure for the callers tree builder
 * @see RescriptCalleeTreeStructure for the callees tree builder
 */
class RescriptCallHierarchyBrowser(
    rootElement: PsiElement,
) : HierarchyBrowserBaseEx(rootElement.project, rootElement) {
    companion object {
        const val CALLERS_TYPE = "Callers"
        const val CALLEES_TYPE = "Callees"
    }

    override fun isApplicableElement(element: PsiElement): Boolean {
        val type = element.node?.elementType ?: return false
        return type in RescriptPsiUtils.BINDING_TYPES
    }

    override fun createTrees(trees: MutableMap<in String, in JTree>) {
        trees[CALLERS_TYPE] = createTree(false)
        trees[CALLEES_TYPE] = createTree(false)
    }

    @Suppress("RedundantNullableReturnType")
    override fun getContentDisplayName(
        typeName: String,
        element: PsiElement,
    ): String? = RescriptPsiUtils.extractName(element)

    override fun createHierarchyTreeStructure(
        typeName: String,
        psiElement: PsiElement,
    ): HierarchyTreeStructure? =
        when (typeName) {
            CALLERS_TYPE -> RescriptCallerTreeStructure(myProject, psiElement)
            CALLEES_TYPE -> RescriptCalleeTreeStructure(myProject, psiElement)
            else -> null
        }

    override fun getElementFromDescriptor(descriptor: HierarchyNodeDescriptor): PsiElement? =
        (descriptor as? RescriptCallHierarchyNodeDescriptor)?.rescriptElement

    override fun getPrevOccurenceActionNameImpl(): String = "Previous Function"

    override fun getNextOccurenceActionNameImpl(): String = "Next Function"

    override fun createLegendPanel(): JPanel? = null

    override fun getActionPlace(): String = "RescriptCallHierarchy"

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
