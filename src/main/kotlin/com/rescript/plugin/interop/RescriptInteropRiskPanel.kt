package com.rescript.plugin.interop

import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import com.rescript.plugin.ui.RescriptToolWindowPanelBase
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
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
 * @see RescriptToolWindowPanelBase for the shared toolbar / status / refresh scaffold
 */
class RescriptInteropRiskPanel(
    private val project: Project,
) : RescriptToolWindowPanelBase(project, TOOLBAR_PLACE) {
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

    init {
        installUi(
            JBScrollPane(list),
            DefaultActionGroup().apply {
                add(createRefreshAction("Re-scan the project for JS interop call sites"))
            },
        )
        scheduleRefresh()
    }

    override fun doRefresh() {
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
     * List cell renderer for [InteropEntry] rows. Layout:
     * `[colour band] [risk/kind] file:line  preview…`
     *
     * The 4 px-wide left band is filled with [COLOR_BY_RISK]'s entry for
     * the row's [RiskLevel], so HIGH/MEDIUM/LOW are scannable without
     * reading the text.
     */
    private class EntryRenderer : ListCellRenderer<InteropEntry> {
        private val text = JBLabel()
        private val band =
            JPanel().apply {
                preferredSize = Dimension(BAND_WIDTH, 1)
                isOpaque = true
            }
        private val row =
            JPanel(BorderLayout()).apply {
                add(band, BorderLayout.WEST)
                add(text, BorderLayout.CENTER)
                border = JBUI.Borders.emptyLeft(2)
                isOpaque = true
            }

        override fun getListCellRendererComponent(
            list: JList<out InteropEntry>,
            value: InteropEntry?,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean,
        ): Component {
            if (value == null) {
                text.text = ""
                return row
            }
            val risk = value.risk.name.lowercase()
            val kind =
                value.kind.name
                    .lowercase()
                    .replace('_', '-')
            text.text = "[$risk/$kind] ${value.file.name}:${value.lineNumber}  ${value.previewLine}"
            band.background = COLOR_BY_RISK.getValue(value.risk)
            val bg = if (isSelected) list.selectionBackground else list.background
            val fg = if (isSelected) list.selectionForeground else list.foreground
            row.background = bg
            text.background = bg
            text.foreground = fg
            text.isOpaque = true
            return row
        }

        private companion object {
            private const val BAND_WIDTH = 4
        }
    }

    private companion object {
        const val TOOLBAR_PLACE = "ReScriptInteropRiskToolbar"
    }
}
