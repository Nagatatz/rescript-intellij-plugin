package com.rescript.plugin.lsp

/**
 * Parses LSP diagnostic messages to extract structured information
 * such as unresolved value and module references.
 *
 * Used by quick fix providers to determine the appropriate fix action.
 *
 * @see RescriptLspUtils
 */
object RescriptLspDiagnosticParser {
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

    // Pattern: "The value X can't be found"
    private val UNRESOLVED_VALUE_PATTERN = Regex("""The value (\w+) can't be found""", RegexOption.IGNORE_CASE)

    // Pattern: "The module or file X can't be found"
    private val UNRESOLVED_MODULE_PATTERN =
        Regex("""The module or file (\w+) can't be found""", RegexOption.IGNORE_CASE)

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
}
