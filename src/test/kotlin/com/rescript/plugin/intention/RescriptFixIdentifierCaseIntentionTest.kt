package com.rescript.plugin.intention

import com.rescript.plugin.RescriptTestUtils
import com.rescript.plugin.lang.RescriptTokenTypes
import com.rescript.plugin.lang.psi.RescriptElementTypes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptFixIdentifierCaseIntentionTest {
    @Test
    fun testFamilyName() {
        val intention = RescriptFixIdentifierCaseIntention()
        assertEquals("Fix identifier case", intention.familyName)
    }

    @Test
    fun testStartInWriteAction() {
        val intention = RescriptFixIdentifierCaseIntention()
        assertTrue(intention.startInWriteAction())
    }

    // -- toPascalCase tests --

    @Test
    fun testToPascalCaseConvertsFirstCharToUppercase() {
        assertEquals("MyModule", RescriptFixIdentifierCaseIntention.toPascalCase("myModule"))
    }

    @Test
    fun testToPascalCaseKeepsAlreadyPascalCase() {
        assertEquals("MyModule", RescriptFixIdentifierCaseIntention.toPascalCase("MyModule"))
    }

    @Test
    fun testToPascalCaseSingleChar() {
        assertEquals("A", RescriptFixIdentifierCaseIntention.toPascalCase("a"))
    }

    @Test
    fun testToPascalCaseEmpty() {
        assertEquals("", RescriptFixIdentifierCaseIntention.toPascalCase(""))
    }

    // -- toCamelCase tests --

    @Test
    fun testToCamelCaseConvertsFirstCharToLowercase() {
        assertEquals("myVar", RescriptFixIdentifierCaseIntention.toCamelCase("MyVar"))
    }

    @Test
    fun testToCamelCaseKeepsAlreadyCamelCase() {
        assertEquals("myVar", RescriptFixIdentifierCaseIntention.toCamelCase("myVar"))
    }

    @Test
    fun testToCamelCaseSingleChar() {
        assertEquals("a", RescriptFixIdentifierCaseIntention.toCamelCase("A"))
    }

    @Test
    fun testToCamelCaseEmpty() {
        assertEquals("", RescriptFixIdentifierCaseIntention.toCamelCase(""))
    }

    // -- isInsideModuleDeclaration tests --

    @Test
    fun testIsInsideModuleDeclarationReturnsTrue() {
        val moduleDecl = RescriptTestUtils.stubPsiElement(RescriptElementTypes.MODULE_DECLARATION)
        val child = RescriptTestUtils.stubPsiElement(RescriptTokenTypes.LIDENT, parent = moduleDecl)
        assertTrue(RescriptFixIdentifierCaseIntention.isInsideModuleDeclaration(child))
    }

    @Test
    fun testIsInsideModuleDeclarationReturnsFalseForLetDecl() {
        val letDecl = RescriptTestUtils.stubPsiElement(RescriptElementTypes.LET_DECLARATION)
        val child = RescriptTestUtils.stubPsiElement(RescriptTokenTypes.LIDENT, parent = letDecl)
        assertFalse(RescriptFixIdentifierCaseIntention.isInsideModuleDeclaration(child))
    }

    @Test
    fun testIsInsideModuleDeclarationReturnsFalseForNoParent() {
        val child = RescriptTestUtils.stubPsiElement(RescriptTokenTypes.LIDENT, parent = null)
        assertFalse(RescriptFixIdentifierCaseIntention.isInsideModuleDeclaration(child))
    }

    // -- isInsideLetDeclaration tests --

    @Test
    fun testIsInsideLetDeclarationReturnsTrue() {
        val letDecl = RescriptTestUtils.stubPsiElement(RescriptElementTypes.LET_DECLARATION)
        val child = RescriptTestUtils.stubPsiElement(RescriptTokenTypes.UIDENT, parent = letDecl)
        assertTrue(RescriptFixIdentifierCaseIntention.isInsideLetDeclaration(child))
    }

    @Test
    fun testIsInsideLetDeclarationReturnsFalseForModuleDecl() {
        val moduleDecl = RescriptTestUtils.stubPsiElement(RescriptElementTypes.MODULE_DECLARATION)
        val child = RescriptTestUtils.stubPsiElement(RescriptTokenTypes.UIDENT, parent = moduleDecl)
        assertFalse(RescriptFixIdentifierCaseIntention.isInsideLetDeclaration(child))
    }

    @Test
    fun testIsInsideLetDeclarationReturnsFalseForNoParent() {
        val child = RescriptTestUtils.stubPsiElement(RescriptTokenTypes.UIDENT, parent = null)
        assertFalse(RescriptFixIdentifierCaseIntention.isInsideLetDeclaration(child))
    }

    @Test
    fun testIsAvailableReturnsFalseForNullEditor() {
        val intention = RescriptFixIdentifierCaseIntention()
        val element = RescriptTestUtils.stubPsiElement(RescriptTokenTypes.LIDENT)
        val project = RescriptTestUtils.stubProject()
        assertFalse(intention.isAvailable(project, null, element))
    }
}
