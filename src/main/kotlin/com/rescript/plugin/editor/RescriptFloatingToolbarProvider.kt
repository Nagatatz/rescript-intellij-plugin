package com.rescript.plugin.editor

import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.editor.toolbar.floating.FloatingToolbarProvider
import com.rescript.plugin.RescriptLanguage

/**
 * Provides a floating toolbar in the editor for ReScript files with quick access
 * to common actions: Format, Open Compiled JS, and Create Interface.
 *
 * The toolbar appears when editing ReScript files and auto-hides when not in use.
 * Implements [FloatingToolbarProvider].
 */
class RescriptFloatingToolbarProvider : FloatingToolbarProvider {
    companion object {
        /**
         * Builds the action group containing ReScript-specific toolbar actions.
         *
         * @return a group with Format, Open Compiled JS, and Create Interface actions
         */
        internal fun buildActionGroup(): DefaultActionGroup {
            val group = DefaultActionGroup()
            val am = ActionManager.getInstance()
            // Format code
            am.getAction("ReformatCode")?.let { group.add(it) }
            // Open compiled JavaScript
            am.getAction("ReScript.OpenCompiledJs")?.let { group.add(it) }
            // Create interface file
            am.getAction("ReScript.CreateInterface")?.let { group.add(it) }
            return group
        }
    }

    override val actionGroup: ActionGroup = buildActionGroup()

    // isApplicable() is deprecated in IntelliJ Platform master (replacement
    // isApplicableAsync was added after 2026.1.1) but no replacement exists
    // in our compile target. Override is required for file-type filtering.
    @Suppress("DEPRECATION")
    override fun isApplicable(dataContext: DataContext): Boolean {
        val file = CommonDataKeys.PSI_FILE.getData(dataContext) ?: return false
        return file.language.isKindOf(RescriptLanguage)
    }
}
