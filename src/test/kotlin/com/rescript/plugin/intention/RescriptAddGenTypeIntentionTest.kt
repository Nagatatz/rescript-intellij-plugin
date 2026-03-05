package com.rescript.plugin.intention

import com.intellij.psi.TokenType
import com.rescript.plugin.RescriptTestUtils
import com.rescript.plugin.lang.psi.RescriptElementTypes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptAddGenTypeIntentionTest {
    @Test
    fun testIntentionText() {
        val intention = RescriptAddGenTypeIntention()
        assertEquals("Add @genType annotation", intention.text)
    }

    @Test
    fun testIntentionFamilyName() {
        val intention = RescriptAddGenTypeIntention()
        assertEquals("Add @genType annotation", intention.familyName)
    }

    @Test
    fun testStartInWriteAction() {
        val intention = RescriptAddGenTypeIntention()
        assertTrue(intention.startInWriteAction())
    }

    @Test
    fun testTextAndFamilyNameAreConsistent() {
        val intention = RescriptAddGenTypeIntention()
        assertEquals(intention.text, intention.familyName)
    }

    // -- findParentDeclaration PSI stub tests --

    @Test
    fun testFindParentDeclarationForLetDeclaration() {
        val letDecl = RescriptTestUtils.stubPsiElement(RescriptElementTypes.LET_DECLARATION)
        val child = RescriptTestUtils.stubPsiElement(RescriptElementTypes.ANNOTATION, parent = letDecl)
        val result = RescriptAddGenTypeIntention.findParentDeclaration(child)
        assertNotNull(result)
        assertEquals(RescriptElementTypes.LET_DECLARATION, result!!.node?.elementType)
    }

    @Test
    fun testFindParentDeclarationForTypeDeclaration() {
        val typeDecl = RescriptTestUtils.stubPsiElement(RescriptElementTypes.TYPE_DECLARATION)
        val child = RescriptTestUtils.stubPsiElement(RescriptElementTypes.ANNOTATION, parent = typeDecl)
        val result = RescriptAddGenTypeIntention.findParentDeclaration(child)
        assertNotNull(result)
        assertEquals(RescriptElementTypes.TYPE_DECLARATION, result!!.node?.elementType)
    }

    @Test
    fun testFindParentDeclarationForModuleDeclaration() {
        val moduleDecl = RescriptTestUtils.stubPsiElement(RescriptElementTypes.MODULE_DECLARATION)
        val child = RescriptTestUtils.stubPsiElement(RescriptElementTypes.ANNOTATION, parent = moduleDecl)
        val result = RescriptAddGenTypeIntention.findParentDeclaration(child)
        assertNotNull(result)
        assertEquals(RescriptElementTypes.MODULE_DECLARATION, result!!.node?.elementType)
    }

    @Test
    fun testFindParentDeclarationReturnsNullForNonDeclaration() {
        val annotation = RescriptTestUtils.stubPsiElement(RescriptElementTypes.ANNOTATION, parent = null)
        val result = RescriptAddGenTypeIntention.findParentDeclaration(annotation)
        assertNull(result)
    }

    @Test
    fun testFindParentDeclarationFindsDirectDeclaration() {
        val letDecl = RescriptTestUtils.stubPsiElement(RescriptElementTypes.LET_DECLARATION)
        val result = RescriptAddGenTypeIntention.findParentDeclaration(letDecl)
        assertNotNull(result)
        assertEquals(RescriptElementTypes.LET_DECLARATION, result!!.node?.elementType)
    }

    @Test
    fun testFindParentDeclarationIgnoresOpenStatement() {
        // OPEN_STATEMENT is NOT in the DECLARATION_TYPES for @genType
        val openStmt = RescriptTestUtils.stubPsiElement(RescriptElementTypes.OPEN_STATEMENT, parent = null)
        val result = RescriptAddGenTypeIntention.findParentDeclaration(openStmt)
        assertNull(result)
    }

    // -- hasGenTypeAnnotation PSI stub tests --

    @Test
    fun testHasGenTypeAnnotationReturnsTrueForGenType() {
        val annotation =
            RescriptTestUtils.stubPsiElement(
                RescriptElementTypes.ANNOTATION,
                text = "@genType",
            )
        val declaration =
            RescriptTestUtils.stubPsiElement(
                RescriptElementTypes.LET_DECLARATION,
                prevSibling = annotation,
            )
        assertTrue(RescriptAddGenTypeIntention.hasGenTypeAnnotation(declaration))
    }

    @Test
    fun testHasGenTypeAnnotationReturnsTrueForGenTypeWithArgs() {
        val annotation =
            RescriptTestUtils.stubPsiElement(
                RescriptElementTypes.ANNOTATION,
                text = "@genType(opaque)",
            )
        val declaration =
            RescriptTestUtils.stubPsiElement(
                RescriptElementTypes.LET_DECLARATION,
                prevSibling = annotation,
            )
        assertTrue(RescriptAddGenTypeIntention.hasGenTypeAnnotation(declaration))
    }

    @Test
    fun testHasGenTypeAnnotationReturnsFalseForOtherAnnotation() {
        val annotation =
            RescriptTestUtils.stubPsiElement(
                RescriptElementTypes.ANNOTATION,
                text = "@module",
            )
        val declaration =
            RescriptTestUtils.stubPsiElement(
                RescriptElementTypes.LET_DECLARATION,
                prevSibling = annotation,
            )
        assertFalse(RescriptAddGenTypeIntention.hasGenTypeAnnotation(declaration))
    }

    @Test
    fun testHasGenTypeAnnotationReturnsFalseForNoPrevSibling() {
        val declaration =
            RescriptTestUtils.stubPsiElement(
                RescriptElementTypes.LET_DECLARATION,
                prevSibling = null,
            )
        assertFalse(RescriptAddGenTypeIntention.hasGenTypeAnnotation(declaration))
    }

    @Test
    fun testHasGenTypeAnnotationSkipsWhitespaceBetween() {
        val annotation =
            RescriptTestUtils.stubPsiElement(
                RescriptElementTypes.ANNOTATION,
                text = "@genType",
                prevSibling = null,
            )
        // Use a whitespace stub (text = "\n") between annotation and declaration
        val ws =
            RescriptTestUtils.stubPsiElement(
                TokenType.WHITE_SPACE,
                text = "\n",
                prevSibling = annotation,
            )
        val declaration =
            RescriptTestUtils.stubPsiElement(
                RescriptElementTypes.LET_DECLARATION,
                prevSibling = ws,
            )
        assertTrue(RescriptAddGenTypeIntention.hasGenTypeAnnotation(declaration))
    }

    @Test
    fun testHasGenTypeAnnotationReturnsFalseForNonAnnotationPrev() {
        // A non-blank, non-annotation prev sibling breaks the loop
        val otherDecl =
            RescriptTestUtils.stubPsiElement(
                RescriptElementTypes.LET_DECLARATION,
                text = "let x = 1",
                prevSibling = null,
            )
        val declaration =
            RescriptTestUtils.stubPsiElement(
                RescriptElementTypes.TYPE_DECLARATION,
                prevSibling = otherDecl,
            )
        assertFalse(RescriptAddGenTypeIntention.hasGenTypeAnnotation(declaration))
    }

    @Test
    fun testFindParentDeclarationWalksMultipleLevels() {
        val moduleDecl = RescriptTestUtils.stubPsiElement(RescriptElementTypes.MODULE_DECLARATION)
        val intermediate = RescriptTestUtils.stubPsiElement(RescriptElementTypes.ANNOTATION, parent = moduleDecl)
        val child = RescriptTestUtils.stubPsiElement(RescriptElementTypes.ANNOTATION, parent = intermediate)
        val result = RescriptAddGenTypeIntention.findParentDeclaration(child)
        assertNotNull(result)
        assertEquals(RescriptElementTypes.MODULE_DECLARATION, result!!.node?.elementType)
    }

    @Test
    fun testIsAvailableReturnsFalseForNullEditor() {
        val intention = RescriptAddGenTypeIntention()
        val element = RescriptTestUtils.stubPsiElement(RescriptElementTypes.LET_DECLARATION)
        val project = RescriptTestUtils.stubProject()
        // containingFile returns null (not RescriptFile), so returns false
        assertFalse(intention.isAvailable(project, null, element))
    }
}
