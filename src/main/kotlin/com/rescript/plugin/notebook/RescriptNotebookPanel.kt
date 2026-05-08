package com.rescript.plugin.notebook

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.components.JBScrollPane
import java.awt.BorderLayout
import java.awt.Component
import java.awt.datatransfer.StringSelection
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * Top-level UI for one notebook file: a toolbar (Add Cell / Run All
 * / Export Markdown) plus a vertically-stacked column of cell panels.
 *
 * The panel keeps its own list of [RescriptNotebookCellPanel]s and
 * exposes [snapshot]/[isModified] for the surrounding [RescriptNotebookFileEditor]
 * to wire into the document save lifecycle.
 */
class RescriptNotebookPanel(
    private val project: Project,
    initialDocument: NotebookDocument,
    private val onModified: () -> Unit,
) : SimpleToolWindowPanel(true, true) {
    private val cellsContainer: JPanel =
        JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
        }

    private val cellPanels: MutableList<RescriptNotebookCellPanel> = mutableListOf()

    private val projectPath: String = project.basePath ?: System.getProperty("user.dir")

    init {
        val initialCells =
            initialDocument.cells.ifEmpty { listOf(NotebookCell(code = "", lastOutput = "")) }
        for (cell in initialCells) addCellInternal(cell)
        rebuildCellsContainer()
        setContent(JBScrollPane(cellsContainer))
        setToolbar(buildToolbar())
    }

    /** Returns the current notebook state as serialised by the file editor. */
    fun snapshot(): NotebookDocument = NotebookDocument(cells = cellPanels.map { it.toCell() })

    private fun buildToolbar(): JComponent {
        val group =
            DefaultActionGroup().apply {
                add(AddCellAction())
                add(RunAllAction())
                addSeparator()
                add(ExportMarkdownAction())
            }
        val toolbar = ActionManager.getInstance().createActionToolbar(TOOLBAR_PLACE, group, true)
        toolbar.targetComponent = this
        return toolbar.component
    }

    private fun addCellInternal(cell: NotebookCell) {
        val panel =
            RescriptNotebookCellPanel(
                initialCell = cell,
                projectPath = projectPath,
                onChanged = { onModified() },
                onDelete = { panel -> deleteCell(panel) },
                onMoveUp = { panel -> moveCell(panel, -1) },
                onMoveDown = { panel -> moveCell(panel, +1) },
            )
        cellPanels.add(panel)
    }

    private fun deleteCell(panel: RescriptNotebookCellPanel) {
        cellPanels.remove(panel)
        if (cellPanels.isEmpty()) addCellInternal(NotebookCell("", ""))
        rebuildCellsContainer()
        onModified()
    }

    private fun moveCell(
        panel: RescriptNotebookCellPanel,
        delta: Int,
    ) {
        val idx = cellPanels.indexOf(panel)
        val target = idx + delta
        if (idx < 0 || target !in cellPanels.indices) return
        cellPanels.removeAt(idx)
        cellPanels.add(target, panel)
        rebuildCellsContainer()
        onModified()
    }

    private fun rebuildCellsContainer() {
        cellsContainer.removeAll()
        for (panel in cellPanels) {
            panel.alignmentX = Component.LEFT_ALIGNMENT
            cellsContainer.add(panel)
            cellsContainer.add(Box.createVerticalStrut(8))
        }
        cellsContainer.revalidate()
        cellsContainer.repaint()
    }

    /**
     * Toolbar action that appends a new empty cell at the end of the notebook
     * and rebuilds the cells container.
     */
    private inner class AddCellAction : AnAction("Add Cell", "Append an empty cell", AllIcons.General.Add) {
        override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

        override fun actionPerformed(e: AnActionEvent) {
            addCellInternal(NotebookCell("", ""))
            rebuildCellsContainer()
            onModified()
        }
    }

    /**
     * Toolbar action that triggers each cell's Run button in order. Per-cell
     * threading is delegated to the cell panels so this action does not block
     * the EDT.
     */
    private inner class RunAllAction :
        AnAction("Run All", "Run every cell in order", AllIcons.Actions.Execute) {
        override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

        override fun actionPerformed(e: AnActionEvent) {
            // Each cell handles its own background execution; clicking
            // Run All simply triggers each one's Run button.
            for (panel in cellPanels) {
                // Defer to per-cell threading; using SwingUtilities to
                // post the click avoids a tight loop on the EDT.
                javax.swing.SwingUtilities.invokeLater { panel.runFromExternal() }
            }
        }
    }

    /**
     * Toolbar action that serialises the current notebook snapshot to Markdown
     * (via [RescriptNotebookMarkdownExporter]) and copies it to the clipboard.
     */
    private inner class ExportMarkdownAction :
        AnAction(
            "Copy as Markdown",
            "Copy the notebook as Markdown to the clipboard",
            AllIcons.Actions.Copy,
        ) {
        override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

        override fun actionPerformed(e: AnActionEvent) {
            val markdown = RescriptNotebookMarkdownExporter.toMarkdown(snapshot())
            CopyPasteManager.getInstance().setContents(StringSelection(markdown))
        }
    }

    private companion object {
        const val TOOLBAR_PLACE = "ReScriptNotebookToolbar"
    }
}
