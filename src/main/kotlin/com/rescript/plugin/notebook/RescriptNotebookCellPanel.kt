package com.rescript.plugin.notebook

import com.intellij.icons.AllIcons
import com.intellij.openapi.application.ApplicationManager
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.rescript.plugin.repl.RescriptReplExecutor
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JTextArea

/**
 * Swing panel for one [NotebookCell]: a code editor on top, a Run
 * button, and a read-only output area below.
 *
 * Cells are intentionally simple — there is no syntax highlighting
 * (the JTextArea uses a monospace font instead) and no LSP wiring.
 * The Run action delegates to [RescriptReplExecutor] for evaluation
 * so behaviour stays consistent with the existing REPL tool window.
 */
class RescriptNotebookCellPanel(
    initialCell: NotebookCell,
    private val projectPath: String,
    private val onChanged: () -> Unit,
    private val onDelete: (RescriptNotebookCellPanel) -> Unit,
    private val onMoveUp: (RescriptNotebookCellPanel) -> Unit,
    private val onMoveDown: (RescriptNotebookCellPanel) -> Unit,
) : JPanel(BorderLayout()) {
    private val codeArea: JTextArea =
        JTextArea(initialCell.code).apply {
            font = Font(Font.MONOSPACED, Font.PLAIN, font.size)
            rows = 4
            tabSize = 2
            document.addDocumentListener(SimpleDocumentListener { onChanged() })
        }

    private val outputArea: JTextArea =
        JTextArea(initialCell.lastOutput).apply {
            font = Font(Font.MONOSPACED, Font.PLAIN, font.size)
            isEditable = false
            rows = 3
            background = OUTPUT_BACKGROUND
        }

    private val runButton: JButton = JButton("Run", AllIcons.Actions.Execute)

    init {
        border =
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(4, 6, 4, 6),
            )
        add(buildHeader(), BorderLayout.NORTH)
        add(JBScrollPane(codeArea), BorderLayout.CENTER)
        add(buildFooter(), BorderLayout.SOUTH)
        runButton.addActionListener { runCell() }
    }

    /** Returns the current cell content for serialization. */
    fun toCell(): NotebookCell = NotebookCell(code = codeArea.text, lastOutput = outputArea.text)

    /** External hook (e.g. the panel's Run All action) for triggering this cell's evaluation. */
    fun runFromExternal() {
        runCell()
    }

    private fun buildHeader(): JPanel =
        JPanel(BorderLayout()).apply {
            add(JBLabel(" Cell"), BorderLayout.WEST)
            add(buildHeaderActions(), BorderLayout.EAST)
        }

    private fun buildHeaderActions(): JPanel =
        JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            add(makeIconButton(AllIcons.Actions.MoveUp, "Move up") { onMoveUp(this@RescriptNotebookCellPanel) })
            add(Box.createHorizontalStrut(4))
            add(makeIconButton(AllIcons.Actions.MoveDown, "Move down") { onMoveDown(this@RescriptNotebookCellPanel) })
            add(Box.createHorizontalStrut(4))
            add(makeIconButton(AllIcons.Actions.GC, "Delete cell") { onDelete(this@RescriptNotebookCellPanel) })
        }

    private fun makeIconButton(
        icon: javax.swing.Icon,
        tooltip: String,
        onClick: () -> Unit,
    ): JButton =
        JButton(icon).apply {
            preferredSize = Dimension(24, 24)
            toolTipText = tooltip
            isBorderPainted = false
            isContentAreaFilled = false
            addActionListener { onClick() }
        }

    private fun buildFooter(): JPanel =
        JPanel(BorderLayout()).apply {
            add(JBScrollPane(outputArea), BorderLayout.CENTER)
            add(runButton, BorderLayout.EAST)
        }

    private fun runCell() {
        val code = codeArea.text
        runButton.isEnabled = false
        runButton.text = "Running…"
        outputArea.foreground = Color.GRAY
        outputArea.text = "(running)"
        ApplicationManager.getApplication().executeOnPooledThread {
            val output = RescriptReplExecutor.execute(code, projectPath)
            ApplicationManager.getApplication().invokeLater {
                runButton.isEnabled = true
                runButton.text = "Run"
                outputArea.foreground =
                    if (output.startsWith("Error")) Color(0xCC0000) else null
                outputArea.text = output
                onChanged()
            }
        }
    }

    private companion object {
        val BORDER_COLOR: Color = Color(0xC0C0C0)
        val OUTPUT_BACKGROUND: Color = Color(0xF5F5F5)
    }
}

/**
 * Tiny [javax.swing.event.DocumentListener] adapter that fires the
 * same callback for any change. Used by the cell panel so any code
 * edit marks the notebook as modified.
 */
private class SimpleDocumentListener(
    private val callback: () -> Unit,
) : javax.swing.event.DocumentListener {
    override fun insertUpdate(e: javax.swing.event.DocumentEvent?) = callback()

    override fun removeUpdate(e: javax.swing.event.DocumentEvent?) = callback()

    override fun changedUpdate(e: javax.swing.event.DocumentEvent?) = callback()
}
