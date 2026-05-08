package com.rescript.plugin.migration

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

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
 */
class RescriptMigrationToolWindowFactory :
    ToolWindowFactory,
    DumbAware {
    override fun createToolWindowContent(
        project: Project,
        toolWindow: ToolWindow,
    ) {
        val panel = RescriptMigrationPanel(project)
        val content = ContentFactory.getInstance().createContent(panel, "", false)
        Disposer.register(content, panel)
        toolWindow.contentManager.addContent(content)
    }
}
