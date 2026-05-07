package com.rescript.plugin.flow

import com.rescript.plugin.diagram.MermaidLabelEscaping

/**
 * Renders a [FlowDiagram] as a Mermaid `flowchart TD` string suitable
 * for the IDE preview, copy-to-clipboard, and pasting into external
 * Mermaid renderers (Mermaid Live, GitHub Markdown, etc.).
 *
 * Reuses [MermaidLabelEscaping] for identifier sanitisation and label
 * escaping so the variant-flow output follows the same id and quoting
 * rules as the dependency-diagram exporter.
 */
object RescriptVariantFlowMermaidExporter {
    private const val ROOT_ID = "root"

    /**
     * Builds the Mermaid string. The root node carries the scrutinee
     * text; every arm renders as a child node, with the pattern
     * summary on the connecting edge label and the body preview as
     * the node label. Nested switches expand recursively, with the
     * inner scrutinee promoted into a sub-graph node.
     *
     * Output ends with a trailing newline.
     */
    fun toMermaid(diagram: FlowDiagram): String =
        buildString {
            appendLine("flowchart TD")
            appendLine("  $ROOT_ID[\"${MermaidLabelEscaping.escapeLabel("switch ${diagram.scrutineeText}")}\"]")
            for (arm in diagram.arms) {
                renderNode(this, parentId = ROOT_ID, node = arm)
            }
        }

    private fun renderNode(
        out: StringBuilder,
        parentId: String,
        node: FlowNode,
    ) {
        val nodeId = MermaidLabelEscaping.sanitize(node.id).ifEmpty { "n" }
        val label = MermaidLabelEscaping.escapeLabel(node.bodyPreview.ifEmpty { node.patternSummary })
        val edgeLabel = MermaidLabelEscaping.escapeLabel(node.patternSummary)
        out.appendLine("  $nodeId[\"$label\"]")
        out.appendLine("  $parentId -->|\"$edgeLabel\"| $nodeId")
        for (child in node.children) {
            renderNode(out, parentId = nodeId, node = child)
        }
    }
}
