package com.rescript.plugin.notebook

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorLocation
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.fileEditor.FileEditorStateLevel
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import java.beans.PropertyChangeListener
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

/**
 * Custom [FileEditor] for `.resnb` notebook files. Wraps a
 * [RescriptNotebookPanel] so the cell-based UI replaces the default
 * JSON text view, and persists changes back through the standard
 * IntelliJ document save lifecycle.
 *
 * @see RescriptNotebookFileEditorProvider
 */
class RescriptNotebookFileEditor(
    private val project: Project,
    private val virtualFile: VirtualFile,
) : UserDataHolderBase(),
    FileEditor {
    private val log = logger<RescriptNotebookFileEditor>()

    private var modified = false

    private val panel: RescriptNotebookPanel

    private val rootComponent: JComponent

    init {
        val (initial, fallbackMessage) = loadInitialDocument()
        panel =
            RescriptNotebookPanel(
                project = project,
                initialDocument = initial,
                onModified = { onCellModified() },
            )
        rootComponent =
            if (fallbackMessage != null) {
                JPanel().apply {
                    add(JLabel("<html>Failed to parse notebook JSON: $fallbackMessage<br>Loaded as empty.</html>"))
                    add(panel)
                }
            } else {
                panel
            }
    }

    private fun loadInitialDocument(): Pair<NotebookDocument, String?> {
        val text =
            try {
                String(virtualFile.contentsToByteArray(), virtualFile.charset)
            } catch (e: Exception) {
                log.warn("Failed to read notebook file ${virtualFile.path}: ${e.message}")
                return NotebookDocument.empty() to e.message
            }
        return try {
            RescriptNotebookSerializer.fromJson(text) to null
        } catch (e: IllegalStateException) {
            log.warn("Failed to parse notebook JSON for ${virtualFile.path}: ${e.message}")
            NotebookDocument.empty() to e.message
        }
    }

    private fun onCellModified() {
        modified = true
        // Push the latest snapshot back to the in-memory document so
        // FileDocumentManager will pick it up on save.
        ApplicationManager.getApplication().invokeLater {
            val document =
                FileDocumentManager.getInstance().getDocument(virtualFile) ?: return@invokeLater
            val text = RescriptNotebookSerializer.toJson(panel.snapshot())
            ApplicationManager.getApplication().runWriteAction {
                document.setText(text)
            }
        }
    }

    override fun getComponent(): JComponent = rootComponent

    override fun getPreferredFocusedComponent(): JComponent? = panel

    override fun getName(): String = "ReScript Notebook"

    override fun setState(state: FileEditorState) {}

    override fun isModified(): Boolean = modified

    override fun isValid(): Boolean = virtualFile.isValid

    override fun addPropertyChangeListener(listener: PropertyChangeListener) {}

    override fun removePropertyChangeListener(listener: PropertyChangeListener) {}

    override fun getCurrentLocation(): FileEditorLocation? = null

    override fun getFile(): VirtualFile = virtualFile

    override fun dispose() {
        // Nothing to release: the panel and its children are reachable
        // only through this editor and become eligible for GC when the
        // editor is closed.
    }

    /** Marker state object — Notebook editor has no per-tab state to persist yet. */
    private object EmptyState : FileEditorState {
        override fun canBeMergedWith(
            otherState: FileEditorState,
            level: FileEditorStateLevel,
        ): Boolean = otherState === this
    }
}
