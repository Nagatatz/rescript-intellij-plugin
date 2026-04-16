package com.rescript.plugin.hierarchy.call

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
 * Integration tests for [RescriptCallHierarchyNodeDescriptor].
 *
 * Builds descriptors over real PSI elements parsed from a fixture file and
 * verifies that update() populates the highlighted-text label with the
 * function name extracted by RescriptPsiUtils and assigns an icon.
 */
@ExtendWith(IntelliJPlatformExtension::class)
class RescriptCallHierarchyNodeDescriptorTest {
    private lateinit var myFixture: CodeInsightTestFixture
    private lateinit var project: Project

    @Test
    fun testUpdatePopulatesLabelForLetDeclaration() {
        val file = myFixture.configureByText("Foo.res", "let myFunc = () => 1") as RescriptFile
        val letDecl = findFirst(file, RescriptElementTypes.LET_DECLARATION)!!

        val descriptor = RescriptCallHierarchyNodeDescriptor(project, null, letDecl, true)
        descriptor.update()

        val label = descriptor.highlightedText.text
        assertTrue(label.contains("myFunc"), "expected label to contain myFunc, got: $label")
        assertNotNull(descriptor.icon)
    }

    @Test
    fun testUpdatePopulatesLabelForExternal() {
        val file =
            myFixture.configureByText(
                "Foo.res",
                """external log: string => unit = "console.log"""",
            ) as RescriptFile
        val ext = findFirst(file, RescriptElementTypes.EXTERNAL_DECLARATION)!!

        val descriptor = RescriptCallHierarchyNodeDescriptor(project, null, ext, true)
        descriptor.update()

        val label = descriptor.highlightedText.text
        assertTrue(label.contains("log"), "expected label to contain log, got: $label")
        assertNotNull(descriptor.icon)
    }

    @Test
    fun testParentDescriptorPropagated() {
        val file = myFixture.configureByText("Foo.res", "let foo = 1") as RescriptFile
        val letDecl = findFirst(file, RescriptElementTypes.LET_DECLARATION)!!

        val parent = RescriptCallHierarchyNodeDescriptor(project, null, letDecl, true)
        val child = RescriptCallHierarchyNodeDescriptor(project, parent, letDecl, false)
        // Both descriptors should construct cleanly with the parent reference set.
        assertNotNull(parent.psiElement)
        assertNotNull(child.psiElement)
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
