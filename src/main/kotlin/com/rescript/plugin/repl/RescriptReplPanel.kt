package com.rescript.plugin.repl

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Font
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JToolBar

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

    private val inputArea =
        JBTextArea(4, 40).apply {
            font = Font(Font.MONOSPACED, Font.PLAIN, 13)
            border = JBUI.Borders.empty(4)
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
                                add(JBScrollPane(inputArea), BorderLayout.CENTER)
                                add(toolbar, BorderLayout.NORTH)
                            },
                            BorderLayout.SOUTH,
                        )
                    }
                setContent(content)
            }

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
                inputArea.text = ""
                runButton.isEnabled = true
            }
        }
    }
}
