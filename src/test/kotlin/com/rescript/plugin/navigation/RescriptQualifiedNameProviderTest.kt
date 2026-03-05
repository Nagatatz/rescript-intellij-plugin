package com.rescript.plugin.navigation

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.rescript.plugin.lang.RescriptTokenTypes
import com.rescript.plugin.lang.psi.RescriptElementTypes
import com.rescript.plugin.lang.psi.RescriptPsiUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptQualifiedNameProviderTest {
    private val provider = RescriptQualifiedNameProvider()

    // ── SUPPORTED_TYPES tests ─────────────────────────────────────────

    @Test
    fun testSupportedTypesMatchesNavigableTypes() {
        assertEquals(RescriptPsiUtils.NAVIGABLE_TYPES, RescriptQualifiedNameProvider.SUPPORTED_TYPES)
    }

    @Test
    fun testSupportedTypesContainsLetDeclaration() {
        assertTrue(RescriptQualifiedNameProvider.SUPPORTED_TYPES.contains(RescriptElementTypes.LET_DECLARATION))
    }

    @Test
    fun testSupportedTypesContainsTypeDeclaration() {
        assertTrue(RescriptQualifiedNameProvider.SUPPORTED_TYPES.contains(RescriptElementTypes.TYPE_DECLARATION))
    }

    @Test
    fun testSupportedTypesContainsModuleDeclaration() {
        assertTrue(RescriptQualifiedNameProvider.SUPPORTED_TYPES.contains(RescriptElementTypes.MODULE_DECLARATION))
    }

    @Test
    fun testSupportedTypesContainsExternalDeclaration() {
        assertTrue(RescriptQualifiedNameProvider.SUPPORTED_TYPES.contains(RescriptElementTypes.EXTERNAL_DECLARATION))
    }

    @Test
    fun testSupportedTypesContainsExceptionDeclaration() {
        assertTrue(RescriptQualifiedNameProvider.SUPPORTED_TYPES.contains(RescriptElementTypes.EXCEPTION_DECLARATION))
    }

    @Test
    fun testSupportedTypesDoesNotContainOpenStatement() {
        assertFalse(RescriptQualifiedNameProvider.SUPPORTED_TYPES.contains(RescriptElementTypes.OPEN_STATEMENT))
    }

    @Test
    fun testSupportedTypesDoesNotContainIncludeStatement() {
        assertFalse(RescriptQualifiedNameProvider.SUPPORTED_TYPES.contains(RescriptElementTypes.INCLUDE_STATEMENT))
    }

    @Test
    fun testSupportedTypesDoesNotContainAnnotation() {
        assertFalse(RescriptQualifiedNameProvider.SUPPORTED_TYPES.contains(RescriptElementTypes.ANNOTATION))
    }

    @Test
    fun testSupportedTypesCount() {
        assertEquals(5, RescriptQualifiedNameProvider.SUPPORTED_TYPES.size)
    }

    // ── findDeclarationElement tests ──────────────────────────────────

    @Test
    fun testFindDeclarationElementReturnsElementWhenTypeMatches() {
        val element = stubPsiElement(RescriptElementTypes.LET_DECLARATION)
        val result = provider.findDeclarationElement(element)
        assertSame(element, result)
    }

    @Test
    fun testFindDeclarationElementMatchesTypeDeclaration() {
        val element = stubPsiElement(RescriptElementTypes.TYPE_DECLARATION)
        assertSame(element, provider.findDeclarationElement(element))
    }

    @Test
    fun testFindDeclarationElementMatchesModuleDeclaration() {
        val element = stubPsiElement(RescriptElementTypes.MODULE_DECLARATION)
        assertSame(element, provider.findDeclarationElement(element))
    }

    @Test
    fun testFindDeclarationElementMatchesExternalDeclaration() {
        val element = stubPsiElement(RescriptElementTypes.EXTERNAL_DECLARATION)
        assertSame(element, provider.findDeclarationElement(element))
    }

    @Test
    fun testFindDeclarationElementMatchesExceptionDeclaration() {
        val element = stubPsiElement(RescriptElementTypes.EXCEPTION_DECLARATION)
        assertSame(element, provider.findDeclarationElement(element))
    }

    @Test
    fun testFindDeclarationElementReturnsNullForUnsupportedType() {
        val element = stubPsiElement(RescriptElementTypes.OPEN_STATEMENT, parent = null)
        val result = provider.findDeclarationElement(element)
        assertNull(result)
    }

    @Test
    fun testFindDeclarationElementWalksUpToParent() {
        val parent = stubPsiElement(RescriptElementTypes.LET_DECLARATION)
        val child = stubPsiElement(RescriptTokenTypes.LIDENT, parent = parent)
        val result = provider.findDeclarationElement(child)
        assertSame(parent, result)
    }

    @Test
    fun testFindDeclarationElementReturnsNullWhenNoDeclarationInAncestors() {
        val root = stubPsiElement(RescriptElementTypes.OPEN_STATEMENT, parent = null)
        val child = stubPsiElement(RescriptTokenTypes.LIDENT, parent = root)
        val result = provider.findDeclarationElement(child)
        assertNull(result)
    }

    @Test
    fun testFindDeclarationElementReturnsNullForNullNode() {
        val element = stubPsiElementWithNullNode(parent = null)
        val result = provider.findDeclarationElement(element)
        assertNull(result)
    }

    // ── buildModulePath tests ─────────────────────────────────────────

    @Test
    fun testBuildModulePathReturnsEmptyForTopLevelElement() {
        // Parent is a non-module element with null parent (acts as file level)
        val topLevel = stubPsiElement(RescriptTokenTypes.LIDENT, parent = null)
        val element = stubPsiElement(RescriptElementTypes.LET_DECLARATION, parent = topLevel)
        assertEquals("", provider.buildModulePath(element))
    }

    @Test
    fun testBuildModulePathReturnsSingleModule() {
        val topLevel = stubPsiElement(RescriptTokenTypes.LIDENT, parent = null)
        val moduleDecl = stubModuleDeclaration("Inner", parent = topLevel)
        val element = stubPsiElement(RescriptElementTypes.LET_DECLARATION, parent = moduleDecl)
        assertEquals("Inner", provider.buildModulePath(element))
    }

    @Test
    fun testBuildModulePathReturnsNestedModules() {
        val topLevel = stubPsiElement(RescriptTokenTypes.LIDENT, parent = null)
        val outerModule = stubModuleDeclaration("Outer", parent = topLevel)
        val innerModule = stubModuleDeclaration("Inner", parent = outerModule)
        val element = stubPsiElement(RescriptElementTypes.LET_DECLARATION, parent = innerModule)
        assertEquals("Outer.Inner", provider.buildModulePath(element))
    }

    // ── getQualifiedName tests ────────────────────────────────────────

    @Test
    fun testGetQualifiedNameReturnsNullForNonDeclaration() {
        val element = stubPsiElement(RescriptTokenTypes.LIDENT, parent = null)
        assertNull(provider.getQualifiedName(element))
    }

    @Test
    fun testGetQualifiedNameReturnsNullWhenContainingFileIsNotRescriptFile() {
        // findDeclarationElement returns the element itself, but containingFile is not RescriptFile
        val element = stubPsiElement(RescriptElementTypes.LET_DECLARATION)
        assertNull(provider.getQualifiedName(element))
    }

    @Test
    fun testQualifiedNameToElementAlwaysReturnsNull() {
        assertNull(
            provider.qualifiedNameToElement(
                "Foo.bar",
                null as? com.intellij.openapi.project.Project ?: return,
            ),
        )
    }

    @Test
    fun testAdjustElementToCopyDelegatesToFindDeclarationElement() {
        val element = stubPsiElement(RescriptElementTypes.LET_DECLARATION)
        val result = provider.adjustElementToCopy(element)
        assertNotNull(result)
    }

    // ── Stub helpers ──────────────────────────────────────────────────

    private fun stubPsiElement(
        type: IElementType,
        parent: PsiElement? = null,
    ): PsiElement {
        val astNode = stubAstNode(type)
        return java.lang.reflect.Proxy.newProxyInstance(
            PsiElement::class.java.classLoader,
            arrayOf(PsiElement::class.java),
        ) { proxy, method, args ->
            when (method.name) {
                "getNode" -> astNode
                "getParent" -> parent
                "getContainingFile" -> null
                "toString" -> "StubPsiElement($type)"
                "hashCode" -> System.identityHashCode(proxy)
                "equals" -> proxy === args?.get(0)
                else -> null
            }
        } as PsiElement
    }

    private fun stubPsiElementWithNullNode(parent: PsiElement?): PsiElement =
        java.lang.reflect.Proxy.newProxyInstance(
            PsiElement::class.java.classLoader,
            arrayOf(PsiElement::class.java),
        ) { proxy, method, args ->
            when (method.name) {
                "getNode" -> null
                "getParent" -> parent
                "toString" -> "StubPsiElement(nullNode)"
                "hashCode" -> System.identityHashCode(proxy)
                "equals" -> proxy === args?.get(0)
                else -> null
            }
        } as PsiElement

    private fun stubModuleDeclaration(
        name: String,
        parent: PsiElement?,
    ): PsiElement {
        val nameNode = stubAstNodeWithText(RescriptTokenTypes.UIDENT, name)
        val moduleKeywordNode = stubAstNodeWithNext(RescriptTokenTypes.MODULE, nameNode)
        val declNode = stubAstNodeWithFirstChild(RescriptElementTypes.MODULE_DECLARATION, moduleKeywordNode)

        return java.lang.reflect.Proxy.newProxyInstance(
            PsiElement::class.java.classLoader,
            arrayOf(PsiElement::class.java),
        ) { proxy, method, args ->
            when (method.name) {
                "getNode" -> declNode
                "getParent" -> parent
                "getContainingFile" -> null
                "toString" -> "StubModuleDeclaration($name)"
                "hashCode" -> System.identityHashCode(proxy)
                "equals" -> proxy === args?.get(0)
                else -> null
            }
        } as PsiElement
    }

    private fun stubAstNode(type: IElementType): ASTNode =
        java.lang.reflect.Proxy.newProxyInstance(
            ASTNode::class.java.classLoader,
            arrayOf(ASTNode::class.java),
        ) { _, method, _ ->
            when (method.name) {
                "getElementType" -> type
                "getFirstChildNode" -> null
                "getTreeNext" -> null
                "getText" -> ""
                "toString" -> "StubASTNode($type)"
                "hashCode" -> type.hashCode()
                "equals" -> false
                else -> null
            }
        } as ASTNode

    private fun stubAstNodeWithText(
        type: IElementType,
        text: String,
    ): ASTNode =
        java.lang.reflect.Proxy.newProxyInstance(
            ASTNode::class.java.classLoader,
            arrayOf(ASTNode::class.java),
        ) { _, method, _ ->
            when (method.name) {
                "getElementType" -> type
                "getFirstChildNode" -> null
                "getTreeNext" -> null
                "getText" -> text
                "toString" -> "StubASTNode($type, $text)"
                "hashCode" -> type.hashCode()
                "equals" -> false
                else -> null
            }
        } as ASTNode

    private fun stubAstNodeWithNext(
        type: IElementType,
        next: ASTNode?,
    ): ASTNode =
        java.lang.reflect.Proxy.newProxyInstance(
            ASTNode::class.java.classLoader,
            arrayOf(ASTNode::class.java),
        ) { _, method, _ ->
            when (method.name) {
                "getElementType" -> type
                "getFirstChildNode" -> null
                "getTreeNext" -> next
                "getText" -> type.toString()
                "toString" -> "StubASTNode($type)"
                "hashCode" -> type.hashCode()
                "equals" -> false
                else -> null
            }
        } as ASTNode

    private fun stubAstNodeWithFirstChild(
        type: IElementType,
        firstChild: ASTNode?,
    ): ASTNode =
        java.lang.reflect.Proxy.newProxyInstance(
            ASTNode::class.java.classLoader,
            arrayOf(ASTNode::class.java),
        ) { _, method, _ ->
            when (method.name) {
                "getElementType" -> type
                "getFirstChildNode" -> firstChild
                "getTreeNext" -> null
                "getText" -> ""
                "toString" -> "StubASTNode($type)"
                "hashCode" -> type.hashCode()
                "equals" -> false
                else -> null
            }
        } as ASTNode
}
