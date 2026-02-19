package com.rescript.plugin.hierarchy

import com.intellij.ide.hierarchy.HierarchyBrowser
import com.intellij.ide.hierarchy.HierarchyProvider
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.psi.PsiElement
import com.rescript.plugin.lang.psi.RescriptElementTypes
import com.rescript.plugin.lang.psi.RescriptFile

class RescriptModuleHierarchyProvider : HierarchyProvider {
    override fun getTarget(dataContext: DataContext): PsiElement? {
        val file = CommonDataKeys.PSI_FILE.getData(dataContext) ?: return null
        if (file !is RescriptFile) return null

        // Try to find the module declaration at the caret
        val editor = CommonDataKeys.EDITOR.getData(dataContext) ?: return file
        val offset = editor.caretModel.offset
        val element = file.findElementAt(offset) ?: return file

        // Walk up to find the nearest MODULE_DECLARATION
        var current: PsiElement? = element
        while (current != null && current !is RescriptFile) {
            if (current.node?.elementType == RescriptElementTypes.MODULE_DECLARATION) {
                return current
            }
            current = current.parent
        }

        return file
    }

    override fun createHierarchyBrowser(target: PsiElement): HierarchyBrowser = RescriptModuleHierarchyBrowser(target)

    override fun browserActivated(hierarchyBrowser: HierarchyBrowser) {
        (hierarchyBrowser as? RescriptModuleHierarchyBrowser)
            ?.changeView(RescriptModuleHierarchyBrowser.MODULE_NESTING_TYPE)
    }
}
