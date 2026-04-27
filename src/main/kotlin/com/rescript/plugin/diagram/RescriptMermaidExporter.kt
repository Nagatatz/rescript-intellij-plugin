package com.rescript.plugin.diagram

/**
 * Renders a [RescriptDependencyDiagramModel] as Mermaid `graph TD` syntax.
 *
 * Mermaid output is suitable for embedding in Markdown previews or for
 * pasting into tools that render Mermaid (e.g. Mermaid Live Editor). Node
 * IDs are sanitized to Mermaid's allowed character set, while the original
 * module names are preserved as bracketed labels.
 *
 * @see RescriptDependencyDiagramModel for the source data structure
 */
object RescriptMermaidExporter {
    /**
     * Generates a Mermaid `graph TD` string for the given dependency model.
     *
     * Every module produces a node declaration so that isolated modules
     * (no incoming or outgoing edges) remain visible. Edges follow the
     * direction `dependent --> dependency`.
     *
     * @param model the dependency model to render
     * @return Mermaid graph syntax, terminated with a trailing newline
     */
    fun toMermaid(model: RescriptDependencyDiagramModel): String {
        val nodes = model.getNodes()
        val idMap = assignIds(nodes.map { it.name })

        return buildString {
            appendLine("graph TD")
            for (node in nodes) {
                val id = idMap.getValue(node.name)
                appendLine("  $id[\"${escapeLabel(node.name)}\"]")
            }
            for (edge in model.getEdges()) {
                val from = idMap[edge.from] ?: continue
                val to = idMap[edge.to] ?: continue
                appendLine("  $from --> $to")
            }
        }
    }

    /**
     * Builds a stable, collision-free name → Mermaid-id map.
     *
     * Two distinct module names that sanitize to the same identifier are
     * disambiguated by appending `_1`, `_2`, ... in iteration order.
     *
     * @param names module names in the order they should be rendered
     * @return ordered mapping from original name to assigned id
     */
    private fun assignIds(names: List<String>): Map<String, String> {
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

    /**
     * Reduces an arbitrary module name to Mermaid's safe identifier alphabet
     * (`A-Z`, `a-z`, `0-9`, `_`). Identifiers that would start with a digit
     * are prefixed with `n_` so the result is always a valid Mermaid id.
     */
    private fun sanitize(name: String): String {
        val sanitized = name.replace(NON_ID_CHARS, "_")
        return if (sanitized.firstOrNull()?.isDigit() == true) "n_$sanitized" else sanitized
    }

    /**
     * Escapes characters that would break the bracketed label syntax
     * `id["Label"]`. Backslashes and quotes are encoded so that arbitrary
     * module names cannot inject Mermaid directives.
     */
    private fun escapeLabel(name: String): String =
        name
            .replace("\\", "\\\\")
            .replace("\"", "&quot;")

    private val NON_ID_CHARS = Regex("[^A-Za-z0-9_]")
}
