package com.rescript.plugin.repl

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CustomShortcutSet
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.EditorTextField
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.util.ui.JBUI
import com.rescript.plugin.RescriptFileType
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Font
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
 * Provides an input area for code entry, an output area for execution results,
 * and a toolbar with Run / Clear actions.
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
    private val outputArea =
        JBTextArea().apply {
            isEditable = false
            font = Font(Font.MONOSPACED, Font.PLAIN, 13)
            border = JBUI.Borders.empty(4)
        }

    // Use EditorTextField instead of JBTextArea so that IntelliJ's action system
    // does not intercept keyboard events before they reach the input area.
    // Uses the 3-param constructor to create a PSI-associated Document for proper
    // editor initialization, and sets contentComponent focusable for tool window focus.
    private val inputArea =
        EditorTextField("", project, RescriptFileType).apply {
            setOneLineMode(false)
            preferredSize = Dimension(Int.MAX_VALUE, JBUI.scale(80)) // ~4 lines height
            addSettingsProvider { editor ->
                editor.contentComponent.isFocusable = true
            }
        }

    private val runButton = JButton("Run")

    private val mainPanel: JComponent

    val component: JComponent
        get() = mainPanel

    init {
        val toolbar =
            JToolBar().apply {
                isFloatable = false
                add(
                    runButton.apply {
                        addActionListener { executeInput() }
                    },
                )
                add(
                    JButton("Clear").apply {
                        addActionListener { outputArea.text = "" }
                    },
                )
            }

        val panel =
            SimpleToolWindowPanel(true, true).apply {
                val content =
                    JPanel(BorderLayout()).apply {
                        add(JBScrollPane(outputArea), BorderLayout.CENTER)
                        add(
                            JPanel(BorderLayout()).apply {
                                // EditorTextField handles its own scrolling
                                add(inputArea, BorderLayout.CENTER)
                                add(toolbar, BorderLayout.NORTH)
                            },
                            BorderLayout.SOUTH,
                        )
                    }
                setContent(content)
            }

        // Register Ctrl+Enter (Cmd+Enter on macOS) shortcut to execute input
        val metaMask =
            if (System.getProperty("os.name").lowercase().contains("mac")) {
                InputEvent.META_DOWN_MASK
            } else {
                InputEvent.CTRL_DOWN_MASK
            }
        val executeShortcut =
            CustomShortcutSet(
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, metaMask),
            )
        object : AnAction() {
            override fun actionPerformed(e: AnActionEvent) {
                executeInput()
            }
        }.registerCustomShortcutSet(executeShortcut, inputArea)

        mainPanel = panel
    }

    private fun executeInput() {
        val code = inputArea.text.trim()
        if (code.isEmpty()) return

        outputArea.append("> $code\n")
        val basePath = project.basePath
        if (basePath == null) {
            outputArea.append("Error: project base path not found\n\n")
            return
        }

        // Disable Run button during execution to prevent concurrent runs
        runButton.isEnabled = false

        // Execute on background thread to avoid blocking the EDT
        ApplicationManager.getApplication().executeOnPooledThread {
            val result = RescriptReplExecutor.execute(code, basePath)
            ApplicationManager.getApplication().invokeLater {
                outputArea.append("$result\n\n")
                inputArea.text = "" // EditorTextField supports text assignment
                runButton.isEnabled = true
            }
        }
    }
}
