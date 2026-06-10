package com.rescript.plugin.coverage

import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import com.rescript.plugin.ui.RescriptToolWindowPanelBase
import java.awt.Component
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JTable
import javax.swing.RowSorter
import javax.swing.SortOrder
import javax.swing.SwingConstants
import javax.swing.table.AbstractTableModel
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.TableRowSorter

/**
 * Tool window UI for the Type Coverage Heat Map.
 *
 * Renders one row per scanned `.res` file with its annotation totals
 * and a colour-coded coverage percentage. Sorted ascending by
 * coverage % on first display so files most in need of annotation
 * float to the top; column headers can be clicked to re-sort.
 * Double-clicking a row opens that file at offset 0.
 *
 * @see RescriptTypeCoverageToolWindowFactory which creates instances of this panel
 * @see RescriptToolWindowPanelBase for the shared toolbar / status / refresh scaffold
 */
class RescriptTypeCoveragePanel(
    private val project: Project,
) : RescriptToolWindowPanelBase(TOOLBAR_PLACE) {
    private val tableModel = CoverageTableModel()
    private val table: JBTable =
        JBTable(tableModel).apply {
            autoCreateRowSorter = false
            val sorter = TableRowSorter(tableModel)
            // Coverage% column should sort numerically — auto-created sorters
            // would compare as strings if the cell value were stringified.
            sorter.setComparator(COL_TOTAL) { a: Any, b: Any -> (a as Int).compareTo(b as Int) }
            sorter.setComparator(COL_ANNOTATED) { a: Any, b: Any -> (a as Int).compareTo(b as Int) }
            sorter.setComparator(COL_INFERRED) { a: Any, b: Any -> (a as Int).compareTo(b as Int) }
            sorter.setComparator(COL_COVERAGE) { a: Any, b: Any -> (a as Double).compareTo(b as Double) }
            sorter.sortKeys = listOf(RowSorter.SortKey(COL_COVERAGE, SortOrder.ASCENDING))
            rowSorter = sorter
            getColumnModel().getColumn(COL_COVERAGE).cellRenderer = CoverageCellRenderer()
            for (col in listOf(COL_TOTAL, COL_ANNOTATED, COL_INFERRED)) {
                getColumnModel().getColumn(col).cellRenderer =
                    DefaultTableCellRenderer().apply { horizontalAlignment = SwingConstants.RIGHT }
            }
            addMouseListener(
                object : MouseAdapter() {
                    override fun mouseClicked(e: MouseEvent) {
                        if (e.clickCount == 2) {
                            val viewRow = rowAtPoint(e.point)
                            if (viewRow < 0) return
                            val modelRow = convertRowIndexToModel(viewRow)
                            val fc = tableModel.row(modelRow) ?: return
                            OpenFileDescriptor(project, fc.file).navigate(true)
                        }
                    }
                },
            )
        }

    init {
        installUi(
            JBScrollPane(table),
            DefaultActionGroup().apply {
                add(createRefreshAction("Re-scan the project for type coverage"))
            },
        )
        scheduleRefresh()
    }

    override fun doRefresh() {
        statusLabel.text = " Scanning…"
        ApplicationManager.getApplication().executeOnPooledThread {
            val result = RescriptTypeCoverageScanner.scan(project)
            ApplicationManager.getApplication().invokeLater {
                tableModel.replaceAll(result.files)
                statusLabel.text = buildStatusLine(result)
            }
        }
    }

    private fun buildStatusLine(result: ProjectCoverage): String {
        val truncationNote =
            if (result.truncated) " (truncated at ${RescriptTypeCoverageScanner.MAX_FILES} files)" else ""
        return " ${result.files.size} files, ${result.totalLets} bindings, " +
            "%.1f%%".format(result.coveragePercent) + " project coverage" + truncationNote
    }

    /**
     * Table model holding one [FileCoverage] row per scanned ReScript
     * file. Column values are typed (`String` / `Int` / `Double`) so
     * the row sorter can compare them correctly.
     */
    private inner class CoverageTableModel : AbstractTableModel() {
        private val rows = mutableListOf<FileCoverage>()

        fun row(index: Int): FileCoverage? = rows.getOrNull(index)

        fun replaceAll(items: List<FileCoverage>) {
            rows.clear()
            rows.addAll(items)
            fireTableDataChanged()
        }

        override fun getRowCount(): Int = rows.size

        override fun getColumnCount(): Int = COLUMN_NAMES.size

        override fun getColumnName(column: Int): String = COLUMN_NAMES[column]

        override fun getColumnClass(columnIndex: Int): Class<*> =
            when (columnIndex) {
                COL_FILE -> String::class.java
                COL_TOTAL, COL_ANNOTATED, COL_INFERRED -> Int::class.javaObjectType
                COL_COVERAGE -> Double::class.javaObjectType
                else -> Any::class.java
            }

        override fun getValueAt(
            rowIndex: Int,
            columnIndex: Int,
        ): Any {
            val fc = rows[rowIndex]
            return when (columnIndex) {
                COL_FILE -> displayPath(fc)
                COL_TOTAL -> fc.totalLets
                COL_ANNOTATED -> fc.annotatedLets
                COL_INFERRED -> fc.inferredLets
                COL_COVERAGE -> fc.coveragePercent
                else -> ""
            }
        }

        private fun displayPath(fc: FileCoverage): String {
            val basePath = project.basePath
            val full = fc.file.path
            return if (basePath != null && full.startsWith(basePath)) {
                full.removePrefix(basePath).removePrefix("/")
            } else {
                full
            }
        }
    }

    /**
     * Renderer that paints the coverage % cell with a heat-map colour
     * (red < 30%, yellow 30–69%, green ≥ 70%) and right-aligns the
     * formatted percentage.
     */
    private class CoverageCellRenderer : DefaultTableCellRenderer() {
        init {
            horizontalAlignment = SwingConstants.RIGHT
        }

        override fun getTableCellRendererComponent(
            table: JTable,
            value: Any?,
            isSelected: Boolean,
            hasFocus: Boolean,
            row: Int,
            column: Int,
        ): Component {
            val pct = (value as? Double) ?: 0.0
            val component =
                super.getTableCellRendererComponent(
                    table,
                    "%.1f%%".format(pct),
                    isSelected,
                    hasFocus,
                    row,
                    column,
                )
            if (!isSelected) {
                background = coverageColor(pct)
            }
            return component
        }

        private fun coverageColor(percent: Double): JBColor =
            when {
                percent < 30.0 -> JBColor(0xF2C2BE, 0x6E2B27)
                percent < 70.0 -> JBColor(0xFCE0A8, 0x7A5A22)
                else -> JBColor(0xC8E5C8, 0x2E5230)
            }
    }

    private companion object {
        const val TOOLBAR_PLACE = "ReScriptTypeCoverageToolbar"
        const val COL_FILE = 0
        const val COL_TOTAL = 1
        const val COL_ANNOTATED = 2
        const val COL_INFERRED = 3
        const val COL_COVERAGE = 4
        val COLUMN_NAMES = arrayOf("File", "Total", "Annotated", "Inferred", "Coverage %")
    }
}
