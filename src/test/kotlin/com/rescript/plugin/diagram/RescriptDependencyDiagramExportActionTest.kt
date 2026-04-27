package com.rescript.plugin.diagram

import com.rescript.plugin.diagram.RescriptDependencyDiagramExportAction.Format
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptDependencyDiagramExportActionTest {
    @Test
    fun `DOT format renders Graphviz digraph header`() {
        val model = RescriptDependencyDiagramModel()
        model.addModule("App", listOf("Utils"))
        val out = RescriptDependencyDiagramExportAction.render(model, Format.DOT)
        assertTrue(out.startsWith("digraph"))
        assertTrue(out.contains("\"App\" -> \"Utils\""))
    }

    @Test
    fun `Mermaid format renders graph TD header`() {
        val model = RescriptDependencyDiagramModel()
        model.addModule("App", listOf("Utils"))
        val out = RescriptDependencyDiagramExportAction.render(model, Format.MERMAID)
        assertTrue(out.startsWith("graph TD"))
        assertTrue(out.contains("App --> Utils"))
    }

    @Test
    fun `DOT and Mermaid produce different output for the same model`() {
        val model = RescriptDependencyDiagramModel()
        model.addModule("App", listOf("Utils"))
        val dot = RescriptDependencyDiagramExportAction.render(model, Format.DOT)
        val mermaid = RescriptDependencyDiagramExportAction.render(model, Format.MERMAID)
        assertNotEquals(dot, mermaid)
    }

    @Test
    fun `empty model renders without throwing in DOT`() {
        val model = RescriptDependencyDiagramModel()
        val out = RescriptDependencyDiagramExportAction.render(model, Format.DOT)
        assertTrue(out.contains("digraph"))
    }

    @Test
    fun `empty model renders without throwing in Mermaid`() {
        val model = RescriptDependencyDiagramModel()
        val out = RescriptDependencyDiagramExportAction.render(model, Format.MERMAID)
        assertEquals("graph TD\n", out)
    }

    @Test
    fun `render is deterministic for repeated calls`() {
        val model = RescriptDependencyDiagramModel()
        model.addModule("A", listOf("B", "C"))
        val first = RescriptDependencyDiagramExportAction.render(model, Format.MERMAID)
        val second = RescriptDependencyDiagramExportAction.render(model, Format.MERMAID)
        assertEquals(first, second)
    }
}
