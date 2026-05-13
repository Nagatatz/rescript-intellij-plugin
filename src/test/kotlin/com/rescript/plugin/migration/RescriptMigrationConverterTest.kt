package com.rescript.plugin.migration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Tests the pure argv builder of [RescriptMigrationConverter]. The
 * actual ProcessBuilder execution is exempt from unit tests under
 * the IDE-fixture / external-process clause in tasklist.md.
 *
 * The argv must carry the file as a project-relative path so the
 * `rescript convert` CLI can resolve it against the cwd we pin to
 * the project base path.
 */
class RescriptMigrationConverterTest {
    @Test
    fun `blank binary path falls back to npx rescript`() {
        assertEquals(
            listOf("npx", "rescript", "convert", "src/Main.re"),
            RescriptMigrationConverter.buildCommand("", "src/Main.re"),
        )
    }

    @Test
    fun `whitespace-only binary path also falls back to npx rescript`() {
        assertEquals(
            listOf("npx", "rescript", "convert", "src/Main.re"),
            RescriptMigrationConverter.buildCommand("   ", "src/Main.re"),
        )
    }

    @Test
    fun `explicit binary path is used directly`() {
        assertEquals(
            listOf("/usr/local/bin/rescript", "convert", "src/Main.re"),
            RescriptMigrationConverter.buildCommand("/usr/local/bin/rescript", "src/Main.re"),
        )
    }

    @Test
    fun `source path with spaces is preserved as a single argument`() {
        assertEquals(
            listOf("npx", "rescript", "convert", "src/My Folder/Main.re"),
            RescriptMigrationConverter.buildCommand("", "src/My Folder/Main.re"),
        )
    }

    @Test
    fun `parseRescriptMajorVersion picks up bare v12 banner`() {
        assertEquals(12, RescriptMigrationConverter.parseRescriptMajorVersion("12.2.0"))
    }

    @Test
    fun `parseRescriptMajorVersion picks up v11 prefixed banner`() {
        assertEquals(11, RescriptMigrationConverter.parseRescriptMajorVersion("rescript 11.1.4"))
    }

    @Test
    fun `parseRescriptMajorVersion handles build metadata trail`() {
        assertEquals(11, RescriptMigrationConverter.parseRescriptMajorVersion("rescript 11.1.4 (build abc)"))
    }

    @Test
    fun `parseRescriptMajorVersion returns null when no version triplet present`() {
        assertEquals(null, RescriptMigrationConverter.parseRescriptMajorVersion("unrecognised output"))
    }

    @Test
    fun `parseRescriptMajorVersion returns null on empty line`() {
        assertEquals(null, RescriptMigrationConverter.parseRescriptMajorVersion(""))
    }

    @Test
    fun `parseRescriptMajorVersion accepts two-component version`() {
        assertEquals(10, RescriptMigrationConverter.parseRescriptMajorVersion("10.1"))
    }
}
