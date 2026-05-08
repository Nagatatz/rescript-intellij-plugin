package com.rescript.plugin.interop

import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import java.awt.BorderLayout
import java.awt.Component
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.DefaultListModel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.ListCellRenderer

/**
 * Tool window UI for the JS interop Risk Map.
 *
 * Lists every `%raw` / `external` / `Obj.magic` / `@bs.*` site in the
 * project, classified by [RescriptInteropClassifier]. Refreshes are
 * manual (project scans are too expensive to debounce on caret motion);
 * the initial scan kicks off in `init`.
 *
 * @see RescriptInteropRiskToolWindowFactory which creates instances of this panel
 */
class RescriptInteropRiskPanel(
    private val project: Project,
) : SimpleToolWindowPanel(true, true),
    Disposable {
    private val listModel = DefaultListModel<InteropEntry>()
    private val list: JBList<InteropEntry> =
        JBList(listModel).apply {
            cellRenderer = EntryRenderer()
            addMouseListener(
                object : MouseAdapter() {
                    override fun mouseClicked(e: MouseEvent) {
                        if (e.clickCount == 2) {
                            val entry = selectedValue ?: return
                            OpenFileDescriptor(project, entry.file, entry.offset).navigate(true)
                        }
                    }
                },
            )
        }

    private val statusLabel: JBLabel = JBLabel(" ")

    init {
        val centerPanel =
            JPanel(BorderLayout()).apply {
                add(JBScrollPane(list), BorderLayout.CENTER)
                add(statusLabel, BorderLayout.SOUTH)
            }
        setContent(centerPanel)
        setToolbar(buildToolbar())
        refreshAsync()
    }

    override fun dispose() {
        // No external listeners to release.
    }

    private fun buildToolbar(): javax.swing.JComponent {
        val group = DefaultActionGroup().apply { add(RefreshAction()) }
        val toolbar = ActionManager.getInstance().createActionToolbar(TOOLBAR_PLACE, group, true)
        toolbar.targetComponent = this
        return toolbar.component
    }

    private fun refreshAsync() {
        statusLabel.text = " Scanning…"
        ApplicationManager.getApplication().executeOnPooledThread {
            val result = RescriptInteropScanner.scan(project)
            ApplicationManager.getApplication().invokeLater {
                listModel.clear()
                for (entry in result.entries) listModel.addElement(entry)
                statusLabel.text = buildStatusLine(result)
            }
        }
    }

    private fun buildStatusLine(result: RescriptInteropScanner.Result): String {
        val byKind = result.entries.groupBy { it.kind }.mapValues { it.value.size }
        val breakdown =
            listOfNotNull(
                byKind[InteropKind.RAW]?.let { "raw=$it" },
                byKind[InteropKind.OBJ_MAGIC]?.let { "obj-magic=$it" },
                byKind[InteropKind.EXTERNAL]?.let { "external=$it" },
                byKind[InteropKind.BS_ATTR]?.let { "bs-attr=$it" },
            ).joinToString(" ")
        val truncatedNote = if (result.truncated) " (showing first ${RescriptInteropScanner.MAX_TOTAL})" else ""
        return " ${result.entries.size} interop site(s)$truncatedNote  $breakdown"
    }

    /**
     * Toolbar action that triggers an asynchronous re-scan of the project for
     * JS interop call sites.
     */
    private inner class RefreshAction :
        AnAction(
            "Refresh",
            "Re-scan the project for JS interop call sites",
            AllIcons.Actions.Refresh,
        ) {
        override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

        override fun actionPerformed(e: AnActionEvent) {
            refreshAsync()
        }
    }

    /**
     * List cell renderer for [InteropEntry] rows. Layout:
     * `[risk/kind] file:line  preview…`
     */
    private class EntryRenderer : ListCellRenderer<InteropEntry> {
        private val delegate = JBLabel()

        override fun getListCellRendererComponent(
            list: JList<out InteropEntry>,
            value: InteropEntry?,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean,
        ): Component {
            value ?: return delegate.also { it.text = "" }
            val risk = value.risk.name.lowercase()
            val kind =
                value.kind.name
                    .lowercase()
                    .replace('_', '-')
            delegate.text = "[$risk/$kind] ${value.file.name}:${value.lineNumber}  ${value.previewLine}"
            delegate.background = if (isSelected) list.selectionBackground else list.background
            delegate.foreground = if (isSelected) list.selectionForeground else list.foreground
            delegate.isOpaque = true
            return delegate
        }
    }

    private companion object {
        const val TOOLBAR_PLACE = "ReScriptInteropRiskToolbar"
    }
}
