package com.rescript.plugin.impact

import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.rescript.plugin.RescriptFileType
import com.rescript.plugin.RescriptInterfaceFileType
import com.rescript.plugin.ui.RescriptEditorCaretTracker
import com.rescript.plugin.ui.RescriptToolWindowPanelBase
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.DefaultListModel
import javax.swing.JList

/**
 * Tool window UI for the type impact preview.
 *
 * Listens to caret motion in any open ReScript editor, resolves the
 * type declaration under the caret, and renders a list of references
 * with their classification kind. Double-click navigates to the
 * reference site.
 *
 * @see RescriptTypeImpactToolWindowFactory which creates instances of this panel
 * @see RescriptToolWindowPanelBase for the shared toolbar / status / refresh scaffold
 */
class RescriptTypeImpactPanel(
    private val project: Project,
) : RescriptToolWindowPanelBase(project, TOOLBAR_PLACE, REFRESH_DEBOUNCE_MS) {
    private val listModel = DefaultListModel<ReferenceEntry>()

    // Incremented on every refresh; pooled results whose generation is stale
    // (a newer caret-driven refresh has started) are discarded on the EDT.
    @Volatile private var refreshGeneration = 0

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

    init {
        installUi(
            JBScrollPane(list),
            DefaultActionGroup().apply {
                add(createRefreshAction("Re-scan references for the type under the caret"))
            },
        )
        RescriptEditorCaretTracker.install(project, this) { _ -> scheduleRefresh() }
        scheduleRefresh()
    }

    override fun doRefresh() {
        // EDT: lightweight prechecks and caret capture (caretModel.offset is EDT-only).
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
        val generation = ++refreshGeneration

        // Pooled thread: target resolution (read action) + word-index reference
        // search must not run on the EDT (SlowOperations / ThreadingAssertions).
        ApplicationManager.getApplication().executeOnPooledThread {
            val target =
                ApplicationManager.getApplication().runReadAction<TypeTarget?> {
                    val psiFile = PsiManager.getInstance(project).findFile(virtualFile)
                    if (psiFile == null) null else RescriptTypeTargetResolver.resolveAt(psiFile, offset)
                }
            val result = target?.let { RescriptTypeReferenceFinder.findReferences(project, it) }
            ApplicationManager.getApplication().invokeLater {
                // Discard stale results superseded by a newer caret-driven refresh.
                if (generation != refreshGeneration) return@invokeLater
                if (target == null || result == null) {
                    renderEmpty("No type declaration under caret.")
                    return@invokeLater
                }
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
        }
    }

    private fun renderEmpty(message: String) {
        listModel.clear()
        statusLabel.text = " $message"
    }

    /**
     * List cell renderer for [ReferenceEntry] rows. Layout:
     * `[kind] file:line  preview…`
     *
     * The `[kind]` prefix is rendered in bold using [colorForKind] so
     * the kind is scannable by colour. The rest of the row uses the
     * standard list foreground / GRAY palette.
     */
    private class EntryRenderer : ColoredListCellRenderer<ReferenceEntry>() {
        override fun customizeCellRenderer(
            list: JList<out ReferenceEntry>,
            value: ReferenceEntry?,
            index: Int,
            selected: Boolean,
            hasFocus: Boolean,
        ) {
            if (value == null) return
            val kindLabel = value.kind.name.lowercase()
            append(
                "[$kindLabel] ",
                SimpleTextAttributes(SimpleTextAttributes.STYLE_BOLD, colorForKind(value.kind)),
            )
            append(
                "${value.file.name}:${value.lineNumber}  ",
                SimpleTextAttributes.REGULAR_ATTRIBUTES,
            )
            append(value.previewLine, SimpleTextAttributes.GRAYED_ATTRIBUTES)
        }
    }

    private companion object {
        const val TOOLBAR_PLACE = "ReScriptTypeImpactToolbar"
        const val REFRESH_DEBOUNCE_MS = 200
    }
}
