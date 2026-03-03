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

    /**
     * Represents a labeled parameter parsed from a function signature.
     *
     * @param name the parameter label name (without ~)
     * @param type the parameter type annotation
     * @param isOptional true if the parameter has a default value (=?)
     */
    data class LabeledParam(
        val name: String,
        val type: String,
        val isOptional: Boolean,
    )

    /**
     * Parses labeled parameters from a ReScript function signature string.
     *
     * Handles signatures like `(~name: string, ~age: int=?, unit) => person`.
     *
     * @param signature the function signature text
     * @return list of labeled parameters found in the signature
     */
    fun parseSignatureLabels(signature: String): List<LabeledParam> =
        RescriptLspSignatureParser.parseSignatureLabels(signature).map {
            LabeledParam(name = it.name, type = it.type, isOptional = it.isOptional)
        }

    /** Information about a variant constructor. */
    data class VariantInfo(
        val name: String,
        val hasPayload: Boolean,
    )

    /**
     * Parses variant constructors from a type hover result.
     *
     * Used by case split to expand a variable into all constructors.
     *
     * @param typeText the type text from LSP hover (e.g., "option<int>" or "color")
     * @return list of constructor names with optional payload indicator, or empty if not a variant
     */
    fun parseVariantConstructors(typeText: String): List<VariantInfo> =
        RescriptLspSignatureParser.parseVariantConstructors(typeText).map {
            VariantInfo(name = it.name, hasPayload = it.hasPayload)
        }

    /**
     * Parses a diagnostic message to extract diagnostic details.
     *
     * @param message the diagnostic message text
     * @return parsed diagnostic info, or null if the message format is not recognized
     */
    fun parseDiagnosticMessage(message: String): DiagnosticInfo? {
        val result = RescriptLspDiagnosticParser.parseDiagnosticMessage(message) ?: return null
        val kind =
            when (result.kind) {
                RescriptLspDiagnosticParser.DiagnosticKind.UNRESOLVED_VALUE -> DiagnosticKind.UNRESOLVED_VALUE
                RescriptLspDiagnosticParser.DiagnosticKind.UNRESOLVED_MODULE -> DiagnosticKind.UNRESOLVED_MODULE
            }
        return DiagnosticInfo(kind = kind, identifier = result.identifier)
    }

    /** The kind of diagnostic identified from a message. */
    enum class DiagnosticKind {
        UNRESOLVED_VALUE,
        UNRESOLVED_MODULE,
    }

    /** Parsed diagnostic information. */
    data class DiagnosticInfo(
        val kind: DiagnosticKind,
        val identifier: String,
    )

    /** Extracts content between the first ( and its matching ). */
    internal fun extractParenContent(text: String): String? = RescriptLspSignatureParser.extractParenContent(text)

    /** Splits text by comma, respecting nested parentheses and angle brackets. */
    internal fun splitByComma(text: String): List<String> = RescriptLspSignatureParser.splitByComma(text)
}
