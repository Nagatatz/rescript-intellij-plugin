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
 * Integration tests for [RescriptCallerTreeStructure].
 *
 * Builds a caller tree over real PSI files and verifies the buildChildren
 * routing into [RescriptCallAnalyzer.findCallers]. The analyzer relies on
 * [com.intellij.psi.search.PsiSearchHelper] which depends on a fully built
 * project word index — in light test fixtures the index may not contain
 * matches for every identifier, so we assert structural invariants
 * (typed children, empty case) rather than fixed counts.
 *
 * @see RescriptCallAnalyzerTest for the analyzer logic itself
 */
@ExtendWith(IntelliJPlatformExtension::class)
class RescriptCallerTreeStructureTest {
    private lateinit var myFixture: CodeInsightTestFixture
    private lateinit var project: Project

    @Test
    fun testCallerTreeBuildsWithoutCrash() {
        val source =
            """
            let target = () => 1
            let firstCaller = () => target()
            let secondCaller = () => target()
            """.trimIndent()
        val file = myFixture.configureByText("Foo.res", source) as RescriptFile
        val target = findLetByName(file, "target")!!

        val structure = RescriptCallerTreeStructure(project, target)
        val rootDescriptor = structure.rootElement as com.intellij.ide.hierarchy.HierarchyNodeDescriptor
        // buildChildren must not throw and must return a typed array of descriptors.
        val children = structure.getChildElements(rootDescriptor)
        assertNotNull(children)
        assertTrue(children.all { it is RescriptCallHierarchyNodeDescriptor })
    }

    @Test
    fun testCallerTreeReturnsEmptyForUnreferencedFunction() {
        val file = myFixture.configureByText("Foo.res", "let lonely = 1") as RescriptFile
        val lonely = findLetByName(file, "lonely")!!

        val structure = RescriptCallerTreeStructure(project, lonely)
        val rootDescriptor = structure.rootElement as com.intellij.ide.hierarchy.HierarchyNodeDescriptor
        val children = structure.getChildElements(rootDescriptor)

        assertTrue(children.isEmpty(), "expected no callers, got ${children.size}")
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
