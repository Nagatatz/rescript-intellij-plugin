package com.rescript.plugin.diagram

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Tests for [RescriptDependencyDiagramGraphView.computeLayout], the pure
 * layout function used by the Visual mode of the dependency diagram tool
 * window. The actual `JComponent` painting is exercised indirectly — the
 * tests cover only the geometry, which is the part that can fail in
 * surprising ways (cycles, branching, deterministic ordering).
 */
class RescriptDependencyDiagramGraphViewTest {
    private val viewport = 600

    @Test
    fun `empty model produces no nodes and no edges`() {
        val model = RescriptDependencyDiagramModel()
        val layout = RescriptDependencyDiagramGraphView.computeLayout(model, viewport)
        assertTrue(layout.nodes.isEmpty(), "no nodes expected")
        assertTrue(layout.edges.isEmpty(), "no edges expected")
        assertTrue(layout.canvasSize.width >= RescriptDependencyDiagramGraphView.MIN_VIEW_WIDTH)
        assertTrue(layout.canvasSize.height >= RescriptDependencyDiagramGraphView.MIN_VIEW_HEIGHT)
    }

    @Test
    fun `single module sits on layer zero`() {
        val model = RescriptDependencyDiagramModel().apply { addModule("Solo", emptyList()) }
        val layout = RescriptDependencyDiagramGraphView.computeLayout(model, viewport)
        assertEquals(1, layout.nodes.size)
        assertEquals("Solo", layout.nodes.single().name)
        assertEquals(0, layout.nodes.single().layer)
        assertTrue(layout.edges.isEmpty())
    }

    @Test
    fun `linear chain produces one node per layer`() {
        // A -> B -> C -> D
        val model =
            RescriptDependencyDiagramModel().apply {
                addModule("A", listOf("B"))
                addModule("B", listOf("C"))
                addModule("C", listOf("D"))
            }
        val layout = RescriptDependencyDiagramGraphView.computeLayout(model, viewport)
        val byName = layout.nodes.associateBy { it.name }
        assertEquals(0, byName.getValue("A").layer)
        assertEquals(1, byName.getValue("B").layer)
        assertEquals(2, byName.getValue("C").layer)
        assertEquals(3, byName.getValue("D").layer)
        assertEquals(3, layout.edges.size)
    }

    @Test
    fun `branching converges onto deepest layer`() {
        // A -> B, A -> C, B -> D, C -> D
        val model =
            RescriptDependencyDiagramModel().apply {
                addModule("A", listOf("B", "C"))
                addModule("B", listOf("D"))
                addModule("C", listOf("D"))
            }
        val layout = RescriptDependencyDiagramGraphView.computeLayout(model, viewport)
        val byName = layout.nodes.associateBy { it.name }
        assertEquals(0, byName.getValue("A").layer)
        assertEquals(1, byName.getValue("B").layer)
        assertEquals(1, byName.getValue("C").layer)
        assertEquals(2, byName.getValue("D").layer)
        assertEquals(4, layout.edges.size)
    }

    @Test
    fun `cycle members still appear in layout`() {
        // A <-> B, isolated C with no deps
        val model =
            RescriptDependencyDiagramModel().apply {
                addModule("A", listOf("B"))
                addModule("B", listOf("A"))
                addModule("C", emptyList())
            }
        val layout = RescriptDependencyDiagramGraphView.computeLayout(model, viewport)
        val names = layout.nodes.map { it.name }.toSet()
        assertTrue("A" in names && "B" in names && "C" in names, "all cycle nodes should be drawn")
        // C has no incoming edges so it must be placed at layer 0; the
        // cycle nodes can't be drained by Kahn's and pile onto the
        // fallback layer beneath C.
        assertEquals(0, layout.nodes.single { it.name == "C" }.layer)
        val cycleLayer = layout.nodes.single { it.name == "A" }.layer
        assertTrue(cycleLayer > 0)
        assertEquals(cycleLayer, layout.nodes.single { it.name == "B" }.layer)
        assertEquals(2, layout.edges.size)
    }

    @Test
    fun `self loop is suppressed from edges but node still renders`() {
        val model = RescriptDependencyDiagramModel().apply { addModule("Recursive", listOf("Recursive")) }
        val layout = RescriptDependencyDiagramGraphView.computeLayout(model, viewport)
        assertEquals(1, layout.nodes.size)
        assertEquals(0, layout.nodes.single().layer)
        assertTrue(layout.edges.isEmpty(), "self-loops are skipped in v1")
    }

    @Test
    fun `layer zero is centered horizontally within the canvas`() {
        val model = RescriptDependencyDiagramModel().apply { addModule("Single", emptyList()) }
        val layout = RescriptDependencyDiagramGraphView.computeLayout(model, viewport)
        val box = layout.nodes.single().box
        val leftMargin = box.x
        val rightMargin = layout.canvasSize.width - (box.x + box.width)
        // Horizontal centering should leave roughly the same margin on
        // both sides; allow a 1px rounding difference.
        assertTrue(kotlin.math.abs(leftMargin - rightMargin) <= 1, "node should be centered")
    }

    @Test
    fun `layout is deterministic across repeated invocations`() {
        val model =
            RescriptDependencyDiagramModel().apply {
                addModule("App", listOf("Utils", "Config"))
                addModule("Utils", listOf("Strings"))
                addModule("Config", emptyList())
            }
        val a = RescriptDependencyDiagramGraphView.computeLayout(model, viewport)
        val b = RescriptDependencyDiagramGraphView.computeLayout(model, viewport)
        assertEquals(a.canvasSize, b.canvasSize)
        assertEquals(a.nodes, b.nodes)
        assertEquals(a.edges, b.edges)
    }

    @Test
    fun `every placed node fits within the reported canvas size`() {
        val model =
            RescriptDependencyDiagramModel().apply {
                addModule("App", listOf("Utils", "Config", "Routes"))
                addModule("Utils", listOf("Strings", "Numbers"))
                addModule("Config", emptyList())
            }
        val layout = RescriptDependencyDiagramGraphView.computeLayout(model, viewport)
        assertFalse(layout.nodes.isEmpty())
        for (node in layout.nodes) {
            assertTrue(node.box.x >= 0)
            assertTrue(node.box.y >= 0)
            assertTrue(node.box.x + node.box.width <= layout.canvasSize.width)
            assertTrue(node.box.y + node.box.height <= layout.canvasSize.height)
        }
    }

    @Test
    fun `edges connect named modules with four-point polylines`() {
        val model =
            RescriptDependencyDiagramModel().apply {
                addModule("Top", listOf("Bottom"))
            }
        val layout = RescriptDependencyDiagramGraphView.computeLayout(model, viewport)
        val polyline = layout.edges.single()
        assertEquals(4, polyline.size, "orthogonal edge should have 4 vertices")
        val topBox = layout.nodes.single { it.name == "Top" }.box
        val bottomBox = layout.nodes.single { it.name == "Bottom" }.box
        // First vertex sits on Top's bottom edge, last vertex sits on
        // Bottom's top edge.
        assertEquals(topBox.y + topBox.height, polyline.first().y)
        assertEquals(bottomBox.y, polyline.last().y)
        assertNotNull(polyline.last())
    }
}
