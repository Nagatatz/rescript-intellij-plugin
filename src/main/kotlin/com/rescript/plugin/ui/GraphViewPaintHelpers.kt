package com.rescript.plugin.ui

import com.intellij.ui.JBColor
import java.awt.BasicStroke
import java.awt.Color
import java.awt.FontMetrics
import java.awt.Graphics2D
import java.awt.Point
import java.awt.Polygon
import java.awt.Rectangle

/**
 * Java2D painting helpers shared by the top-down graph views
 * (Variant Flow and Module Dependency Diagram).
 *
 * Both views used to carry identical copies of the orthogonal edge
 * painter, the arrow-head triangle, the ellipsis label truncation, and
 * the legend strip, along with the same drawing constants. This object
 * is the single source for those primitives; layout computation stays
 * in each view because the two use different algorithms (recursive
 * tree vs layered DAG).
 *
 * @see com.rescript.plugin.flow.RescriptVariantFlowGraphView
 * @see com.rescript.plugin.diagram.RescriptDependencyDiagramGraphView
 */
internal object GraphViewPaintHelpers {
    /** Edge / arrow colour used by both graph views (light, dark). */
    val DEFAULT_EDGE_COLOR: Color = JBColor(Color(0xCB3939), Color(0xE6484F))

    /** Height in pixels reserved at the bottom of the canvas for the legend strip. */
    const val LEGEND_HEIGHT = 28

    /** Corner arc radius used when drawing rounded-rectangle node boxes. */
    const val ROUND_RADIUS = 12

    /** Horizontal padding inside a node box, applied on each side. */
    const val NODE_PADDING_X = 16

    // Arrow-head triangle geometry shared by both views.
    private const val ARROW_HALF_WIDTH = 5
    private const val ARROW_HEIGHT = 8

    // Legend swatch geometry shared by both views.
    private const val LEGEND_SWATCH_SIZE = 14
    private const val LEGEND_ITEM_GAP = 12

    /**
     * One legend strip entry: a coloured swatch with [fill]/[border]
     * followed by [label] text.
     */
    data class LegendItem(
        val label: String,
        val fill: Color,
        val border: Color,
    )

    /**
     * Draws every polyline in [edges] with an arrow head on the final
     * segment, pointing from the second-to-last vertex toward the last
     * vertex.
     *
     * @param g target graphics, stroke and colour are overwritten
     * @param edges polylines as absolute canvas points
     * @param color edge and arrow colour
     */
    fun paintEdges(
        g: Graphics2D,
        edges: List<List<Point>>,
        color: Color = DEFAULT_EDGE_COLOR,
    ) {
        g.color = color
        g.stroke = BasicStroke(1.5f)
        for (polyline in edges) {
            for (i in 0 until polyline.size - 1) {
                val a = polyline[i]
                val b = polyline[i + 1]
                g.drawLine(a.x, a.y, b.x, b.y)
            }
            if (polyline.size >= 2) {
                paintArrowHead(g, polyline[polyline.size - 2], polyline.last())
            }
        }
    }

    /**
     * Paints a filled triangle at [to] pointing away from [from].
     * Arrows are assumed vertical (top-down layouts); a downward
     * triangle is drawn when [to] is at or below [from].
     *
     * @param g target graphics, current colour is used
     * @param from second-to-last vertex of the edge polyline
     * @param to final vertex where the head is placed
     */
    fun paintArrowHead(
        g: Graphics2D,
        from: Point,
        to: Point,
    ) {
        val arrow = Polygon()
        if (to.y >= from.y) {
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

    /**
     * Returns [text] unchanged when it fits into [maxWidth] pixels,
     * otherwise the longest prefix that fits together with a trailing
     * ellipsis.
     *
     * @param text label to fit
     * @param fm metrics of the font the label will be drawn with
     * @param maxWidth available width in pixels
     * @return the original text or a truncated variant ending in `…`
     */
    fun truncateToWidth(
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
     * Paints a rounded-rectangle node box with a centred, truncated label.
     *
     * The label is split on `\n`; at most [maxLines] lines are rendered.
     * Each line is truncated with an ellipsis when it would exceed
     * `box.width − 2 × [NODE_PADDING_X]` pixels.
     *
     * @param g target graphics; colour and stroke are overwritten
     * @param box bounding rectangle of the node on the canvas
     * @param label text to render (may contain `\n`)
     * @param fill background fill colour
     * @param border stroke colour for the rounded border
     * @param maxLines maximum number of lines to render (default 1)
     */
    fun paintNode(
        g: Graphics2D,
        box: Rectangle,
        label: String,
        fill: Color,
        border: Color,
        maxLines: Int = 1,
    ) {
        g.color = fill
        g.fillRoundRect(box.x, box.y, box.width, box.height, ROUND_RADIUS, ROUND_RADIUS)
        g.color = border
        g.stroke = BasicStroke(1.5f)
        g.drawRoundRect(box.x, box.y, box.width, box.height, ROUND_RADIUS, ROUND_RADIUS)
        g.color = JBColor.foreground()
        val fm = g.fontMetrics
        val lines = label.split('\n').take(maxLines)
        val totalTextHeight = fm.height * lines.size
        var textY = box.y + (box.height - totalTextHeight) / 2 + fm.ascent
        for (line in lines) {
            val trimmed = truncateToWidth(line, fm, box.width - 2 * NODE_PADDING_X)
            val textX = box.x + (box.width - fm.stringWidth(trimmed)) / 2
            g.drawString(trimmed, textX, textY)
            textY += fm.height
        }
    }

    /**
     * Draws the legend strip: a row of rounded swatches with labels,
     * left-aligned at [margin] and vertically anchored at [baseY].
     *
     * @param g target graphics
     * @param baseY top of the legend strip (canvas height minus [LEGEND_HEIGHT])
     * @param items legend entries drawn left-to-right
     * @param margin left inset in pixels
     */
    fun paintLegend(
        g: Graphics2D,
        baseY: Int,
        items: List<LegendItem>,
        margin: Int,
    ) {
        val fm = g.fontMetrics
        var x = margin
        val y = baseY + 4
        for (item in items) {
            g.color = item.fill
            g.fillRoundRect(x, y, LEGEND_SWATCH_SIZE, LEGEND_SWATCH_SIZE, 4, 4)
            g.color = item.border
            g.drawRoundRect(x, y, LEGEND_SWATCH_SIZE, LEGEND_SWATCH_SIZE, 4, 4)
            g.color = JBColor.foreground()
            g.drawString(item.label, x + LEGEND_SWATCH_SIZE + 4, y + fm.ascent)
            x += LEGEND_SWATCH_SIZE + 6 + fm.stringWidth(item.label) + LEGEND_ITEM_GAP
        }
    }
}
