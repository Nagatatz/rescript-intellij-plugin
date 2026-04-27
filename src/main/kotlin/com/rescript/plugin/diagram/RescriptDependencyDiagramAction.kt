package com.rescript.plugin.diagram

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.wm.ToolWindowManager

/**
 * Opens the ReScript Module Diagram tool window from the Analyze menu.
 *
 * Registered in `plugin.xml` as a child of `AnalyzeMenu` so users can reach
 * the dependency diagram from the standard analysis menu in addition to
 * the side tool window strip.
 *
 * @see RescriptDependencyDiagramToolWindowFactory which provides the tool window content
 */
class RescriptDependencyDiagramAction :
    AnAction(
        "Show ReScript Module Diagram",
        "Open the ReScript module dependency diagram tool window",
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
        const val TOOL_WINDOW_ID: String = "ReScript Module Diagram"
    }
}
