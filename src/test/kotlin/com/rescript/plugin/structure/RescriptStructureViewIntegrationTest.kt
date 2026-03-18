package com.rescript.plugin.structure

import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.rescript.plugin.IntelliJPlatformExtension
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Integration test for the ReScript Structure View using the full IDE platform.
 *
 * Verifies that the structure view model correctly represents the hierarchy
 * of declarations in a ReScript file.
 */
@ExtendWith(IntelliJPlatformExtension::class)
class RescriptStructureViewIntegrationTest {
    private lateinit var myFixture: CodeInsightTestFixture

    private val testDataPath: String = "src/test/testData/structure"

    @Test
    fun testStructureViewContainsTopLevelDeclarations() {
        myFixture.configureByFile("ModuleStructure.res")
        myFixture.testStructureView { view ->
            val root = view.treeModel.root
            val children = root.children
            // Top-level declarations: let, type, module, external, exception
            assertTrue(children.size >= 5, "Expected at least 5 top-level declarations, got ${children.size}")
        }
    }

    @Test
    fun testStructureViewShowsModuleChildren() {
        myFixture.configureByFile("ModuleStructure.res")
        myFixture.testStructureView { view ->
            val root = view.treeModel.root
            val moduleElement =
                root.children.find { child ->
                    (child as? StructureViewTreeElement)?.presentation?.presentableText == "Utils"
                } as? StructureViewTreeElement

            assertNotNull(moduleElement, "Expected to find 'Utils' module in structure view")
            val moduleChildren = moduleElement!!.children
            // Utils contains: let helper, type config, module Inner
            assertTrue(
                moduleChildren.size >= 3,
                "Expected at least 3 children in Utils module, got ${moduleChildren.size}",
            )
        }
    }

    @Test
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
            assertTrue(names.contains("x"), "Expected 'x' in structure view")
            assertTrue(names.contains("M"), "Expected 'M' in structure view")
            assertTrue(names.contains("t"), "Expected 't' in structure view")
        }
    }
}
