package com.rescript.plugin.diagram

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBScrollPane
import com.rescript.plugin.diagram.RescriptDependencyDiagramExportAction.Format
import com.rescript.plugin.flow.MermaidSourceColorizer
import com.rescript.plugin.ui.RescriptToolWindowPanelBase
import com.rescript.plugin.util.HtmlEditorPaneFactory
import java.awt.CardLayout
import javax.swing.JEditorPane
import javax.swing.JPanel

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
 * @see RescriptToolWindowPanelBase for the shared toolbar / status / refresh scaffold
 */
class RescriptDependencyDiagramPanel(
    private val project: Project,
) : RescriptToolWindowPanelBase(TOOLBAR_PLACE) {
    /**
     * Read-only HTML pane displaying the Mermaid source. The HTML
     * payload is produced by [MermaidSourceColorizer] so keywords,
     * arrows, quoted node labels and `%%` comments pick up the editor
     * scheme's colours. Copy-as-Mermaid still exports the raw string
     * because it regenerates it through [RescriptMermaidExporter],
     * never reading the rendered pane.
     */
    private val textArea: JEditorPane = HtmlEditorPaneFactory.createReadOnlyHtmlPane()

    private val graphView: RescriptDependencyDiagramGraphView = RescriptDependencyDiagramGraphView()

    private val viewCards: CardLayout = CardLayout()

    private val viewSwitcher: JPanel =
        JPanel(viewCards).apply {
            add(JBScrollPane(graphView), CARD_VISUAL)
            add(JBScrollPane(textArea), CARD_SOURCE)
        }

    @Volatile
    private var visualMode: Boolean = true

    init {
        installUi(
            viewSwitcher,
            DefaultActionGroup().apply {
                add(VisualModeAction())
                add(SourceModeAction())
                addSeparator()
                add(createRefreshAction("Rebuild the dependency graph"))
                addSeparator()
                add(RescriptDependencyDiagramExportAction(Format.DOT))
                add(RescriptDependencyDiagramExportAction(Format.MERMAID))
            },
        )
        viewCards.show(viewSwitcher, CARD_VISUAL)
        scheduleRefresh()
    }

    override fun doRefresh() {
        val model = RescriptDependencyDiagramProvider.buildDiagram(project)
        textArea.text = MermaidSourceColorizer.render(RescriptMermaidExporter.toMermaid(model))
        textArea.caretPosition = 0
        graphView.setModel(model)
        statusLabel.text = " Modules: ${model.moduleCount()}   Edges: ${model.edgeCount()}"
    }

    private fun switchView(toVisual: Boolean) {
        visualMode = toVisual
        viewCards.show(viewSwitcher, if (toVisual) CARD_VISUAL else CARD_SOURCE)
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
