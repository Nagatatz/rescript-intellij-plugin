package com.rescript.plugin.diagram

import com.intellij.ui.JBColor
import com.rescript.plugin.ui.GraphViewPaintHelpers
import java.awt.Color
import java.awt.Dimension
import java.awt.FontMetrics
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Point
import java.awt.Rectangle
import java.awt.RenderingHints
import javax.swing.JComponent

/**
 * Swing component that renders a [RescriptDependencyDiagramModel] as an
 * actual top-down layered graph instead of Mermaid `flowchart TD` source
 * text.
 *
 * The component follows the same split established by
 * `RescriptVariantFlowGraphView` in the `flow/` package: a pure
 * [computeLayout] that takes a model plus the available width and
 * returns geometry only, and a `paintComponent` that draws the geometry
 * using Java2D. Keeping the layout pure lets it be unit-tested without
 * a live UI, and keeps the painting code small enough to verify by eye.
 *
 * Layering uses Kahn's BFS: nodes with no incoming edges (no other
 * module depends on them — typically entry points like `Main`) sit on
 * layer 0 at the top, and each downstream dependency is pushed one
 * layer lower. Nodes that participate in cycles cannot be assigned by
 * Kahn's algorithm; v1 piles them onto a single extra layer beneath the
 * rest rather than trying to break the cycle visually.
 *
 * @see RescriptDependencyDiagramPanel which embeds this view in the
 *   tool window
 */
class RescriptDependencyDiagramGraphView : JComponent() {
    @Volatile
    private var model: RescriptDependencyDiagramModel? = null

    init {
        background = JBColor.background()
        isOpaque = true
        preferredSize = Dimension(MIN_VIEW_WIDTH, MIN_VIEW_HEIGHT)
    }

    /**
     * Replace the rendered model with [next]; passing null clears the
     * view. Triggers a repaint and refreshes preferredSize so the
     * enclosing scroll pane recomputes its viewport.
     */
    fun setModel(next: RescriptDependencyDiagramModel?) {
        this.model = next
        val layout = next?.let { computeLayout(it, width.coerceAtLeast(MIN_VIEW_WIDTH)) }
        preferredSize = layout?.canvasSize ?: Dimension(MIN_VIEW_WIDTH, MIN_VIEW_HEIGHT)
        revalidate()
        repaint()
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val g2 = g.create() as Graphics2D
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
            g2.color = background
            g2.fillRect(0, 0, width, height)
            val current = model ?: return
            val layout = computeLayout(current, width.coerceAtLeast(MIN_VIEW_WIDTH), g2.fontMetrics)
            for (node in layout.nodes) {
                val (fill, border) = PALETTE.getValue(node.role)
                GraphViewPaintHelpers.paintNode(g2, node.box, node.name, fill, border)
            }
            GraphViewPaintHelpers.paintEdges(g2, layout.edges)
            GraphViewPaintHelpers.paintLegend(
                g2,
                layout.canvasSize.height - LEGEND_HEIGHT,
                LEGEND_ITEMS,
                MARGIN,
            )
        } finally {
            g2.dispose()
        }
    }

    /**
     * Geometric record for a single placed module box.
     *
     * @property name module name displayed as the label
     * @property layer 0-based layer index, 0 = topmost (entry points)
     * @property box absolute rectangle inside the canvas
     * @property role structural classification (entry-point / intermediate /
     *   leaf / cycle member) used by the view to pick a palette colour;
     *   computed by [RescriptDependencyDiagramModel.classifyNodes]
     */
    data class LayoutNode(
        val name: String,
        val layer: Int,
        val box: Rectangle,
        val role: NodeRole,
    )

    /**
     * Geometric result of laying out a dependency graph.
     *
     * @property nodes every placed module box, in `(layer, x)` order
     * @property edges polylines connecting each `from → to`; each
     *   polyline has 4 vertices (from-bottom → midline → to-top-x →
     *   to-top) for an orthogonal route
     * @property canvasSize total size required to contain all nodes
     *   and edges, including margins
     */
    data class Layout(
        val nodes: List<LayoutNode>,
        val edges: List<List<Point>>,
        val canvasSize: Dimension,
    )

    companion object {
        const val MIN_VIEW_WIDTH = 420
        const val MIN_VIEW_HEIGHT = 160
        private const val NODE_HEIGHT = 32
        private const val V_GAP = 36
        private const val H_GAP = 16
        private const val MARGIN = 12
        private const val NODE_MIN_WIDTH = 100
        private const val NODE_MAX_WIDTH = 240
        private const val DEFAULT_CHAR_WIDTH = 7

        /** Height in pixels reserved at the bottom of the canvas for the legend strip. */
        internal const val LEGEND_HEIGHT = GraphViewPaintHelpers.LEGEND_HEIGHT

        private val ENTRY_FILL = JBColor(Color(0xE7F0FF), Color(0x223A6E))
        private val ENTRY_BORDER = JBColor(Color(0x3E72C2), Color(0x6B9CE6))
        private val INTERMEDIATE_FILL = JBColor(Color(0xFFF3F4), Color(0x4A1518))
        private val INTERMEDIATE_BORDER = JBColor(Color(0xCB3939), Color(0xE6484F))
        private val LEAF_FILL = JBColor(Color(0xE7FBE7), Color(0x254B25))
        private val LEAF_BORDER = JBColor(Color(0x3E9E3E), Color(0x68C268))
        private val CYCLE_FILL = JBColor(Color(0xFFE0B3), Color(0x5A3A12))
        private val CYCLE_BORDER = JBColor(Color(0xD2691E), Color(0xE89E55))

        /**
         * Fill/border colour pair for each [NodeRole]. Colours are
         * `JBColor(light, dark)` so the palette adapts to the active
         * IDE theme without per-call branching.
         */
        internal val PALETTE: Map<NodeRole, Pair<Color, Color>> =
            mapOf(
                NodeRole.ENTRY_POINT to (ENTRY_FILL to ENTRY_BORDER),
                NodeRole.INTERMEDIATE to (INTERMEDIATE_FILL to INTERMEDIATE_BORDER),
                NodeRole.LEAF to (LEAF_FILL to LEAF_BORDER),
                NodeRole.CYCLE_MEMBER to (CYCLE_FILL to CYCLE_BORDER),
            )

        /**
         * Legend strip rows resolved through [PALETTE], drawn
         * left-to-right at the bottom. Declared after [PALETTE] because
         * companion properties initialise in source order.
         */
        private val LEGEND_ITEMS: List<GraphViewPaintHelpers.LegendItem> =
            listOf(
                "Entry point" to NodeRole.ENTRY_POINT,
                "Intermediate" to NodeRole.INTERMEDIATE,
                "Leaf" to NodeRole.LEAF,
                "Cycle" to NodeRole.CYCLE_MEMBER,
            ).map { (label, role) ->
                val (fill, border) = PALETTE.getValue(role)
                GraphViewPaintHelpers.LegendItem(label, fill, border)
            }

        /**
         * Lays out [model] inside a viewport of width [viewportWidth].
         *
         * Optional [fm] is consulted to size each node to the actual
         * rendered width of its text; when null (as in unit tests) a
         * conservative per-character estimate is used so the layout
         * remains deterministic.
         *
         * Layering uses Kahn's BFS so layer 0 contains nodes with no
         * incoming edges (entry points). Nodes inside cycles can't be
         * assigned by Kahn's and are placed on a single extra layer
         * beneath the rest. Self-loops are skipped during edge
         * generation but the node itself is still drawn.
         *
         * @param model dependency model to render
         * @param viewportWidth available width in CSS-like pixels
         * @param fm optional font metrics used for label width
         * @return geometric layout
         */
        fun computeLayout(
            model: RescriptDependencyDiagramModel,
            viewportWidth: Int,
            fm: FontMetrics? = null,
        ): Layout {
            val nodeNames = model.getNodes().map { it.name }
            if (nodeNames.isEmpty()) {
                return Layout(
                    nodes = emptyList(),
                    edges = emptyList(),
                    canvasSize =
                        Dimension(
                            viewportWidth.coerceAtLeast(MIN_VIEW_WIDTH),
                            MIN_VIEW_HEIGHT,
                        ),
                )
            }
            val nodeSet = nodeNames.toSet()
            val edges =
                model
                    .getEdges()
                    .filter { it.from != it.to && it.from in nodeSet && it.to in nodeSet }

            val roleByName =
                RescriptDependencyDiagramModel.classifyNodes(model.getNodes(), edges)
            val layerByName = assignLayers(nodeNames, edges)

            // Group node names by layer, sorted alphabetically within each layer
            // for deterministic layout (no crossing-reduction heuristic in v1).
            val byLayer: Map<Int, List<String>> =
                layerByName.entries
                    .groupBy({ it.value }, { it.key })
                    .mapValues { (_, names) -> names.sorted() }
            val orderedLayers = byLayer.keys.sorted()

            val nodeWidthByName: Map<String, Int> =
                nodeNames.associateWith { nodeWidth(it, fm, NODE_MIN_WIDTH, NODE_MAX_WIDTH) }

            val layerWidth: Map<Int, Int> =
                orderedLayers.associateWith { idx ->
                    val names = byLayer.getValue(idx)
                    names.sumOf { nodeWidthByName.getValue(it) } +
                        (names.size - 1).coerceAtLeast(0) * H_GAP
                }

            val contentWidth = layerWidth.values.maxOrNull() ?: 0
            val canvasWidth =
                (contentWidth + 2 * MARGIN).coerceAtLeast(viewportWidth.coerceAtLeast(MIN_VIEW_WIDTH))

            val placedNodes = mutableListOf<LayoutNode>()
            val boxByName = mutableMapOf<String, Rectangle>()

            for ((rowIndex, layerIdx) in orderedLayers.withIndex()) {
                val names = byLayer.getValue(layerIdx)
                val width = layerWidth.getValue(layerIdx)
                val startX = (canvasWidth - width) / 2
                val y = MARGIN + rowIndex * (NODE_HEIGHT + V_GAP)
                var x = startX
                for (name in names) {
                    val w = nodeWidthByName.getValue(name)
                    val box = Rectangle(x, y, w, NODE_HEIGHT)
                    boxByName[name] = box
                    val role = roleByName[name] ?: NodeRole.INTERMEDIATE
                    placedNodes.add(LayoutNode(name, layerIdx, box, role))
                    x += w + H_GAP
                }
            }

            val placedEdges = mutableListOf<List<Point>>()
            for (edge in edges) {
                val fromBox = boxByName[edge.from] ?: continue
                val toBox = boxByName[edge.to] ?: continue
                val fromBottom = Point(fromBox.x + fromBox.width / 2, fromBox.y + fromBox.height)
                val toTop = Point(toBox.x + toBox.width / 2, toBox.y)
                val midY = fromBottom.y + (toTop.y - fromBottom.y) / 2
                placedEdges.add(
                    listOf(
                        fromBottom,
                        Point(fromBottom.x, midY),
                        Point(toTop.x, midY),
                        toTop,
                    ),
                )
            }

            val rowsHeight =
                if (orderedLayers.isEmpty()) {
                    0
                } else {
                    orderedLayers.size * NODE_HEIGHT + (orderedLayers.size - 1) * V_GAP
                }
            val canvasHeight =
                (MARGIN + rowsHeight + MARGIN).coerceAtLeast(MIN_VIEW_HEIGHT) + LEGEND_HEIGHT

            return Layout(
                nodes = placedNodes,
                edges = placedEdges,
                canvasSize = Dimension(canvasWidth, canvasHeight),
            )
        }

        /**
         * Assigns a layer index to every node in [nodeNames] using
         * Kahn's BFS. Nodes that participate in cycles cannot be drained
         * by Kahn's algorithm; they are collected at the end onto a
         * single layer beneath the already-assigned ones so they remain
         * visible.
         */
        private fun assignLayers(
            nodeNames: List<String>,
            edges: List<RescriptDependencyDiagramModel.ModuleEdge>,
        ): Map<String, Int> {
            val inDegree = mutableMapOf<String, Int>()
            val adj = mutableMapOf<String, MutableList<String>>()
            for (name in nodeNames) {
                inDegree[name] = 0
                adj[name] = mutableListOf()
            }
            for (edge in edges) {
                inDegree[edge.to] = (inDegree[edge.to] ?: 0) + 1
                adj.getValue(edge.from).add(edge.to)
            }

            val layer = mutableMapOf<String, Int>()
            val remaining = inDegree.toMutableMap()
            val queue = ArrayDeque<String>()
            // Initial seed: nodes with no incoming edges, in stable
            // alphabetical order so the cycle-fallback layer below is
            // deterministic.
            for (name in nodeNames.sorted()) {
                if (remaining[name] == 0) {
                    layer[name] = 0
                    queue.add(name)
                }
            }
            while (queue.isNotEmpty()) {
                val n = queue.removeFirst()
                val nextLayer = (layer[n] ?: 0) + 1
                for (m in adj.getValue(n)) {
                    layer[m] = maxOf(layer[m] ?: 0, nextLayer)
                    val newDeg = (remaining[m] ?: 0) - 1
                    remaining[m] = newDeg
                    if (newDeg == 0) queue.add(m)
                }
            }
            val maxAssigned = layer.values.maxOrNull() ?: -1
            val cycleLayer = maxAssigned + 1
            for (name in nodeNames) {
                if (name !in layer) layer[name] = cycleLayer
            }
            return layer
        }

        private fun nodeWidth(
            label: String,
            fm: FontMetrics?,
            minWidth: Int,
            maxWidth: Int,
        ): Int {
            val widest = fm?.stringWidth(label) ?: (label.length * DEFAULT_CHAR_WIDTH)
            return (widest + 2 * GraphViewPaintHelpers.NODE_PADDING_X).coerceAtLeast(minWidth).coerceAtMost(maxWidth)
        }
    }
}
