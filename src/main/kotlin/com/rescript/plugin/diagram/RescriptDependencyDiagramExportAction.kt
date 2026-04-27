package com.rescript.plugin.diagram

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.DumbAware
import java.awt.datatransfer.StringSelection
import javax.swing.Icon

/**
 * Copies the ReScript module dependency diagram to the system clipboard
 * in the requested text format.
 *
 * Two instances are wired into the tool window toolbar — one for DOT
 * (Graphviz) output and one for Mermaid — sharing this single class via
 * the [Format] discriminator. Pure rendering is exposed through
 * [RescriptDependencyDiagramExportAction.render] so the format conversion
 * can be unit-tested independently of IDE state.
 */
class RescriptDependencyDiagramExportAction(
    private val format: Format,
) : AnAction(format.actionText, format.description, format.icon),
    DumbAware {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val model = RescriptDependencyDiagramProvider.buildDiagram(project)
        val content = render(model, format)
        CopyPasteManager.getInstance().setContents(StringSelection(content))
    }

    /** Output format selector for the export action. */
    enum class Format(
        val actionText: String,
        val description: String,
        val icon: Icon,
    ) {
        DOT(
            actionText = "Copy as DOT",
            description = "Copy the dependency graph in Graphviz DOT format",
            icon = AllIcons.Actions.Copy,
        ),
        MERMAID(
            actionText = "Copy as Mermaid",
            description = "Copy the dependency graph in Mermaid format",
            icon = AllIcons.Actions.Copy,
        ),
    }

    companion object {
        /**
         * Renders the model to the textual representation matching [format].
         *
         * Pure function with no IDE dependencies; safe to call from unit
         * tests and from background threads.
         *
         * @param model the dependency model to render
         * @param format the target text format
         * @return the rendered string
         */
        fun render(
            model: RescriptDependencyDiagramModel,
            format: Format,
        ): String =
            when (format) {
                Format.DOT -> model.toDot()
                Format.MERMAID -> RescriptMermaidExporter.toMermaid(model)
            }
    }
}
