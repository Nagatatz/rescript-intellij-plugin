package com.rescript.plugin.binding

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.lang.reflect.Field
import java.nio.file.Files

/**
 * Tests for [DtsParserProcess.extractScript] — resource extraction from the
 * plugin classpath to a temporary file, with caching behaviour.
 */
class DtsParserProcessTest {
    private lateinit var cachedField: Field

    @BeforeEach
    fun setUp() {
        // Reset cachedScriptPath to null before each test via reflection
        cachedField = DtsParserProcess::class.java.getDeclaredField("cachedScriptPath")
        cachedField.isAccessible = true
        cachedField.set(DtsParserProcess, null)
    }

    @AfterEach
    fun tearDown() {
        // Clean up: reset cache
        cachedField.set(DtsParserProcess, null)
    }

    @Test
    fun testExtractScriptReturnsTempFile() {
        val path = DtsParserProcess.extractScript()
        assertNotNull(path, "extractScript should return a non-null Path")
        assertTrue(Files.isRegularFile(path), "Extracted file should exist")
    }

    @Test
    fun testExtractScriptFileContainsContent() {
        val path = DtsParserProcess.extractScript()
        val size = Files.size(path)
        assertTrue(size > 0, "Extracted file should have non-empty content (size=$size)")
    }

    @Test
    fun testExtractScriptCachesResult() {
        val first = DtsParserProcess.extractScript()
        val second = DtsParserProcess.extractScript()
        assertEquals(first, second, "Second call should return the same cached Path")
    }
}
