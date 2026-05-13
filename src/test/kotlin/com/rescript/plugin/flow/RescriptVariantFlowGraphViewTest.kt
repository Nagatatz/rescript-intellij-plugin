package com.rescript.plugin.flow

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Unit tests for [RescriptVariantFlowGraphView.computeLayout], the
 * pure layout helper that owns all of the geometry decisions for the
 * visual switch-flow view. The Java2D painting code itself is
 * exercised at runtime in the IDE; here we lock down the layout so
 * future tweaks to spacing / cap widths can be reviewed with diffs.
 */
class RescriptVariantFlowGraphViewTest {
    private fun makeArm(
        id: String,
        pattern: String,
        body: String = "",
    ) = FlowNode(id = id, patternSummary = pattern, bodyPreview = body, children = emptyList())

    @Test
    fun `single-arm diagram produces a root + arm + connecting edge`() {
        val diagram =
            FlowDiagram(
                scrutineeText = "x",
                arms = listOf(makeArm("a", "Some(_)")),
            )
        val layout = RescriptVariantFlowGraphView.computeLayout(diagram, viewportWidth = 600)
        assertEquals(1, layout.armBoxes.size)
        assertEquals(1, layout.edges.size)
        // Edge is a 4-vertex orthogonal polyline.
        assertEquals(4, layout.edges.single().size)
    }

    @Test
    fun `root box sits above the arm row`() {
        val diagram =
            FlowDiagram(
                scrutineeText = "x",
                arms = listOf(makeArm("a", "Some(_)")),
            )
        val layout = RescriptVariantFlowGraphView.computeLayout(diagram, viewportWidth = 600)
        val arm = layout.armBoxes.single().first
        assertTrue(layout.rootBox.y + layout.rootBox.height <= arm.y)
    }

    @Test
    fun `three arms layout produces three boxes and three edges`() {
        val diagram =
            FlowDiagram(
                scrutineeText = "status",
                arms =
                    listOf(
                        makeArm("a", "Loading"),
                        makeArm("b", "Loaded(_)"),
                        makeArm("c", "Failed(_)"),
                    ),
            )
        val layout = RescriptVariantFlowGraphView.computeLayout(diagram, viewportWidth = 800)
        assertEquals(3, layout.armBoxes.size)
        assertEquals(3, layout.edges.size)
        // All edges originate at the root's bottom midpoint.
        val rootBottomMid = layout.rootBox.x + layout.rootBox.width / 2
        for (edge in layout.edges) {
            assertEquals(rootBottomMid, edge.first().x)
            assertEquals(layout.rootBox.y + layout.rootBox.height, edge.first().y)
        }
    }

    @Test
    fun `canvas size grows to fit content`() {
        val many =
            (1..8).map { makeArm("a$it", "Constructor$it") }
        val layout =
            RescriptVariantFlowGraphView.computeLayout(
                FlowDiagram("x", many),
                viewportWidth = 600,
            )
        // Canvas must accommodate every arm box.
        for ((box, _) in layout.armBoxes) {
            assertTrue(box.x + box.width <= layout.canvasSize.width)
            assertTrue(box.y + box.height <= layout.canvasSize.height)
        }
    }

    @Test
    fun `arm boxes line up horizontally on a single row when there is space`() {
        val arms =
            listOf(
                makeArm("a", "A"),
                makeArm("b", "B"),
                makeArm("c", "C"),
            )
        val layout =
            RescriptVariantFlowGraphView.computeLayout(
                FlowDiagram("x", arms),
                viewportWidth = 1200, // plenty of room
            )
        // All three arms share the same top y.
        val ys = layout.armBoxes.map { it.first.y }.toSet()
        assertEquals(1, ys.size, "expected a single row, got rows at $ys")
    }

    @Test
    fun `arm boxes wrap to a second row when viewport is narrow`() {
        val arms =
            (1..6).map { makeArm("a$it", "Constructor$it") }
        val layout =
            RescriptVariantFlowGraphView.computeLayout(
                FlowDiagram("x", arms),
                viewportWidth = 300, // intentionally cramped
            )
        // Expect at least two distinct y positions for arm boxes.
        val rows = layout.armBoxes.map { it.first.y }.toSet()
        assertNotEquals(1, rows.size, "expected wrapping into multiple rows")
    }

    @Test
    fun `arm label includes pattern and body preview when both are present`() {
        val arm = makeArm("a", "Some(_)", body = "doStuff()")
        val layout =
            RescriptVariantFlowGraphView.computeLayout(
                FlowDiagram("x", listOf(arm)),
                viewportWidth = 600,
            )
        val label = layout.armBoxes.single().second
        assertTrue(label.contains("Some(_)"))
        assertTrue(label.contains("doStuff()"))
    }

    @Test
    fun `empty arms list still produces a root box`() {
        val layout =
            RescriptVariantFlowGraphView.computeLayout(
                FlowDiagram("x", emptyList()),
                viewportWidth = 600,
            )
        assertEquals(0, layout.armBoxes.size)
        assertEquals(0, layout.edges.size)
        assertTrue(layout.rootBox.width > 0)
    }
}
