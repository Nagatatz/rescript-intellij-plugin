package com.rescript.plugin.lsp

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServer
import com.intellij.platform.lsp.api.LspServerManager
import com.rescript.plugin.util.RescriptOffsetUtils
import org.eclipse.lsp4j.HoverParams
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.TextDocumentIdentifier
import java.net.URI

/**
 * Shared utility functions for talking to the ReScript LSP server:
 * server lookup, URI conversion, and hover-based type retrieval used
 * by multiple features (pipe chain hints, labeled args insertion,
 * case split, and argument conversion).
 *
 * Parsing of hover/diagnostic payloads lives in
 * [RescriptLspSignatureParser] and [RescriptLspDiagnosticParser];
 * callers reference those parsers directly.
 *
 * @see RescriptLspServerSupportProvider
 * @see RescriptExpressionTypeProvider
 * @see RescriptLspSignatureParser
 * @see RescriptLspDiagnosticParser
 */
object RescriptLspUtils {
    private val LOG = logger<RescriptLspUtils>()

    // LspServer / LspServerManager are deprecated in 2026.2 EAP; the replacement
    // LspClientDescriptor API does not exist on the 2026.1.2 compile target.

    /**
     * Returns the first ReScript LSP server for the given project, or null if unavailable.
     *
     * @param project the current project
     * Uses unstable `LspServerManager` API — review on platform upgrade.
     *
     * @return the first available LSP server, or null
     */
    @Suppress("UnstableApiUsage", "DEPRECATION")
    fun getServer(project: Project): LspServer? =
        LspServerManager
            .getInstance(project)
            .getServersForProvider(RescriptLspServerSupportProvider::class.java)
            .firstOrNull()

    /**
     * Converts a VirtualFile to an LSP-compatible file URI string.
     *
     * @param file the virtual file to convert
     * @return the file URI string (e.g., "file:///path/to/file.res")
     */
    fun toLspUri(file: VirtualFile): String =
        file.url.let { url ->
            if (url.startsWith("file://")) url else "file://${file.path}"
        }

    /**
     * Converts an LSP file URI to an IntelliJ VFS-compatible URL.
     *
     * Normalizes URIs like "file:///path" through java.net.URI parsing.
     * **Important:** Callers must validate the result with
     * [com.rescript.plugin.util.RescriptSecurityUtils.isWithinProject] before
     * performing file operations, as the URI originates from an external LSP server.
     *
     * @param uri the LSP file URI string
     * @return the VFS-compatible URL string
     */
    fun lspUriToVfsUrl(uri: String): String =
        try {
            val parsed = URI(uri)
            "file://${parsed.path}"
        } catch (e: Exception) {
            LOG.trace("Failed to parse LSP URI, using raw value: $uri — ${e.message}")
            uri
        }

    // LspServer / sendRequestSync are deprecated in 2026.2 EAP; the replacement
    // LspClientDescriptor API does not exist on the 2026.1.2 compile target.

    /**
     * Retrieves the type string for a given position via LSP hover.
     *
     * @param project the current project
     * @param file the virtual file to query
     * @param offset the character offset in the document
     * @return the type string extracted from the hover response, or null if unavailable
     */
    @Suppress("DEPRECATION")
    fun getHoverType(
        project: Project,
        file: VirtualFile,
        offset: Int,
    ): String? {
        try {
            val server = getServer(project) ?: return null

            val position =
                ApplicationManager.getApplication().runReadAction<Position?> {
                    val document =
                        FileDocumentManager.getInstance().getDocument(file)
                            ?: return@runReadAction null
                    RescriptOffsetUtils.offsetToPosition(document, offset)
                } ?: return null

            val uri = toLspUri(file)

            val params =
                HoverParams(
                    TextDocumentIdentifier(uri),
                    position,
                )

            val hoverResult =
                // Explicit 10s timeout (the platform default). Omitting it emits
                // the synthetic sendRequestSync$default, which 2026.2 relocated to
                // the new LspClient super-interface and no longer resolves here.
                server.sendRequestSync(10_000) { languageServer ->
                    languageServer.textDocumentService.hover(params)
                } ?: return null

            val content = hoverResult.contents ?: return null
            return when {
                // Only handle the plain-string branch of the legacy
                // Either<List<Either<String, MarkedString>>, MarkupContent>
                // payload. The MarkedString branch is deprecated and
                // unused by rescript-language-server (which always
                // returns MarkupContent on the right).
                content.isLeft -> {
                    content.left
                        .firstOrNull()
                        ?.takeIf { it.isLeft }
                        ?.left
                }

                content.isRight -> {
                    val markdown = content.right.value
                    RescriptExpressionTypeProvider.extractTypeFromMarkdown(markdown)
                }

                else -> {
                    null
                }
            }
        } catch (e: Exception) {
            LOG.trace("Failed to get hover type from LSP server: ${e.message}")
            return null
        }
    }
}
