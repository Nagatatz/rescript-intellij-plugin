package com.rescript.plugin.hierarchy

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.rescript.plugin.lang.psi.RescriptElementTypes
import com.rescript.plugin.lang.psi.RescriptFile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

/**
 * Unit tests for [RescriptModuleHierarchyProvider]'s companion utility functions.
 *
 * Tests the PSI tree traversal logic that finds the nearest MODULE_DECLARATION
 * ancestor from a given element. Uses Mockito to construct mock PSI trees.
 * The IDE-dependent DataContext extraction and hierarchy browser creation
 * are not tested here.
 *
 * @see RescriptModuleHierarchyProvider
 */
class RescriptModuleHierarchyProviderTest {
    // ── findNearestModuleDeclaration ─────────────────────────────────────

    @Test
    fun testFindNearestModuleDeclarationDirectParent() {
        // element → moduleDecl → file
        val file = mock(RescriptFile::class.java)
        val moduleDecl = mockPsiElementWithType(RescriptElementTypes.MODULE_DECLARATION, file)
        val element = mockPsiElement(moduleDecl)

        val result = RescriptModuleHierarchyProvider.findNearestModuleDeclaration(element)
        assertEquals(moduleDecl, result)
    }

    @Test
    fun testFindNearestModuleDeclarationGrandparent() {
        // element → intermediate → moduleDecl → file
        val file = mock(RescriptFile::class.java)
        val moduleDecl = mockPsiElementWithType(RescriptElementTypes.MODULE_DECLARATION, file)
        val intermediate = mockPsiElement(moduleDecl)
        val element = mockPsiElement(intermediate)

        val result = RescriptModuleHierarchyProvider.findNearestModuleDeclaration(element)
        assertEquals(moduleDecl, result)
    }

    @Test
    fun testFindNearestModuleDeclarationNoModule() {
        // element → intermediate → file (no MODULE_DECLARATION in chain)
        val file = mock(RescriptFile::class.java)
        val intermediate = mockPsiElement(file)
        val element = mockPsiElement(intermediate)

        val result = RescriptModuleHierarchyProvider.findNearestModuleDeclaration(element)
        assertNull(result)
    }

    @Test
    fun testFindNearestModuleDeclarationElementIsModule() {
        // The element itself is a MODULE_DECLARATION
        val file = mock(RescriptFile::class.java)
        val moduleDecl = mockPsiElementWithType(RescriptElementTypes.MODULE_DECLARATION, file)

        val result = RescriptModuleHierarchyProvider.findNearestModuleDeclaration(moduleDecl)
        assertEquals(moduleDecl, result)
    }

    @Test
    fun testFindNearestModuleDeclarationElementIsFile() {
        // The element is already a RescriptFile — loop doesn't enter
        val file = mock(RescriptFile::class.java)

        val result = RescriptModuleHierarchyProvider.findNearestModuleDeclaration(file)
        assertNull(result)
    }

    @Test
    fun testFindNearestModuleDeclarationNestedModules() {
        // element → innerModule → outerModule → file
        // Should return innerModule (the nearest one)
        val file = mock(RescriptFile::class.java)
        val outerModule = mockPsiElementWithType(RescriptElementTypes.MODULE_DECLARATION, file)
        val innerModule = mockPsiElementWithType(RescriptElementTypes.MODULE_DECLARATION, outerModule)
        val element = mockPsiElement(innerModule)

        val result = RescriptModuleHierarchyProvider.findNearestModuleDeclaration(element)
        assertEquals(innerModule, result)
    }

    @Test
    fun testGetTargetReturnsNullForEmptyDataContext() {
        val provider = RescriptModuleHierarchyProvider()
        assertNull(provider.getTarget(com.intellij.openapi.actionSystem.DataContext.EMPTY_CONTEXT))
    }

    @Test
    fun testProviderCanBeInstantiated() {
        val provider = RescriptModuleHierarchyProvider()
        assertNotNull(provider)
    }

    // ── helpers ──────────────────────────────────────────────────────────

    /**
     * Creates a mock PsiElement with the given parent and no special element type.
     */
    private fun mockPsiElement(parent: PsiElement): PsiElement {
        val element = mock(PsiElement::class.java)
        `when`(element.parent).thenReturn(parent)
        // node returns null → elementType check fails, traversal continues
        `when`(element.node).thenReturn(null)
        return element
    }

    /**
     * Creates a mock PsiElement whose ASTNode reports the given element type.
     */
    private fun mockPsiElementWithType(
        elementType: com.intellij.psi.tree.IElementType,
        parent: PsiElement,
    ): PsiElement {
        val node = mock(ASTNode::class.java)
        `when`(node.elementType).thenReturn(elementType)
        val element = mock(PsiElement::class.java)
        `when`(element.parent).thenReturn(parent)
        `when`(element.node).thenReturn(node)
        return element
    }
}
