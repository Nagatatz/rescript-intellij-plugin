package com.rescript.plugin.lsp

import com.intellij.openapi.project.Project
import com.intellij.platform.lsp.api.Lsp4jClient
import com.intellij.platform.lsp.api.LspServerNotificationsHandler
import org.eclipse.lsp4j.jsonrpc.services.JsonNotification

/**
 * Custom LSP client that handles ReScript-specific notifications
 * from the language server.
 *
 * Receives `rescript/compilationStatus` notifications and updates
 * [RescriptCompilationStatusService] accordingly.
 */
class RescriptLsp4jClient(
    handler: LspServerNotificationsHandler,
    private val project: Project,
) : Lsp4jClient(handler) {
    @Suppress("unused") // Called by LSP4J via @JsonNotification reflection
    @JsonNotification("rescript/compilationStatus")
    fun compilationStatus(params: CompilationStatusParams) {
        if (project.isDisposed) return
        val service = RescriptCompilationStatusService.getInstance(project)
        service.updateStatus(
            RescriptCompilationStatusService.CompilationStatus(
                status = params.status,
                errorCount = params.errorCount,
                warningCount = params.warningCount,
            ),
        )
    }

    data class CompilationStatusParams(
        val project: String = "",
        val projectRootPath: String = "",
        val status: String = "unknown",
        val errorCount: Int = 0,
        val warningCount: Int = 0,
    )
}
