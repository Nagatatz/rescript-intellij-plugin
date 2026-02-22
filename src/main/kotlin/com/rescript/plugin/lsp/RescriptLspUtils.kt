package com.rescript.plugin.lsp

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServerManager
import org.eclipse.lsp4j.HoverParams
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.TextDocumentIdentifier

/**
 * Shared utility functions for interacting with the ReScript LSP server.
 *
 * Provides hover-based type retrieval and signature parsing used by
 * multiple A-priority features (pipe chain hints, labeled args insertion,
 * case split, and argument conversion).
 *
 * @see RescriptLspServerSupportProvider
 * @see RescriptExpressionTypeProvider
 */
object RescriptLspUtils {
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
            @Suppress("UnstableApiUsage")
            val servers =
                LspServerManager
                    .getInstance(project)
                    .getServersForProvider(RescriptLspServerSupportProvider::class.java)

            val server = servers.firstOrNull() ?: return null

            val document =
                com.intellij.openapi.fileEditor.FileDocumentManager
                    .getInstance()
                    .getDocument(file) ?: return null

            val lineNumber = document.getLineNumber(offset)
            val column = offset - document.getLineStartOffset(lineNumber)

            val uri =
                file.url.let { url ->
                    if (url.startsWith("file://")) url else "file://${file.path}"
                }

            val params =
                HoverParams(
                    TextDocumentIdentifier(uri),
                    Position(lineNumber, column),
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
    fun parseSignatureLabels(signature: String): List<LabeledParam> {
        val params = mutableListOf<LabeledParam>()

        // Extract parameter list between first ( and matching )
        val parenContent = extractParenContent(signature) ?: return params

        // Split by comma, respecting nested parens/brackets
        val parts = splitByComma(parenContent)

        for (part in parts) {
            val trimmed = part.trim()
            // Match ~label: type or ~label: type=?
            val match = LABELED_PARAM_PATTERN.find(trimmed)
            if (match != null) {
                params.add(
                    LabeledParam(
                        name = match.groupValues[1],
                        type = match.groupValues[2].trim(),
                        isOptional = match.groupValues[3].isNotEmpty(),
                    ),
                )
            }
        }

        return params
    }

    /**
     * Parses variant constructors from a type hover result.
     *
     * Used by case split to expand a variable into all constructors.
     *
     * @param typeText the type text from LSP hover (e.g., "option<int>" or "color")
     * @return list of constructor names with optional payload indicator, or empty if not a variant
     */
    fun parseVariantConstructors(typeText: String): List<VariantInfo> {
        val results = mutableListOf<VariantInfo>()

        // Handle built-in types
        if (typeText.startsWith("option<") || typeText == "option") {
            results.add(VariantInfo("Some", hasPayload = true))
            results.add(VariantInfo("None", hasPayload = false))
            return results
        }
        if (typeText.startsWith("result<") || typeText == "result") {
            results.add(VariantInfo("Ok", hasPayload = true))
            results.add(VariantInfo("Error", hasPayload = true))
            return results
        }

        // Parse inline variant definitions: | Constructor1 | Constructor2(payload)
        if (typeText.contains("|")) {
            val arms = typeText.split("|").map { it.trim() }.filter { it.isNotEmpty() }
            for (arm in arms) {
                val match = CONSTRUCTOR_PATTERN.find(arm)
                if (match != null) {
                    results.add(
                        VariantInfo(
                            name = match.groupValues[1],
                            hasPayload = match.groupValues[2].isNotEmpty(),
                        ),
                    )
                }
            }
        }

        return results
    }

    /** Information about a variant constructor. */
    data class VariantInfo(
        val name: String,
        val hasPayload: Boolean,
    )

    /**
     * Parses a diagnostic message to extract diagnostic details.
     *
     * @param message the diagnostic message text
     * @return parsed diagnostic info, or null if the message format is not recognized
     */
    fun parseDiagnosticMessage(message: String): DiagnosticInfo? {
        // "The value identifier is not found"
        val unresolvedMatch = UNRESOLVED_VALUE_PATTERN.find(message)
        if (unresolvedMatch != null) {
            return DiagnosticInfo(
                kind = DiagnosticKind.UNRESOLVED_VALUE,
                identifier = unresolvedMatch.groupValues[1],
            )
        }

        // "The module or file X can't be found"
        val unresolvedModuleMatch = UNRESOLVED_MODULE_PATTERN.find(message)
        if (unresolvedModuleMatch != null) {
            return DiagnosticInfo(
                kind = DiagnosticKind.UNRESOLVED_MODULE,
                identifier = unresolvedModuleMatch.groupValues[1],
            )
        }

        return null
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

    // ── Internal helpers ──────────────────────────────────────────────

    // Match labeled params like ~name: type or ~name: type=?
    // Uses .+? to allow = in types (e.g., (int, string) => unit)
    private val LABELED_PARAM_PATTERN = Regex("""~(\w+)\s*:\s*(.+?)\s*(=\?)?\s*$""")
    private val CONSTRUCTOR_PATTERN = Regex("""^([A-Z]\w*)(?:\((.+)\))?$""")
    private val UNRESOLVED_VALUE_PATTERN = Regex("""The value (\w+) can't be found""", RegexOption.IGNORE_CASE)
    private val UNRESOLVED_MODULE_PATTERN =
        Regex("""The module or file (\w+) can't be found""", RegexOption.IGNORE_CASE)

    /** Extracts content between the first ( and its matching ). */
    internal fun extractParenContent(text: String): String? {
        val start = text.indexOf('(')
        if (start < 0) return null

        var depth = 0
        for (i in start until text.length) {
            when (text[i]) {
                '(' -> depth++
                ')' -> {
                    depth--
                    if (depth == 0) {
                        return text.substring(start + 1, i)
                    }
                }
            }
        }
        return null
    }

    /** Splits text by comma, respecting nested parentheses and angle brackets. */
    internal fun splitByComma(text: String): List<String> {
        val parts = mutableListOf<String>()
        var depth = 0
        var current = StringBuilder()

        for (ch in text) {
            when (ch) {
                '(', '<', '{', '[' -> {
                    depth++
                    current.append(ch)
                }
                ')', '>', '}', ']' -> {
                    depth--
                    current.append(ch)
                }
                ',' -> {
                    if (depth == 0) {
                        parts.add(current.toString())
                        current = StringBuilder()
                    } else {
                        current.append(ch)
                    }
                }
                else -> current.append(ch)
            }
        }

        if (current.isNotEmpty()) {
            parts.add(current.toString())
        }

        return parts
    }
}
