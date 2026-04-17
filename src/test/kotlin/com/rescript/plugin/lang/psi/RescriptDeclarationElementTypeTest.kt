package com.rescript.plugin.lang.psi

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptDeclarationElementTypeTest {
    @Test
    fun testExternalIdPrefix() {
        assertEquals("rescript.LET_DECLARATION", RescriptStubElementTypes.LET_DECLARATION.externalId)
    }

    @Test
    fun testExternalIdForModule() {
        assertEquals("rescript.MODULE_DECLARATION", RescriptStubElementTypes.MODULE_DECLARATION.externalId)
    }

    @Test
    fun testExternalIdForType() {
        assertEquals("rescript.TYPE_DECLARATION", RescriptStubElementTypes.TYPE_DECLARATION.externalId)
    }

    @Test
    fun testExternalIdForExternal() {
        assertEquals("rescript.EXTERNAL_DECLARATION", RescriptStubElementTypes.EXTERNAL_DECLARATION.externalId)
    }

    @Test
    fun testExternalIdForException() {
        assertEquals("rescript.EXCEPTION_DECLARATION", RescriptStubElementTypes.EXCEPTION_DECLARATION.externalId)
    }

    @Test
    fun testToString() {
        assertEquals("LET_DECLARATION", RescriptStubElementTypes.LET_DECLARATION.toString())
    }

    // These assertions guard against accidental type changes in the element-type
    // registry. The `is` checks look tautological to the compiler because the
    // singleton `val`s expose their concrete type, so we suppress the warning.

    @Test
    @Suppress("USELESS_IS_CHECK")
    fun testIsSubtypeOfIElementType() {
        assertTrue(RescriptStubElementTypes.LET_DECLARATION is com.intellij.psi.tree.IElementType)
    }

    @Test
    @Suppress("USELESS_IS_CHECK")
    fun testElementTypesAreStubBased() {
        assertTrue(RescriptElementTypes.LET_DECLARATION is RescriptDeclarationElementType)
        assertTrue(RescriptElementTypes.TYPE_DECLARATION is RescriptDeclarationElementType)
        assertTrue(RescriptElementTypes.MODULE_DECLARATION is RescriptDeclarationElementType)
        assertTrue(RescriptElementTypes.EXTERNAL_DECLARATION is RescriptDeclarationElementType)
        assertTrue(RescriptElementTypes.EXCEPTION_DECLARATION is RescriptDeclarationElementType)
    }

    @Test
    @Suppress("USELESS_IS_CHECK")
    fun testNonStubElementTypesRemainPlain() {
        assertTrue(RescriptElementTypes.OPEN_STATEMENT is RescriptElementType)
        assertTrue(RescriptElementTypes.INCLUDE_STATEMENT is RescriptElementType)
        assertTrue(RescriptElementTypes.ANNOTATION is RescriptElementType)
        assertTrue(RescriptElementTypes.JSX_ELEMENT is RescriptElementType)
        assertTrue(RescriptElementTypes.JSX_SELF_CLOSING_ELEMENT is RescriptElementType)
        assertTrue(RescriptElementTypes.JSX_FRAGMENT is RescriptElementType)
    }

    @Test
    fun testElementTypesDelegateToStubTypes() {
        // Verify that RescriptElementTypes delegates to the canonical instances in RescriptStubElementTypes
        assertTrue(RescriptElementTypes.LET_DECLARATION === RescriptStubElementTypes.LET_DECLARATION)
        assertTrue(RescriptElementTypes.TYPE_DECLARATION === RescriptStubElementTypes.TYPE_DECLARATION)
        assertTrue(RescriptElementTypes.MODULE_DECLARATION === RescriptStubElementTypes.MODULE_DECLARATION)
        assertTrue(RescriptElementTypes.EXTERNAL_DECLARATION === RescriptStubElementTypes.EXTERNAL_DECLARATION)
        assertTrue(RescriptElementTypes.EXCEPTION_DECLARATION === RescriptStubElementTypes.EXCEPTION_DECLARATION)
    }

    // Note: RescriptDeclarationStub instantiation tests are omitted because StubBase
    // requires IDE lifecycle context (Application, Project). The stub data preservation
    // is implicitly tested through integration with the IDE stub indexing infrastructure.
}
