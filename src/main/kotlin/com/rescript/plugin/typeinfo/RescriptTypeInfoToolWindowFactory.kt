package com.rescript.plugin.typeinfo

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory

/**
 * Factory for the "ReScript Type" tool window that displays the inferred type
 * of the expression at the current caret position.
 *
 * Implements [ToolWindowFactory] to register the tool window with the IDE.
 *
 * @see RescriptTypeInfoPanel for the panel implementation
 */
class RescriptTypeInfoToolWindowFactory :
    ToolWindowFactory,
    DumbAware {
    companion object {
        @Suppress("unused") // Public API constant for programmatic tool window access
        const val TOOL_WINDOW_ID = "ReScript Type"
    }

    override fun createToolWindowContent(
        project: Project,
        toolWindow: ToolWindow,
    ) {
        val panel = RescriptTypeInfoPanel(project, toolWindow.disposable)
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
