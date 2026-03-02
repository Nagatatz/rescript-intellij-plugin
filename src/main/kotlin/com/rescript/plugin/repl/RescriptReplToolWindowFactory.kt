package com.rescript.plugin.repl

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory

/**
 * Factory for the "ReScript REPL" tool window that provides an interactive
 * execution environment for ReScript code.
 *
 * Implements [ToolWindowFactory] and [DumbAware] to register the tool window
 * that is available even during indexing.
 *
 * @see RescriptReplPanel for the UI panel implementation
 * @see RescriptReplExecutor for the code execution engine
 */
class RescriptReplToolWindowFactory :
    ToolWindowFactory,
    DumbAware {
    companion object {
        const val TOOL_WINDOW_ID = "ReScript REPL"
    }

    override fun createToolWindowContent(
        project: Project,
        toolWindow: ToolWindow,
    ) {
        val panel = RescriptReplPanel(project, toolWindow.disposable)
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
