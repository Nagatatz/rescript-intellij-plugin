package com.rescript.plugin.lang.psi

import com.rescript.plugin.RescriptLanguage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptPsiTest {
    // ── RescriptElementType ────────────────────────────────────────

    @Test
    fun `element type has correct debug name`() {
        val type = RescriptElementType("TEST_TYPE")
        assertEquals("TEST_TYPE", type.toString())
    }

    @Test
    fun `element type belongs to ReScript language`() {
        val type = RescriptElementType("TEST_TYPE")
        assertEquals(RescriptLanguage, type.language)
    }

    // ── RescriptElementTypes non-stub types ────────────────────────

    @Test
    fun `OPEN_STATEMENT is defined`() {
        assertNotNull(RescriptElementTypes.OPEN_STATEMENT)
        assertEquals("OPEN_STATEMENT", RescriptElementTypes.OPEN_STATEMENT.toString())
    }

    @Test
    fun `INCLUDE_STATEMENT is defined`() {
        assertNotNull(RescriptElementTypes.INCLUDE_STATEMENT)
        assertEquals("INCLUDE_STATEMENT", RescriptElementTypes.INCLUDE_STATEMENT.toString())
    }

    @Test
    fun `ANNOTATION is defined`() {
        assertNotNull(RescriptElementTypes.ANNOTATION)
        assertEquals("ANNOTATION", RescriptElementTypes.ANNOTATION.toString())
    }

    @Test
    fun `JSX element types are defined`() {
        assertNotNull(RescriptElementTypes.JSX_ELEMENT)
        assertNotNull(RescriptElementTypes.JSX_SELF_CLOSING_ELEMENT)
        assertNotNull(RescriptElementTypes.JSX_FRAGMENT)
    }

    @Test
    fun `non-stub element types are RescriptElementType instances`() {
        assertTrue(RescriptElementTypes.OPEN_STATEMENT is RescriptElementType)
        assertTrue(RescriptElementTypes.INCLUDE_STATEMENT is RescriptElementType)
        assertTrue(RescriptElementTypes.ANNOTATION is RescriptElementType)
        assertTrue(RescriptElementTypes.JSX_ELEMENT is RescriptElementType)
        assertTrue(RescriptElementTypes.JSX_SELF_CLOSING_ELEMENT is RescriptElementType)
        assertTrue(RescriptElementTypes.JSX_FRAGMENT is RescriptElementType)
    }

    // ── RescriptStubElementTypes ───────────────────────────────────

    @Test
    fun `all five declaration types are distinct instances`() {
        val types =
            setOf(
                RescriptStubElementTypes.LET_DECLARATION,
                RescriptStubElementTypes.TYPE_DECLARATION,
                RescriptStubElementTypes.MODULE_DECLARATION,
                RescriptStubElementTypes.EXTERNAL_DECLARATION,
                RescriptStubElementTypes.EXCEPTION_DECLARATION,
            )
        assertEquals(5, types.size)
    }
}
