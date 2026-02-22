package com.rescript.plugin.navigation

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.rescript.plugin.RescriptFileType
import com.rescript.plugin.RescriptInterfaceFileType

/**
 * Action that navigates from a `.resi` interface declaration to the corresponding
 * `.res` implementation declaration, providing the "Go to Implementation" entry
 * in the Navigate menu.
 *
 * Delegates to [RescriptGotoSuperHandler] which already supports bidirectional
 * navigation between `.res` and `.resi` files. This action simply provides the
 * menu item and keyboard shortcut (Ctrl+Alt+B) entry point.
 *
 * @see RescriptGotoSuperHandler for the underlying navigation logic
 */
class RescriptGotoImplementationAction : AnAction() {
    private val handler = RescriptGotoSuperHandler()

    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val project = e.project ?: return
        val file = e.getData(CommonDataKeys.PSI_FILE) ?: return

        handler.invoke(project, editor, file)
    }

    override fun update(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.PSI_FILE)
        val virtualFile = file?.virtualFile

        // Enable only for ReScript files (.res and .resi)
        e.presentation.isEnabledAndVisible =
            virtualFile != null &&
            (virtualFile.fileType is RescriptFileType || virtualFile.fileType is RescriptInterfaceFileType)
    }

    override fun getActionUpdateThread() = com.intellij.openapi.actionSystem.ActionUpdateThread.BGT
}
