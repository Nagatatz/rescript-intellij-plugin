package com.rescript.plugin.flow

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.JBScrollPane
import com.rescript.plugin.RescriptFileType
import com.rescript.plugin.RescriptInterfaceFileType
import com.rescript.plugin.ui.RescriptToolWindowPanelBase
import com.rescript.plugin.util.HtmlEditorPaneFactory
import java.awt.CardLayout
import java.awt.datatransfer.StringSelection
import javax.swing.JEditorPane
import javax.swing.JPanel

/**
 * Tool window UI for the variant flow diagram.
 *
 * Listens to caret movements in any open ReScript editor and re-renders
 * the Mermaid `flowchart TD` for the `switch` expression containing the
 * caret. Also exposes Copy-as-Mermaid and Copy-as-DOT toolbar actions
 * for sharing the current diagram externally.
 *
 * @see RescriptVariantFlowToolWindowFactory which creates instances of this panel
 * @see RescriptToolWindowPanelBase for the shared toolbar / status / refresh scaffold
 */
class RescriptVariantFlowPanel(
    private val project: Project,
) : RescriptToolWindowPanelBase(TOOLBAR_PLACE, REFRESH_DEBOUNCE_MS) {
    /**
     * Read-only HTML pane displaying the Mermaid source. The HTML
     * payload is produced by [MermaidSourceColorizer] so keywords,
     * arrows, quoted labels and `%%` comments pick up the editor
     * scheme's colours. The raw Mermaid string is preserved in
     * [currentMermaidSource] so the Copy Mermaid toolbar action
     * exports plain text rather than the rendered HTML.
     */
    private val textArea: JEditorPane = HtmlEditorPaneFactory.createReadOnlyHtmlPane()

    @Volatile
    private var currentMermaidSource: String = ""

    private val graphView: RescriptVariantFlowGraphView = RescriptVariantFlowGraphView()

    private val viewCards: CardLayout = CardLayout()

    private val viewSwitcher: JPanel =
        JPanel(viewCards).apply {
            add(JBScrollPane(graphView), CARD_VISUAL)
            add(JBScrollPane(textArea), CARD_SOURCE)
        }

    @Volatile
    private var currentDiagram: FlowDiagram? = null

    @Volatile
    private var currentJumpTarget: JumpTarget? = null

    @Volatile
    private var visualMode: Boolean = true

    init {
        installUi(
            viewSwitcher,
            DefaultActionGroup().apply {
                add(VisualModeAction())
                add(SourceModeAction())
                addSeparator()
                add(createRefreshAction("Rebuild the variant flow diagram"))
                addSeparator()
                add(JumpToSwitchAction())
                addSeparator()
                add(CopyAction(Format.MERMAID))
                add(CopyAction(Format.DOT))
            },
        )
        attachEditorListeners()
        viewCards.show(viewSwitcher, CARD_VISUAL)
        scheduleRefresh()
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

    override fun doRefresh() {
        val ctx = currentEditorContext()
        if (ctx == null) {
            renderEmpty(RescriptVariantFlowHints.Reason.NO_EDITOR)
            return
        }
        if (ctx.file.fileType != RescriptFileType && ctx.file.fileType != RescriptInterfaceFileType) {
            renderEmpty(RescriptVariantFlowHints.Reason.NOT_RESCRIPT)
            return
        }
        val diagram =
            ApplicationManager.getApplication().runReadAction<FlowDiagram?> {
                RescriptVariantFlowModel.buildAtOffset(ctx.source, ctx.offset)
            }
        currentDiagram = diagram
        if (diagram == null) {
            renderEmpty(RescriptVariantFlowHints.Reason.NO_SWITCH)
        } else {
            currentJumpTarget = JumpTarget(ctx.file, ctx.offset)
            val mermaid = RescriptVariantFlowMermaidExporter.toMermaid(diagram)
            currentMermaidSource = mermaid
            textArea.text = MermaidSourceColorizer.render(mermaid)
            textArea.caretPosition = 0
            graphView.setDiagram(diagram)
            statusLabel.text = " Arms: ${countArms(diagram)}"
        }
    }

    private fun countArms(diagram: FlowDiagram): Int {
        fun walk(nodes: List<FlowNode>): Int = nodes.sumOf { 1 + walk(it.children) }
        return walk(diagram.arms)
    }

    private fun renderEmpty(reason: RescriptVariantFlowHints.Reason) {
        val message = RescriptVariantFlowHints.emptyStateMessage(reason)
        currentMermaidSource = message
        // Empty-state messages are not Mermaid source, but rendering them
        // through the colorizer guarantees the HTML wrapper / escaping is
        // consistent with the highlighted code path.
        textArea.text = MermaidSourceColorizer.render(message)
        statusLabel.text = " ${RescriptVariantFlowHints.shortStatusLabel(reason)}"
        graphView.setDiagram(null)
        currentDiagram = null
        currentJumpTarget = null
    }

    private fun switchView(toVisual: Boolean) {
        visualMode = toVisual
        viewCards.show(viewSwitcher, if (toVisual) CARD_VISUAL else CARD_SOURCE)
    }

    /**
     * Snapshot of the active editor used to drive a refresh — file, full source
     * text, and caret offset captured at the moment the toolbar action fires.
     */
    private data class EditorContext(
        val file: VirtualFile,
        val source: String,
        val offset: Int,
    )

    /**
     * Editor location the `Jump to switch` toolbar action navigates to;
     * captured every time a diagram is successfully rebuilt so the user
     * can re-focus the source switch even after browsing other editors.
     */
    private data class JumpTarget(
        val file: VirtualFile,
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

    /**
     * Toolbar action that returns the caret to the `switch` expression
     * whose flow diagram is currently rendered. Disabled when no diagram
     * has been rendered yet (or the source file has been closed).
     */
    private inner class JumpToSwitchAction :
        AnAction(
            "Jump to Switch",
            "Move the caret to the switch expression for the rendered diagram",
            AllIcons.Actions.EditSource,
        ) {
        override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

        override fun update(e: AnActionEvent) {
            e.presentation.isEnabled = currentJumpTarget != null
        }

        override fun actionPerformed(e: AnActionEvent) {
            val target = currentJumpTarget ?: return
            OpenFileDescriptor(project, target.file, target.offset).navigate(true)
        }
    }

    /**
     * Toolbar action that exports the current diagram in the given [Format]
     * (Mermaid or DOT) and copies the result to the system clipboard. Disabled
     * when no diagram has been rendered yet.
     */
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

    /**
     * Toolbar toggle that flips the central panel into the visual
     * (Java2D) rendering of the variant flow diagram. Mutually
     * exclusive with [SourceModeAction] — exactly one is selected.
     */
    private inner class VisualModeAction :
        ToggleAction(
            "Visual",
            "Render the diagram as a top-down graph",
            AllIcons.Toolwindows.ToolWindowHierarchy,
        ) {
        override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

        override fun isSelected(e: AnActionEvent): Boolean = visualMode

        override fun setSelected(
            e: AnActionEvent,
            state: Boolean,
        ) {
            if (state) switchView(true)
        }
    }

    /**
     * Toolbar toggle that flips the central panel into the textual
     * Mermaid `flowchart TD` source for copy-paste workflows.
     */
    private inner class SourceModeAction :
        ToggleAction(
            "Source",
            "Show the Mermaid flowchart source text",
            AllIcons.FileTypes.Text,
        ) {
        override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

        override fun isSelected(e: AnActionEvent): Boolean = !visualMode

        override fun setSelected(
            e: AnActionEvent,
            state: Boolean,
        ) {
            if (state) switchView(false)
        }
    }

    private companion object {
        const val TOOLBAR_PLACE = "ReScriptVariantFlowToolbar"
        const val REFRESH_DEBOUNCE_MS = 200
        const val CARD_VISUAL = "visual"
        const val CARD_SOURCE = "source"
    }
}
