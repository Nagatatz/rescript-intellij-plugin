package com.rescript.plugin.coverage

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

/**
 * Factory that creates the ReScript Type Coverage tool window content.
 *
 * Registered as `com.intellij.toolWindow` in `plugin.xml` under the id
 * `"ReScript Type Coverage"`. Each project gets its own panel; the
 * panel is registered as a [com.intellij.openapi.Disposable] child of
 * the tool window content so it tears down with the project.
 *
 * @see ToolWindowFactory
 * @see RescriptTypeCoveragePanel
 */
class RescriptTypeCoverageToolWindowFactory :
    ToolWindowFactory,
    DumbAware {
    override fun createToolWindowContent(
        project: Project,
        toolWindow: ToolWindow,
    ) {
        val panel = RescriptTypeCoveragePanel(project)
        val content = ContentFactory.getInstance().createContent(panel, "", false)
        Disposer.register(content, panel)
        toolWindow.contentManager.addContent(content)
    }
}
