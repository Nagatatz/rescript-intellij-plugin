package com.rescript.plugin.preview

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import com.rescript.plugin.lsp.RescriptCompilationStatusService
import com.rescript.plugin.navigation.RescriptOpenCompiledJsAction
import com.rescript.plugin.util.RescriptFileUtil
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.SwingConstants

/**
 * Panel that displays the compiled JavaScript output for the active ReScript file.
 *
 * Renders the JS content in a read-only [EditorEx] with syntax highlighting.
 * Automatically updates when the active editor file changes or when compilation
 * succeeds (via [RescriptCompilationStatusService] listener). Provides toolbar
 * actions to refresh the preview and open the JS file in a full editor tab.
 *
 * @see RescriptCompiledJsPreviewToolWindowFactory for the tool window registration
 * @see RescriptOpenCompiledJsAction for locating the compiled JS file
 */
class RescriptCompiledJsPreviewPanel(
    private val project: Project,
    parentDisposable: Disposable,
) {
    private val mainPanel = JPanel(BorderLayout())
    private val statusLabel = JBLabel("No ReScript file selected", SwingConstants.CENTER)
    private var currentEditor: EditorEx? = null
    private var currentJsFile: VirtualFile? = null

    val component: JComponent
        get() = mainPanel

    init {
        mainPanel.add(statusLabel, BorderLayout.CENTER)

        // Create toolbar
        val actionGroup = DefaultActionGroup()
        actionGroup.add(RefreshAction())
        actionGroup.add(OpenInEditorAction())
        val toolbar =
            ActionManager.getInstance().createActionToolbar(
                "RescriptJsPreview",
                actionGroup,
                true,
            )
        toolbar.targetComponent = mainPanel
        mainPanel.add(toolbar.component, BorderLayout.NORTH)

        // Listen for active file changes
        project.messageBus
            .connect(parentDisposable)
            .subscribe(
                FileEditorManagerListener.FILE_EDITOR_MANAGER,
                object : FileEditorManagerListener {
                    override fun selectionChanged(event: FileEditorManagerEvent) {
                        val file = event.newFile
                        if (file != null && RescriptFileUtil.isRescriptFile(file)) {
                            updatePreview(file)
                        }
                    }
                },
            )

        // Listen for compilation status changes
        RescriptCompilationStatusService.getInstance(project).addListener(parentDisposable) { status ->
            if (status.status == "success") {
                refreshCurrentPreview()
            }
        }

        // Register for disposal
        Disposer.register(parentDisposable) { disposeEditor() }

        // Show preview for currently active file
        val selectedFile = FileEditorManager.getInstance(project).selectedFiles.firstOrNull()
        if (selectedFile != null && RescriptFileUtil.isRescriptFile(selectedFile)) {
            updatePreview(selectedFile)
        }
    }

    private fun updatePreview(resFile: VirtualFile) {
        val jsFile = RescriptOpenCompiledJsAction.findCompiledJsFile(project, resFile)
        if (jsFile == null) {
            showMessage("Compiled JS not found for ${resFile.name}. Build your project first.")
            return
        }

        currentJsFile = jsFile
        showEditor(jsFile)
    }

    private fun refreshCurrentPreview() {
        val selectedFile = FileEditorManager.getInstance(project).selectedFiles.firstOrNull()
        if (selectedFile != null && RescriptFileUtil.isRescriptFile(selectedFile)) {
            updatePreview(selectedFile)
        }
    }

    private fun showMessage(text: String) {
        disposeEditor()
        statusLabel.text = text
        statusLabel.isVisible = true
        mainPanel.revalidate()
        mainPanel.repaint()
    }

    private fun showEditor(jsFile: VirtualFile) {
        disposeEditor()
        statusLabel.isVisible = false

        val content = String(jsFile.contentsToByteArray(), Charsets.UTF_8)
        val document =
            EditorFactory.getInstance().createDocument(content).apply {
                setReadOnly(true)
            }

        val jsFileType = FileTypeManager.getInstance().getFileTypeByExtension("js")
        val editor =
            EditorFactory.getInstance().createEditor(
                document,
                project,
                jsFileType,
                true, // isViewer
            ) as EditorEx

        editor.settings.apply {
            isLineNumbersShown = true
            isFoldingOutlineShown = true
            isWhitespacesShown = false
            additionalLinesCount = 0
        }

        editor.setBorder(JBUI.Borders.empty())
        currentEditor = editor

        mainPanel.add(editor.component, BorderLayout.CENTER)
        mainPanel.revalidate()
        mainPanel.repaint()
    }

    private fun disposeEditor() {
        currentEditor?.let { editor ->
            mainPanel.remove(editor.component)
            EditorFactory.getInstance().releaseEditor(editor)
            currentEditor = null
        }
    }

    /** Toolbar action that refreshes the compiled JS preview for the active ReScript file. */
    private inner class RefreshAction :
        AnAction("Refresh", "Refresh compiled JS preview", com.intellij.icons.AllIcons.Actions.Refresh) {
        override fun actionPerformed(e: AnActionEvent) {
            refreshCurrentPreview()
        }
    }

    /** Toolbar action that opens the compiled JS file in a full editor tab. */
    private inner class OpenInEditorAction :
        AnAction(
            "Open in Editor",
            "Open compiled JS file in editor",
            com.intellij.icons.AllIcons.Actions.OpenNewTab,
        ) {
        override fun actionPerformed(e: AnActionEvent) {
            val jsFile = currentJsFile ?: return
            FileEditorManager.getInstance(project).openFile(jsFile, true)
        }

        override fun getActionUpdateThread() = ActionUpdateThread.BGT

        override fun update(e: AnActionEvent) {
            e.presentation.isEnabled = currentJsFile != null
        }
    }
}
