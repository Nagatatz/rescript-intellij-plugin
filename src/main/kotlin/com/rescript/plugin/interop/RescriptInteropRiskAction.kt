package com.rescript.plugin.interop

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.wm.ToolWindowManager

/**
 * Opens the ReScript Interop Risk Map tool window from the Tools
 * menu, mirroring the entry points used by the existing dependency,
 * variant flow, and type impact tool windows.
 *
 * @see RescriptInteropRiskToolWindowFactory which provides the tool window content
 */
class RescriptInteropRiskAction :
    AnAction(
        "Show JS Interop Risk Map",
        "Open the ReScript JS interop risk map tool window",
        AllIcons.General.Warning,
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
        const val TOOL_WINDOW_ID: String = "ReScript Interop Risk"
    }
}
