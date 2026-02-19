package com.rescript.plugin.editor

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.rescript.plugin.lang.psi.RescriptElementTypes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptStatementUpDownMoverTest {
    private val mover = RescriptStatementUpDownMover()

    /**
     * Creates a PsiElement stub with specified type, parent, prevSibling, and nextSibling.
     */
    private fun stubPsiElement(
        type: IElementType,
        parent: PsiElement? = null,
        prevSibling: PsiElement? = null,
        nextSibling: PsiElement? = null,
    ): PsiElement {
        val node =
            java.lang.reflect.Proxy.newProxyInstance(
                ASTNode::class.java.classLoader,
                arrayOf(ASTNode::class.java),
            ) { _, method, _ ->
                when (method.name) {
                    "getElementType" -> type
                    "toString" -> "StubASTNode($type)"
                    "hashCode" -> System.identityHashCode(type)
                    "equals" -> false
                    else -> null
                }
            } as ASTNode

        return java.lang.reflect.Proxy.newProxyInstance(
            PsiElement::class.java.classLoader,
            arrayOf(PsiElement::class.java),
        ) { _, method, _ ->
            when (method.name) {
                "getNode" -> node
                "getParent" -> parent
                "getPrevSibling" -> prevSibling
                "getNextSibling" -> nextSibling
                "toString" -> "StubPsiElement($type)"
                "hashCode" -> System.identityHashCode(node)
                "equals" -> false
                else -> null
            }
        } as PsiElement
    }

    @Test
    fun testMoverCanBeInstantiated() {
        assertNotNull(mover)
    }

    @Test
    fun testDeclarationTypesContainsLetDeclaration() {
        assertTrue(
            RescriptStatementUpDownMover.DECLARATION_TYPES.contains(RescriptElementTypes.LET_DECLARATION),
        )
    }

    @Test
    fun testDeclarationTypesContainsTypeDeclaration() {
        assertTrue(
            RescriptStatementUpDownMover.DECLARATION_TYPES.contains(RescriptElementTypes.TYPE_DECLARATION),
        )
    }

    @Test
    fun testDeclarationTypesContainsModuleDeclaration() {
        assertTrue(
            RescriptStatementUpDownMover.DECLARATION_TYPES.contains(RescriptElementTypes.MODULE_DECLARATION),
        )
    }

    @Test
    fun testDeclarationTypesContainsExternalDeclaration() {
        assertTrue(
            RescriptStatementUpDownMover.DECLARATION_TYPES.contains(RescriptElementTypes.EXTERNAL_DECLARATION),
        )
    }

    @Test
    fun testDeclarationTypesContainsOpenStatement() {
        assertTrue(
            RescriptStatementUpDownMover.DECLARATION_TYPES.contains(RescriptElementTypes.OPEN_STATEMENT),
        )
    }

    @Test
    fun testDeclarationTypesContainsIncludeStatement() {
        assertTrue(
            RescriptStatementUpDownMover.DECLARATION_TYPES.contains(RescriptElementTypes.INCLUDE_STATEMENT),
        )
    }

    @Test
    fun testDeclarationTypesContainsExceptionDeclaration() {
        assertTrue(
            RescriptStatementUpDownMover.DECLARATION_TYPES.contains(RescriptElementTypes.EXCEPTION_DECLARATION),
        )
    }

    @Test
    fun testDeclarationTypesDoesNotContainAnnotation() {
        assertFalse(
            RescriptStatementUpDownMover.DECLARATION_TYPES.contains(RescriptElementTypes.ANNOTATION),
        )
    }

    @Test
    fun testDeclarationTypesDoesNotContainJsxElement() {
        assertFalse(
            RescriptStatementUpDownMover.DECLARATION_TYPES.contains(RescriptElementTypes.JSX_ELEMENT),
        )
    }

    @Test
    fun testDeclarationTypesCount() {
        assertEquals(7, RescriptStatementUpDownMover.DECLARATION_TYPES.size)
    }

    // -- findDeclaration PSI stub tests --

    @Test
    fun testFindDeclarationFindsLetDeclarationParent() {
        val letDecl = stubPsiElement(RescriptElementTypes.LET_DECLARATION)
        val child = stubPsiElement(RescriptElementTypes.ANNOTATION, parent = letDecl)
        val result = mover.findDeclaration(child)
        assertNotNull(result)
        assertEquals(RescriptElementTypes.LET_DECLARATION, result!!.node?.elementType)
    }

    @Test
    fun testFindDeclarationFindsDirectDeclaration() {
        val moduleDecl = stubPsiElement(RescriptElementTypes.MODULE_DECLARATION)
        val result = mover.findDeclaration(moduleDecl)
        assertNotNull(result)
        assertEquals(RescriptElementTypes.MODULE_DECLARATION, result!!.node?.elementType)
    }

    @Test
    fun testFindDeclarationReturnsNullForNonDeclaration() {
        val annotation = stubPsiElement(RescriptElementTypes.ANNOTATION, parent = null)
        val result = mover.findDeclaration(annotation)
        assertNull(result)
    }

    // -- findNextDeclaration PSI stub tests --

    @Test
    fun testFindNextDeclarationFindsNextSiblingDeclaration() {
        val nextDecl = stubPsiElement(RescriptElementTypes.TYPE_DECLARATION)
        val current = stubPsiElement(RescriptElementTypes.LET_DECLARATION, nextSibling = nextDecl)
        val result = mover.findNextDeclaration(current)
        assertNotNull(result)
        assertEquals(RescriptElementTypes.TYPE_DECLARATION, result!!.node?.elementType)
    }

    @Test
    fun testFindNextDeclarationSkipsWhitespace() {
        val nextDecl = stubPsiElement(RescriptElementTypes.MODULE_DECLARATION)
        val whitespace = stubPsiElement(TokenType.WHITE_SPACE, nextSibling = nextDecl)
        val current = stubPsiElement(RescriptElementTypes.LET_DECLARATION, nextSibling = whitespace)
        val result = mover.findNextDeclaration(current)
        assertNotNull(result)
        assertEquals(RescriptElementTypes.MODULE_DECLARATION, result!!.node?.elementType)
    }

    @Test
    fun testFindNextDeclarationReturnsNullWhenNoNextSibling() {
        val current = stubPsiElement(RescriptElementTypes.LET_DECLARATION, nextSibling = null)
        val result = mover.findNextDeclaration(current)
        assertNull(result)
    }

    @Test
    fun testFindNextDeclarationReturnsNullForNonDeclarationSibling() {
        val jsxElement = stubPsiElement(RescriptElementTypes.JSX_ELEMENT, nextSibling = null)
        val current = stubPsiElement(RescriptElementTypes.LET_DECLARATION, nextSibling = jsxElement)
        val result = mover.findNextDeclaration(current)
        assertNull(result)
    }

    // -- findPreviousDeclaration PSI stub tests --

    @Test
    fun testFindPreviousDeclarationFindsPrevSiblingDeclaration() {
        val prevDecl = stubPsiElement(RescriptElementTypes.LET_DECLARATION)
        val current = stubPsiElement(RescriptElementTypes.TYPE_DECLARATION, prevSibling = prevDecl)
        val result = mover.findPreviousDeclaration(current)
        assertNotNull(result)
        assertEquals(RescriptElementTypes.LET_DECLARATION, result!!.node?.elementType)
    }

    @Test
    fun testFindPreviousDeclarationSkipsWhitespace() {
        val prevDecl = stubPsiElement(RescriptElementTypes.LET_DECLARATION)
        val whitespace = stubPsiElement(TokenType.WHITE_SPACE, prevSibling = prevDecl)
        val current = stubPsiElement(RescriptElementTypes.TYPE_DECLARATION, prevSibling = whitespace)
        val result = mover.findPreviousDeclaration(current)
        assertNotNull(result)
        assertEquals(RescriptElementTypes.LET_DECLARATION, result!!.node?.elementType)
    }

    @Test
    fun testFindPreviousDeclarationReturnsNullWhenNoPrevSibling() {
        val current = stubPsiElement(RescriptElementTypes.LET_DECLARATION, prevSibling = null)
        val result = mover.findPreviousDeclaration(current)
        assertNull(result)
    }

    @Test
    fun testFindPreviousDeclarationReturnsNullForNonDeclarationSibling() {
        val jsxElement = stubPsiElement(RescriptElementTypes.JSX_ELEMENT, prevSibling = null)
        val current = stubPsiElement(RescriptElementTypes.TYPE_DECLARATION, prevSibling = jsxElement)
        val result = mover.findPreviousDeclaration(current)
        assertNull(result)
    }

    @Test
    fun testFindPreviousDeclarationSkipsAnnotation() {
        val prevDecl = stubPsiElement(RescriptElementTypes.LET_DECLARATION)
        val annotation = stubPsiElement(RescriptElementTypes.ANNOTATION, prevSibling = prevDecl)
        val current = stubPsiElement(RescriptElementTypes.TYPE_DECLARATION, prevSibling = annotation)
        val result = mover.findPreviousDeclaration(current)
        assertNotNull(result)
        assertEquals(RescriptElementTypes.LET_DECLARATION, result!!.node?.elementType)
    }
}
