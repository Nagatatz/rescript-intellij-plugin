package com.rescript.plugin.diagram

/**
 * Shared helper for producing safe DOT quoted-label strings from arbitrary input.
 *
 * Extracted so that both [com.rescript.plugin.flow.RescriptVariantFlowDotExporter]
 * and [RescriptDependencyDiagramModel] use identical escaping rules, preventing
 * divergent output if one call site is updated without the other.
 *
 * @see RescriptDependencyDiagramModel.toDot
 * @see com.rescript.plugin.flow.RescriptVariantFlowDotExporter.toDot
 */
object DotLabelEscaping {
    /**
     * Escapes characters that would terminate or corrupt a DOT quoted label
     * (`"…"` syntax): backslashes, double quotes, and embedded newlines.
     *
     * @param value raw label text, possibly containing special characters
     * @return the escaped string safe for embedding between `"…"` in DOT output
     */
    fun escapeDotLabel(value: String): String =
        value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
}
