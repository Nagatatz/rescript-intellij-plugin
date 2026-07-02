package com.rescript.plugin.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Verifies that [RescriptMessageSanitizer] strips absolute filesystem paths from
 * user-facing messages using the pure internal overload (no IntelliJ fixture required).
 */
class RescriptMessageSanitizerTest {
    @Test
    fun `home directory prefix is replaced with tilde`() {
        val result =
            RescriptMessageSanitizer.sanitize(
                "config at /Users/alice/.config/rescript is missing",
                homeDir = "/Users/alice",
                projectBasePath = null,
            )
        assertEquals("config at ~/.config/rescript is missing", result)
    }

    @Test
    fun `project base path is stripped to project marker`() {
        val result =
            RescriptMessageSanitizer.sanitize(
                "cannot write /Users/alice/acme/src/Main.res",
                homeDir = "/Users/alice",
                projectBasePath = "/Users/alice/acme",
            )
        assertEquals("cannot write <project>/src/Main.res", result)
    }

    @Test
    fun `message without paths is unchanged`() {
        val message = "TypeScript not found in node_modules. Install it via: npm install typescript"
        val result =
            RescriptMessageSanitizer.sanitize(
                message,
                homeDir = "/Users/alice",
                projectBasePath = "/Users/alice/acme",
            )
        assertEquals(message, result)
    }

    @Test
    fun `multi-line npm stderr has absolute path sanitized on the path line`() {
        val stderr =
            buildString {
                append("npm ERR! code EACCES\n")
                append("npm ERR! path /Users/alice/acme/node_modules/.staging\n")
                append("npm ERR! errno -13")
            }
        val result =
            RescriptMessageSanitizer.sanitize(
                stderr,
                homeDir = "/Users/alice",
                projectBasePath = null,
            )
        val lines = result.lines()
        // The code and errno lines are untouched.
        assertEquals("npm ERR! code EACCES", lines[0])
        assertEquals("npm ERR! errno -13", lines[2])
        // The path line has the home directory masked.
        assertEquals("npm ERR! path ~/acme/node_modules/.staging", lines[1])
        assertFalse(result.contains("/Users/alice"), "home directory must not leak")
    }

    @Test
    fun `absolute path fallback collapses paths not matching home or base`() {
        val result =
            RescriptMessageSanitizer.sanitize(
                "npm ERR! path /Users/bob/other/node_modules/.staging",
                homeDir = "/Users/alice",
                projectBasePath = "/Users/alice/acme",
            )
        // Neither the home nor the base prefix matches, so the fallback collapses to basename.
        assertEquals("npm ERR! path .staging", result)
        assertFalse(result.contains("/Users/bob"), "foreign home directory must not leak")
    }

    @Test
    fun `null home and base path are handled without error`() {
        val result =
            RescriptMessageSanitizer.sanitize(
                "plain message with no absolute paths",
                homeDir = null,
                projectBasePath = null,
            )
        assertEquals("plain message with no absolute paths", result)
    }

    @Test
    fun `null home and base path still apply absolute path fallback`() {
        val result =
            RescriptMessageSanitizer.sanitize(
                "failed at /var/folders/xy/tmpfile.log",
                homeDir = null,
                projectBasePath = null,
            )
        assertEquals("failed at tmpfile.log", result)
        assertTrue(result.contains("tmpfile.log"))
    }
}
