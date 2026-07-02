package com.rescript.plugin.flow

import com.rescript.plugin.diagram.DotLabelEscaping

/**
 * Renders a [FlowDiagram] as graphviz DOT (`digraph`) suitable for
 * the "Export DOT" tool window action. The output is plain text that
 * `dot -Tsvg` (and any DOT-compatible renderer) can consume.
 */
object RescriptVariantFlowDotExporter {
    private const val ROOT_ID = "root"

    /**
     * Builds the DOT string. The root node carries `switch X`;
     * arms become child nodes whose edge label holds the pattern
     * summary and node label holds the body preview. Nested switches
     * are emitted as sub-trees rooted at the parent arm.
     *
     * Output ends with a trailing newline.
     */
    fun toDot(diagram: FlowDiagram): String =
        buildString {
            appendLine("digraph SwitchFlow {")
            appendLine("  rankdir=TB;")
            appendLine("  $ROOT_ID [label=\"${DotLabelEscaping.escapeDotLabel("switch ${diagram.scrutineeText}")}\"];")
            for (arm in diagram.arms) {
                renderNode(this, parentId = ROOT_ID, node = arm)
            }
            appendLine("}")
        }

    private fun renderNode(
        out: StringBuilder,
        parentId: String,
        node: FlowNode,
    ) {
        val nodeId = sanitiseDotId(node.id)
        val label = node.bodyPreview.ifEmpty { node.patternSummary }
        out.appendLine("  $nodeId [label=\"${DotLabelEscaping.escapeDotLabel(label)}\"];")
        out.appendLine("  $parentId -> $nodeId [label=\"${DotLabelEscaping.escapeDotLabel(node.patternSummary)}\"];")
        for (child in node.children) {
            renderNode(out, parentId = nodeId, node = child)
        }
    }

    /**
     * DOT identifier rules differ slightly from Mermaid: we accept
     * letters, digits, and underscores, and prefix a digit-leading id
     * with `n_` to keep the result a valid bare id.
     */
    private fun sanitiseDotId(id: String): String {
        val cleaned = id.replace(Regex("[^A-Za-z0-9_]"), "_")
        return if (cleaned.firstOrNull()?.isDigit() == true) "n_$cleaned" else cleaned.ifEmpty { "n" }
    }
}
