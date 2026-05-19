package com.rescript.plugin.typeinfo

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.ui.EditorTextField
import com.intellij.util.Alarm
import com.intellij.util.ui.JBUI
import com.rescript.plugin.RescriptFileType
import com.rescript.plugin.lsp.RescriptLspUtils
import com.rescript.plugin.util.RescriptFileUtil
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
            addSettingsProvider { editor ->
                editor.settings.isLineNumbersShown = false
                editor.settings.isFoldingOutlineShown = false
                editor.settings.isRightMarginShown = false
                editor.settings.isCaretRowShown = false
            }
        }

    // UnstableApiUsage: Alarm(ThreadToUse.POOLED_THREAD).
    // Reviewed 2026-05-19 against IntelliJ Platform 2026.1.2. The enum
    // value is annotated @ApiStatus.Internal, but the simple "cancel +
    // re-schedule" debounce pattern used here has no straightforward
    // replacement in 2026.1.x; migrating to coroutines would force a
    // different lifecycle model (CoroutineScope tied to the parent
    // Disposable) and is out of scope for this maintenance pass.
    // Re-evaluate on the next major platform bump.
    @Suppress("UnstableApiUsage")
    private val alarm = Alarm(Alarm.ThreadToUse.POOLED_THREAD, parentDisposable)

    val component: JComponent
        get() = mainPanel

    init {
        mainPanel.add(typeField, BorderLayout.NORTH)

        // Listen for caret position changes across all editors
        val caretListener =
            object : CaretListener {
                override fun caretPositionChanged(event: CaretEvent) {
                    scheduleUpdate(event.editor)
                }
            }
        EditorFactory.getInstance().eventMulticaster.addCaretListener(caretListener, parentDisposable)

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
        alarm.cancelAllRequests()

        // Capture offset and file on EDT before scheduling background work.
        // Caret model access requires EDT or read action — capturing here avoids
        // the threading violation that occurs when accessed from the pooled thread.
        val offset = ApplicationManager.getApplication().runReadAction<Int> { editor.caretModel.offset }
        val file = FileDocumentManager.getInstance().getFile(editor.document)
        if (file == null || !RescriptFileUtil.isRescriptFile(file)) {
            showMessage(NO_RESCRIPT_FILE)
            return
        }

        alarm.addRequest({
            val typeText = RescriptLspUtils.getHoverType(project, file, offset)
            showMessage(typeText ?: NO_TYPE)
        }, DEBOUNCE_MS)
    }

    private fun showMessage(text: String) {
        ApplicationManager.getApplication().invokeLater({
            typeField.text = text
        }, ModalityState.any())
    }

    companion object {
        private const val DEBOUNCE_MS = 300
        private const val NO_RESCRIPT_FILE = "No ReScript file selected"
        private const val NO_TYPE = "No type information"
    }
}
