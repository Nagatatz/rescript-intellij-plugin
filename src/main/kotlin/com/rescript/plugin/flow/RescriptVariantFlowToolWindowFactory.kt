package com.rescript.plugin.flow

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.rescript.plugin.util.RescriptToolWindowContent

/**
 * Factory that creates the ReScript Switch Flow tool window content.
 *
 * Registered as `com.intellij.toolWindow` in `plugin.xml` under the id
 * `"ReScript Switch Flow"`. Each project gets its own panel; the panel
 * is registered as a [com.intellij.openapi.Disposable] child of the
 * tool window content so it tears down with the project.
 *
 * @see ToolWindowFactory
 * @see RescriptVariantFlowPanel
 * @see RescriptToolWindowContent for the shared install helper
 */
class RescriptVariantFlowToolWindowFactory :
    ToolWindowFactory,
    DumbAware {
    override fun createToolWindowContent(
        project: Project,
        toolWindow: ToolWindow,
    ) {
        val panel = RescriptVariantFlowPanel(project)
        RescriptToolWindowContent.install(toolWindow, panel, panel)
    }
}
