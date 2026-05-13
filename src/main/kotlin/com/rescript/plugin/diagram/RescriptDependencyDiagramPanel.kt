package com.rescript.plugin.diagram

import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.rescript.plugin.diagram.RescriptDependencyDiagramExportAction.Format
import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.Font
import javax.swing.JPanel
import javax.swing.JTextArea

/**
 * Tool window UI for the ReScript module dependency diagram.
 *
 * Hosts two views in a [CardLayout]: a Java2D visual graph rendered by
 * [RescriptDependencyDiagramGraphView] (default) and the previous read-only
 * Mermaid `flowchart TD` source text. A Visual / Source toggle on the
 * toolbar swaps between them, mirroring the pattern in
 * `RescriptVariantFlowPanel`. Refresh and Copy-as-DOT / Copy-as-Mermaid
 * actions remain available in both modes. The bottom status bar shows
 * module and edge counts.
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

    private val graphView: RescriptDependencyDiagramGraphView = RescriptDependencyDiagramGraphView()

    private val viewCards: CardLayout = CardLayout()

    private val viewSwitcher: JPanel =
        JPanel(viewCards).apply {
            add(JBScrollPane(graphView), CARD_VISUAL)
            add(JBScrollPane(textArea), CARD_SOURCE)
        }

    private val statusLabel: JBLabel = JBLabel(" ")

    @Volatile
    private var visualMode: Boolean = true

    init {
        val centerPanel =
            JPanel(BorderLayout()).apply {
                add(viewSwitcher, BorderLayout.CENTER)
                add(statusLabel, BorderLayout.SOUTH)
            }
        setContent(centerPanel)
        setToolbar(buildToolbar())
        viewCards.show(viewSwitcher, CARD_VISUAL)
        refresh()
    }

    private fun buildToolbar(): javax.swing.JComponent {
        val group =
            DefaultActionGroup().apply {
                add(VisualModeAction())
                add(SourceModeAction())
                addSeparator()
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
        graphView.setModel(model)
        statusLabel.text = " Modules: ${model.moduleCount()}   Edges: ${model.edgeCount()}"
    }

    private fun switchView(toVisual: Boolean) {
        visualMode = toVisual
        viewCards.show(viewSwitcher, if (toVisual) CARD_VISUAL else CARD_SOURCE)
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

    /**
     * Toolbar toggle that flips the central panel into the Java2D
     * rendering of the dependency graph. Mutually exclusive with
     * [SourceModeAction] — exactly one is selected.
     */
    private inner class VisualModeAction :
        ToggleAction(
            "Visual",
            "Render the dependency graph as a top-down diagram",
            AllIcons.Toolwindows.ToolWindowHierarchy,
        ) {
        override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

        override fun isSelected(e: AnActionEvent): Boolean = visualMode

        override fun setSelected(
            e: AnActionEvent,
            state: Boolean,
        ) {
            if (state) switchView(true)
        }
    }

    /**
     * Toolbar toggle that flips the central panel into the textual
     * Mermaid `flowchart TD` source for copy-paste workflows.
     */
    private inner class SourceModeAction :
        ToggleAction(
            "Source",
            "Show the Mermaid flowchart source text",
            AllIcons.FileTypes.Text,
        ) {
        override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

        override fun isSelected(e: AnActionEvent): Boolean = !visualMode

        override fun setSelected(
            e: AnActionEvent,
            state: Boolean,
        ) {
            if (state) switchView(false)
        }
    }

    private companion object {
        const val TOOLBAR_PLACE = "ReScriptModuleDiagramToolbar"
        const val CARD_VISUAL = "visual"
        const val CARD_SOURCE = "source"
    }
}
