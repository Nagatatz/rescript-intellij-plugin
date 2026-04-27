package com.rescript.plugin.diagram

import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.rescript.plugin.diagram.RescriptDependencyDiagramExportAction.Format
import java.awt.BorderLayout
import java.awt.Font
import javax.swing.JPanel
import javax.swing.JTextArea

/**
 * Tool window UI for the ReScript module dependency diagram.
 *
 * Renders the current dependency graph as Mermaid text in a read-only
 * editor and exposes Refresh / Copy-as-DOT / Copy-as-Mermaid actions on
 * the toolbar. The bottom status bar shows module and edge counts.
 *
 * @see RescriptDependencyDiagramToolWindowFactory which creates instances of this panel
 */
class RescriptDependencyDiagramPanel(
    private val project: Project,
) : SimpleToolWindowPanel(true, true),
    Disposable {
    private val textArea: JTextArea =
        JTextArea().apply {
            isEditable = false
            font = Font(Font.MONOSPACED, Font.PLAIN, font.size)
        }

    private val statusLabel: JBLabel = JBLabel(" ")

    init {
        val centerPanel =
            JPanel(BorderLayout()).apply {
                add(JBScrollPane(textArea), BorderLayout.CENTER)
                add(statusLabel, BorderLayout.SOUTH)
            }
        setContent(centerPanel)
        setToolbar(buildToolbar())
        refresh()
    }

    private fun buildToolbar(): javax.swing.JComponent {
        val group =
            DefaultActionGroup().apply {
                add(RefreshAction())
                addSeparator()
                add(RescriptDependencyDiagramExportAction(Format.DOT))
                add(RescriptDependencyDiagramExportAction(Format.MERMAID))
            }
        val toolbar = ActionManager.getInstance().createActionToolbar(TOOLBAR_PLACE, group, true)
        toolbar.targetComponent = this
        return toolbar.component
    }

    private fun refresh() {
        val model = RescriptDependencyDiagramProvider.buildDiagram(project)
        textArea.text = RescriptMermaidExporter.toMermaid(model)
        textArea.caretPosition = 0
        statusLabel.text = " Modules: ${model.moduleCount()}   Edges: ${model.edgeCount()}"
    }

    override fun dispose() {
        // Children are disposed via Swing GC. No external resources to release.
    }

    /** Toolbar action that rebuilds the dependency graph from the current PSI state. */
    private inner class RefreshAction :
        AnAction(
            "Refresh",
            "Rebuild the dependency graph",
            AllIcons.Actions.Refresh,
        ) {
        override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

        override fun actionPerformed(e: AnActionEvent) {
            refresh()
        }
    }

    private companion object {
        const val TOOLBAR_PLACE = "ReScriptModuleDiagramToolbar"
    }
}
