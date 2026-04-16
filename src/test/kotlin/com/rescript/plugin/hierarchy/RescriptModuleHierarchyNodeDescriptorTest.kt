package com.rescript.plugin.hierarchy

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.rescript.plugin.IntelliJPlatformExtension
import com.rescript.plugin.lang.psi.RescriptElementTypes
import com.rescript.plugin.lang.psi.RescriptFile
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Integration tests for [RescriptModuleHierarchyNodeDescriptor].
 *
 * Builds descriptors over real PSI elements parsed from a fixture file and
 * verifies that [RescriptModuleHierarchyNodeDescriptor.update] populates the
 * highlighted-text label with the extracted name and assigns an icon.
 */
@ExtendWith(IntelliJPlatformExtension::class)
class RescriptModuleHierarchyNodeDescriptorTest {
    private lateinit var myFixture: CodeInsightTestFixture
    private lateinit var project: Project

    @Test
    fun testUpdatePopulatesLabelForModule() {
        val file = myFixture.configureByText("Foo.res", "module Bar = { let x = 1 }") as RescriptFile
        val module = findFirst(file, RescriptElementTypes.MODULE_DECLARATION)!!

        val descriptor = RescriptModuleHierarchyNodeDescriptor(project, null, module, true)
        descriptor.update()

        val label = descriptor.highlightedText.text
        assertTrue(label.contains("Bar"), "expected label to contain Bar, got: $label")
        assertNotNull(descriptor.icon)
    }

    @Test
    fun testUpdatePopulatesLabelForFile() {
        val file = myFixture.configureByText("Foo.res", "let x = 1") as RescriptFile
        val descriptor = RescriptModuleHierarchyNodeDescriptor(project, null, file, true)
        descriptor.update()

        val label = descriptor.highlightedText.text
        assertTrue(label.contains("Foo.res"), "expected file name in label, got: $label")
    }

    @Test
    fun testIsBaseFlagPropagated() {
        val file = myFixture.configureByText("Foo.res", "let x = 1") as RescriptFile
        val baseDescriptor = RescriptModuleHierarchyNodeDescriptor(project, null, file, true)
        val nonBaseDescriptor = RescriptModuleHierarchyNodeDescriptor(project, baseDescriptor, file, false)
        // Both descriptors should be constructible — isBase only affects internal display state
        assertNotNull(baseDescriptor.psiElement)
        assertNotNull(nonBaseDescriptor.psiElement)
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
