package com.rescript.plugin.lang.psi

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

/**
 * Unit tests for [RescriptStubElementTypes] and [RescriptDeclarationElementType],
 * verifying stub element type definitions, external ID format, and debug name
 * representation.
 *
 * Note: indexStub logic tests are omitted because [RescriptDeclarationStub]
 * extends StubBase which requires IntelliJ Platform test infrastructure
 * (the constructor validates the parent stub internally).
 *
 * @see RescriptStubElementTypes
 * @see RescriptDeclarationElementType
 */
class RescriptStubElementTypesTest {
    // ── Field existence ──────────────────────────────────────────────

    @Test
    fun `all five declaration element types are defined`() {
        assertNotNull(RescriptStubElementTypes.LET_DECLARATION)
        assertNotNull(RescriptStubElementTypes.TYPE_DECLARATION)
        assertNotNull(RescriptStubElementTypes.MODULE_DECLARATION)
        assertNotNull(RescriptStubElementTypes.EXTERNAL_DECLARATION)
        assertNotNull(RescriptStubElementTypes.EXCEPTION_DECLARATION)
    }

    // ── External ID format ───────────────────────────────────────────

    @Test
    fun `LET_DECLARATION has correct external ID`() {
        assertEquals("rescript.LET_DECLARATION", RescriptStubElementTypes.LET_DECLARATION.externalId)
    }

    @Test
    fun `TYPE_DECLARATION has correct external ID`() {
        assertEquals("rescript.TYPE_DECLARATION", RescriptStubElementTypes.TYPE_DECLARATION.externalId)
    }

    @Test
    fun `MODULE_DECLARATION has correct external ID`() {
        assertEquals("rescript.MODULE_DECLARATION", RescriptStubElementTypes.MODULE_DECLARATION.externalId)
    }

    @Test
    fun `EXTERNAL_DECLARATION has correct external ID`() {
        assertEquals("rescript.EXTERNAL_DECLARATION", RescriptStubElementTypes.EXTERNAL_DECLARATION.externalId)
    }

    @Test
    fun `EXCEPTION_DECLARATION has correct external ID`() {
        assertEquals("rescript.EXCEPTION_DECLARATION", RescriptStubElementTypes.EXCEPTION_DECLARATION.externalId)
    }

    // ── External ID prefix ───────────────────────────────────────────

    @Test
    fun `all external IDs start with rescript prefix`() {
        val types =
            listOf(
                RescriptStubElementTypes.LET_DECLARATION,
                RescriptStubElementTypes.TYPE_DECLARATION,
                RescriptStubElementTypes.MODULE_DECLARATION,
                RescriptStubElementTypes.EXTERNAL_DECLARATION,
                RescriptStubElementTypes.EXCEPTION_DECLARATION,
            )
        types.forEach { type ->
            assert(type.externalId.startsWith("rescript.")) {
                "${type.externalId} does not start with 'rescript.'"
            }
        }
    }

    // ── toString ─────────────────────────────────────────────────────

    @Test
    fun `LET_DECLARATION toString returns debug name`() {
        assertEquals("LET_DECLARATION", RescriptStubElementTypes.LET_DECLARATION.toString())
    }

    @Test
    fun `TYPE_DECLARATION toString returns debug name`() {
        assertEquals("TYPE_DECLARATION", RescriptStubElementTypes.TYPE_DECLARATION.toString())
    }

    @Test
    fun `MODULE_DECLARATION toString returns debug name`() {
        assertEquals("MODULE_DECLARATION", RescriptStubElementTypes.MODULE_DECLARATION.toString())
    }

    @Test
    fun `EXTERNAL_DECLARATION toString returns debug name`() {
        assertEquals("EXTERNAL_DECLARATION", RescriptStubElementTypes.EXTERNAL_DECLARATION.toString())
    }

    @Test
    fun `EXCEPTION_DECLARATION toString returns debug name`() {
        assertEquals("EXCEPTION_DECLARATION", RescriptStubElementTypes.EXCEPTION_DECLARATION.toString())
    }

    // ── Unique instances ─────────────────────────────────────────────

    @Test
    fun `each element type is a distinct instance`() {
        val types =
            listOf(
                RescriptStubElementTypes.LET_DECLARATION,
                RescriptStubElementTypes.TYPE_DECLARATION,
                RescriptStubElementTypes.MODULE_DECLARATION,
                RescriptStubElementTypes.EXTERNAL_DECLARATION,
                RescriptStubElementTypes.EXCEPTION_DECLARATION,
            )
        assertEquals(5, types.toSet().size)
    }

    @Test
    fun `each element type has unique external ID`() {
        val ids =
            listOf(
                RescriptStubElementTypes.LET_DECLARATION.externalId,
                RescriptStubElementTypes.TYPE_DECLARATION.externalId,
                RescriptStubElementTypes.MODULE_DECLARATION.externalId,
                RescriptStubElementTypes.EXTERNAL_DECLARATION.externalId,
                RescriptStubElementTypes.EXCEPTION_DECLARATION.externalId,
            )
        assertEquals(5, ids.toSet().size)
    }

    // ── Type consistency ─────────────────────────────────────────────

    @Test
    fun `all element types are instances of RescriptDeclarationElementType`() {
        val types: List<Any> =
            listOf(
                RescriptStubElementTypes.LET_DECLARATION,
                RescriptStubElementTypes.TYPE_DECLARATION,
                RescriptStubElementTypes.MODULE_DECLARATION,
                RescriptStubElementTypes.EXTERNAL_DECLARATION,
                RescriptStubElementTypes.EXCEPTION_DECLARATION,
            )
        types.forEach { type ->
            assert(type is RescriptDeclarationElementType) {
                "$type is not a RescriptDeclarationElementType"
            }
        }
    }
}
