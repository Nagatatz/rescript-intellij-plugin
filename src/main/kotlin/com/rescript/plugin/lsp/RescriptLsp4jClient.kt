package com.rescript.plugin.lsp

import com.intellij.openapi.project.Project
import com.intellij.platform.lsp.api.Lsp4jClient
import com.intellij.platform.lsp.api.LspServerNotificationsHandler
import org.eclipse.lsp4j.jsonrpc.services.JsonNotification

/**
 * Custom LSP client that handles ReScript-specific notifications
 * from the language server.
 *
 * Receives `rescript/compilationStatus` and `rescript/compilationFinished`
 * notifications and updates [RescriptCompilationStatusService] accordingly.
 */
class RescriptLsp4jClient(
    handler: LspServerNotificationsHandler,
    private val project: Project,
) : Lsp4jClient(handler) {
    @Suppress("unused") // Called by LSP4J via @JsonNotification reflection
    @JsonNotification("rescript/compilationStatus")
    fun compilationStatus(params: CompilationStatusParams?) {
        if (project.isDisposed || params == null) return
        val service = RescriptCompilationStatusService.getInstance(project)
        service.updateStatus(
            RescriptCompilationStatusService.CompilationStatus(
                status = params.status,
                errorCount = params.errorCount,
                warningCount = params.warningCount,
            ),
        )
    }

    @Suppress("unused") // Called by LSP4J via @JsonNotification reflection
    @JsonNotification("rescript/compilationFinished")
    fun compilationFinished(params: CompilationFinishedParams?) {
        if (project.isDisposed || params == null) return
        val service = RescriptCompilationStatusService.getInstance(project)
        service.notifyCompilationFinished(params)
    }

    /** Parameters for the `rescript/compilationStatus` LSP notification. */
    data class CompilationStatusParams(
        val project: String = "",
        val projectRootPath: String = "",
        val status: String = "unknown",
        val errorCount: Int = 0,
        val warningCount: Int = 0,
    )

    /** Parameters for the `rescript/compilationFinished` LSP notification. */
    data class CompilationFinishedParams(
        val project: String = "",
        val projectRootPath: String = "",
    )
}
