package com.rescript.plugin.binding

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path

class DtsNodeDetectorTest {
    private lateinit var tempDir: Path

    @Before
    fun setUp() {
        tempDir = Files.createTempDirectory("dts-node-detector-test")
    }

    @After
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
