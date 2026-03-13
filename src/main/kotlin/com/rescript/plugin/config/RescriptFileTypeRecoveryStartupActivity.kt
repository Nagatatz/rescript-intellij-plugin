package com.rescript.plugin.config

import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.rescript.plugin.RescriptFileType
import com.rescript.plugin.RescriptInterfaceFileType
import com.rescript.plugin.lsp.RescriptLspDetector

/**
 * Checks file type associations for `.res` and `.resi` at project startup and offers
 * to restore them if they have been accidentally removed or overridden.
 *
 * This handles the case where a user inadvertently disassociates the ReScript file
 * extensions in Settings > Editor > File Types, causing loss of icons and syntax highlighting.
 *
 * @see RescriptFileType
 * @see RescriptInterfaceFileType
 */
class RescriptFileTypeRecoveryStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        // Only check for ReScript projects
        if (!RescriptLspDetector.isRescriptProject(project.basePath)) return

        val fileTypeManager = FileTypeManager.getInstance()
        val missingExtensions = mutableListOf<Pair<String, com.intellij.openapi.fileTypes.LanguageFileType>>()

        if (fileTypeManager.getFileTypeByExtension("res") !== RescriptFileType) {
            missingExtensions.add("res" to RescriptFileType)
        }
        if (fileTypeManager.getFileTypeByExtension("resi") !== RescriptInterfaceFileType) {
            missingExtensions.add("resi" to RescriptInterfaceFileType)
        }

        if (missingExtensions.isEmpty()) return

        val extensionNames = missingExtensions.joinToString(", ") { ".${it.first}" }

        val notification =
            NotificationGroupManager
                .getInstance()
                .getNotificationGroup("ReScript")
                .createNotification(
                    "ReScript file type association missing",
                    "$extensionNames file extension is not associated with ReScript. Icons and syntax highlighting may not work.",
                    NotificationType.WARNING,
                )

        notification.addAction(
            NotificationAction.createSimpleExpiring("Fix file associations") {
                fixFileAssociations(missingExtensions)
            },
        )

        notification.addAction(
            NotificationAction.createSimpleExpiring("Dismiss") {},
        )

        notification.notify(project)
    }

    private fun fixFileAssociations(extensions: List<Pair<String, com.intellij.openapi.fileTypes.LanguageFileType>>) {
        val fileTypeManager = FileTypeManager.getInstance()
        for ((extension, fileType) in extensions) {
            fileTypeManager.associateExtension(fileType, extension)
        }
    }
}
