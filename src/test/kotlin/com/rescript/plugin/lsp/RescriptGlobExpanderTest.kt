package com.rescript.plugin.lsp

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

/**
 * Unit tests for [RescriptGlobExpander] covering single-segment, `*`, `**`, and
 * excluded-directory behaviour.
 */
class RescriptGlobExpanderTest {
    private lateinit var tempDir: Path

    @BeforeEach
    fun setUp() {
        tempDir = Files.createTempDirectory("rescript-glob-expander-test")
    }

    @AfterEach
    fun tearDown() {
        tempDir.toFile().deleteRecursively()
    }

    @Test
    fun `expand returns matching directories for star segment`() {
        val core = tempDir.resolve("packages/core").also { Files.createDirectories(it) }
        val server = tempDir.resolve("packages/server").also { Files.createDirectories(it) }
        Files.createDirectories(tempDir.resolve("apps/web"))

        val result = RescriptGlobExpander.expand(tempDir, "packages/*")

        assertEquals(2, result.size)
        assertTrue(result.contains(core.normalize()))
        assertTrue(result.contains(server.normalize()))
    }

    @Test
    fun `expand resolves plain segment patterns`() {
        val target = tempDir.resolve("packages/core").also { Files.createDirectories(it) }

        val result = RescriptGlobExpander.expand(tempDir, "packages/core")

        assertEquals(listOf(target.normalize()), result)
    }

    @Test
    fun `expand skips excluded directories under star segment`() {
        Files.createDirectories(tempDir.resolve("packages/core"))
        Files.createDirectories(tempDir.resolve("packages/node_modules/foo"))

        val result = RescriptGlobExpander.expand(tempDir, "packages/*")

        assertEquals(1, result.size)
        assertTrue(result[0].endsWith("core"))
    }

    @Test
    fun `expand handles double star at any depth`() {
        Files.createDirectories(tempDir.resolve("a/b/c"))
        Files.createDirectories(tempDir.resolve("a/d"))
        Files.createDirectories(tempDir.resolve("e"))

        val result = RescriptGlobExpander.expand(tempDir, "**")

        assertTrue(result.size >= 4) // a, a/b, a/b/c, a/d, e
    }

    @Test
    fun `expand returns empty list for negated patterns`() {
        Files.createDirectories(tempDir.resolve("packages/core"))

        val result = RescriptGlobExpander.expand(tempDir, "!packages/core")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `expand returns empty list for blank pattern`() {
        val result = RescriptGlobExpander.expand(tempDir, "   ")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `expand handles trailing slash`() {
        val target = tempDir.resolve("packages/core").also { Files.createDirectories(it) }

        val result = RescriptGlobExpander.expand(tempDir, "packages/core/")

        assertEquals(listOf(target.normalize()), result)
    }

    @Test
    fun `expand normalises backslashes to forward slashes`() {
        val target = tempDir.resolve("packages/core").also { Files.createDirectories(it) }

        val result = RescriptGlobExpander.expand(tempDir, "packages\\core")

        assertEquals(listOf(target.normalize()), result)
    }

    @Test
    fun `expand returns empty list when intermediate path is missing`() {
        val result = RescriptGlobExpander.expand(tempDir, "missing/*")
        assertTrue(result.isEmpty())
    }
}
