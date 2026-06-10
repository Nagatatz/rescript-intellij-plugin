package com.rescript.plugin.notebook

import com.intellij.icons.AllIcons
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.project.Project
import com.intellij.ui.EditorTextField
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.rescript.plugin.RescriptFileType
import com.rescript.plugin.repl.RescriptReplExecutor
import com.rescript.plugin.util.EditorTextFieldFactory
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
 * The code area uses [EditorTextField] bound to [RescriptFileType] so
 * cells get the same syntax highlighting as the REPL input field; the
 * output area stays as a plain [JTextArea] because evaluation output
 * is unstructured stdout/stderr. The Run action delegates to
 * [RescriptReplExecutor] for evaluation so behaviour stays consistent
 * with the existing REPL tool window.
 */
class RescriptNotebookCellPanel(
    project: Project,
    initialCell: NotebookCell,
    private val projectPath: String,
    private val onChanged: () -> Unit,
    private val onDelete: (RescriptNotebookCellPanel) -> Unit,
    private val onMoveUp: (RescriptNotebookCellPanel) -> Unit,
    private val onMoveDown: (RescriptNotebookCellPanel) -> Unit,
) : JPanel(BorderLayout()) {
    private val codeArea: EditorTextField =
        EditorTextField(
            EditorFactory.getInstance().createDocument(initialCell.code),
            project,
            RescriptFileType,
            false,
            false,
        ).apply {
            // Approx 4 visible rows at the editor scheme's font size; the
            // surrounding JBScrollPane handles overflow if the user types
            // more.
            preferredSize = Dimension(0, CODE_AREA_PREFERRED_HEIGHT)
            EditorTextFieldFactory.applyPanelDefaults(this) { editor ->
                editor.settings.isUseSoftWraps = true
            }
            addDocumentListener(
                object : DocumentListener {
                    override fun documentChanged(event: DocumentEvent) {
                        onChanged()
                    }
                },
            )
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
        // EditorTextField already provides its own scrolling and gutter,
        // so wrap only in a thin container to keep BorderLayout happy.
        add(codeArea, BorderLayout.CENTER)
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
        outputArea.foreground = JBColor.GRAY
        outputArea.text = "(running)"
        ApplicationManager.getApplication().executeOnPooledThread {
            val output = RescriptReplExecutor.execute(code, projectPath)
            ApplicationManager.getApplication().invokeLater {
                runButton.isEnabled = true
                runButton.text = "Run"
                outputArea.foreground =
                    if (output.startsWith("Error")) ERROR_FOREGROUND else null
                outputArea.text = output
                onChanged()
            }
        }
    }

    private companion object {
        // JBColor pairs (light, dark) so the panel adapts to the active
        // IDE theme. Previous Color(0x…) literals only worked on the
        // light theme, leaving the cell border invisible in dark mode.
        val BORDER_COLOR: JBColor = JBColor(Color(0xC0C0C0), Color(0x3C3C3C))
        val OUTPUT_BACKGROUND: JBColor = JBColor(Color(0xF5F5F5), Color(0x2B2B2B))
        val ERROR_FOREGROUND: JBColor = JBColor(Color(0xCC0000), Color(0xE74C3C))

        /** Initial vertical pixel budget for the code area: about four editor rows. */
        const val CODE_AREA_PREFERRED_HEIGHT = 80
    }
}
