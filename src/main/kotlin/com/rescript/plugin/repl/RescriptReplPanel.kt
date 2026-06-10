package com.rescript.plugin.repl

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CustomShortcutSet
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.SystemInfo
import com.intellij.ui.EditorTextField
import com.intellij.ui.OnePixelSplitter
import com.rescript.plugin.RescriptFileType
import com.rescript.plugin.util.EditorTextFieldFactory
import java.awt.BorderLayout
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

    // ── Input area (editable, syntax-highlighted) ──

    private val inputArea: EditorTextField =
        EditorTextField(
            EditorFactory.getInstance().createDocument(""),
            project,
            RescriptFileType,
            false, // isViewer
            false, // oneLineMode
        ).apply {
            setPlaceholder("Enter ReScript code here... (⌘+Enter to run)")
            EditorTextFieldFactory.applyPanelDefaults(this) { editor ->
                editor.contentComponent.isFocusable = true
            }
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

        // Input panel: editor + toolbar below
        val inputPanel =
            JPanel(BorderLayout()).apply {
                add(inputArea, BorderLayout.CENTER)
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
        val metaMask = if (SystemInfo.isMac) InputEvent.META_DOWN_MASK else InputEvent.CTRL_DOWN_MASK
        object : AnAction() {
            override fun actionPerformed(e: AnActionEvent) = executeInput()
        }.registerCustomShortcutSet(
            CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, metaMask)),
            inputArea,
        )

        // Register Up/Down arrow for history navigation
        object : AnAction() {
            override fun actionPerformed(e: AnActionEvent) = navigateHistory(-1)
        }.registerCustomShortcutSet(
            CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0)),
            inputArea,
        )
        object : AnAction() {
            override fun actionPerformed(e: AnActionEvent) = navigateHistory(1)
        }.registerCustomShortcutSet(
            CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0)),
            inputArea,
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
        inputArea.isEnabled = false

        // Execute on background thread
        ApplicationManager.getApplication().executeOnPooledThread {
            val result = RescriptReplExecutor.execute(code, basePath)
            ApplicationManager.getApplication().invokeLater {
                appendOutput("$result\n\n")
                inputArea.text = ""
                runButton.isEnabled = true
                inputArea.isEnabled = true
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
}
