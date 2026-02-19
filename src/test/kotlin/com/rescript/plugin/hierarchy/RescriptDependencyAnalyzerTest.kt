package com.rescript.plugin.hierarchy

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptDependencyAnalyzerTest {
    @Test
    fun `module path joining from tokens`() {
        val tokens = listOf("Belt", ".", "Array")
        val result = tokens.joinToString("")
        assertEquals("Belt.Array", result)
    }

    @Test
    fun `single module name`() {
        val tokens = listOf("Utils")
        val result = tokens.joinToString("")
        assertEquals("Utils", result)
    }

    @Test
    fun `deeply nested module path`() {
        val tokens = listOf("Belt", ".", "Map", ".", "String")
        val result = tokens.joinToString("")
        assertEquals("Belt.Map.String", result)
    }

    @Test
    fun `top-level module extraction from paths`() {
        val paths = listOf("Belt.Array", "Belt.Map", "Js.Promise", "Utils")
        val topLevel = paths.map { it.substringBefore(".") }.toSet()
        assertEquals(setOf("Belt", "Js", "Utils"), topLevel)
    }

    @Test
    fun `ModuleNode data class holds correct values`() {
        val node =
            RescriptDependencyAnalyzer.ModuleNode(
                name = "MyModule",
                element = null,
                children = emptyList(),
            )
        assertEquals("MyModule", node.name)
        assertEquals(null, node.element)
        assertTrue(node.children.isEmpty())
    }

    @Test
    fun `ModuleNode supports nested children`() {
        val child =
            RescriptDependencyAnalyzer.ModuleNode(
                name = "Child",
                element = null,
                children = emptyList(),
            )
        val parent =
            RescriptDependencyAnalyzer.ModuleNode(
                name = "Parent",
                element = null,
                children = listOf(child),
            )
        assertEquals(1, parent.children.size)
        assertEquals("Child", parent.children[0].name)
    }

    @Test
    fun `ModuleNode supports multiple levels of nesting`() {
        val grandchild =
            RescriptDependencyAnalyzer.ModuleNode("GrandChild", null, emptyList())
        val child =
            RescriptDependencyAnalyzer.ModuleNode("Child", null, listOf(grandchild))
        val root =
            RescriptDependencyAnalyzer.ModuleNode("Root", null, listOf(child))

        assertEquals("Root", root.name)
        assertEquals("Child", root.children[0].name)
        assertEquals("GrandChild", root.children[0].children[0].name)
    }

    @Test
    fun `ReferenceKind has OPEN and INCLUDE values`() {
        assertEquals(2, RescriptDependencyAnalyzer.ReferenceKind.entries.size)
        assertEquals(
            RescriptDependencyAnalyzer.ReferenceKind.OPEN,
            RescriptDependencyAnalyzer.ReferenceKind.valueOf("OPEN"),
        )
        assertEquals(
            RescriptDependencyAnalyzer.ReferenceKind.INCLUDE,
            RescriptDependencyAnalyzer.ReferenceKind.valueOf("INCLUDE"),
        )
    }

    @Test
    fun `top-level module extraction handles empty path`() {
        val path = ""
        val topLevel = path.substringBefore(".")
        assertEquals("", topLevel)
    }

    @Test
    fun `top-level module extraction handles path without dot`() {
        val path = "Utils"
        val topLevel = path.substringBefore(".")
        assertEquals("Utils", topLevel)
    }

    @Test
    fun `top-level module extraction handles multi-dot path`() {
        val path = "Belt.Map.String"
        val topLevel = path.substringBefore(".")
        assertEquals("Belt", topLevel)
    }

    @Test
    fun `ModuleNode equality by data class`() {
        val node1 = RescriptDependencyAnalyzer.ModuleNode("A", null, emptyList())
        val node2 = RescriptDependencyAnalyzer.ModuleNode("A", null, emptyList())
        assertEquals(node1, node2)
    }

    @Test
    fun `empty module tree is empty list`() {
        val nodes = emptyList<RescriptDependencyAnalyzer.ModuleNode>()
        assertTrue(nodes.isEmpty())
    }
}
