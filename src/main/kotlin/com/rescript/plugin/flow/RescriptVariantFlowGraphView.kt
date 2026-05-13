package com.rescript.plugin.flow

import com.intellij.ui.JBColor
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Dimension
import java.awt.FontMetrics
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Point
import java.awt.Polygon
import java.awt.Rectangle
import java.awt.RenderingHints
import javax.swing.JComponent

/**
 * Swing component that renders a [FlowDiagram] as an actual top-down
 * graph instead of Mermaid `flowchart TD` source text.
 *
 * The component is split into a pure layout helper
 * ([computeLayout]) that takes a model + the available width and
 * returns geometry only, and a `paintComponent` that draws the
 * geometry using Java2D. Keeping the layout computation pure means
 * it can be unit-tested without a live UI, and the painting code
 * stays small enough to verify by eye.
 *
 * Nested `switch` arms (`FlowNode.children`) are expanded into a
 * sub-tree drawn beneath the parent arm, so the visual mode follows
 * the same recursion the Mermaid / DOT exporters already perform
 * (capped by `RescriptVariantFlowModel.MAX_NESTING_DEPTH`). When no
 * arm has children, the layout falls back to the original flat row
 * that wraps onto additional rows in narrow viewports.
 */
class RescriptVariantFlowGraphView : JComponent() {
    @Volatile
    private var diagram: FlowDiagram? = null

    init {
        background = JBColor.background()
        isOpaque = true
        preferredSize = Dimension(420, 160)
    }

    /**
     * Replace the rendered diagram with [next]; passing null clears
     * the view. Triggers a repaint and refreshes preferredSize so the
     * enclosing scroll pane recomputes its viewport.
     */
    fun setDiagram(next: FlowDiagram?) {
        this.diagram = next
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
            val current = diagram ?: return
            val layout = computeLayout(current, width.coerceAtLeast(MIN_VIEW_WIDTH), g2.fontMetrics)
            paintNode(g2, layout.rootBox, current.scrutineeText, ROOT_FILL, ROOT_BORDER)
            for ((box, label) in layout.armBoxes) {
                paintNode(g2, box, label, ARM_FILL, ARM_BORDER)
            }
            paintEdges(g2, layout.edges)
        } finally {
            g2.dispose()
        }
    }

    private fun paintNode(
        g: Graphics2D,
        box: Rectangle,
        label: String,
        fill: Color,
        border: Color,
    ) {
        g.color = fill
        g.fillRoundRect(box.x, box.y, box.width, box.height, ROUND_RADIUS, ROUND_RADIUS)
        g.color = border
        g.stroke = BasicStroke(1.5f)
        g.drawRoundRect(box.x, box.y, box.width, box.height, ROUND_RADIUS, ROUND_RADIUS)
        g.color = JBColor.foreground()
        val fm = g.fontMetrics
        val lines = label.split('\n').take(2)
        val totalTextHeight = fm.height * lines.size
        var textY = box.y + (box.height - totalTextHeight) / 2 + fm.ascent
        for (line in lines) {
            val trimmed = truncateToWidth(line, fm, box.width - 2 * NODE_PADDING_X)
            val textX = box.x + (box.width - fm.stringWidth(trimmed)) / 2
            g.drawString(trimmed, textX, textY)
            textY += fm.height
        }
    }

    private fun paintEdges(
        g: Graphics2D,
        edges: List<List<Point>>,
    ) {
        g.color = EDGE_COLOR
        g.stroke = BasicStroke(1.5f)
        for (polyline in edges) {
            for (i in 0 until polyline.size - 1) {
                val a = polyline[i]
                val b = polyline[i + 1]
                g.drawLine(a.x, a.y, b.x, b.y)
            }
            // Draw an arrow head on the final segment, pointing from
            // the second-to-last vertex toward the last vertex.
            if (polyline.size >= 2) {
                paintArrowHead(g, polyline[polyline.size - 2], polyline.last())
            }
        }
    }

    private fun paintArrowHead(
        g: Graphics2D,
        from: Point,
        to: Point,
    ) {
        // Always vertical arrows (TD layout); paint a simple triangle.
        val arrow = Polygon()
        if (to.y > from.y) {
            arrow.addPoint(to.x, to.y)
            arrow.addPoint(to.x - ARROW_HALF_WIDTH, to.y - ARROW_HEIGHT)
            arrow.addPoint(to.x + ARROW_HALF_WIDTH, to.y - ARROW_HEIGHT)
        } else {
            arrow.addPoint(to.x, to.y)
            arrow.addPoint(to.x - ARROW_HALF_WIDTH, to.y + ARROW_HEIGHT)
            arrow.addPoint(to.x + ARROW_HALF_WIDTH, to.y + ARROW_HEIGHT)
        }
        g.fillPolygon(arrow)
    }

    private fun truncateToWidth(
        text: String,
        fm: FontMetrics,
        maxWidth: Int,
    ): String {
        if (fm.stringWidth(text) <= maxWidth) return text
        val ellipsis = "…"
        val ellipsisWidth = fm.stringWidth(ellipsis)
        var endIndex = text.length
        while (endIndex > 0 && fm.stringWidth(text.substring(0, endIndex)) + ellipsisWidth > maxWidth) {
            endIndex--
        }
        return text.substring(0, endIndex.coerceAtLeast(0)) + ellipsis
    }

    /**
     * Geometric result of laying out [diagram] inside a viewport of
     * width [viewportWidth].
     *
     * @property rootBox bounds of the scrutinee node
     * @property armBoxes pairs of (bounds, display label) for each
     *   first-level arm, in source order
     * @property edges polylines connecting the root to each arm; each
     *   polyline has 4 vertices for a clean orthogonal route
     *   (root-bottom → midline → arm-top-x → arm-top)
     * @property canvasSize total size required to contain all nodes
     *   and edges, including padding
     */
    data class Layout(
        val rootBox: Rectangle,
        val armBoxes: List<Pair<Rectangle, String>>,
        val edges: List<List<Point>>,
        val canvasSize: Dimension,
    )

    companion object {
        private const val MIN_VIEW_WIDTH = 420
        private const val MIN_VIEW_HEIGHT = 160
        private const val NODE_PADDING_X = 16
        private const val NODE_PADDING_Y = 8
        private const val ROOT_HEIGHT = 36
        private const val ARM_HEIGHT = 48
        private const val ROUND_RADIUS = 12
        private const val V_GAP = 32
        private const val H_GAP = 16
        private const val MARGIN = 12
        private const val ROOT_MIN_WIDTH = 180
        private const val ARM_MIN_WIDTH = 110
        private const val ARM_MAX_WIDTH = 240
        private const val ARROW_HALF_WIDTH = 5
        private const val ARROW_HEIGHT = 8
        private const val DEFAULT_CHAR_WIDTH = 7

        private val ROOT_FILL: Color = JBColor(Color(0xFFE7E8), Color(0x7A2226))
        private val ROOT_BORDER: Color = JBColor(Color(0xCB3939), Color(0xE6484F))
        private val ARM_FILL: Color = JBColor(Color(0xFFF3F4), Color(0x4A1518))
        private val ARM_BORDER: Color = JBColor(Color(0xCB3939), Color(0xE6484F))
        private val EDGE_COLOR: Color = JBColor(Color(0xCB3939), Color(0xE6484F))

        /**
         * Lays out [diagram] inside a viewport of width [viewportWidth].
         *
         * Optional [fm] is consulted to size each node to the actual
         * rendered width of its text; when null (as in unit tests) a
         * conservative per-character estimate is used so the layout
         * remains deterministic.
         *
         * When any top-level arm has nested children, a recursive tree
         * layout is used (sub-trees grow downwards, the canvas grows
         * horizontally so the enclosing scroll pane handles overflow).
         * Otherwise the flat row layout with wrapping is used.
         *
         * @param diagram diagram to render
         * @param viewportWidth available width in CSS-like pixels
         * @param fm optional font metrics used for label width
         * @return geometric layout
         */
        fun computeLayout(
            diagram: FlowDiagram,
            viewportWidth: Int,
            fm: FontMetrics? = null,
        ): Layout {
            val hasNested = diagram.arms.any { it.children.isNotEmpty() }
            return if (hasNested) {
                computeTreeLayout(diagram, viewportWidth, fm)
            } else {
                computeFlatLayout(diagram, viewportWidth, fm)
            }
        }

        /**
         * Flat layout: a single row of arm boxes beneath the scrutinee
         * root, wrapping onto additional rows when [viewportWidth] is
         * too narrow to fit them side by side.
         */
        private fun computeFlatLayout(
            diagram: FlowDiagram,
            viewportWidth: Int,
            fm: FontMetrics?,
        ): Layout {
            val rootLabel = "switch ${diagram.scrutineeText}"
            val rootWidth = nodeWidth(rootLabel, fm, ROOT_MIN_WIDTH, ARM_MAX_WIDTH)

            val armLabels = diagram.arms.map { armLabel(it) }
            val baseArmWidth =
                armLabels.maxOfOrNull { nodeWidth(it, fm, ARM_MIN_WIDTH, ARM_MAX_WIDTH) } ?: ARM_MIN_WIDTH

            // Decide how many arms can sit on a row given the viewport.
            val usableWidth = (viewportWidth - 2 * MARGIN).coerceAtLeast(baseArmWidth)
            val perRow = ((usableWidth + H_GAP) / (baseArmWidth + H_GAP)).coerceAtLeast(1)
            val rows = (armLabels.size + perRow - 1) / perRow
            val rowWidth = perRow * baseArmWidth + (perRow - 1).coerceAtLeast(0) * H_GAP
            val canvasWidth = (rowWidth + 2 * MARGIN).coerceAtLeast(rootWidth + 2 * MARGIN)

            val rootX = (canvasWidth - rootWidth) / 2
            val rootY = MARGIN
            val rootBox = Rectangle(rootX, rootY, rootWidth, ROOT_HEIGHT)

            val firstRowY = rootY + ROOT_HEIGHT + V_GAP
            val armBoxes = mutableListOf<Pair<Rectangle, String>>()
            val edges = mutableListOf<List<Point>>()

            for ((index, label) in armLabels.withIndex()) {
                val row = index / perRow
                val col = index % perRow
                val rowArmCount =
                    if (row < rows - 1 || armLabels.size % perRow == 0) {
                        perRow
                    } else {
                        armLabels.size % perRow
                    }
                val thisRowWidth = rowArmCount * baseArmWidth + (rowArmCount - 1).coerceAtLeast(0) * H_GAP
                val rowStartX = (canvasWidth - thisRowWidth) / 2
                val x = rowStartX + col * (baseArmWidth + H_GAP)
                val y = firstRowY + row * (ARM_HEIGHT + V_GAP)
                val box = Rectangle(x, y, baseArmWidth, ARM_HEIGHT)
                armBoxes.add(box to label)

                val rootBottom = Point(rootBox.x + rootBox.width / 2, rootBox.y + rootBox.height)
                val armTop = Point(box.x + box.width / 2, box.y)
                val midY = rootBottom.y + (armTop.y - rootBottom.y) / 2
                edges.add(
                    listOf(
                        rootBottom,
                        Point(rootBottom.x, midY),
                        Point(armTop.x, midY),
                        armTop,
                    ),
                )
            }

            val canvasHeight =
                if (armBoxes.isEmpty()) {
                    rootY + ROOT_HEIGHT + MARGIN
                } else {
                    val lastBox = armBoxes.last().first
                    lastBox.y + lastBox.height + MARGIN
                }

            return Layout(
                rootBox = rootBox,
                armBoxes = armBoxes,
                edges = edges,
                canvasSize = Dimension(canvasWidth, canvasHeight),
            )
        }

        /**
         * Tree layout: each top-level arm is laid out as its own
         * sub-tree via [computeArmSubtree], and the sub-trees are
         * placed side by side beneath the scrutinee root. The canvas
         * grows as wide as needed; the enclosing scroll pane (set up
         * in `RescriptVariantFlowPanel`) handles horizontal overflow.
         */
        private fun computeTreeLayout(
            diagram: FlowDiagram,
            viewportWidth: Int,
            fm: FontMetrics?,
        ): Layout {
            val subtrees = diagram.arms.map { computeArmSubtree(it, fm) }
            val armsTotalWidth =
                subtrees.sumOf { it.width } + (subtrees.size - 1).coerceAtLeast(0) * H_GAP
            val armsMaxHeight = subtrees.maxOfOrNull { it.height } ?: 0

            val rootLabel = "switch ${diagram.scrutineeText}"
            val rootWidth = nodeWidth(rootLabel, fm, ROOT_MIN_WIDTH, ARM_MAX_WIDTH)

            val contentWidth = maxOf(rootWidth, armsTotalWidth)
            val canvasWidth = (contentWidth + 2 * MARGIN).coerceAtLeast(viewportWidth)

            val rootX = (canvasWidth - rootWidth) / 2
            val rootY = MARGIN
            val rootBox = Rectangle(rootX, rootY, rootWidth, ROOT_HEIGHT)

            val armsStartX = (canvasWidth - armsTotalWidth) / 2
            val armsY = rootY + ROOT_HEIGHT + V_GAP

            val armBoxes = mutableListOf<Pair<Rectangle, String>>()
            val edges = mutableListOf<List<Point>>()
            val rootBottom = Point(rootBox.x + rootBox.width / 2, rootBox.y + rootBox.height)

            var cursorX = armsStartX
            for (subtree in subtrees) {
                val dx = cursorX
                val dy = armsY
                for ((box, label) in subtree.armBoxes) {
                    armBoxes.add(
                        Rectangle(box.x + dx, box.y + dy, box.width, box.height) to label,
                    )
                }
                for (polyline in subtree.edges) {
                    edges.add(polyline.map { Point(it.x + dx, it.y + dy) })
                }
                val subRootTopX = subtree.rootBox.x + dx + subtree.rootBox.width / 2
                val subRootTopY = subtree.rootBox.y + dy
                val midY = rootBottom.y + (subRootTopY - rootBottom.y) / 2
                edges.add(
                    listOf(
                        rootBottom,
                        Point(rootBottom.x, midY),
                        Point(subRootTopX, midY),
                        Point(subRootTopX, subRootTopY),
                    ),
                )
                cursorX += subtree.width + H_GAP
            }

            val canvasHeight = armsY + armsMaxHeight + MARGIN
            return Layout(
                rootBox = rootBox,
                armBoxes = armBoxes,
                edges = edges,
                canvasSize = Dimension(canvasWidth, canvasHeight),
            )
        }

        /**
         * Result of laying out a single arm together with its (recursive)
         * children. Coordinates are local to the sub-tree's bounding box
         * — callers translate by `(dx, dy)` when placing the sub-tree on
         * the canvas.
         *
         * @property rootBox bounds of the arm's own box, sub-tree-local
         * @property armBoxes own box plus every descendant box, with
         *   labels, in source order
         * @property edges polylines for every parent→child connection
         *   inside this sub-tree (does not include the edge from the
         *   outer scrutinee root)
         * @property width sub-tree's full horizontal footprint
         * @property height sub-tree's full vertical footprint
         */
        private data class ArmSubtree(
            val rootBox: Rectangle,
            val armBoxes: List<Pair<Rectangle, String>>,
            val edges: List<List<Point>>,
            val width: Int,
            val height: Int,
        )

        private fun computeArmSubtree(
            node: FlowNode,
            fm: FontMetrics?,
        ): ArmSubtree {
            val label = armLabel(node)
            val ownWidth = nodeWidth(label, fm, ARM_MIN_WIDTH, ARM_MAX_WIDTH)

            if (node.children.isEmpty()) {
                val rect = Rectangle(0, 0, ownWidth, ARM_HEIGHT)
                return ArmSubtree(
                    rootBox = rect,
                    armBoxes = listOf(rect to label),
                    edges = emptyList(),
                    width = ownWidth,
                    height = ARM_HEIGHT,
                )
            }

            val childSubtrees = node.children.map { computeArmSubtree(it, fm) }
            val childrenWidth =
                childSubtrees.sumOf { it.width } + (childSubtrees.size - 1).coerceAtLeast(0) * H_GAP
            val width = maxOf(ownWidth, childrenWidth)
            val maxChildHeight = childSubtrees.maxOf { it.height }
            val height = ARM_HEIGHT + V_GAP + maxChildHeight

            val ownX = (width - ownWidth) / 2
            val ownBox = Rectangle(ownX, 0, ownWidth, ARM_HEIGHT)

            val armBoxes = mutableListOf<Pair<Rectangle, String>>()
            val edges = mutableListOf<List<Point>>()
            armBoxes.add(ownBox to label)

            val childrenStartX = (width - childrenWidth) / 2
            var childX = childrenStartX
            val childY = ARM_HEIGHT + V_GAP
            val ownBottom = Point(ownBox.x + ownBox.width / 2, ownBox.y + ownBox.height)

            for (child in childSubtrees) {
                val dx = childX
                val dy = childY
                for ((box, lbl) in child.armBoxes) {
                    armBoxes.add(
                        Rectangle(box.x + dx, box.y + dy, box.width, box.height) to lbl,
                    )
                }
                for (polyline in child.edges) {
                    edges.add(polyline.map { Point(it.x + dx, it.y + dy) })
                }
                val childRootTopX = child.rootBox.x + dx + child.rootBox.width / 2
                val childRootTopY = child.rootBox.y + dy
                val midY = ownBottom.y + (childRootTopY - ownBottom.y) / 2
                edges.add(
                    listOf(
                        ownBottom,
                        Point(ownBottom.x, midY),
                        Point(childRootTopX, midY),
                        Point(childRootTopX, childRootTopY),
                    ),
                )
                childX += child.width + H_GAP
            }

            return ArmSubtree(
                rootBox = ownBox,
                armBoxes = armBoxes,
                edges = edges,
                width = width,
                height = height,
            )
        }

        private fun nodeWidth(
            label: String,
            fm: FontMetrics?,
            minWidth: Int,
            maxWidth: Int,
        ): Int {
            val widestLine =
                label.split('\n').maxOfOrNull { line ->
                    fm?.stringWidth(line) ?: (line.length * DEFAULT_CHAR_WIDTH)
                } ?: minWidth
            return (widestLine + 2 * NODE_PADDING_X)
                .coerceAtLeast(minWidth)
                .coerceAtMost(maxWidth)
        }

        private fun armLabel(node: FlowNode): String {
            val pattern = node.patternSummary.ifBlank { "(arm)" }
            // When this arm has nested children, the children render
            // the body visually below the arm box, so we drop the body
            // preview from the label to avoid duplicating the same
            // text both as a body string and as child boxes.
            if (node.children.isNotEmpty()) return pattern
            val body = node.bodyPreview
            return if (body.isBlank()) pattern else "$pattern\n$body"
        }
    }
}
