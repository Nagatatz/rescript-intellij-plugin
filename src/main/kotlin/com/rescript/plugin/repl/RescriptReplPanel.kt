package com.rescript.plugin.repl

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.util.Disposer
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JToolBar
import javax.swing.KeyStroke

/**
 * Swing panel for the ReScript REPL tool window.
 *
 * Layout (top to bottom):
 * - Output area (EditorEx, read-only, auto-scrolling)
 * - Resizable divider (OnePixelSplitter, draggable)
 * - Input area (EditorTextField, ReScript syntax highlighting)
 * - Toolbar (Run / Clear buttons)
 *
 * Supports input history navigation with Up/Down arrow keys.
 *
 * @param project the current IntelliJ project
 * @param parentDisposable lifecycle owner for resource cleanup
 *
 * @see RescriptReplToolWindowFactory which creates this panel
 * @see RescriptReplExecutor for the underlying execution engine
 */
class RescriptReplPanel(
    private val project: Project,
    parentDisposable: Disposable,
) {
    // ── Output area (read-only editor with auto-scroll) ──

    private val outputDocument = EditorFactory.getInstance().createDocument("")

    private val outputEditor: EditorEx =
        (
            EditorFactory.getInstance().createEditor(
                outputDocument,
                project,
                PlainTextFileType.INSTANCE,
                true, // isViewer — read-only
            ) as EditorEx
        ).apply {
            settings.isLineNumbersShown = false
            settings.isFoldingOutlineShown = false
            settings.isLineMarkerAreaShown = false
            settings.isIndentGuidesShown = false
            settings.isRightMarginShown = false
            settings.additionalLinesCount = 0
            settings.isAdditionalPageAtBottom = false
            settings.isCaretRowShown = false
        }

    // ── Input area (editable, with placeholder) ──

    private val inputArea: JBTextArea =
        PlaceholderTextArea("Enter ReScript code here... (⌘+Enter to run)").apply {
            font = Font(Font.MONOSPACED, Font.PLAIN, 13)
            border = JBUI.Borders.empty(4)
            lineWrap = true
            wrapStyleWord = true
            rows = 4
        }

    // ── Input history ──

    private val history = mutableListOf<String>()
    private var historyIndex = -1
    private var currentInput = ""

    // ── UI components ──

    private val runButton = JButton("Run")
    private val mainPanel: JComponent

    val component: JComponent
        get() = mainPanel

    init {
        // Dispose the output editor when the tool window is closed
        Disposer.register(parentDisposable) {
            EditorFactory.getInstance().releaseEditor(outputEditor)
        }

        val toolbar =
            JToolBar().apply {
                isFloatable = false
                add(
                    runButton.apply {
                        toolTipText = "Execute code (⌘+Enter)"
                        addActionListener { executeInput() }
                    },
                )
                add(
                    JButton("Clear").apply {
                        toolTipText = "Clear output"
                        addActionListener { clearOutput() }
                    },
                )
            }

        // Input panel: scrollable text area + toolbar below
        val inputPanel =
            JPanel(BorderLayout()).apply {
                add(JBScrollPane(inputArea), BorderLayout.CENTER)
                add(toolbar, BorderLayout.SOUTH)
            }

        // Resizable split: output (top 70%) / input (bottom 30%)
        val splitter =
            OnePixelSplitter(true, 0.7f).apply {
                firstComponent = outputEditor.component
                secondComponent = inputPanel
            }

        val panel =
            SimpleToolWindowPanel(true, true).apply {
                setContent(splitter)
            }

        // Register Cmd+Enter / Ctrl+Enter shortcut to execute
        val metaMask =
            if (System.getProperty("os.name").lowercase().contains("mac")) {
                InputEvent.META_DOWN_MASK
            } else {
                InputEvent.CTRL_DOWN_MASK
            }
        inputArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, metaMask), "executeRepl")
        inputArea.getActionMap().put(
            "executeRepl",
            object : javax.swing.AbstractAction() {
                override fun actionPerformed(e: java.awt.event.ActionEvent?) = executeInput()
            },
        )

        // Register Up/Down arrow for history navigation
        inputArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "historyUp")
        inputArea.getActionMap().put(
            "historyUp",
            object : javax.swing.AbstractAction() {
                override fun actionPerformed(e: java.awt.event.ActionEvent?) = navigateHistory(-1)
            },
        )
        inputArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "historyDown")
        inputArea.getActionMap().put(
            "historyDown",
            object : javax.swing.AbstractAction() {
                override fun actionPerformed(e: java.awt.event.ActionEvent?) = navigateHistory(1)
            },
        )

        mainPanel = panel
    }

    private fun executeInput() {
        val code = inputArea.text.trim()
        if (code.isEmpty()) return

        // Save to history
        history.add(code)
        historyIndex = -1
        currentInput = ""

        appendOutput("> $code\n")
        val basePath = project.basePath
        if (basePath == null) {
            appendOutput("Error: project base path not found\n\n")
            return
        }

        // Disable Run button during execution
        runButton.isEnabled = false
        inputArea.isEditable = false

        // Execute on background thread
        ApplicationManager.getApplication().executeOnPooledThread {
            val result = RescriptReplExecutor.execute(code, basePath)
            ApplicationManager.getApplication().invokeLater {
                appendOutput("$result\n\n")
                inputArea.text = ""
                runButton.isEnabled = true
                inputArea.isEditable = true
                inputArea.requestFocusInWindow()
            }
        }
    }

    /**
     * Appends text to the output editor and auto-scrolls to the bottom.
     *
     * @param text the text to append
     */
    private fun appendOutput(text: String) {
        WriteCommandAction.runWriteCommandAction(project) {
            outputDocument.insertString(outputDocument.textLength, text)
        }
        outputEditor.caretModel.moveToOffset(outputDocument.textLength)
        outputEditor.scrollingModel.scrollToCaret(ScrollType.MAKE_VISIBLE)
    }

    /**
     * Clears all output text.
     */
    private fun clearOutput() {
        WriteCommandAction.runWriteCommandAction(project) {
            outputDocument.deleteString(0, outputDocument.textLength)
        }
    }

    /**
     * Navigates through input history.
     *
     * @param direction -1 for previous (Up), +1 for next (Down)
     */
    private fun navigateHistory(direction: Int) {
        if (history.isEmpty()) return

        // Save current input when starting history navigation
        if (historyIndex == -1 && direction == -1) {
            currentInput = inputArea.text
        }

        val newIndex = historyIndex + direction
        when {
            newIndex < 0 -> {
                // Past the oldest entry — do nothing
                return
            }

            newIndex >= history.size -> {
                // Back to current input
                historyIndex = -1
                inputArea.text = currentInput
                return
            }

            else -> {
                // History entries are stored oldest-first; navigate from newest
                historyIndex = newIndex
                inputArea.text = history[history.size - 1 - newIndex]
            }
        }
    }

    /**
     * A [JBTextArea] that displays placeholder text when empty and unfocused.
     *
     * @param placeholder the hint text to display
     */
    private class PlaceholderTextArea(
        private val placeholder: String,
    ) : JBTextArea() {
        init {
            addFocusListener(
                object : FocusAdapter() {
                    override fun focusGained(e: FocusEvent?) = repaint()

                    override fun focusLost(e: FocusEvent?) = repaint()
                },
            )
        }

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            if (text.isEmpty() && !isFocusOwner) {
                val g2 = g as Graphics2D
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
                g2.color = Color.GRAY
                val insets = insets
                g2.drawString(placeholder, insets.left + 2, g.getFontMetrics().height + insets.top)
            }
        }
    }
}
