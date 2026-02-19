package com.rescript.plugin.errorlens

import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import com.intellij.openapi.util.Disposer
import com.rescript.plugin.RescriptFileType
import com.rescript.plugin.RescriptInterfaceFileType
import com.rescript.plugin.settings.RescriptProjectSettings

/**
 * Listens for editor creation and release events to install and uninstall
 * [RescriptErrorLensManager] instances for ReScript files.
 *
 * Registered as an `editorFactoryListener` extension point so that every
 * new editor for `.res` or `.resi` files automatically gets Error Lens
 * inline diagnostic annotations when the feature is enabled.
 *
 * @see RescriptErrorLensManager for the per-editor inlay management
 */
class RescriptErrorLensEditorListener : EditorFactoryListener {
    /** Tracks active managers keyed by editor, for cleanup on editor release. */
    private val managers = mutableMapOf<Editor, Pair<RescriptErrorLensManager, Disposable>>()

    override fun editorCreated(event: EditorFactoryEvent) {
        val editor = event.editor
        val project = editor.project ?: return

        // Check if this editor is for a ReScript file
        val virtualFile = editor.virtualFile ?: return
        val fileType = virtualFile.fileType
        if (fileType != RescriptFileType && fileType != RescriptInterfaceFileType) return

        // Check if Error Lens is enabled
        val settings = RescriptProjectSettings.getInstance(project)
        if (!settings.errorLensEnabled) return

        // Create a disposable parent that will be disposed when the editor is released
        val disposable = Disposer.newDisposable("RescriptErrorLens:${virtualFile.name}")

        val manager = RescriptErrorLensManager(editor, disposable)
        managers[editor] = Pair(manager, disposable)
        manager.install()
    }

    override fun editorReleased(event: EditorFactoryEvent) {
        val editor = event.editor
        val pair = managers.remove(editor) ?: return
        val (manager, disposable) = pair

        manager.dispose()
        Disposer.dispose(disposable)
    }
}
