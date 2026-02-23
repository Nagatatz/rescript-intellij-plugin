package com.rescript.plugin.hierarchy.call

import com.intellij.ide.hierarchy.HierarchyBrowser
import com.intellij.ide.hierarchy.HierarchyProvider
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.psi.PsiElement
import com.rescript.plugin.lang.psi.RescriptFile
import com.rescript.plugin.lang.psi.RescriptPsiUtils

/**
 * Entry point for the ReScript call hierarchy feature (`Ctrl+Alt+H`).
 *
 * Determines the hierarchy target by walking up from the caret position to find
 * the nearest LET_DECLARATION or EXTERNAL_DECLARATION. Implements the
 * [HierarchyProvider] extension point.
 *
 * @see RescriptCallHierarchyBrowser for the hierarchy UI
 * @see RescriptCallAnalyzer for call relationship analysis
 */
class RescriptCallHierarchyProvider : HierarchyProvider {
    override fun getTarget(dataContext: DataContext): PsiElement? {
        val file = CommonDataKeys.PSI_FILE.getData(dataContext) ?: return null
        if (file !is RescriptFile) return null

        val editor = CommonDataKeys.EDITOR.getData(dataContext) ?: return null
        val offset = editor.caretModel.offset
        val element = file.findElementAt(offset) ?: return null

        return RescriptPsiUtils.findEnclosingDeclaration(element, RescriptPsiUtils.BINDING_TYPES)
    }

    override fun createHierarchyBrowser(target: PsiElement): HierarchyBrowser = RescriptCallHierarchyBrowser(target)

    override fun browserActivated(hierarchyBrowser: HierarchyBrowser) {
        (hierarchyBrowser as? RescriptCallHierarchyBrowser)
            ?.changeView(RescriptCallHierarchyBrowser.CALLERS_TYPE)
    }
}
