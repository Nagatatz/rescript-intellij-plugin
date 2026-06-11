package com.rescript.plugin.diagram

import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.rescript.plugin.diagram.RescriptDependencyDiagramExportAction.Format
import com.rescript.plugin.flow.MermaidSourceColorizer
import com.rescript.plugin.ui.DualViewToolWindowPanel
import com.rescript.plugin.util.HtmlEditorPaneFactory
import javax.swing.JEditorPane

/**
 * Tool window UI for the ReScript module dependency diagram.
 *
 * Hosts two views behind [DualViewToolWindowPanel]'s card toggle: a
 * Java2D visual graph rendered by [RescriptDependencyDiagramGraphView]
 * (default) and the read-only Mermaid `flowchart TD` source text.
 * Refresh and Copy-as-DOT / Copy-as-Mermaid actions remain available in
 * both modes. The bottom status bar shows module and edge counts.
 *
 * @see RescriptDependencyDiagramToolWindowFactory which creates instances of this panel
 * @see DualViewToolWindowPanel for the shared Visual / Source scaffold
 */
class RescriptDependencyDiagramPanel(
    private val project: Project,
) : DualViewToolWindowPanel(TOOLBAR_PLACE) {
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

    init {
        installUi(
            buildDualView(visual = graphView, source = textArea),
            DefaultActionGroup().apply {
                add(createVisualModeAction("Render the dependency graph as a top-down diagram"))
                add(createSourceModeAction("Show the Mermaid flowchart source text"))
                addSeparator()
                add(createRefreshAction("Rebuild the dependency graph"))
                addSeparator()
                add(RescriptDependencyDiagramExportAction(Format.DOT))
                add(RescriptDependencyDiagramExportAction(Format.MERMAID))
            },
        )
        scheduleRefresh()
    }

    override fun doRefresh() {
        val model = RescriptDependencyDiagramProvider.buildDiagram(project)
        textArea.text = MermaidSourceColorizer.render(RescriptMermaidExporter.toMermaid(model))
        textArea.caretPosition = 0
        graphView.setModel(model)
        statusLabel.text = " Modules: ${model.moduleCount()}   Edges: ${model.edgeCount()}"
    }

    private companion object {
        const val TOOLBAR_PLACE = "ReScriptModuleDiagramToolbar"
    }
}
