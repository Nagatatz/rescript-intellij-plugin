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
 * @see MermaidLabelEscaping for the shared id and label rules
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
        val idMap = MermaidLabelEscaping.assignIds(nodes.map { it.name })

        return buildString {
            appendLine("graph TD")
            for (node in nodes) {
                val id = idMap.getValue(node.name)
                appendLine("  $id[\"${MermaidLabelEscaping.escapeLabel(node.name)}\"]")
            }
            for (edge in model.getEdges()) {
                val from = idMap[edge.from] ?: continue
                val to = idMap[edge.to] ?: continue
                appendLine("  $from --> $to")
            }
        }
    }
}
