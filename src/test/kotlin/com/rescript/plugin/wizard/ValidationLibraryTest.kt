package com.rescript.plugin.wizard

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ValidationLibraryTest {
    @Test
    fun `exposes zod and sury entries`() {
        val names = ValidationLibrary.entries.map { it.name }
        assertTrue(names.contains("ZOD"))
        assertTrue(names.contains("SURY"))
        assertEquals(2, ValidationLibrary.entries.size)
    }

    @Test
    fun `display name and npm package coincide for zod`() {
        assertEquals("zod", ValidationLibrary.ZOD.displayName)
        assertEquals("zod", ValidationLibrary.ZOD.npmPackage)
    }

    @Test
    fun `display name and npm package coincide for sury`() {
        assertEquals("sury", ValidationLibrary.SURY.displayName)
        assertEquals("sury", ValidationLibrary.SURY.npmPackage)
    }

    @Test
    fun `variantKey is lowercase name`() {
        assertEquals("zod", ValidationLibrary.ZOD.variantKey())
        assertEquals("sury", ValidationLibrary.SURY.variantKey())
    }
}
