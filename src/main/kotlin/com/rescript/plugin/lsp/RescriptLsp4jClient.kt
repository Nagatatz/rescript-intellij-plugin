package com.rescript.plugin.lsp

import com.intellij.openapi.project.Project
import com.intellij.platform.lsp.api.Lsp4jClient
import com.intellij.platform.lsp.api.LspServerNotificationsHandler
import org.eclipse.lsp4j.ConfigurationParams
import org.eclipse.lsp4j.LogTraceParams
import org.eclipse.lsp4j.MessageActionItem
import org.eclipse.lsp4j.MessageParams
import org.eclipse.lsp4j.ProgressParams
import org.eclipse.lsp4j.PublishDiagnosticsParams
import org.eclipse.lsp4j.RegistrationParams
import org.eclipse.lsp4j.ShowDocumentParams
import org.eclipse.lsp4j.ShowDocumentResult
import org.eclipse.lsp4j.ShowMessageRequestParams
import org.eclipse.lsp4j.UnregistrationParams
import org.eclipse.lsp4j.WorkDoneProgressCreateParams
import org.eclipse.lsp4j.WorkspaceFolder
import org.eclipse.lsp4j.jsonrpc.services.JsonNotification
import java.util.concurrent.CompletableFuture

/**
 * Custom LSP client that handles ReScript-specific notifications
 * from the language server.
 *
 * Receives `rescript/compilationStatus` and `rescript/compilationFinished`
 * notifications and updates [RescriptCompilationStatusService] accordingly.
 *
 * Wraps the default [LspServerNotificationsHandler] to suppress the LSP server's
 * "Start a build" prompt, which duplicates the plugin's own build notification
 * via [com.rescript.plugin.run.RescriptBuildWatchStartupActivity].
 */
class RescriptLsp4jClient(
    handler: LspServerNotificationsHandler,
    private val project: Project,
) : Lsp4jClient(FilteringNotificationsHandler(handler)) {
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

/**
 * Decorates [LspServerNotificationsHandler] to suppress the LSP server's
 * "Start a build" message request, preventing duplicate build prompts.
 *
 * @see RescriptLsp4jClient
 */
private class FilteringNotificationsHandler(
    private val delegate: LspServerNotificationsHandler,
) : LspServerNotificationsHandler {
    override fun showMessageRequest(params: ShowMessageRequestParams): CompletableFuture<MessageActionItem> {
        if (params.message.orEmpty().contains("Start a build")) {
            return CompletableFuture.completedFuture(MessageActionItem())
        }
        return delegate.showMessageRequest(params)
    }

    override fun showMessage(params: MessageParams) {
        if (params.message.orEmpty().contains("Start a build")) return
        delegate.showMessage(params)
    }

    // Delegate all other methods unchanged

    override fun applyEdit(params: org.eclipse.lsp4j.ApplyWorkspaceEditParams) = delegate.applyEdit(params)

    override fun registerCapability(params: RegistrationParams) = delegate.registerCapability(params)

    override fun unregisterCapability(params: UnregistrationParams) = delegate.unregisterCapability(params)

    override fun telemetryEvent(obj: Any) = delegate.telemetryEvent(obj)

    override fun publishDiagnostics(params: PublishDiagnosticsParams) = delegate.publishDiagnostics(params)

    override fun showDocument(params: ShowDocumentParams): CompletableFuture<ShowDocumentResult> =
        delegate.showDocument(params)

    override fun logMessage(params: MessageParams) = delegate.logMessage(params)

    override fun workspaceFolders(): CompletableFuture<List<WorkspaceFolder>> = delegate.workspaceFolders()

    override fun configuration(params: ConfigurationParams): CompletableFuture<List<Any?>> =
        delegate.configuration(params)

    override fun createProgress(params: WorkDoneProgressCreateParams): CompletableFuture<Void> =
        delegate.createProgress(params)

    override fun notifyProgress(params: ProgressParams) = delegate.notifyProgress(params)

    override fun logTrace(params: LogTraceParams) = delegate.logTrace(params)

    override fun refreshSemanticTokens(): CompletableFuture<Void> = delegate.refreshSemanticTokens()

    override fun refreshCodeLenses(): CompletableFuture<Void> = delegate.refreshCodeLenses()

    override fun refreshInlayHints(): CompletableFuture<Void> = delegate.refreshInlayHints()

    override fun refreshInlineValues(): CompletableFuture<Void> = delegate.refreshInlineValues()

    override fun refreshDiagnostics(): CompletableFuture<Void> = delegate.refreshDiagnostics()
}
