package com.rescript.plugin.editor

import com.rescript.plugin.lang.psi.RescriptElementTypes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
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
}
