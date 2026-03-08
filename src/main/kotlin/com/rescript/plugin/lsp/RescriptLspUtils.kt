package com.rescript.plugin.lsp

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServer
import com.intellij.platform.lsp.api.LspServerManager
import com.rescript.plugin.util.RescriptOffsetUtils
import org.eclipse.lsp4j.HoverParams
import org.eclipse.lsp4j.TextDocumentIdentifier
import java.net.URI

/**
 * Shared utility functions for interacting with the ReScript LSP server.
 *
 * Provides hover-based type retrieval and signature parsing used by
 * multiple A-priority features (pipe chain hints, labeled args insertion,
 * case split, and argument conversion).
 *
 * Signature parsing is delegated to [RescriptLspSignatureParser] and
 * diagnostic parsing to [RescriptLspDiagnosticParser].
 *
 * @see RescriptLspServerSupportProvider
 * @see RescriptExpressionTypeProvider
 * @see RescriptLspSignatureParser
 * @see RescriptLspDiagnosticParser
 */
object RescriptLspUtils {
    /**
     * Returns the first ReScript LSP server for the given project, or null if unavailable.
     *
     * @param project the current project
     * Uses unstable `LspServerManager` API — review on platform upgrade.
     *
     * @return the first available LSP server, or null
     */
    @Suppress("UnstableApiUsage")
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
     *
     * @param uri the LSP file URI string
     * @return the VFS-compatible URL string
     */
    fun lspUriToVfsUrl(uri: String): String =
        try {
            val parsed = URI(uri)
            "file://${parsed.path}"
        } catch (_: Exception) {
            uri
        }

    /**
     * Retrieves the type string for a given position via LSP hover.
     *
     * @param project the current project
     * @param file the virtual file to query
     * @param offset the character offset in the document
     * @return the type string extracted from the hover response, or null if unavailable
     */
    fun getHoverType(
        project: Project,
        file: VirtualFile,
        offset: Int,
    ): String? {
        try {
            val server = getServer(project) ?: return null

            val document =
                com.intellij.openapi.fileEditor.FileDocumentManager
                    .getInstance()
                    .getDocument(file) ?: return null

            val position = RescriptOffsetUtils.offsetToPosition(document, offset)

            val uri = toLspUri(file)

            val params =
                HoverParams(
                    TextDocumentIdentifier(uri),
                    position,
                )

            val hoverResult =
                server.sendRequestSync { languageServer ->
                    languageServer.textDocumentService.hover(params)
                } ?: return null

            val content = hoverResult.contents ?: return null
            return when {
                content.isLeft ->
                    content.left
                        .firstOrNull()
                        ?.let { if (it.isLeft) it.left else it.right.value }
                content.isRight -> {
                    val markdown = content.right.value
                    RescriptExpressionTypeProvider.extractTypeFromMarkdown(markdown)
                }
                else -> null
            }
        } catch (_: Exception) {
            return null
        }
    }

    /** Type alias for [RescriptLspSignatureParser.LabeledParam]. */
    typealias LabeledParam = RescriptLspSignatureParser.LabeledParam

    /**
     * Parses labeled parameters from a ReScript function signature string.
     *
     * Handles signatures like `(~name: string, ~age: int=?, unit) => person`.
     *
     * @param signature the function signature text
     * @return list of labeled parameters found in the signature
     */
    fun parseSignatureLabels(signature: String): List<LabeledParam> =
        RescriptLspSignatureParser.parseSignatureLabels(signature)

    /** Type alias for [RescriptLspSignatureParser.VariantInfo]. */
    typealias VariantInfo = RescriptLspSignatureParser.VariantInfo

    /**
     * Parses variant constructors from a type hover result.
     *
     * Used by case split to expand a variable into all constructors.
     *
     * @param typeText the type text from LSP hover (e.g., "option<int>" or "color")
     * @return list of constructor names with optional payload indicator, or empty if not a variant
     */
    fun parseVariantConstructors(typeText: String): List<VariantInfo> =
        RescriptLspSignatureParser.parseVariantConstructors(typeText)

    /**
     * Parses a diagnostic message to extract diagnostic details.
     *
     * @param message the diagnostic message text
     * @return parsed diagnostic info, or null if the message format is not recognized
     */
    fun parseDiagnosticMessage(message: String): DiagnosticInfo? =
        RescriptLspDiagnosticParser.parseDiagnosticMessage(message)

    /** Type alias for [RescriptLspDiagnosticParser.DiagnosticKind]. */
    typealias DiagnosticKind = RescriptLspDiagnosticParser.DiagnosticKind

    /** Type alias for [RescriptLspDiagnosticParser.DiagnosticInfo]. */
    typealias DiagnosticInfo = RescriptLspDiagnosticParser.DiagnosticInfo

    /** Extracts content between the first ( and its matching ). */
    internal fun extractParenContent(text: String): String? = RescriptLspSignatureParser.extractParenContent(text)

    /** Splits text by comma, respecting nested parentheses and angle brackets. */
    internal fun splitByComma(text: String): List<String> = RescriptLspSignatureParser.splitByComma(text)
}
