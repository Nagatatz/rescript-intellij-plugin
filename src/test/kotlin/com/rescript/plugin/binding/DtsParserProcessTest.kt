package com.rescript.plugin.binding

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Field
import java.nio.file.Files

/**
 * Tests for [DtsParserProcess.extractScript] — resource extraction from the
 * plugin classpath to a temporary file, with caching behaviour.
 */
class DtsParserProcessTest {
    private lateinit var cachedField: Field

    @Before
    fun setUp() {
        // Reset cachedScriptPath to null before each test via reflection
        cachedField = DtsParserProcess::class.java.getDeclaredField("cachedScriptPath")
        cachedField.isAccessible = true
        cachedField.set(DtsParserProcess, null)
    }

    @After
    fun tearDown() {
        // Clean up: reset cache
        cachedField.set(DtsParserProcess, null)
    }

    @Test
    fun testExtractScriptReturnsTempFile() {
        val path = DtsParserProcess.extractScript()
        assertNotNull("extractScript should return a non-null Path", path)
        assertTrue("Extracted file should exist", Files.isRegularFile(path))
    }

    @Test
    fun testExtractScriptFileContainsContent() {
        val path = DtsParserProcess.extractScript()
        val size = Files.size(path)
        assertTrue("Extracted file should have non-empty content (size=$size)", size > 0)
    }

    @Test
    fun testExtractScriptCachesResult() {
        val first = DtsParserProcess.extractScript()
        val second = DtsParserProcess.extractScript()
        assertEquals("Second call should return the same cached Path", first, second)
    }
}
