package com.rescript.plugin.flow

import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.Alarm
import com.rescript.plugin.RescriptFileType
import com.rescript.plugin.RescriptInterfaceFileType
import java.awt.BorderLayout
import java.awt.Font
import java.awt.datatransfer.StringSelection
import javax.swing.JPanel
import javax.swing.JTextArea

/**
 * Tool window UI for the variant flow diagram.
 *
 * Listens to caret movements in any open ReScript editor and re-renders
 * the Mermaid `flowchart TD` for the `switch` expression containing the
 * caret. Also exposes Copy-as-Mermaid and Copy-as-DOT toolbar actions
 * for sharing the current diagram externally.
 *
 * @see RescriptVariantFlowToolWindowFactory which creates instances of this panel
 */
class RescriptVariantFlowPanel(
    private val project: Project,
) : SimpleToolWindowPanel(true, true),
    Disposable {
    private val textArea: JTextArea =
        JTextArea().apply {
            isEditable = false
            font = Font(Font.MONOSPACED, Font.PLAIN, font.size)
        }

    private val statusLabel: JBLabel = JBLabel(" ")

    private val refreshAlarm: Alarm = Alarm(Alarm.ThreadToUse.SWING_THREAD, this)

    @Volatile
    private var currentDiagram: FlowDiagram? = null

    init {
        val centerPanel =
            JPanel(BorderLayout()).apply {
                add(JBScrollPane(textArea), BorderLayout.CENTER)
                add(statusLabel, BorderLayout.SOUTH)
            }
        setContent(centerPanel)
        setToolbar(buildToolbar())
        attachEditorListeners()
        scheduleRefresh()
    }

    override fun dispose() {
        // Alarm is registered as a child disposable of `this`, so it
        // tears down automatically. EditorFactory listeners receive
        // their own parent disposable and clean up the same way.
    }

    private fun buildToolbar(): javax.swing.JComponent {
        val group =
            com.intellij.openapi.actionSystem.DefaultActionGroup().apply {
                add(RefreshAction())
                addSeparator()
                add(CopyAction(Format.MERMAID))
                add(CopyAction(Format.DOT))
            }
        val toolbar = ActionManager.getInstance().createActionToolbar(TOOLBAR_PLACE, group, true)
        toolbar.targetComponent = this
        return toolbar.component
    }

    /**
     * Subscribes to editor caret events for every existing and
     * future ReScript editor in the project so the panel can react
     * to caret motion without holding strong references to specific
     * files.
     */
    private fun attachEditorListeners() {
        val editorFactory =
            com.intellij.openapi.editor.EditorFactory
                .getInstance()
        for (editor in editorFactory.allEditors) {
            attachCaretListener(editor)
        }
        editorFactory.addEditorFactoryListener(
            object : EditorFactoryListener {
                override fun editorCreated(event: EditorFactoryEvent) {
                    attachCaretListener(event.editor)
                }
            },
            this,
        )
    }

    private fun attachCaretListener(editor: com.intellij.openapi.editor.Editor) {
        if (editor.project != project) return
        if (editor !is EditorEx) return
        editor.caretModel.addCaretListener(
            object : CaretListener {
                override fun caretPositionChanged(event: CaretEvent) {
                    scheduleRefresh()
                }
            },
            this,
        )
    }

    private fun scheduleRefresh() {
        refreshAlarm.cancelAllRequests()
        refreshAlarm.addRequest({ refresh() }, REFRESH_DEBOUNCE_MS)
    }

    private fun refresh() {
        val (file, source, offset) =
            currentEditorContext() ?: run {
                renderEmpty("Open a ReScript file to see its switch flow.")
                return
            }
        if (file.fileType != RescriptFileType && file.fileType != RescriptInterfaceFileType) {
            renderEmpty("Open a ReScript file to see its switch flow.")
            return
        }
        val diagram =
            ApplicationManager.getApplication().runReadAction<FlowDiagram?> {
                RescriptVariantFlowModel.buildAtOffset(source, offset)
            }
        currentDiagram = diagram
        if (diagram == null) {
            renderEmpty("No switch under caret.")
        } else {
            textArea.text = RescriptVariantFlowMermaidExporter.toMermaid(diagram)
            textArea.caretPosition = 0
            statusLabel.text = " Arms: ${countArms(diagram)}"
        }
    }

    private fun countArms(diagram: FlowDiagram): Int {
        fun walk(nodes: List<FlowNode>): Int = nodes.sumOf { 1 + walk(it.children) }
        return walk(diagram.arms)
    }

    private fun renderEmpty(message: String) {
        textArea.text = ""
        statusLabel.text = " $message"
        currentDiagram = null
    }

    private data class EditorContext(
        val file: VirtualFile,
        val source: String,
        val offset: Int,
    )

    private fun currentEditorContext(): EditorContext? {
        val fileEditor = FileEditorManager.getInstance(project).selectedEditor as? TextEditor ?: return null
        val editor = fileEditor.editor
        val file = fileEditor.file
        val text = editor.document.text
        val offset = editor.caretModel.offset
        return EditorContext(file, text, offset)
    }

    /**
     * Output formats produced by the toolbar copy actions. Kept in
     * sync with the user-facing labels so the action descriptions are
     * authored once.
     */
    private enum class Format(
        val label: String,
        val description: String,
    ) {
        MERMAID("Copy Mermaid", "Copy the current diagram as Mermaid flowchart syntax"),
        DOT("Copy DOT", "Copy the current diagram as graphviz DOT"),
    }

    private inner class RefreshAction :
        AnAction(
            "Refresh",
            "Rebuild the variant flow diagram",
            AllIcons.Actions.Refresh,
        ) {
        override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

        override fun actionPerformed(e: AnActionEvent) {
            scheduleRefresh()
        }
    }

    private inner class CopyAction(
        private val format: Format,
    ) : AnAction(
            format.label,
            format.description,
            AllIcons.Actions.Copy,
        ) {
        override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

        override fun update(e: AnActionEvent) {
            e.presentation.isEnabled = currentDiagram != null
        }

        override fun actionPerformed(e: AnActionEvent) {
            val diagram = currentDiagram ?: return
            val text =
                when (format) {
                    Format.MERMAID -> RescriptVariantFlowMermaidExporter.toMermaid(diagram)
                    Format.DOT -> RescriptVariantFlowDotExporter.toDot(diagram)
                }
            CopyPasteManager.getInstance().setContents(StringSelection(text))
        }
    }

    private companion object {
        const val TOOLBAR_PLACE = "ReScriptVariantFlowToolbar"
        const val REFRESH_DEBOUNCE_MS = 200
    }
}
