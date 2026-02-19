package com.rescript.plugin.run

import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path

class RescriptCliDetectorTest {
    private lateinit var tempDir: Path

    @Before
    fun setUp() {
        tempDir = Files.createTempDirectory("rescript-cli-test")
    }

    @After
    fun tearDown() {
        tempDir.toFile().deleteRecursively()
    }

    private fun createCli(baseDir: Path) {
        val binDir = baseDir.resolve("node_modules/.bin")
        Files.createDirectories(binDir)
        val cli = binDir.resolve("rescript")
        Files.createFile(cli)
        cli.toFile().setExecutable(true)
    }

    @Test
    fun `findCli returns path when CLI exists in workingDirectory`() {
        createCli(tempDir)
        val result = RescriptCliDetector.findCli(tempDir.toString(), null)
        assertNotNull(result)
        assertTrue(result!!.contains("node_modules/.bin/rescript"))
    }

    @Test
    fun `findCli returns path when CLI exists in projectBasePath`() {
        createCli(tempDir)
        val result = RescriptCliDetector.findCli(null, tempDir.toString())
        assertNotNull(result)
        assertTrue(result!!.contains("node_modules/.bin/rescript"))
    }

    @Test
    fun `findCli prefers workingDirectory over projectBasePath`() {
        val workDir = tempDir.resolve("work")
        val projDir = tempDir.resolve("proj")
        Files.createDirectories(workDir)
        Files.createDirectories(projDir)
        createCli(workDir)
        createCli(projDir)

        val result = RescriptCliDetector.findCli(workDir.toString(), projDir.toString())
        assertNotNull(result)
        assertTrue(result!!.contains("work"))
    }

    @Test
    fun `findCli searches parent directories`() {
        createCli(tempDir)
        val subDir = tempDir.resolve("sub/dir")
        Files.createDirectories(subDir)

        val result = RescriptCliDetector.findCli(null, subDir.toString())
        assertNotNull(result)
    }

    @Test
    fun `findCli returns null when CLI not found`() {
        val result = RescriptCliDetector.findCli(tempDir.toString(), tempDir.toString())
        assertNull(result)
    }

    @Test
    fun `findCli returns null for null arguments`() {
        val result = RescriptCliDetector.findCli(null, null)
        assertNull(result)
    }
}
