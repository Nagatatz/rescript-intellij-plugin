package com.rescript.plugin.dependencies

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory

/**
 * Factory for the "ReScript Dependencies" tool window that displays a tree view
 * of packages listed in `rescript.json` (`bs-dependencies`, `bs-dev-dependencies`,
 * `pinned-dependencies`).
 *
 * Shows each dependency with its installed version resolved from `node_modules/<pkg>/package.json`.
 * Double-clicking a dependency opens its folder in the project view.
 *
 * @see RescriptDependenciesPanel for the panel implementation
 */
class RescriptDependenciesToolWindowFactory :
    ToolWindowFactory,
    DumbAware {
    override fun createToolWindowContent(
        project: Project,
        toolWindow: ToolWindow,
    ) {
        val panel = RescriptDependenciesPanel(project)
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
