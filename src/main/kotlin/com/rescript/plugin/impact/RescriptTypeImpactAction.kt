package com.rescript.plugin.impact

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.wm.ToolWindowManager

/**
 * Opens the ReScript Type Impact tool window from the Tools menu.
 *
 * Registered in `plugin.xml` as a child of `ToolsMenu` so users can
 * reach the impact preview from the standard menu in addition to the
 * side tool window strip.
 *
 * @see RescriptTypeImpactToolWindowFactory which provides the tool window content
 */
class RescriptTypeImpactAction :
    AnAction(
        "Show Type Impact",
        "Open the ReScript type impact preview tool window",
        AllIcons.Actions.Find,
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
        const val TOOL_WINDOW_ID: String = "ReScript Type Impact"
    }
}
