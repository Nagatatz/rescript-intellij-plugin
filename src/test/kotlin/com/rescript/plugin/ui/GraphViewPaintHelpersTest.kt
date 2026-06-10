package com.rescript.plugin.ui

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.awt.Color
import java.awt.Point
import java.awt.image.BufferedImage

/**
 * Tests for the shared graph painting primitives.
 *
 * Painting methods are exercised against a [BufferedImage] graphics
 * context (no live UI needed); `truncateToWidth` is verified as a pure
 * function using the image's real font metrics.
 */
class GraphViewPaintHelpersTest {
    private fun graphics(): Pair<BufferedImage, java.awt.Graphics2D> {
        val image = BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB)
        return image to image.createGraphics()
    }

    @Test
    fun `paintEdges draws polylines and arrow heads without throwing`() {
        val (_, g) = graphics()
        try {
            val edges =
                listOf(
                    listOf(Point(10, 10), Point(10, 50), Point(60, 50), Point(60, 90)),
                    // Upward edge exercises the second arrow-head branch.
                    listOf(Point(100, 90), Point(100, 10)),
                    // Degenerate single-point polyline must not paint an arrow.
                    listOf(Point(5, 5)),
                )
            GraphViewPaintHelpers.paintEdges(g, edges)
            GraphViewPaintHelpers.paintEdges(g, edges, Color.BLUE)
        } finally {
            g.dispose()
        }
    }

    @Test
    fun `paintEdges leaves a coloured pixel along a drawn segment`() {
        val (image, g) = graphics()
        try {
            GraphViewPaintHelpers.paintEdges(
                g,
                listOf(listOf(Point(10, 10), Point(10, 100))),
                Color.RED,
            )
        } finally {
            g.dispose()
        }
        assertEquals(Color.RED.rgb, image.getRGB(10, 50))
    }

    @Test
    fun `paintLegend draws swatches and labels without throwing`() {
        val (image, g) = graphics()
        try {
            val items =
                listOf(
                    GraphViewPaintHelpers.LegendItem("Alpha", Color.RED, Color.BLACK),
                    GraphViewPaintHelpers.LegendItem("Beta", Color.GREEN, Color.BLUE),
                )
            GraphViewPaintHelpers.paintLegend(g, 150, items, 12)
        } finally {
            g.dispose()
        }
        // The first swatch is filled at (margin.., baseY+4..); sample its centre.
        assertEquals(Color.RED.rgb, image.getRGB(18, 160))
    }

    @Test
    fun `truncateToWidth returns short text unchanged`() {
        val (_, g) = graphics()
        try {
            val fm = g.fontMetrics
            val text = "Short"
            assertEquals(text, GraphViewPaintHelpers.truncateToWidth(text, fm, fm.stringWidth(text)))
        } finally {
            g.dispose()
        }
    }

    @Test
    fun `truncateToWidth appends an ellipsis to long text and fits the budget`() {
        val (_, g) = graphics()
        try {
            val fm = g.fontMetrics
            val text = "A rather long label that will not fit"
            val maxWidth = fm.stringWidth(text) / 3
            val truncated = GraphViewPaintHelpers.truncateToWidth(text, fm, maxWidth)
            assertTrue(truncated.endsWith("…"), "expected ellipsis suffix, got: $truncated")
            assertTrue(fm.stringWidth(truncated) <= maxWidth + fm.stringWidth("…"))
            assertTrue(truncated.length < text.length)
        } finally {
            g.dispose()
        }
    }
}
