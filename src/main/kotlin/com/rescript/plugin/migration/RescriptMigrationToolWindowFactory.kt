package com.rescript.plugin.migration

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.rescript.plugin.util.RescriptToolWindowContent

/**
 * Factory for the ReScript Migration Pilot tool window.
 *
 * Registered as `com.intellij.toolWindow` in `plugin.xml` under the
 * id `"ReScript Migration Pilot"`. Each project gets its own panel;
 * the panel registers itself as a child disposable of the tool
 * window content so it tears down with the project.
 *
 * @see ToolWindowFactory
 * @see RescriptMigrationPanel
 * @see RescriptToolWindowContent for the shared install helper
 */
class RescriptMigrationToolWindowFactory :
    ToolWindowFactory,
    DumbAware {
    override fun createToolWindowContent(
        project: Project,
        toolWindow: ToolWindow,
    ) {
        val panel = RescriptMigrationPanel(project)
        RescriptToolWindowContent.install(toolWindow, panel, panel)
    }
}
