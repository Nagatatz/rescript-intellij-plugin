package com.rescript.plugin.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledOnOs
import org.junit.jupiter.api.condition.OS
import org.junit.jupiter.api.io.TempDir
import java.io.File

class RescriptSecurityUtilsTest {
    @TempDir
    lateinit var tempDir: File

    // ── isValidExecutable ─────────────────────────────────────────

    @Test
    fun `isValidExecutable returns false for empty string`() {
        assertFalse(RescriptSecurityUtils.isValidExecutable(""))
    }

    @Test
    fun `isValidExecutable returns false for blank string`() {
        assertFalse(RescriptSecurityUtils.isValidExecutable("   "))
    }

    @Test
    fun `isValidExecutable returns false for nonexistent path`() {
        assertFalse(RescriptSecurityUtils.isValidExecutable("/nonexistent/path/to/binary"))
    }

    @Test
    fun `isValidExecutable returns false for directory`() {
        val dir = File(tempDir, "mydir").apply { mkdir() }
        assertFalse(RescriptSecurityUtils.isValidExecutable(dir.absolutePath))
    }

    @Test
    @DisabledOnOs(
        value = [OS.WINDOWS],
        disabledReason = "Windows has no execute bit; File.canExecute() is always true for a readable file.",
    )
    fun `isValidExecutable returns false for non-executable file`() {
        val file = File(tempDir, "script.sh").apply { createNewFile() }
        file.setExecutable(false)
        assertFalse(RescriptSecurityUtils.isValidExecutable(file.absolutePath))
    }

    @Test
    fun `isValidExecutable returns true for executable file`() {
        val file = File(tempDir, "script.sh").apply { createNewFile() }
        file.setExecutable(true)
        assertTrue(RescriptSecurityUtils.isValidExecutable(file.absolutePath))
    }

    // ── isValidPackageName ────────────────────────────────────────

    @Test
    fun `isValidPackageName returns false for empty string`() {
        assertFalse(RescriptSecurityUtils.isValidPackageName(""))
    }

    @Test
    fun `isValidPackageName returns false for blank string`() {
        assertFalse(RescriptSecurityUtils.isValidPackageName("   "))
    }

    @Test
    fun `isValidPackageName accepts simple package name`() {
        assertTrue(RescriptSecurityUtils.isValidPackageName("rescript"))
    }

    @Test
    fun `isValidPackageName accepts scoped package name`() {
        assertTrue(RescriptSecurityUtils.isValidPackageName("@rescript/core"))
    }

    @Test
    fun `isValidPackageName accepts package with dots and hyphens`() {
        assertTrue(RescriptSecurityUtils.isValidPackageName("my-package.js"))
    }

    @Test
    fun `isValidPackageName accepts package with underscore`() {
        assertTrue(RescriptSecurityUtils.isValidPackageName("my_package"))
    }

    @Test
    fun `isValidPackageName rejects path traversal`() {
        assertFalse(RescriptSecurityUtils.isValidPackageName("../../../etc/passwd"))
    }

    @Test
    fun `isValidPackageName rejects shell metacharacters`() {
        assertFalse(RescriptSecurityUtils.isValidPackageName("pkg; rm -rf /"))
    }

    @Test
    fun `isValidPackageName rejects backticks`() {
        assertFalse(RescriptSecurityUtils.isValidPackageName("pkg`id`"))
    }

    @Test
    fun `isValidPackageName rejects dollar sign`() {
        assertFalse(RescriptSecurityUtils.isValidPackageName("pkg\$HOME"))
    }

    // ── escapeHtml ────────────────────────────────────────────────

    @Test
    fun `escapeHtml escapes angle brackets`() {
        assertEquals("&lt;script&gt;", RescriptSecurityUtils.escapeHtml("<script>"))
    }

    @Test
    fun `escapeHtml escapes ampersand`() {
        assertEquals("a &amp; b", RescriptSecurityUtils.escapeHtml("a & b"))
    }

    @Test
    fun `escapeHtml escapes double quotes`() {
        assertEquals("&quot;value&quot;", RescriptSecurityUtils.escapeHtml("\"value\""))
    }

    @Test
    fun `escapeHtml preserves safe text`() {
        assertEquals("hello world", RescriptSecurityUtils.escapeHtml("hello world"))
    }

    @Test
    fun `escapeHtml handles empty string`() {
        assertEquals("", RescriptSecurityUtils.escapeHtml(""))
    }

    // ── Constants ─────────────────────────────────────────────────

    @Test
    fun `MAX_PARENT_TRAVERSAL_DEPTH is 10`() {
        assertEquals(10, RescriptSecurityUtils.MAX_PARENT_TRAVERSAL_DEPTH)
    }

    @Test
    fun `PROCESS_TIMEOUT_SECONDS is 30`() {
        assertEquals(30L, RescriptSecurityUtils.PROCESS_TIMEOUT_SECONDS)
    }
}
