package com.rescript.plugin.flow

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.wm.ToolWindowManager

/**
 * Opens the ReScript Switch Flow tool window from the Tools menu.
 *
 * Registered in `plugin.xml` as a child of `ToolsMenu` so users can
 * reach the variant flow diagram from the standard menu in addition
 * to the side tool window strip.
 *
 * @see RescriptVariantFlowToolWindowFactory which provides the tool window content
 */
class RescriptVariantFlowAction :
    AnAction(
        "Show Switch Flow Diagram",
        "Open the ReScript variant flow diagram tool window",
        AllIcons.FileTypes.Diagram,
    ),
    DumbAware {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.project != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow(TOOL_WINDOW_ID) ?: return
        toolWindow.activate(null)
    }

    companion object {
        /** Id used to register the tool window in `plugin.xml`. */
        const val TOOL_WINDOW_ID: String = "ReScript Switch Flow"
    }
}
