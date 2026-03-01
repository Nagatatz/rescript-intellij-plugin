package com.rescript.plugin.structure

import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Integration test for the ReScript Structure View using the full IDE platform.
 *
 * Verifies that the structure view model correctly represents the hierarchy
 * of declarations in a ReScript file.
 */
class RescriptStructureViewIntegrationTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String = "src/test/testData/structure"

    fun testStructureViewContainsTopLevelDeclarations() {
        myFixture.configureByFile("ModuleStructure.res")
        myFixture.testStructureView { view ->
            val root = view.treeModel.root
            val children = root.children
            // Top-level declarations: let, type, module, external, exception
            assertTrue("Expected at least 5 top-level declarations, got ${children.size}", children.size >= 5)
        }
    }

    fun testStructureViewShowsModuleChildren() {
        myFixture.configureByFile("ModuleStructure.res")
        myFixture.testStructureView { view ->
            val root = view.treeModel.root
            val moduleElement =
                root.children.find { child ->
                    (child as? StructureViewTreeElement)?.presentation?.presentableText == "Utils"
                } as? StructureViewTreeElement

            assertNotNull("Expected to find 'Utils' module in structure view", moduleElement)
            val moduleChildren = moduleElement!!.children
            // Utils contains: let helper, type config, module Inner
            assertTrue(
                "Expected at least 3 children in Utils module, got ${moduleChildren.size}",
                moduleChildren.size >= 3,
            )
        }
    }

    fun testStructureViewFromInlineCode() {
        myFixture.configureByText(
            "Inline.res",
            """
            let x = 1
            module M = {
              let y = 2
            }
            type t = int
            """.trimIndent(),
        )
        myFixture.testStructureView { view ->
            val root = view.treeModel.root
            val names =
                root.children.mapNotNull { child ->
                    (child as? StructureViewTreeElement)?.presentation?.presentableText
                }
            assertTrue("Expected 'x' in structure view", names.contains("x"))
            assertTrue("Expected 'M' in structure view", names.contains("M"))
            assertTrue("Expected 't' in structure view", names.contains("t"))
        }
    }
}
