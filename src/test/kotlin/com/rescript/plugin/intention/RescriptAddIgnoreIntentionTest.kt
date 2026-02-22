package com.rescript.plugin.intention

import com.rescript.plugin.RescriptTestUtils
import com.rescript.plugin.lang.psi.RescriptElementTypes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptAddIgnoreIntentionTest {
    @Test
    fun testIntentionText() {
        val intention = RescriptAddIgnoreIntention()
        assertEquals("Add ->ignore", intention.text)
    }

    @Test
    fun testIntentionFamilyName() {
        val intention = RescriptAddIgnoreIntention()
        assertEquals("Add ->ignore", intention.familyName)
    }

    @Test
    fun testStartInWriteAction() {
        val intention = RescriptAddIgnoreIntention()
        assertTrue(intention.startInWriteAction())
    }

    @Test
    fun testTextAndFamilyNameAreConsistent() {
        val intention = RescriptAddIgnoreIntention()
        assertEquals(intention.text, intention.familyName)
    }

    // -- findParentDeclaration tests --

    @Test
    fun testFindParentDeclarationForLetDeclaration() {
        val letDecl = RescriptTestUtils.stubPsiElement(RescriptElementTypes.LET_DECLARATION)
        val child = RescriptTestUtils.stubPsiElement(RescriptElementTypes.ANNOTATION, parent = letDecl)
        val result = RescriptAddIgnoreIntention.findParentDeclaration(child)
        assertNotNull(result)
        assertEquals(RescriptElementTypes.LET_DECLARATION, result!!.node?.elementType)
    }

    @Test
    fun testFindParentDeclarationForExternalDeclaration() {
        val externalDecl = RescriptTestUtils.stubPsiElement(RescriptElementTypes.EXTERNAL_DECLARATION)
        val child = RescriptTestUtils.stubPsiElement(RescriptElementTypes.ANNOTATION, parent = externalDecl)
        val result = RescriptAddIgnoreIntention.findParentDeclaration(child)
        assertNotNull(result)
        assertEquals(RescriptElementTypes.EXTERNAL_DECLARATION, result!!.node?.elementType)
    }

    @Test
    fun testFindParentDeclarationReturnsNullForNonDeclaration() {
        val openStmt = RescriptTestUtils.stubPsiElement(RescriptElementTypes.OPEN_STATEMENT, parent = null)
        val result = RescriptAddIgnoreIntention.findParentDeclaration(openStmt)
        assertNull(result)
    }

    @Test
    fun testFindParentDeclarationReturnsNullForModuleDeclaration() {
        val moduleDecl = RescriptTestUtils.stubPsiElement(RescriptElementTypes.MODULE_DECLARATION, parent = null)
        val result = RescriptAddIgnoreIntention.findParentDeclaration(moduleDecl)
        assertNull(result)
    }

    // -- alreadyHasIgnore tests --

    @Test
    fun testAlreadyHasIgnoreReturnsTrueForIgnore() {
        assertTrue(RescriptAddIgnoreIntention.alreadyHasIgnore("expr->ignore"))
    }

    @Test
    fun testAlreadyHasIgnoreReturnsTrueForIgnoreWithSpace() {
        assertTrue(RescriptAddIgnoreIntention.alreadyHasIgnore("expr-> ignore"))
    }

    @Test
    fun testAlreadyHasIgnoreReturnsTrueWithTrailingWhitespace() {
        assertTrue(RescriptAddIgnoreIntention.alreadyHasIgnore("expr->ignore  "))
    }

    @Test
    fun testAlreadyHasIgnoreReturnsFalseForOther() {
        assertFalse(RescriptAddIgnoreIntention.alreadyHasIgnore("expr->map"))
    }

    @Test
    fun testAlreadyHasIgnoreReturnsFalseForEmpty() {
        assertFalse(RescriptAddIgnoreIntention.alreadyHasIgnore(""))
    }

    @Test
    fun testAlreadyHasIgnoreReturnsFalseForSimpleExpr() {
        assertFalse(RescriptAddIgnoreIntention.alreadyHasIgnore("let x = 1"))
    }

    @Test
    fun testIsAvailableReturnsFalseForNullEditor() {
        val intention = RescriptAddIgnoreIntention()
        val element = RescriptTestUtils.stubPsiElement(RescriptElementTypes.LET_DECLARATION)
        val project = RescriptTestUtils.stubProject()
        assertFalse(intention.isAvailable(project, null, element))
    }
}
