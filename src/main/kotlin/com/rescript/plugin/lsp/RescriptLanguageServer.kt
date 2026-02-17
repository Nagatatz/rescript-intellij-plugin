package com.rescript.plugin.lsp

import org.eclipse.lsp4j.TextDocumentIdentifier
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest
import org.eclipse.lsp4j.services.LanguageServer
import java.util.concurrent.CompletableFuture

/**
 * Custom LSP server interface for ReScript-specific requests.
 *
 * Extends the standard [LanguageServer] with additional methods
 * defined by rescript-language-server:
 * - `textDocument/createInterface` — generate .resi from .res
 * - `textDocument/openCompiled` — resolve compiled .js path
 */
interface RescriptLanguageServer : LanguageServer {
    @JsonRequest("textDocument/createInterface")
    fun createInterface(params: TextDocumentIdentifier): CompletableFuture<TextDocumentIdentifier>

    @JsonRequest("textDocument/openCompiled")
    fun openCompiled(params: TextDocumentIdentifier): CompletableFuture<TextDocumentIdentifier>
}
