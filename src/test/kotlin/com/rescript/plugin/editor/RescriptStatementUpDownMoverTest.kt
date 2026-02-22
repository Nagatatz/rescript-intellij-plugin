package com.rescript.plugin.editor

import com.intellij.psi.TokenType
import com.rescript.plugin.RescriptTestUtils
import com.rescript.plugin.lang.psi.RescriptElementTypes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptStatementUpDownMoverTest {
    private val mover = RescriptStatementUpDownMover()

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
        val letDecl = RescriptTestUtils.stubPsiElement(RescriptElementTypes.LET_DECLARATION)
        val child = RescriptTestUtils.stubPsiElement(RescriptElementTypes.ANNOTATION, parent = letDecl)
        val result = mover.findDeclaration(child)
        assertNotNull(result)
        assertEquals(RescriptElementTypes.LET_DECLARATION, result!!.node?.elementType)
    }

    @Test
    fun testFindDeclarationFindsDirectDeclaration() {
        val moduleDecl = RescriptTestUtils.stubPsiElement(RescriptElementTypes.MODULE_DECLARATION)
        val result = mover.findDeclaration(moduleDecl)
        assertNotNull(result)
        assertEquals(RescriptElementTypes.MODULE_DECLARATION, result!!.node?.elementType)
    }

    @Test
    fun testFindDeclarationReturnsNullForNonDeclaration() {
        val annotation = RescriptTestUtils.stubPsiElement(RescriptElementTypes.ANNOTATION, parent = null)
        val result = mover.findDeclaration(annotation)
        assertNull(result)
    }

    // -- findNextDeclaration PSI stub tests --

    @Test
    fun testFindNextDeclarationFindsNextSiblingDeclaration() {
        val nextDecl = RescriptTestUtils.stubPsiElement(RescriptElementTypes.TYPE_DECLARATION)
        val current = RescriptTestUtils.stubPsiElement(RescriptElementTypes.LET_DECLARATION, nextSibling = nextDecl)
        val result = mover.findNextDeclaration(current)
        assertNotNull(result)
        assertEquals(RescriptElementTypes.TYPE_DECLARATION, result!!.node?.elementType)
    }

    @Test
    fun testFindNextDeclarationSkipsWhitespace() {
        val nextDecl = RescriptTestUtils.stubPsiElement(RescriptElementTypes.MODULE_DECLARATION)
        val whitespace = RescriptTestUtils.stubPsiElement(TokenType.WHITE_SPACE, nextSibling = nextDecl)
        val current = RescriptTestUtils.stubPsiElement(RescriptElementTypes.LET_DECLARATION, nextSibling = whitespace)
        val result = mover.findNextDeclaration(current)
        assertNotNull(result)
        assertEquals(RescriptElementTypes.MODULE_DECLARATION, result!!.node?.elementType)
    }

    @Test
    fun testFindNextDeclarationReturnsNullWhenNoNextSibling() {
        val current = RescriptTestUtils.stubPsiElement(RescriptElementTypes.LET_DECLARATION, nextSibling = null)
        val result = mover.findNextDeclaration(current)
        assertNull(result)
    }

    @Test
    fun testFindNextDeclarationReturnsNullForNonDeclarationSibling() {
        val jsxElement = RescriptTestUtils.stubPsiElement(RescriptElementTypes.JSX_ELEMENT, nextSibling = null)
        val current = RescriptTestUtils.stubPsiElement(RescriptElementTypes.LET_DECLARATION, nextSibling = jsxElement)
        val result = mover.findNextDeclaration(current)
        assertNull(result)
    }

    // -- findPreviousDeclaration PSI stub tests --

    @Test
    fun testFindPreviousDeclarationFindsPrevSiblingDeclaration() {
        val prevDecl = RescriptTestUtils.stubPsiElement(RescriptElementTypes.LET_DECLARATION)
        val current = RescriptTestUtils.stubPsiElement(RescriptElementTypes.TYPE_DECLARATION, prevSibling = prevDecl)
        val result = mover.findPreviousDeclaration(current)
        assertNotNull(result)
        assertEquals(RescriptElementTypes.LET_DECLARATION, result!!.node?.elementType)
    }

    @Test
    fun testFindPreviousDeclarationSkipsWhitespace() {
        val prevDecl = RescriptTestUtils.stubPsiElement(RescriptElementTypes.LET_DECLARATION)
        val whitespace = RescriptTestUtils.stubPsiElement(TokenType.WHITE_SPACE, prevSibling = prevDecl)
        val current = RescriptTestUtils.stubPsiElement(RescriptElementTypes.TYPE_DECLARATION, prevSibling = whitespace)
        val result = mover.findPreviousDeclaration(current)
        assertNotNull(result)
        assertEquals(RescriptElementTypes.LET_DECLARATION, result!!.node?.elementType)
    }

    @Test
    fun testFindPreviousDeclarationReturnsNullWhenNoPrevSibling() {
        val current = RescriptTestUtils.stubPsiElement(RescriptElementTypes.LET_DECLARATION, prevSibling = null)
        val result = mover.findPreviousDeclaration(current)
        assertNull(result)
    }

    @Test
    fun testFindPreviousDeclarationReturnsNullForNonDeclarationSibling() {
        val jsxElement = RescriptTestUtils.stubPsiElement(RescriptElementTypes.JSX_ELEMENT, prevSibling = null)
        val current = RescriptTestUtils.stubPsiElement(RescriptElementTypes.TYPE_DECLARATION, prevSibling = jsxElement)
        val result = mover.findPreviousDeclaration(current)
        assertNull(result)
    }

    @Test
    fun testFindPreviousDeclarationSkipsAnnotation() {
        val prevDecl = RescriptTestUtils.stubPsiElement(RescriptElementTypes.LET_DECLARATION)
        val annotation = RescriptTestUtils.stubPsiElement(RescriptElementTypes.ANNOTATION, prevSibling = prevDecl)
        val current = RescriptTestUtils.stubPsiElement(RescriptElementTypes.TYPE_DECLARATION, prevSibling = annotation)
        val result = mover.findPreviousDeclaration(current)
        assertNotNull(result)
        assertEquals(RescriptElementTypes.LET_DECLARATION, result!!.node?.elementType)
    }

    // -- findLeadingAnnotation PSI stub tests --

    @Test
    fun testFindLeadingAnnotationReturnsDeclarationWhenNoPrevSibling() {
        val declaration = RescriptTestUtils.stubPsiElement(RescriptElementTypes.LET_DECLARATION, prevSibling = null)
        val result = mover.findLeadingAnnotation(declaration)
        assertEquals(RescriptElementTypes.LET_DECLARATION, result.node?.elementType)
    }

    @Test
    fun testFindLeadingAnnotationReturnsDeclarationWhenPrevIsNotAnnotation() {
        val prevDecl = RescriptTestUtils.stubPsiElement(RescriptElementTypes.TYPE_DECLARATION, prevSibling = null)
        val declaration = RescriptTestUtils.stubPsiElement(RescriptElementTypes.LET_DECLARATION, prevSibling = prevDecl)
        val result = mover.findLeadingAnnotation(declaration)
        assertEquals(RescriptElementTypes.LET_DECLARATION, result.node?.elementType)
    }

    @Test
    fun testFindLeadingAnnotationFindsAnnotation() {
        val annotation = RescriptTestUtils.stubPsiElement(RescriptElementTypes.ANNOTATION, prevSibling = null)
        val declaration =
            RescriptTestUtils.stubPsiElement(
                RescriptElementTypes.LET_DECLARATION,
                prevSibling = annotation,
            )
        val result = mover.findLeadingAnnotation(declaration)
        assertEquals(RescriptElementTypes.ANNOTATION, result.node?.elementType)
    }

    @Test
    fun testFindLeadingAnnotationFindsChainedAnnotations() {
        val firstAnnotation = RescriptTestUtils.stubPsiElement(RescriptElementTypes.ANNOTATION, prevSibling = null)
        val secondAnnotation =
            RescriptTestUtils.stubPsiElement(RescriptElementTypes.ANNOTATION, prevSibling = firstAnnotation)
        val declaration =
            RescriptTestUtils.stubPsiElement(RescriptElementTypes.LET_DECLARATION, prevSibling = secondAnnotation)
        val result = mover.findLeadingAnnotation(declaration)
        assertEquals(RescriptElementTypes.ANNOTATION, result.node?.elementType)
    }

    @Test
    fun testFindLeadingAnnotationSkipsWhitespaceBeforeAnnotation() {
        val annotation = RescriptTestUtils.stubPsiElement(RescriptElementTypes.ANNOTATION, prevSibling = null)
        val whitespace = RescriptTestUtils.stubPsiElement(TokenType.WHITE_SPACE, prevSibling = annotation)
        val declaration =
            RescriptTestUtils.stubPsiElement(
                RescriptElementTypes.LET_DECLARATION,
                prevSibling = whitespace,
            )
        val result = mover.findLeadingAnnotation(declaration)
        assertEquals(RescriptElementTypes.ANNOTATION, result.node?.elementType)
    }

    // -- findNextDeclaration annotation branch tests --

    @Test
    fun testFindNextDeclarationFindsDeclarationAfterAnnotation() {
        val nextDecl = RescriptTestUtils.stubPsiElement(RescriptElementTypes.TYPE_DECLARATION, nextSibling = null)
        val annotation = RescriptTestUtils.stubPsiElement(RescriptElementTypes.ANNOTATION, nextSibling = nextDecl)
        val current = RescriptTestUtils.stubPsiElement(RescriptElementTypes.LET_DECLARATION, nextSibling = annotation)
        val result = mover.findNextDeclaration(current)
        assertNotNull(result)
        assertEquals(RescriptElementTypes.TYPE_DECLARATION, result!!.node?.elementType)
    }

    @Test
    fun testFindNextDeclarationReturnsNullWhenAnnotationFollowedByNonDeclaration() {
        val jsxElement = RescriptTestUtils.stubPsiElement(RescriptElementTypes.JSX_ELEMENT, nextSibling = null)
        val annotation = RescriptTestUtils.stubPsiElement(RescriptElementTypes.ANNOTATION, nextSibling = jsxElement)
        val current = RescriptTestUtils.stubPsiElement(RescriptElementTypes.LET_DECLARATION, nextSibling = annotation)
        val result = mover.findNextDeclaration(current)
        assertNull(result)
    }

    @Test
    fun testFindNextDeclarationReturnsNullWhenAnnotationHasNoNextSibling() {
        val annotation = RescriptTestUtils.stubPsiElement(RescriptElementTypes.ANNOTATION, nextSibling = null)
        val current = RescriptTestUtils.stubPsiElement(RescriptElementTypes.LET_DECLARATION, nextSibling = annotation)
        val result = mover.findNextDeclaration(current)
        assertNull(result)
    }

    @Test
    fun testFindNextDeclarationSkipsWhitespaceAfterAnnotation() {
        val nextDecl = RescriptTestUtils.stubPsiElement(RescriptElementTypes.MODULE_DECLARATION, nextSibling = null)
        val whitespace = RescriptTestUtils.stubPsiElement(TokenType.WHITE_SPACE, nextSibling = nextDecl)
        val annotation = RescriptTestUtils.stubPsiElement(RescriptElementTypes.ANNOTATION, nextSibling = whitespace)
        val current = RescriptTestUtils.stubPsiElement(RescriptElementTypes.LET_DECLARATION, nextSibling = annotation)
        val result = mover.findNextDeclaration(current)
        assertNotNull(result)
        assertEquals(RescriptElementTypes.MODULE_DECLARATION, result!!.node?.elementType)
    }

    @Test
    fun testFindNextDeclarationSkipsMultipleAnnotations() {
        val nextDecl = RescriptTestUtils.stubPsiElement(RescriptElementTypes.TYPE_DECLARATION, nextSibling = null)
        val secondAnnotation =
            RescriptTestUtils.stubPsiElement(RescriptElementTypes.ANNOTATION, nextSibling = nextDecl)
        val annotation =
            RescriptTestUtils.stubPsiElement(RescriptElementTypes.ANNOTATION, nextSibling = secondAnnotation)
        val current = RescriptTestUtils.stubPsiElement(RescriptElementTypes.LET_DECLARATION, nextSibling = annotation)
        val result = mover.findNextDeclaration(current)
        assertNotNull(result)
        assertEquals(RescriptElementTypes.TYPE_DECLARATION, result!!.node?.elementType)
    }

    // -- findPreviousDeclaration additional tests --

    @Test
    fun testFindPreviousDeclarationSkipsMultipleAnnotations() {
        val prevDecl = RescriptTestUtils.stubPsiElement(RescriptElementTypes.LET_DECLARATION, prevSibling = null)
        val firstAnnotation =
            RescriptTestUtils.stubPsiElement(RescriptElementTypes.ANNOTATION, prevSibling = prevDecl)
        val secondAnnotation =
            RescriptTestUtils.stubPsiElement(RescriptElementTypes.ANNOTATION, prevSibling = firstAnnotation)
        val current =
            RescriptTestUtils.stubPsiElement(RescriptElementTypes.TYPE_DECLARATION, prevSibling = secondAnnotation)
        val result = mover.findPreviousDeclaration(current)
        assertNotNull(result)
        assertEquals(RescriptElementTypes.LET_DECLARATION, result!!.node?.elementType)
    }

    @Test
    fun testFindPreviousDeclarationSkipsWhitespaceAndAnnotation() {
        val prevDecl = RescriptTestUtils.stubPsiElement(RescriptElementTypes.MODULE_DECLARATION, prevSibling = null)
        val annotation = RescriptTestUtils.stubPsiElement(RescriptElementTypes.ANNOTATION, prevSibling = prevDecl)
        val whitespace = RescriptTestUtils.stubPsiElement(TokenType.WHITE_SPACE, prevSibling = annotation)
        val current = RescriptTestUtils.stubPsiElement(RescriptElementTypes.LET_DECLARATION, prevSibling = whitespace)
        val result = mover.findPreviousDeclaration(current)
        assertNotNull(result)
        assertEquals(RescriptElementTypes.MODULE_DECLARATION, result!!.node?.elementType)
    }
}
