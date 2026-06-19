package com.rescript.plugin.lsp

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.platform.lsp.api.LspServerManager
import com.rescript.plugin.run.RescriptCliDetector
import com.rescript.plugin.settings.RescriptProjectSettings

/**
 * Action to dump diagnostic information about the ReScript LSP server.
 *
 * Collects and displays the LSP server status, detected paths, and
 * project settings as a notification in the Event Log. Useful for
 * troubleshooting LSP connectivity issues.
 *
 * Available via Tools menu.
 *
 * @see RescriptLspServerSupportProvider
 * @see RescriptRestartLspAction
 */
class RescriptDumpLspStateAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val state = collectLspState(project)

        NotificationGroupManager
            .getInstance()
            .getNotificationGroup("ReScript")
            .createNotification("ReScript LSP Diagnostics", state, NotificationType.INFORMATION)
            .notify(project)
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    // LspServerManager / LspServer are deprecated in 2026.2 EAP; the replacement
    // LspClientDescriptor API does not exist on the 2026.1.2 compile target.

    /**
     * Collects diagnostic information about the LSP server and project configuration.
     *
     * @param project the current project
     * Uses unstable `LspServerManager` API — review on platform upgrade.
     *
     * @return formatted diagnostic string
     */
    @Suppress("UnstableApiUsage", "DEPRECATION")
    internal fun collectLspState(project: Project): String {
        val settings = RescriptProjectSettings.getInstance(project)
        val basePath = project.basePath

        // LSP server status
        val servers =
            LspServerManager
                .getInstance(project)
                .getServersForProvider(RescriptLspServerSupportProvider::class.java)
        val serverStatus =
            if (servers.isEmpty()) {
                "Not running"
            } else {
                servers.joinToString(", ") { it.state.name }
            }

        // Detection info
        val isRescriptProject = RescriptLspDetector.isRescriptProject(basePath)
        val isLspAvailable = RescriptLspDetector.isLspAvailable(basePath)
        val cliPath = RescriptCliDetector.findCli(basePath, basePath)

        return buildString {
            appendLine("**LSP Server**")
            appendLine("- Status: $serverStatus")
            appendLine("- Server count: ${servers.size}")
            appendLine()
            appendLine("**Project Detection**")
            appendLine("- Base path: ${basePath ?: "(none)"}")
            appendLine("- Is ReScript project: $isRescriptProject")
            appendLine("- LSP available in node_modules: $isLspAvailable")
            appendLine("- CLI path: ${cliPath ?: "(not found)"}")
            appendLine()
            appendLine("**Settings**")
            appendLine("- Custom LSP path: ${settings.lspServerPath.ifEmpty { "(auto-detect)" }}")
            appendLine("- Custom Node path: ${settings.nodePath.ifEmpty { "(auto-detect)" }}")
            appendLine("- Incremental typechecking: ${settings.incrementalTypecheckingEnabled}")
            appendLine("- Signature help: ${settings.signatureHelpEnabled}")
            appendLine("- Inlay hints: ${settings.inlayHintsEnabled}")
            appendLine("- Compile status: ${settings.compileStatusEnabled}")
            appendLine("- Log level: ${settings.logLevel}")
        }
    }
}
