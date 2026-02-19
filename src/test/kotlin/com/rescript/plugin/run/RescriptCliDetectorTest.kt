package com.rescript.plugin.run

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class RescriptCliDetectorTest {
    @get:Rule
    val tempDir = TemporaryFolder()

    private fun createRescriptBin(baseDir: File): File {
        val binDir = File(baseDir, "node_modules/.bin")
        binDir.mkdirs()
        val rescript = File(binDir, "rescript")
        rescript.writeText("#!/bin/sh\n")
        rescript.setExecutable(true)
        return rescript
    }

    // ── findCli with workingDirectory ──

    @Test
    fun `findCli returns path when rescript exists in workingDirectory`() {
        val projectDir = tempDir.newFolder("project")
        val bin = createRescriptBin(projectDir)

        val result = RescriptCliDetector.findCli(projectDir.absolutePath, null)
        assertNotNull(result)
        assertEquals(bin.absolutePath, result)
    }

    @Test
    fun `findCli returns null when no rescript in workingDirectory`() {
        val projectDir = tempDir.newFolder("empty-project")

        val result = RescriptCliDetector.findCli(projectDir.absolutePath, null)
        assertNull(result)
    }

    // ── findCli with projectBasePath ──

    @Test
    fun `findCli returns path from projectBasePath when workingDirectory is null`() {
        val projectDir = tempDir.newFolder("project2")
        val bin = createRescriptBin(projectDir)

        val result = RescriptCliDetector.findCli(null, projectDir.absolutePath)
        assertNotNull(result)
        assertEquals(bin.absolutePath, result)
    }

    // ── findCli prefers workingDirectory ──

    @Test
    fun `findCli prefers workingDirectory over projectBasePath`() {
        val workDir = tempDir.newFolder("work")
        val projDir = tempDir.newFolder("proj")
        val workBin = createRescriptBin(workDir)
        createRescriptBin(projDir)

        val result = RescriptCliDetector.findCli(workDir.absolutePath, projDir.absolutePath)
        assertEquals(workBin.absolutePath, result)
    }

    // ── findCli with parent directory search ──

    @Test
    fun `findCli searches parent directories`() {
        val parentDir = tempDir.newFolder("parent")
        val childDir = File(parentDir, "child/grandchild")
        childDir.mkdirs()
        val bin = createRescriptBin(parentDir)

        val result = RescriptCliDetector.findCli(null, childDir.absolutePath)
        assertNotNull(result)
        assertEquals(bin.absolutePath, result)
    }

    // ── findCli with null arguments ──

    @Test
    fun `findCli returns null when both arguments are null`() {
        val result = RescriptCliDetector.findCli(null, null)
        assertNull(result)
    }

    // ── findCli non-executable file ──

    @Test
    fun `findCli returns null when rescript file is not executable`() {
        val projectDir = tempDir.newFolder("project-noexec")
        val binDir = File(projectDir, "node_modules/.bin")
        binDir.mkdirs()
        val rescript = File(binDir, "rescript")
        rescript.writeText("#!/bin/sh\n")
        rescript.setExecutable(false)

        val result = RescriptCliDetector.findCli(projectDir.absolutePath, null)
        assertNull(result)
    }
}
