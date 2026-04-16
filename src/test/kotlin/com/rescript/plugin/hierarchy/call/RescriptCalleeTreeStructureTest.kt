package com.rescript.plugin.hierarchy.call

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.rescript.plugin.IntelliJPlatformExtension
import com.rescript.plugin.lang.psi.RescriptElementTypes
import com.rescript.plugin.lang.psi.RescriptFile
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Integration tests for [RescriptCalleeTreeStructure].
 *
 * Builds a callee tree over real PSI files and verifies that
 * [RescriptCalleeTreeStructure.buildChildren] returns descriptors for every
 * declaration the root function references in its body, and returns no
 * children when the body contains no calls.
 */
@ExtendWith(IntelliJPlatformExtension::class)
class RescriptCalleeTreeStructureTest {
    private lateinit var myFixture: CodeInsightTestFixture
    private lateinit var project: Project

    @Test
    fun testCalleeTreeFindsCalledFunctions() {
        val source =
            """
            let helper = () => 1
            let other = () => 2
            let main = () => {
              helper()
              other()
            }
            """.trimIndent()
        val file = myFixture.configureByText("Foo.res", source) as RescriptFile
        val main = findLetByName(file, "main")!!

        val structure = RescriptCalleeTreeStructure(project, main)
        val rootDescriptor = structure.rootElement as com.intellij.ide.hierarchy.HierarchyNodeDescriptor
        val children = structure.getChildElements(rootDescriptor)

        assertTrue(children.size >= 2, "expected at least 2 callees, got ${children.size}")
        assertTrue(children.all { it is RescriptCallHierarchyNodeDescriptor })
    }

    @Test
    fun testCalleeTreeReturnsEmptyForLeafFunction() {
        val file = myFixture.configureByText("Foo.res", "let solo = 42") as RescriptFile
        val solo = findLetByName(file, "solo")!!

        val structure = RescriptCalleeTreeStructure(project, solo)
        val rootDescriptor = structure.rootElement as com.intellij.ide.hierarchy.HierarchyNodeDescriptor
        val children = structure.getChildElements(rootDescriptor)

        assertTrue(children.isEmpty(), "expected no callees, got ${children.size}")
    }

    private fun findLetByName(
        scope: PsiElement,
        name: String,
    ): PsiElement? {
        if (scope.node?.elementType == RescriptElementTypes.LET_DECLARATION &&
            scope.text.contains(Regex("""\blet\s+(rec\s+)?$name\b"""))
        ) {
            return scope
        }
        for (child in scope.children) {
            val found = findLetByName(child, name)
            if (found != null) return found
        }
        return null
    }
}
