package com.rescript.plugin.lsp

import com.intellij.ide.util.PropertiesComponent
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

/**
 * Shows a balloon notification at project startup when `@rescript/language-server` is not installed.
 *
 * Checks whether the project is a ReScript project (has `rescript.json` or `bsconfig.json`),
 * whether a custom LSP path is configured, and whether the language server is already available
 * in `node_modules`. If all conditions indicate the LSP is missing, displays a balloon with
 * "Install with {pm}" and "Configure..." actions.
 *
 * Uses [PropertiesComponent] for session-scoped dismiss management.
 *
 * @see RescriptLspDetector
 * @see RescriptLspInstaller
 * @see RescriptPackageManagerDetector
 */
class RescriptLspStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        // Skip if already dismissed this session
        if (PropertiesComponent.getInstance(project).getBoolean(DISMISSED_KEY, false)) return

        val basePath = project.basePath

        // Only show for ReScript projects
        if (!RescriptLspDetector.isRescriptProject(basePath)) return

        // Skip if custom LSP path is configured
        if (RescriptLspDetector.isLspConfigured(project)) return

        // Skip if LSP is already available
        if (RescriptLspDetector.isLspAvailable(basePath)) return

        showInstallNotification(project)
    }

    private fun showInstallNotification(project: Project) {
        val detection = RescriptPackageManagerDetector.detect(project.basePath)

        val notification =
            NotificationGroupManager
                .getInstance()
                .getNotificationGroup("ReScript")
                .createNotification(
                    "ReScript Language Server not found",
                    "Install @rescript/language-server for code completion, diagnostics, and navigation.",
                    NotificationType.WARNING,
                )

        notification.addAction(
            NotificationAction.createSimpleExpiring("Install with ${detection.packageManager.displayName}") {
                RescriptLspInstaller.install(
                    project,
                    detection.workingDirectory,
                    detection.packageManager,
                )
            },
        )

        notification.addAction(
            NotificationAction.createSimpleExpiring("Configure...") {
                ShowSettingsUtil.getInstance().showSettingsDialog(project, "ReScript")
            },
        )

        notification.addAction(
            NotificationAction.createSimpleExpiring("Don\u2019t show again") {
                PropertiesComponent.getInstance(project).setValue(DISMISSED_KEY, true)
            },
        )

        notification.notify(project)
    }

    companion object {
        private const val DISMISSED_KEY = "rescript.startup.lsp.install.dismissed"
    }
}
