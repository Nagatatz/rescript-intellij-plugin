package com.rescript.plugin.impact

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.rescript.plugin.util.RescriptToolWindowContent

/**
 * Factory that creates the ReScript Type Impact tool window content.
 *
 * Registered as `com.intellij.toolWindow` in `plugin.xml` under the id
 * `"ReScript Type Impact"`. Each project gets its own panel; the panel
 * is registered as a [com.intellij.openapi.Disposable] child of the
 * tool window content so it tears down with the project.
 *
 * @see ToolWindowFactory
 * @see RescriptTypeImpactPanel
 * @see RescriptToolWindowContent for the shared install helper
 */
class RescriptTypeImpactToolWindowFactory :
    ToolWindowFactory,
    DumbAware {
    override fun createToolWindowContent(
        project: Project,
        toolWindow: ToolWindow,
    ) {
        val panel = RescriptTypeImpactPanel(project)
        RescriptToolWindowContent.install(toolWindow, panel, panel)
    }
}
