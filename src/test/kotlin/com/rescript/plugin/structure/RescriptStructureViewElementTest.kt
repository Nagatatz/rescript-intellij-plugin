package com.rescript.plugin.structure

import com.intellij.lang.ASTNode
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.rescript.plugin.lang.RescriptTokenTypes
import com.rescript.plugin.lang.psi.RescriptElementTypes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptStructureViewElementTest {
    /**
     * Creates a NavigatablePsiElement stub with specified elementType and children.
     * The element's AST node has a keyword child followed by an identifier child,
     * allowing extractName to work correctly.
     */
    private fun stubNavigatableElement(
        type: IElementType,
        keyword: IElementType? = null,
        identName: String = "myName",
        children: Array<PsiElement> = emptyArray(),
    ): NavigatablePsiElement {
        // Build a chain of child AST nodes: keyword -> identifier
        val identNode = stubAstNode(RescriptTokenTypes.LIDENT, identName, treeNext = null)
        val keywordNode =
            if (keyword != null) {
                stubAstNode(keyword, keyword.toString(), treeNext = identNode)
            } else {
                null
            }

        val node =
            java.lang.reflect.Proxy.newProxyInstance(
                ASTNode::class.java.classLoader,
                arrayOf(ASTNode::class.java),
            ) { _, method, _ ->
                when (method.name) {
                    "getElementType" -> type
                    "getFirstChildNode" -> keywordNode ?: identNode
                    "getTreeNext" -> null
                    "getText" -> identName
                    "toString" -> "StubASTNode($type)"
                    "hashCode" -> System.identityHashCode(type)
                    "equals" -> false
                    else -> null
                }
            } as ASTNode

        return java.lang.reflect.Proxy.newProxyInstance(
            NavigatablePsiElement::class.java.classLoader,
            arrayOf(NavigatablePsiElement::class.java),
        ) { _, method, _ ->
            when (method.name) {
                "getNode" -> node
                "getChildren" -> children
                "canNavigate" -> true
                "canNavigateToSource" -> true
                "navigate" -> Unit
                "getName" -> identName
                "toString" -> "StubNavigatablePsiElement($type, $identName)"
                "hashCode" -> System.identityHashCode(type)
                "equals" -> false
                else -> null
            }
        } as NavigatablePsiElement
    }

    private fun stubAstNode(
        type: IElementType,
        text: String,
        treeNext: ASTNode? = null,
    ): ASTNode =
        java.lang.reflect.Proxy.newProxyInstance(
            ASTNode::class.java.classLoader,
            arrayOf(ASTNode::class.java),
        ) { _, method, _ ->
            when (method.name) {
                "getElementType" -> type
                "getText" -> text
                "getTreeNext" -> treeNext
                "getFirstChildNode" -> null
                "toString" -> "StubASTNode($type, $text)"
                "hashCode" -> System.identityHashCode(type)
                "equals" -> false
                else -> null
            }
        } as ASTNode

    @Test
    fun testGetAlphaSortKeyReturnsExtractedName() {
        val element =
            stubNavigatableElement(
                RescriptElementTypes.LET_DECLARATION,
                keyword = RescriptTokenTypes.LET,
                identName = "myFunction",
            )
        val viewElement = RescriptStructureViewElement(element)
        assertEquals("myFunction", viewElement.alphaSortKey)
    }

    @Test
    fun testGetAlphaSortKeyForModuleDeclaration() {
        val element =
            stubNavigatableElement(
                RescriptElementTypes.MODULE_DECLARATION,
                keyword = RescriptTokenTypes.MODULE,
                identName = "MyModule",
            )
        val viewElement = RescriptStructureViewElement(element)
        assertEquals("MyModule", viewElement.alphaSortKey)
    }

    @Test
    fun testGetPresentationReturnsPresentationData() {
        val element =
            stubNavigatableElement(
                RescriptElementTypes.LET_DECLARATION,
                keyword = RescriptTokenTypes.LET,
                identName = "foo",
            )
        val viewElement = RescriptStructureViewElement(element)
        val presentation: ItemPresentation = viewElement.presentation
        assertNotNull(presentation)
        assertEquals("foo", presentation.presentableText)
    }

    @Test
    fun testGetValueReturnsElement() {
        val element =
            stubNavigatableElement(
                RescriptElementTypes.LET_DECLARATION,
                keyword = RescriptTokenTypes.LET,
                identName = "bar",
            )
        val viewElement = RescriptStructureViewElement(element)
        assertSame(element, viewElement.value)
    }

    @Test
    fun testCanNavigateDelegatesToElement() {
        val element =
            stubNavigatableElement(
                RescriptElementTypes.LET_DECLARATION,
                keyword = RescriptTokenTypes.LET,
                identName = "baz",
            )
        val viewElement = RescriptStructureViewElement(element)
        assertTrue(viewElement.canNavigate())
        assertTrue(viewElement.canNavigateToSource())
    }

    @Test
    fun testGetChildrenFiltersNavigableTypes() {
        // Create children: one LET_DECLARATION (navigable) and one ANNOTATION (not navigable)
        val childLet =
            stubNavigatableElement(
                RescriptElementTypes.LET_DECLARATION,
                keyword = RescriptTokenTypes.LET,
                identName = "childFn",
            )
        val childAnnotation =
            stubNavigatableElement(
                RescriptElementTypes.ANNOTATION,
                identName = "@attr",
            )
        val parent =
            stubNavigatableElement(
                RescriptElementTypes.MODULE_DECLARATION,
                keyword = RescriptTokenTypes.MODULE,
                identName = "Parent",
                children = arrayOf(childLet, childAnnotation),
            )
        val viewElement = RescriptStructureViewElement(parent)
        val children = viewElement.children
        // Only LET_DECLARATION should pass the filter
        assertEquals(1, children.size)
    }

    @Test
    fun testGetChildrenReturnsEmptyForNoNavigableChildren() {
        val childAnnotation =
            stubNavigatableElement(
                RescriptElementTypes.ANNOTATION,
                identName = "@attr",
            )
        val parent =
            stubNavigatableElement(
                RescriptElementTypes.MODULE_DECLARATION,
                keyword = RescriptTokenTypes.MODULE,
                identName = "EmptyModule",
                children = arrayOf(childAnnotation),
            )
        val viewElement = RescriptStructureViewElement(parent)
        assertEquals(0, viewElement.children.size)
    }

    @Test
    fun testGetChildrenReturnsEmptyWhenNoChildren() {
        val parent =
            stubNavigatableElement(
                RescriptElementTypes.MODULE_DECLARATION,
                keyword = RescriptTokenTypes.MODULE,
                identName = "NoChildren",
            )
        val viewElement = RescriptStructureViewElement(parent)
        assertEquals(0, viewElement.children.size)
    }
}
