package com.rescript.plugin.editor

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotificationProvider
import com.intellij.ui.EditorNotifications
import com.rescript.plugin.settings.RescriptProjectSettings
import java.nio.file.Files
import java.nio.file.Path
import java.util.function.Function
import javax.swing.JComponent

/**
 * Shows an editor notification bar when the ReScript Language Server is not found.
 *
 * Appears at the top of `.res`/`.resi` files when `@rescript/language-server` is neither
 * installed in `node_modules` nor configured in plugin settings. Provides "Configure..."
 * and "Dismiss" actions.
 */
class RescriptEditorNotificationProvider :
    EditorNotificationProvider,
    DumbAware {
    override fun collectNotificationData(
        project: Project,
        file: VirtualFile,
    ): Function<in FileEditor, out JComponent?> {
        return Function { _ ->
            if (!isRescriptFile(file)) return@Function null
            if (isDismissed(project)) return@Function null
            if (isLspConfigured(project)) return@Function null
            if (isLspAvailable(project)) return@Function null

            createPanel(project)
        }
    }

    private fun isRescriptFile(file: VirtualFile): Boolean = file.extension == "res" || file.extension == "resi"

    private fun isDismissed(project: Project): Boolean =
        PropertiesComponent.getInstance(project).getBoolean(DISMISSED_KEY, false)

    private fun isLspConfigured(project: Project): Boolean {
        val settings = RescriptProjectSettings.getInstance(project)
        return settings.lspServerPath.isNotEmpty()
    }

    private fun isLspAvailable(project: Project): Boolean {
        val basePath = project.basePath ?: return false
        val lspDir = Path.of(basePath, "node_modules", "@rescript", "language-server")
        return Files.isDirectory(lspDir)
    }

    private fun createPanel(project: Project): EditorNotificationPanel {
        val panel = EditorNotificationPanel(EditorNotificationPanel.Status.Warning)
        panel.text = "ReScript Language Server not found. Install @rescript/language-server for full IDE support."

        panel.createActionLabel("Configure...") {
            ShowSettingsUtil.getInstance().showSettingsDialog(project, "ReScript")
        }

        panel.createActionLabel("Dismiss") {
            PropertiesComponent.getInstance(project).setValue(DISMISSED_KEY, true)
            EditorNotifications.getInstance(project).updateAllNotifications()
        }

        return panel
    }

    companion object {
        private const val DISMISSED_KEY = "rescript.notification.lsp.dismissed"
    }
}
