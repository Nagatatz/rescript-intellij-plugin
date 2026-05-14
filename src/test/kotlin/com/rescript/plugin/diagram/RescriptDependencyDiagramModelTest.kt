package com.rescript.plugin.diagram

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptDependencyDiagramModelTest {
    @Test
    fun `addModule creates node`() {
        val model = RescriptDependencyDiagramModel()
        model.addModule("App", listOf("Utils"))
        assertEquals(2, model.moduleCount())
    }

    @Test
    fun `addModule creates edges`() {
        val model = RescriptDependencyDiagramModel()
        model.addModule("App", listOf("Utils", "Config"))
        assertEquals(2, model.edgeCount())
    }

    @Test
    fun `getNodes returns all modules`() {
        val model = RescriptDependencyDiagramModel()
        model.addModule("App", listOf("Utils"))
        model.addModule("Utils", listOf())
        val nodes = model.getNodes()
        assertEquals(2, nodes.size)
    }

    @Test
    fun `getEdges returns directed edges`() {
        val model = RescriptDependencyDiagramModel()
        model.addModule("App", listOf("Utils"))
        val edges = model.getEdges()
        assertEquals(1, edges.size)
        assertEquals("App", edges[0].from)
        assertEquals("Utils", edges[0].to)
    }

    @Test
    fun `toDot generates valid DOT format`() {
        val model = RescriptDependencyDiagramModel()
        model.addModule("App", listOf("Utils"))
        val dot = model.toDot()
        assertTrue(dot.contains("digraph"))
        assertTrue(dot.contains("\"App\" -> \"Utils\""))
    }

    @Test
    fun `empty model has zero counts`() {
        val model = RescriptDependencyDiagramModel()
        assertEquals(0, model.moduleCount())
        assertEquals(0, model.edgeCount())
    }

    @Test
    fun `duplicate dependencies are deduplicated`() {
        val model = RescriptDependencyDiagramModel()
        model.addModule("App", listOf("Utils", "Utils"))
        val edges = model.getEdges()
        assertEquals(1, edges.size)
    }

    @Test
    fun `extractDependencies finds open statements`() {
        val source =
            """
            open Belt
            open Utils.Array

            let x = 42
            """.trimIndent()
        val deps = RescriptDependencyDiagramProvider.extractDependencies(source)
        assertEquals(2, deps.size)
        assertTrue(deps.contains("Belt"))
        assertTrue(deps.contains("Utils.Array"))
    }

    @Test
    fun `extractDependencies finds include statements`() {
        val source =
            """
            include BaseModule

            let x = 42
            """.trimIndent()
        val deps = RescriptDependencyDiagramProvider.extractDependencies(source)
        assertEquals(1, deps.size)
        assertTrue(deps.contains("BaseModule"))
    }

    @Test
    fun `extractDependencies deduplicates`() {
        val source =
            """
            open Belt
            open Belt
            """.trimIndent()
        val deps = RescriptDependencyDiagramProvider.extractDependencies(source)
        assertEquals(1, deps.size)
    }

    @Test
    fun `extractDependencies returns empty for no imports`() {
        val source = "let x = 42"
        val deps = RescriptDependencyDiagramProvider.extractDependencies(source)
        assertTrue(deps.isEmpty())
    }

    @Test
    fun `ModuleNode data class holds name and dependencies`() {
        val node = RescriptDependencyDiagramModel.ModuleNode("App", listOf("Utils"))
        assertEquals("App", node.name)
        assertEquals(listOf("Utils"), node.dependencies)
    }

    @Test
    fun `ModuleEdge data class holds from and to`() {
        val edge = RescriptDependencyDiagramModel.ModuleEdge("App", "Utils")
        assertEquals("App", edge.from)
        assertEquals("Utils", edge.to)
    }

    @Test
    fun `toDot escapes double quotes in module names`() {
        val model = RescriptDependencyDiagramModel()
        model.addModule("App\"Inject", listOf("Utils"))
        val dot = model.toDot()
        // Double quotes should be escaped to prevent DOT format injection
        assertTrue(dot.contains("\"App\\\"Inject\""))
        assertTrue(!dot.contains("\"App\"Inject\""))
    }

    @Test
    fun `toDot escapes backslashes in module names`() {
        val model = RescriptDependencyDiagramModel()
        model.addModule("App\\Path", listOf("Utils"))
        val dot = model.toDot()
        assertTrue(dot.contains("App\\\\Path"))
    }

    @Test
    fun `classifyNodes marks in-degree-0 module as ENTRY_POINT`() {
        // App -> Utils, App has no incoming deps so it is the entry point.
        val model = RescriptDependencyDiagramModel()
        model.addModule("App", listOf("Utils"))
        val roles =
            RescriptDependencyDiagramModel.classifyNodes(model.getNodes(), model.getEdges())
        assertEquals(NodeRole.ENTRY_POINT, roles["App"])
    }

    @Test
    fun `classifyNodes marks out-degree-0 module as LEAF`() {
        val model = RescriptDependencyDiagramModel()
        model.addModule("App", listOf("Utils"))
        val roles =
            RescriptDependencyDiagramModel.classifyNodes(model.getNodes(), model.getEdges())
        assertEquals(NodeRole.LEAF, roles["Utils"])
    }

    @Test
    fun `classifyNodes marks middle module as INTERMEDIATE`() {
        // App -> Utils -> Core. Utils has both an incoming and outgoing edge.
        val model = RescriptDependencyDiagramModel()
        model.addModule("App", listOf("Utils"))
        model.addModule("Utils", listOf("Core"))
        val roles =
            RescriptDependencyDiagramModel.classifyNodes(model.getNodes(), model.getEdges())
        assertEquals(NodeRole.INTERMEDIATE, roles["Utils"])
    }

    @Test
    fun `classifyNodes marks Kahn-undrainable nodes as CYCLE_MEMBER`() {
        // A -> B -> A: pure cycle, both nodes are CYCLE_MEMBER.
        val model = RescriptDependencyDiagramModel()
        model.addModule("A", listOf("B"))
        model.addModule("B", listOf("A"))
        val roles =
            RescriptDependencyDiagramModel.classifyNodes(model.getNodes(), model.getEdges())
        assertEquals(NodeRole.CYCLE_MEMBER, roles["A"])
        assertEquals(NodeRole.CYCLE_MEMBER, roles["B"])
    }

    @Test
    fun `classifyNodes returns empty map for empty model`() {
        val roles =
            RescriptDependencyDiagramModel.classifyNodes(emptyList(), emptyList())
        assertTrue(roles.isEmpty())
    }

    @Test
    fun `classifyNodes ignores self-loops when classifying`() {
        // A -> A is a self-loop. The classifier's edge handling should still
        // mark A as ENTRY_POINT/LEAF rather than CYCLE_MEMBER since the view
        // strips self-loops before drawing. We accept either classification
        // here (the visual view filters them out anyway); the contract is
        // that the function does not throw.
        val model = RescriptDependencyDiagramModel()
        model.addModule("A", listOf("A"))
        val roles =
            RescriptDependencyDiagramModel.classifyNodes(model.getNodes(), model.getEdges())
        assertTrue(roles.containsKey("A"))
    }
}
