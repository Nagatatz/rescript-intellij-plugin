package com.rescript.plugin.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptPathsTest {
    // ── Config file names ──────────────────────────────────────────

    @Test
    fun `RESCRIPT_JSON has correct value`() {
        assertEquals("rescript.json", RescriptPaths.RESCRIPT_JSON)
    }

    @Test
    fun `BSCONFIG_JSON has correct value`() {
        assertEquals("bsconfig.json", RescriptPaths.BSCONFIG_JSON)
    }

    @Test
    fun `CONFIG_FILE_NAMES contains both config files`() {
        assertTrue(RescriptPaths.CONFIG_FILE_NAMES.contains("rescript.json"))
        assertTrue(RescriptPaths.CONFIG_FILE_NAMES.contains("bsconfig.json"))
        assertEquals(2, RescriptPaths.CONFIG_FILE_NAMES.size)
    }

    // ── node_modules paths ─────────────────────────────────────────

    @Test
    fun `NODE_MODULES_BIN has correct value`() {
        assertEquals("node_modules/.bin", RescriptPaths.NODE_MODULES_BIN)
    }

    @Test
    fun `LSP_PACKAGE_DIR has correct value`() {
        assertEquals("node_modules/@rescript/language-server", RescriptPaths.LSP_PACKAGE_DIR)
    }

    @Test
    fun `LSP_CLI_JS has correct value`() {
        assertEquals("node_modules/@rescript/language-server/out/cli.js", RescriptPaths.LSP_CLI_JS)
    }

    @Test
    fun `LSP_CLI_JS starts with LSP_PACKAGE_DIR`() {
        assertTrue(RescriptPaths.LSP_CLI_JS.startsWith(RescriptPaths.LSP_PACKAGE_DIR))
    }

    @Test
    fun `RESCRIPT_PACKAGE_JSON has correct value`() {
        assertEquals("node_modules/rescript/package.json", RescriptPaths.RESCRIPT_PACKAGE_JSON)
    }

    @Test
    fun `RESCRIPT_TOOLS has correct value`() {
        assertEquals("node_modules/rescript/rescript-tools", RescriptPaths.RESCRIPT_TOOLS)
    }

    @Test
    fun `RESCRIPT_TOOLS_EXE has correct value`() {
        assertEquals("node_modules/rescript/rescript-tools.exe", RescriptPaths.RESCRIPT_TOOLS_EXE)
    }

    @Test
    fun `RESCRIPT_TOOLS_BIN_NAME has correct value`() {
        assertEquals("rescript-tools", RescriptPaths.RESCRIPT_TOOLS_BIN_NAME)
    }

    @Test
    fun `LSP_BIN_NAME has correct value`() {
        assertEquals("rescript-language-server", RescriptPaths.LSP_BIN_NAME)
    }

    // ── Excluded path segments ─────────────────────────────────────

    @Test
    fun `EXCLUDED_PATH_SEGMENTS contains node_modules`() {
        assertTrue(RescriptPaths.EXCLUDED_PATH_SEGMENTS.contains("/node_modules/"))
    }

    @Test
    fun `EXCLUDED_PATH_SEGMENTS contains lib_bs`() {
        assertTrue(RescriptPaths.EXCLUDED_PATH_SEGMENTS.contains("/lib/bs/"))
    }

    @Test
    fun `EXCLUDED_PATH_SEGMENTS contains lib_ocaml`() {
        assertTrue(RescriptPaths.EXCLUDED_PATH_SEGMENTS.contains("/lib/ocaml/"))
    }

    @Test
    fun `EXCLUDED_PATH_SEGMENTS has exactly 3 entries`() {
        assertEquals(3, RescriptPaths.EXCLUDED_PATH_SEGMENTS.size)
    }

    @Test
    fun `NODE_MODULES_SEGMENT has correct value`() {
        assertEquals("/node_modules/", RescriptPaths.NODE_MODULES_SEGMENT)
    }

    // ── Path consistency checks ────────────────────────────────────

    @Test
    fun `NODE_MODULES_SEGMENT is included in EXCLUDED_PATH_SEGMENTS`() {
        assertTrue(RescriptPaths.EXCLUDED_PATH_SEGMENTS.contains(RescriptPaths.NODE_MODULES_SEGMENT))
    }

    @Test
    fun `all paths are relative - no leading slash`() {
        assertFalse(RescriptPaths.NODE_MODULES_BIN.startsWith("/"))
        assertFalse(RescriptPaths.LSP_PACKAGE_DIR.startsWith("/"))
        assertFalse(RescriptPaths.LSP_CLI_JS.startsWith("/"))
        assertFalse(RescriptPaths.RESCRIPT_PACKAGE_JSON.startsWith("/"))
        assertFalse(RescriptPaths.RESCRIPT_TOOLS.startsWith("/"))
        assertFalse(RescriptPaths.RESCRIPT_TOOLS_EXE.startsWith("/"))
    }
}
