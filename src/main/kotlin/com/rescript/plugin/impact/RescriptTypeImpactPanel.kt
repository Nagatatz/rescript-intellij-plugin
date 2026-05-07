package com.rescript.plugin.impact

import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.psi.PsiManager
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.Alarm
import com.rescript.plugin.RescriptFileType
import com.rescript.plugin.RescriptInterfaceFileType
import java.awt.BorderLayout
import java.awt.Component
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.DefaultListModel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.ListCellRenderer

/**
 * Tool window UI for the type impact preview.
 *
 * Listens to caret motion in any open ReScript editor, resolves the
 * type declaration under the caret, and renders a list of references
 * with their classification kind. Double-click navigates to the
 * reference site.
 *
 * @see RescriptTypeImpactToolWindowFactory which creates instances of this panel
 */
class RescriptTypeImpactPanel(
    private val project: Project,
) : SimpleToolWindowPanel(true, true),
    Disposable {
    private val listModel = DefaultListModel<ReferenceEntry>()
    private val list: JBList<ReferenceEntry> =
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

    private val statusLabel: JBLabel = JBLabel(" ")

    private val refreshAlarm: Alarm = Alarm(Alarm.ThreadToUse.SWING_THREAD, this)

    init {
        val centerPanel =
            JPanel(BorderLayout()).apply {
                add(JBScrollPane(list), BorderLayout.CENTER)
                add(statusLabel, BorderLayout.SOUTH)
            }
        setContent(centerPanel)
        setToolbar(buildToolbar())
        attachEditorListeners()
        scheduleRefresh()
    }

    override fun dispose() {
        // Alarm is a child disposable of this panel; editor factory
        // listeners are bound to the same parent disposable.
    }

    private fun buildToolbar(): javax.swing.JComponent {
        val group =
            DefaultActionGroup().apply {
                add(RefreshAction())
            }
        val toolbar = ActionManager.getInstance().createActionToolbar(TOOLBAR_PLACE, group, true)
        toolbar.targetComponent = this
        return toolbar.component
    }

    private fun attachEditorListeners() {
        val editorFactory =
            com.intellij.openapi.editor.EditorFactory
                .getInstance()
        for (editor in editorFactory.allEditors) attachCaretListener(editor)
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
        val fileEditor = FileEditorManager.getInstance(project).selectedEditor as? TextEditor
        if (fileEditor == null) {
            renderEmpty("Open a ReScript file to see type impact.")
            return
        }
        val virtualFile = fileEditor.file
        if (virtualFile.fileType != RescriptFileType && virtualFile.fileType != RescriptInterfaceFileType) {
            renderEmpty("Open a ReScript file to see type impact.")
            return
        }
        val offset = fileEditor.editor.caretModel.offset

        val target =
            ApplicationManager.getApplication().runReadAction<TypeTarget?> {
                val psiFile = PsiManager.getInstance(project).findFile(virtualFile)
                if (psiFile == null) null else RescriptTypeTargetResolver.resolveAt(psiFile, offset)
            }
        if (target == null) {
            renderEmpty("No type declaration under caret.")
            return
        }
        val result = RescriptTypeReferenceFinder.findReferences(project, target)
        listModel.clear()
        for (entry in result.entries) listModel.addElement(entry)
        val truncationNote =
            if (result.truncated) {
                " (showing first ${RescriptTypeReferenceFinder.MAX_REFERENCES})"
            } else {
                ""
            }
        statusLabel.text = " ${target.name}: ${result.entries.size} reference(s)$truncationNote"
    }

    private fun renderEmpty(message: String) {
        listModel.clear()
        statusLabel.text = " $message"
    }

    private inner class RefreshAction :
        AnAction(
            "Refresh",
            "Re-scan references for the type under the caret",
            AllIcons.Actions.Refresh,
        ) {
        override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

        override fun actionPerformed(e: AnActionEvent) {
            scheduleRefresh()
        }
    }

    /**
     * List cell renderer for [ReferenceEntry] rows. Layout:
     * `[KIND] file:line  preview…`
     */
    private class EntryRenderer : ListCellRenderer<ReferenceEntry> {
        private val delegate =
            com.intellij.ui.components
                .JBLabel()

        override fun getListCellRendererComponent(
            list: JList<out ReferenceEntry>,
            value: ReferenceEntry?,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean,
        ): Component {
            value ?: return delegate.also { it.text = "" }
            val kind = value.kind.name.lowercase()
            val name = value.file.name
            delegate.text = "[$kind] $name:${value.lineNumber}  ${value.previewLine}"
            delegate.background = if (isSelected) list.selectionBackground else list.background
            delegate.foreground = if (isSelected) list.selectionForeground else list.foreground
            delegate.isOpaque = true
            return delegate
        }
    }

    private companion object {
        const val TOOLBAR_PLACE = "ReScriptTypeImpactToolbar"
        const val REFRESH_DEBOUNCE_MS = 200
    }
}
