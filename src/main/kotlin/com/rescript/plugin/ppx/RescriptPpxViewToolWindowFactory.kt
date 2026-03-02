package com.rescript.plugin.ppx

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory

/**
 * Factory for the "ReScript PPX" tool window that displays PPX annotation
 * expansion information for the current file.
 *
 * Implements [ToolWindowFactory] and [DumbAware] to register the PPX view
 * tool window that remains available during indexing.
 *
 * @see RescriptPpxViewPanel for the panel implementation
 */
class RescriptPpxViewToolWindowFactory :
    ToolWindowFactory,
    DumbAware {
    companion object {
        const val TOOL_WINDOW_ID = "ReScript PPX"
    }

    override fun createToolWindowContent(
        project: Project,
        toolWindow: ToolWindow,
    ) {
        val panel = RescriptPpxViewPanel(project, toolWindow.disposable)
        val content =
            toolWindow.contentManager.factory.createContent(
                panel.component,
                null,
                false,
            )
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project): Boolean = true
}
