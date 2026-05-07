package com.rescript.plugin.flow

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

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
 */
class RescriptVariantFlowToolWindowFactory :
    ToolWindowFactory,
    DumbAware {
    override fun createToolWindowContent(
        project: Project,
        toolWindow: ToolWindow,
    ) {
        val panel = RescriptVariantFlowPanel(project)
        val content = ContentFactory.getInstance().createContent(panel, "", false)
        Disposer.register(content, panel)
        toolWindow.contentManager.addContent(content)
    }
}
