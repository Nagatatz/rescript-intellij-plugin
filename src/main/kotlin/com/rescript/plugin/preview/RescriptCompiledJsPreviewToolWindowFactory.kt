package com.rescript.plugin.preview

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory

class RescriptCompiledJsPreviewToolWindowFactory :
    ToolWindowFactory,
    DumbAware {
    companion object {
        const val TOOL_WINDOW_ID = "ReScript JS"
    }

    override fun createToolWindowContent(
        project: Project,
        toolWindow: ToolWindow,
    ) {
        val panel = RescriptCompiledJsPreviewPanel(project, toolWindow.disposable)
        val content =
            toolWindow.contentManager.factory.createContent(
                panel.component,
                null,
                false,
            )
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project): Boolean {
        // Show the tool window when there are ReScript files
        return true
    }
}
