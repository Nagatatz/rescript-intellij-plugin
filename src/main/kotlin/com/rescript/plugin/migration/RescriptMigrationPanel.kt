package com.rescript.plugin.migration

import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import java.awt.BorderLayout
import java.awt.Component
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.DefaultListModel
import javax.swing.JCheckBox
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JSplitPane
import javax.swing.ListCellRenderer

/**
 * Tool window UI for the Reason → ReScript migration pilot.
 *
 * Lists every `.re` / `.rei` file in the project, lets the user
 * tick the ones to convert, and runs `rescript convert` on each
 * selected file in turn. Conversion output (success or stderr) is
 * shown in a secondary list below the main candidate list.
 *
 * @see RescriptMigrationToolWindowFactory which creates instances of this panel
 */
class RescriptMigrationPanel(
    private val project: Project,
) : SimpleToolWindowPanel(true, true),
    Disposable {
    private val candidatesModel = DefaultListModel<MigrationCandidate>()
    private val resultsModel = DefaultListModel<ConversionResult>()
    private val selected: MutableSet<VirtualFile> = mutableSetOf()

    private val candidatesList: JBList<MigrationCandidate> =
        JBList(candidatesModel).apply {
            cellRenderer = CandidateRenderer { selected }
            addMouseListener(
                object : MouseAdapter() {
                    override fun mouseClicked(e: MouseEvent) {
                        val idx = locationToIndex(e.point)
                        if (idx < 0) return
                        val candidate = candidatesModel.get(idx)
                        if (candidate.file in
                            selected
                        ) {
                            selected.remove(candidate.file)
                        } else {
                            selected.add(candidate.file)
                        }
                        repaint()
                    }
                },
            )
        }

    private val resultsList: JBList<ConversionResult> = JBList(resultsModel).apply { cellRenderer = ResultRenderer() }

    private val statusLabel: JBLabel = JBLabel(" ")

    init {
        val candidatesPanel =
            JPanel(BorderLayout()).apply {
                add(JBLabel(" Candidates"), BorderLayout.NORTH)
                add(JBScrollPane(candidatesList), BorderLayout.CENTER)
            }
        val resultsPanel =
            JPanel(BorderLayout()).apply {
                add(JBLabel(" Results"), BorderLayout.NORTH)
                add(JBScrollPane(resultsList), BorderLayout.CENTER)
            }
        val split =
            JSplitPane(JSplitPane.VERTICAL_SPLIT, candidatesPanel, resultsPanel).apply {
                resizeWeight = 0.7
            }
        val centerPanel =
            JPanel(BorderLayout()).apply {
                add(split, BorderLayout.CENTER)
                add(statusLabel, BorderLayout.SOUTH)
            }
        setContent(centerPanel)
        setToolbar(buildToolbar())
        refreshCandidates()
    }

    override fun dispose() {
        // No external listeners to release.
    }

    private fun buildToolbar(): javax.swing.JComponent {
        val group =
            DefaultActionGroup().apply {
                add(RefreshAction())
                addSeparator()
                add(SelectAllAction())
                add(ClearSelectionAction())
                addSeparator()
                add(ConvertAction())
            }
        val toolbar = ActionManager.getInstance().createActionToolbar(TOOLBAR_PLACE, group, true)
        toolbar.targetComponent = this
        return toolbar.component
    }

    private fun refreshCandidates() {
        statusLabel.text = " Scanning…"
        ApplicationManager.getApplication().executeOnPooledThread {
            val candidates = RescriptMigrationFinder.findCandidates(project)
            ApplicationManager.getApplication().invokeLater {
                candidatesModel.clear()
                for (c in candidates) candidatesModel.addElement(c)
                resultsModel.clear()
                statusLabel.text =
                    if (candidates.isEmpty()) " No Reason files found" else " ${candidates.size} candidate(s)"
            }
        }
    }

    private fun convertSelected() {
        val targets = candidatesModel.elements().toList().filter { it.file in selected }
        if (targets.isEmpty()) {
            statusLabel.text = " Select at least one file before running Convert"
            return
        }
        statusLabel.text = " Converting 0/${targets.size}…"
        resultsModel.clear()
        ApplicationManager.getApplication().executeOnPooledThread {
            var success = 0
            var failed = 0
            for ((index, target) in targets.withIndex()) {
                val result = RescriptMigrationConverter.convert(project, target)
                if (result.status == ConversionStatus.SUCCESS) success++ else failed++
                ApplicationManager.getApplication().invokeLater {
                    resultsModel.addElement(result)
                    statusLabel.text = " Converting ${index + 1}/${targets.size}…"
                }
            }
            ApplicationManager.getApplication().invokeLater {
                statusLabel.text = " Done — $success succeeded, $failed failed"
                refreshCandidates()
            }
        }
    }

    /**
     * Toolbar action that re-scans the project for `.re` / `.rei` migration
     * candidates.
     */
    private inner class RefreshAction :
        AnAction("Refresh", "Re-scan the project for `.re` / `.rei` files", AllIcons.Actions.Refresh) {
        override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

        override fun actionPerformed(e: AnActionEvent) {
            refreshCandidates()
        }
    }

    /**
     * Toolbar action that marks every listed migration candidate as selected.
     */
    private inner class SelectAllAction :
        AnAction("Select All", "Select every candidate", AllIcons.Actions.Selectall) {
        override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

        override fun actionPerformed(e: AnActionEvent) {
            selected.clear()
            for (i in 0 until candidatesModel.size()) selected.add(candidatesModel.get(i).file)
            candidatesList.repaint()
        }
    }

    /**
     * Toolbar action that clears the current candidate selection without
     * removing any rows from the list.
     */
    private inner class ClearSelectionAction :
        AnAction("Clear Selection", "Deselect every candidate", AllIcons.Actions.Unselectall) {
        override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

        override fun actionPerformed(e: AnActionEvent) {
            selected.clear()
            candidatesList.repaint()
        }
    }

    /**
     * Toolbar action that invokes `rescript convert` against every selected
     * candidate, renames the resulting `.res` / `.resi` files in the VFS, and
     * refreshes the list when the batch finishes.
     */
    private inner class ConvertAction :
        AnAction("Convert Selected", "Run rescript convert on every checked file", AllIcons.Actions.Execute) {
        override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

        override fun actionPerformed(e: AnActionEvent) {
            convertSelected()
        }
    }

    /**
     * Renders a [MigrationCandidate] as `[checkbox] relative/path.re`.
     * The checkbox state is read from the panel's selection set on
     * every paint so list-wide actions (Select All / Clear) only need
     * to repaint without touching individual cells.
     */
    private class CandidateRenderer(
        private val selectedProvider: () -> Set<VirtualFile>,
    ) : ListCellRenderer<MigrationCandidate> {
        private val checkbox = JCheckBox()
        private val label = JBLabel()
        private val container =
            JPanel(BorderLayout()).apply {
                add(checkbox, BorderLayout.WEST)
                add(label, BorderLayout.CENTER)
            }

        override fun getListCellRendererComponent(
            list: JList<out MigrationCandidate>,
            value: MigrationCandidate?,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean,
        ): Component {
            value ?: return container.also { label.text = "" }
            checkbox.isSelected = value.file in selectedProvider()
            label.text = value.relativePath
            container.background = if (isSelected) list.selectionBackground else list.background
            label.foreground = if (isSelected) list.selectionForeground else list.foreground
            return container
        }
    }

    /**
     * Renders a [ConversionResult] in the secondary list with a
     * status prefix and the captured stderr / message tail.
     */
    private class ResultRenderer : ListCellRenderer<ConversionResult> {
        private val label = JBLabel()

        override fun getListCellRendererComponent(
            list: JList<out ConversionResult>,
            value: ConversionResult?,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean,
        ): Component {
            value ?: return label.also { it.text = "" }
            val status = if (value.status == ConversionStatus.SUCCESS) "[ok]" else "[fail]"
            val tail = if (value.message.isBlank()) "" else "  ${value.message.lineSequence().first()}"
            label.text = "$status ${value.candidate.relativePath}$tail"
            label.background = if (isSelected) list.selectionBackground else list.background
            label.foreground = if (isSelected) list.selectionForeground else list.foreground
            label.isOpaque = true
            return label
        }
    }

    private companion object {
        const val TOOLBAR_PLACE = "ReScriptMigrationToolbar"
    }
}
