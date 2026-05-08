package com.rescript.plugin.migration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Tests the pure argv builder of [RescriptMigrationConverter]. The
 * actual ProcessBuilder execution is exempt from unit tests under
 * the IDE-fixture / external-process clause in tasklist.md.
 */
class RescriptMigrationConverterTest {
    @Test
    fun `blank binary path falls back to npx rescript`() {
        assertEquals(
            listOf("npx", "rescript", "convert", "/tmp/Main.re"),
            RescriptMigrationConverter.buildCommand("", "/tmp/Main.re"),
        )
    }

    @Test
    fun `whitespace-only binary path also falls back to npx rescript`() {
        assertEquals(
            listOf("npx", "rescript", "convert", "/tmp/Main.re"),
            RescriptMigrationConverter.buildCommand("   ", "/tmp/Main.re"),
        )
    }

    @Test
    fun `explicit binary path is used directly`() {
        assertEquals(
            listOf("/usr/local/bin/rescript", "convert", "/tmp/Main.re"),
            RescriptMigrationConverter.buildCommand("/usr/local/bin/rescript", "/tmp/Main.re"),
        )
    }

    @Test
    fun `source path with spaces is preserved as a single argument`() {
        assertEquals(
            listOf("npx", "rescript", "convert", "/tmp/My Folder/Main.re"),
            RescriptMigrationConverter.buildCommand("", "/tmp/My Folder/Main.re"),
        )
    }
}
