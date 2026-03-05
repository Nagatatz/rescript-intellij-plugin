package com.rescript.plugin.binding

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

class DtsNodeDetectorTest {
    private lateinit var tempDir: Path

    @BeforeEach
    fun setUp() {
        tempDir = Files.createTempDirectory("dts-node-detector-test")
    }

    @AfterEach
    fun tearDown() {
        tempDir.toFile().deleteRecursively()
    }

    // ── findTypeScriptPath ────────────────────────────────────────────

    @Test
    fun `findTypeScriptPath returns path when typescript exists`() {
        val tsDir = tempDir.resolve("node_modules/typescript")
        Files.createDirectories(tsDir)

        val result = DtsNodeDetector.findTypeScriptPath(tempDir.toString())
        assertNotNull(result)
        assertEquals(tsDir.toString(), result)
    }

    @Test
    fun `findTypeScriptPath returns null when typescript not found`() {
        assertNull(DtsNodeDetector.findTypeScriptPath(tempDir.toString()))
    }

    @Test
    fun `findTypeScriptPath returns null for null path`() {
        assertNull(DtsNodeDetector.findTypeScriptPath(null))
    }

    @Test
    fun `findTypeScriptPath searches parent directories`() {
        val tsDir = tempDir.resolve("node_modules/typescript")
        Files.createDirectories(tsDir)
        val subDir = tempDir.resolve("packages/my-app")
        Files.createDirectories(subDir)

        val result = DtsNodeDetector.findTypeScriptPath(subDir.toString())
        assertNotNull(result)
        assertEquals(tsDir.toString(), result)
    }

    @Test
    fun `findTypeScriptPath returns null when typescript is a file`() {
        val tsPath = tempDir.resolve("node_modules/typescript")
        Files.createDirectories(tsPath.parent)
        Files.createFile(tsPath)

        assertNull(DtsNodeDetector.findTypeScriptPath(tempDir.toString()))
    }

    // ── resolveNodePath ───────────────────────────────────────────────

    @Test
    fun `resolveNodePath returns default when settings is null`() {
        assertEquals("node", DtsNodeDetector.resolveNodePath(null))
    }

    // ── isTypeScriptAvailable ─────────────────────────────────────────

    @Test
    fun `isTypeScriptAvailable returns true with package json`() {
        val tsDir = tempDir.resolve("node_modules/typescript")
        Files.createDirectories(tsDir)
        Files.createFile(tsDir.resolve("package.json"))

        assertTrue(DtsNodeDetector.isTypeScriptAvailable(tsDir.toString()))
    }

    @Test
    fun `isTypeScriptAvailable returns false without package json`() {
        val tsDir = tempDir.resolve("node_modules/typescript")
        Files.createDirectories(tsDir)

        assertFalse(DtsNodeDetector.isTypeScriptAvailable(tsDir.toString()))
    }

    @Test
    fun `isTypeScriptAvailable returns false for null path`() {
        assertFalse(DtsNodeDetector.isTypeScriptAvailable(null))
    }

    @Test
    fun `isTypeScriptAvailable returns false for non-existent path`() {
        assertFalse(DtsNodeDetector.isTypeScriptAvailable("/nonexistent/path"))
    }

    // ── isNodeAvailable ───────────────────────────────────────────────

    @Test
    fun `isNodeAvailable returns false for nonexistent path`() {
        assertFalse(DtsNodeDetector.isNodeAvailable("/nonexistent/node-binary"))
    }
}
