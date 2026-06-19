package com.rescript.plugin.lsp

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.platform.lsp.api.LspServerManager

/**
 * Action to restart the ReScript Language Server.
 *
 * Available via Tools menu. Stops the current LSP server instance
 * and starts a new one, picking up any configuration changes.
 *
 * @see RescriptLspServerSupportProvider
 */
class RescriptRestartLspAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        // LspServerManager is deprecated in 2026.2 EAP; the replacement LspClientDescriptor
        // API does not exist on the 2026.1.2 compile target. UnstableApiUsage kept too.
        @Suppress("UnstableApiUsage", "DEPRECATION")
        LspServerManager
            .getInstance(project)
            .stopAndRestartIfNeeded(RescriptLspServerSupportProvider::class.java)
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}
