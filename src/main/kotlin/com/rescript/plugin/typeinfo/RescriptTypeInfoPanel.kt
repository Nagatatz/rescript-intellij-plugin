package com.rescript.plugin.typeinfo

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.ui.EditorTextField
import com.intellij.util.ui.JBUI
import com.rescript.plugin.RescriptFileType
import com.rescript.plugin.lsp.RescriptLspUtils
import com.rescript.plugin.ui.RescriptEditorCaretTracker
import com.rescript.plugin.util.EditorTextFieldFactory
import com.rescript.plugin.util.RescriptCoroutineDebouncer
import com.rescript.plugin.util.RescriptCoroutineScopeService
import com.rescript.plugin.util.RescriptFileUtil
import kotlinx.coroutines.Dispatchers
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * Panel that displays the inferred type of the expression at the current caret position.
 *
 * Listens to caret movements across all editors and queries the LSP server for
 * type information via [RescriptLspUtils.getHoverType]. Uses a 300ms debounce
 * to avoid excessive LSP requests during rapid cursor movement.
 *
 * @see RescriptTypeInfoToolWindowFactory for the tool window registration
 * @see RescriptLspUtils.getHoverType for LSP hover integration
 */
class RescriptTypeInfoPanel(
    private val project: Project,
    parentDisposable: Disposable,
) {
    private val mainPanel = JPanel(BorderLayout())

    /**
     * Read-only single-line editor field that displays the hover type for
     * the caret position. Using `EditorTextField(..., RescriptFileType,
     * isViewer = true, oneLineMode = true)` instead of a `JBLabel` gives
     * the type string the same syntax highlighting the editor uses,
     * matching the user's colour scheme automatically.
     */
    private val typeField: EditorTextField =
        EditorTextField(
            EditorFactory.getInstance().createDocument(NO_RESCRIPT_FILE),
            project,
            RescriptFileType,
            true,
            true,
        ).apply {
            border = JBUI.Borders.empty(8)
            EditorTextFieldFactory.applyPanelDefaults(this) { editor ->
                editor.settings.isCaretRowShown = false
            }
        }

    // Background debounce for LSP hover queries; Dispatchers.Default
    // matches the old Alarm(POOLED_THREAD) threading. Pending work dies
    // with the project scope, and earlier with parentDisposable below.
    private val debouncer =
        RescriptCoroutineDebouncer(
            project.service<RescriptCoroutineScopeService>().scope,
            DEBOUNCE_MS,
            Dispatchers.Default,
        )

    val component: JComponent
        get() = mainPanel

    init {
        mainPanel.add(typeField, BorderLayout.NORTH)

        // Drop any pending hover query when the tool window content goes
        // away before the project does.
        Disposer.register(parentDisposable) { debouncer.cancel() }

        // Listen for caret position changes in this project's editors.
        RescriptEditorCaretTracker.install(project, parentDisposable) { editor ->
            scheduleUpdate(editor)
        }

        // Listen for active file changes
        project.messageBus
            .connect(parentDisposable)
            .subscribe(
                FileEditorManagerListener.FILE_EDITOR_MANAGER,
                object : FileEditorManagerListener {
                    override fun selectionChanged(event: FileEditorManagerEvent) {
                        val editor =
                            event.newFile?.let { file ->
                                FileEditorManager.getInstance(project).selectedTextEditor
                            }
                        if (editor != null) {
                            scheduleUpdate(editor)
                        } else {
                            showMessage(NO_RESCRIPT_FILE)
                        }
                    }
                },
            )

        // Show type for currently active editor
        val currentEditor = FileEditorManager.getInstance(project).selectedTextEditor
        if (currentEditor != null) {
            scheduleUpdate(currentEditor)
        }
    }

    /**
     * Schedules a debounced type info update for the given editor.
     *
     * Captures the caret offset and file on the current thread (expected to be EDT)
     * before scheduling the LSP query on a pooled thread to avoid threading violations.
     *
     * @param editor the editor whose caret position changed
     */
    private fun scheduleUpdate(editor: Editor) {
        debouncer.cancel()

        // Capture offset and file on EDT before scheduling background work.
        // Caret model access requires EDT or read action — capturing here avoids
        // the threading violation that occurs when accessed from the pooled thread.
        val offset = ApplicationManager.getApplication().runReadAction<Int> { editor.caretModel.offset }
        val file = FileDocumentManager.getInstance().getFile(editor.document)
        if (file == null || !RescriptFileUtil.isRescriptFile(file)) {
            showMessage(NO_RESCRIPT_FILE)
            return
        }

        debouncer.schedule {
            val typeText = RescriptLspUtils.getHoverType(project, file, offset)
            showMessage(typeText ?: NO_TYPE)
        }
    }

    private fun showMessage(text: String) {
        ApplicationManager.getApplication().invokeLater({
            typeField.text = text
        }, ModalityState.any())
    }

    companion object {
        private const val DEBOUNCE_MS = 300L
        private const val NO_RESCRIPT_FILE = "No ReScript file selected"
        private const val NO_TYPE = "No type information"
    }
}
