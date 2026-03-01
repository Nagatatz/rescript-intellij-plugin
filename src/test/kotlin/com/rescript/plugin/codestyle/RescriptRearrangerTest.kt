package com.rescript.plugin.codestyle

import com.rescript.plugin.lang.psi.RescriptElementTypes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class RescriptRearrangerTest {
    @Test
    fun `classifyElement returns OPEN_INCLUDE for open statements`() {
        assertEquals(
            DeclarationCategory.OPEN_INCLUDE,
            RescriptRearranger.classifyElement(RescriptElementTypes.OPEN_STATEMENT),
        )
    }

    @Test
    fun `classifyElement returns OPEN_INCLUDE for include statements`() {
        assertEquals(
            DeclarationCategory.OPEN_INCLUDE,
            RescriptRearranger.classifyElement(RescriptElementTypes.INCLUDE_STATEMENT),
        )
    }

    @Test
    fun `classifyElement returns TYPE for type declarations`() {
        assertEquals(
            DeclarationCategory.TYPE,
            RescriptRearranger.classifyElement(RescriptElementTypes.TYPE_DECLARATION),
        )
    }

    @Test
    fun `classifyElement returns EXCEPTION for exception declarations`() {
        assertEquals(
            DeclarationCategory.EXCEPTION,
            RescriptRearranger.classifyElement(RescriptElementTypes.EXCEPTION_DECLARATION),
        )
    }

    @Test
    fun `classifyElement returns MODULE for module declarations`() {
        assertEquals(
            DeclarationCategory.MODULE,
            RescriptRearranger.classifyElement(RescriptElementTypes.MODULE_DECLARATION),
        )
    }

    @Test
    fun `classifyElement returns EXTERNAL for external declarations`() {
        assertEquals(
            DeclarationCategory.EXTERNAL,
            RescriptRearranger.classifyElement(RescriptElementTypes.EXTERNAL_DECLARATION),
        )
    }

    @Test
    fun `classifyElement returns LET for let declarations`() {
        assertEquals(
            DeclarationCategory.LET,
            RescriptRearranger.classifyElement(RescriptElementTypes.LET_DECLARATION),
        )
    }

    @Test
    fun `classifyElement returns null for JSX elements`() {
        assertNull(RescriptRearranger.classifyElement(RescriptElementTypes.JSX_ELEMENT))
    }

    @Test
    fun `classifyElement returns null for annotations`() {
        assertNull(RescriptRearranger.classifyElement(RescriptElementTypes.ANNOTATION))
    }

    @Test
    fun `sortOrder follows canonical order`() {
        assertEquals(0, RescriptRearranger.sortOrder(DeclarationCategory.OPEN_INCLUDE))
        assertEquals(1, RescriptRearranger.sortOrder(DeclarationCategory.TYPE))
        assertEquals(2, RescriptRearranger.sortOrder(DeclarationCategory.EXCEPTION))
        assertEquals(3, RescriptRearranger.sortOrder(DeclarationCategory.MODULE))
        assertEquals(4, RescriptRearranger.sortOrder(DeclarationCategory.EXTERNAL))
        assertEquals(5, RescriptRearranger.sortOrder(DeclarationCategory.LET))
    }

    @Test
    fun `DEFAULT_ORDER has 6 categories`() {
        assertEquals(6, RescriptRearranger.DEFAULT_ORDER.size)
    }

    @Test
    fun `rearranger can be instantiated`() {
        assertNotNull(RescriptRearranger())
    }

    @Test
    fun `declaration categories cover all 6 types`() {
        assertEquals(6, DeclarationCategory.entries.size)
    }
}
