package com.rescript.plugin.migration

import com.intellij.testFramework.LightVirtualFile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

/**
 * Shape tests for the Migration Pilot's domain model: [MigrationCandidate],
 * [ConversionStatus] and [ConversionResult]. Companions to the heavier
 * E2E suite in `RescriptMigrationConverterE2eTest`.
 */
class RescriptMigrationModelTest {
    @Test
    fun `MigrationCandidate retains file and relativePath`() {
        val file = LightVirtualFile("Foo.re")
        val candidate = MigrationCandidate(file, "src/Foo.re")
        assertEquals(file, candidate.file)
        assertEquals("src/Foo.re", candidate.relativePath)
    }

    @Test
    fun `MigrationCandidate equality is by all fields`() {
        val file = LightVirtualFile("Foo.re")
        val a = MigrationCandidate(file, "src/Foo.re")
        val b = MigrationCandidate(file, "src/Foo.re")
        val different = a.copy(relativePath = "lib/Foo.re")
        assertEquals(a, b)
        assertNotEquals(a, different)
    }

    @Test
    fun `ConversionStatus has SUCCESS and FAILED in declared order`() {
        assertEquals(listOf("SUCCESS", "FAILED"), ConversionStatus.entries.map { it.name })
    }

    @Test
    fun `ConversionResult retains every property verbatim`() {
        val candidate = MigrationCandidate(LightVirtualFile("Bar.re"), "src/Bar.re")
        val result = ConversionResult(candidate, ConversionStatus.SUCCESS, "converted")
        assertEquals(candidate, result.candidate)
        assertEquals(ConversionStatus.SUCCESS, result.status)
        assertEquals("converted", result.message)
    }

    @Test
    fun `ConversionResult equality is by all fields`() {
        val candidate = MigrationCandidate(LightVirtualFile("Bar.re"), "src/Bar.re")
        val a = ConversionResult(candidate, ConversionStatus.FAILED, "boom")
        val b = ConversionResult(candidate, ConversionStatus.FAILED, "boom")
        val different = a.copy(status = ConversionStatus.SUCCESS)
        assertEquals(a, b)
        assertNotEquals(a, different)
    }
}
