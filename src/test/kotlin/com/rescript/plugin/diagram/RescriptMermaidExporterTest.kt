package com.rescript.plugin.diagram

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptMermaidExporterTest {
    @Test
    fun `empty model produces graph header only`() {
        val model = RescriptDependencyDiagramModel()
        val mermaid = RescriptMermaidExporter.toMermaid(model)
        assertEquals("graph TD\n", mermaid)
    }

    @Test
    fun `single module without dependencies emits node declaration`() {
        val model = RescriptDependencyDiagramModel()
        model.addModule("App", listOf())
        val mermaid = RescriptMermaidExporter.toMermaid(model)
        assertTrue(mermaid.startsWith("graph TD\n"))
        assertTrue(mermaid.contains("App[\"App\"]"))
        assertFalse(mermaid.contains("-->"))
    }

    @Test
    fun `simple dependency produces node and edge lines`() {
        val model = RescriptDependencyDiagramModel()
        model.addModule("App", listOf("Utils"))
        val mermaid = RescriptMermaidExporter.toMermaid(model)
        assertTrue(mermaid.contains("App[\"App\"]"))
        assertTrue(mermaid.contains("Utils[\"Utils\"]"))
        assertTrue(mermaid.contains("App --> Utils"))
    }

    @Test
    fun `module names with special characters are sanitized to safe ids`() {
        val model = RescriptDependencyDiagramModel()
        model.addModule("My-Module.A", listOf("Other Module"))
        val mermaid = RescriptMermaidExporter.toMermaid(model)
        assertTrue(mermaid.contains("My_Module_A"))
        assertTrue(mermaid.contains("Other_Module"))
        assertTrue(mermaid.contains("My_Module_A --> Other_Module"))
        assertTrue(mermaid.contains("[\"My-Module.A\"]"))
        assertTrue(mermaid.contains("[\"Other Module\"]"))
    }

    @Test
    fun `module names starting with digit get n_ prefix`() {
        val model = RescriptDependencyDiagramModel()
        model.addModule("3DRenderer", listOf())
        val mermaid = RescriptMermaidExporter.toMermaid(model)
        assertTrue(mermaid.contains("n_3DRenderer[\"3DRenderer\"]"))
    }

    @Test
    fun `colliding sanitized names are disambiguated with numeric suffix`() {
        val model = RescriptDependencyDiagramModel()
        model.addModule("Foo Bar", listOf())
        model.addModule("Foo-Bar", listOf())
        val mermaid = RescriptMermaidExporter.toMermaid(model)
        assertTrue(mermaid.contains("Foo_Bar[\"Foo Bar\"]"))
        assertTrue(mermaid.contains("Foo_Bar_1[\"Foo-Bar\"]"))
    }

    @Test
    fun `quotes in module names are escaped to prevent label injection`() {
        val model = RescriptDependencyDiagramModel()
        model.addModule("Weird\"Name", listOf())
        val mermaid = RescriptMermaidExporter.toMermaid(model)
        assertTrue(mermaid.contains("Weird&quot;Name"))
        assertFalse(mermaid.contains("\"Weird\""))
    }

    @Test
    fun `backslashes in module names are escaped`() {
        val model = RescriptDependencyDiagramModel()
        model.addModule("Path\\Module", listOf())
        val mermaid = RescriptMermaidExporter.toMermaid(model)
        assertTrue(mermaid.contains("Path\\\\Module"))
    }

    @Test
    fun `isolated dependency target nodes are emitted as nodes`() {
        val model = RescriptDependencyDiagramModel()
        model.addModule("App", listOf("Utils", "Config"))
        val mermaid = RescriptMermaidExporter.toMermaid(model)
        assertTrue(mermaid.contains("Utils[\"Utils\"]"))
        assertTrue(mermaid.contains("Config[\"Config\"]"))
    }

    @Test
    fun `mermaid output ends with newline`() {
        val model = RescriptDependencyDiagramModel()
        model.addModule("App", listOf("Utils"))
        val mermaid = RescriptMermaidExporter.toMermaid(model)
        assertTrue(mermaid.endsWith("\n"))
    }

    @Test
    fun `id assignment is stable across calls`() {
        val model = RescriptDependencyDiagramModel()
        model.addModule("A", listOf("B"))
        model.addModule("B", listOf())
        val first = RescriptMermaidExporter.toMermaid(model)
        val second = RescriptMermaidExporter.toMermaid(model)
        assertEquals(first, second)
    }

    @Test
    fun `distinct names get distinct ids`() {
        val model = RescriptDependencyDiagramModel()
        model.addModule("Foo Bar", listOf())
        model.addModule("Foo-Bar", listOf())
        model.addModule("Foo.Bar", listOf())
        val mermaid = RescriptMermaidExporter.toMermaid(model)
        assertTrue(mermaid.contains("Foo_Bar["))
        assertTrue(mermaid.contains("Foo_Bar_1["))
        assertTrue(mermaid.contains("Foo_Bar_2["))
        // Each label appears with the correct original name
        assertTrue(mermaid.contains("[\"Foo Bar\"]"))
        assertTrue(mermaid.contains("[\"Foo-Bar\"]"))
        assertTrue(mermaid.contains("[\"Foo.Bar\"]"))
        assertNotEquals(0, mermaid.indexOf("Foo_Bar_2"))
    }
}
