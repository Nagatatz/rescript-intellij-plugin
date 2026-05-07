package com.rescript.plugin.diagram

/**
 * Shared helpers for producing safe Mermaid identifiers and bracketed
 * label text from arbitrary input strings.
 *
 * Extracted from [RescriptMermaidExporter] so that other tools that emit
 * Mermaid (e.g. the variant flow diagram) can reuse the same id assignment
 * and escaping rules without duplicating logic or risking divergent output.
 */
object MermaidLabelEscaping {
    private val NON_ID_CHARS = Regex("[^A-Za-z0-9_]")

    /**
     * Escapes characters that would break the bracketed label syntax
     * `id["Label"]`. Backslashes and quotes are encoded so that arbitrary
     * names cannot inject Mermaid directives.
     */
    fun escapeLabel(name: String): String =
        name
            .replace("\\", "\\\\")
            .replace("\"", "&quot;")

    /**
     * Reduces an arbitrary string to Mermaid's safe identifier alphabet
     * (`A-Z`, `a-z`, `0-9`, `_`). Identifiers that would start with a digit
     * are prefixed with `n_` so the result is always a valid Mermaid id.
     */
    fun sanitize(name: String): String {
        val sanitized = name.replace(NON_ID_CHARS, "_")
        return if (sanitized.firstOrNull()?.isDigit() == true) "n_$sanitized" else sanitized
    }

    /**
     * Builds a stable, collision-free name → Mermaid-id map.
     *
     * Two distinct names that sanitize to the same identifier are
     * disambiguated by appending `_1`, `_2`, ... in iteration order.
     *
     * @param names input names in the order they should be rendered
     * @return ordered mapping from original name to assigned id
     */
    fun assignIds(names: List<String>): Map<String, String> {
        val used = mutableSetOf<String>()
        val map = linkedMapOf<String, String>()
        for (name in names) {
            val base = sanitize(name).ifEmpty { "n" }
            var id = base
            var i = 1
            while (id in used) {
                id = "${base}_$i"
                i++
            }
            used.add(id)
            map[name] = id
        }
        return map
    }
}
