package com.rescript.plugin.hierarchy

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.rescript.plugin.IntelliJPlatformExtension
import com.rescript.plugin.lang.psi.RescriptElementTypes
import com.rescript.plugin.lang.psi.RescriptFile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Integration tests for [RescriptModuleHierarchyTreeStructure] and
 * [RescriptModuleDependencyTreeStructure].
 *
 * Builds tree structures over real PSI files and verifies that:
 * - Module nesting tree exposes only nested MODULE_DECLARATION children.
 * - Module dependency tree expands at the file root and lists open/include
 *   targets, but does not expand non-file nodes.
 */
@ExtendWith(IntelliJPlatformExtension::class)
class RescriptModuleHierarchyTreeStructureTest {
    private lateinit var myFixture: CodeInsightTestFixture
    private lateinit var project: Project

    // ── RescriptModuleHierarchyTreeStructure (nesting) ──────────────────

    @Test
    fun testNestingTreeFindsNestedModuleChildren() {
        val source =
            """
            module Outer = {
              module Inner = {
                let x = 1
              }
            }
            """.trimIndent()
        val file = myFixture.configureByText("Foo.res", source) as RescriptFile
        val outer = findFirst(file, RescriptElementTypes.MODULE_DECLARATION)!!

        val structure = RescriptModuleHierarchyTreeStructure(project, outer)
        val rootDescriptor = structure.rootElement as com.intellij.ide.hierarchy.HierarchyNodeDescriptor
        val children = structure.getChildElements(rootDescriptor)

        assertEquals(1, children.size)
        assertTrue(children[0] is RescriptModuleHierarchyNodeDescriptor)
    }

    @Test
    fun testNestingTreeReturnsEmptyForLeafModule() {
        val file = myFixture.configureByText("Foo.res", "module Solo = { let x = 1 }") as RescriptFile
        val solo = findFirst(file, RescriptElementTypes.MODULE_DECLARATION)!!

        val structure = RescriptModuleHierarchyTreeStructure(project, solo)
        val rootDescriptor = structure.rootElement as com.intellij.ide.hierarchy.HierarchyNodeDescriptor
        val children = structure.getChildElements(rootDescriptor)

        assertEquals(0, children.size)
    }

    @Test
    fun testNestingTreeExposesAllNestedModulesAtTopLevel() {
        val source =
            """
            module A = { let x = 1 }
            module B = { let y = 2 }
            """.trimIndent()
        val file = myFixture.configureByText("Foo.res", source) as RescriptFile

        val structure = RescriptModuleHierarchyTreeStructure(project, file)
        val rootDescriptor = structure.rootElement as com.intellij.ide.hierarchy.HierarchyNodeDescriptor
        val children = structure.getChildElements(rootDescriptor)

        assertEquals(2, children.size)
    }

    // ── RescriptModuleDependencyTreeStructure ────────────────────────────

    @Test
    fun testDependencyTreeListsOpenStatements() {
        val source =
            """
            open Belt
            open Js.Array

            let x = 1
            """.trimIndent()
        val file = myFixture.configureByText("Foo.res", source) as RescriptFile

        val structure = RescriptModuleDependencyTreeStructure(project, file)
        val rootDescriptor = structure.rootElement as com.intellij.ide.hierarchy.HierarchyNodeDescriptor
        val children = structure.getChildElements(rootDescriptor)

        assertEquals(2, children.size)
    }

    @Test
    fun testDependencyTreeReturnsEmptyForNonFileRoot() {
        val file = myFixture.configureByText("Foo.res", "module Bar = { open Belt }") as RescriptFile
        val module = findFirst(file, RescriptElementTypes.MODULE_DECLARATION)!!

        val structure = RescriptModuleDependencyTreeStructure(project, module)
        val rootDescriptor = structure.rootElement as com.intellij.ide.hierarchy.HierarchyNodeDescriptor
        // Non-file roots are not expanded — only the file itself yields dependency children.
        val children = structure.getChildElements(rootDescriptor)
        assertEquals(0, children.size)
    }

    private fun findFirst(
        scope: PsiElement,
        elementType: com.intellij.psi.tree.IElementType,
    ): PsiElement? {
        if (scope.node?.elementType == elementType) return scope
        for (child in scope.children) {
            val found = findFirst(child, elementType)
            if (found != null) return found
        }
        return null
    }
}
