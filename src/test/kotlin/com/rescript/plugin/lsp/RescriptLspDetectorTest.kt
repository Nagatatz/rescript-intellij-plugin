package com.rescript.plugin.lsp

import com.intellij.openapi.project.Project
import com.rescript.plugin.settings.RescriptProjectSettings
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.nio.file.Files
import java.nio.file.Path

/**
 * Unit tests for [RescriptLspDetector], covering LSP availability checks,
 * ReScript project detection, and custom LSP configuration detection.
 *
 * @see RescriptLspDetector
 */
class RescriptLspDetectorTest {
    private lateinit var tempDir: Path

    @BeforeEach
    fun setUp() {
        tempDir = Files.createTempDirectory("rescript-lsp-detector-test")
    }

    @AfterEach
    fun tearDown() {
        tempDir.toFile().deleteRecursively()
    }

    // ── isLspAvailable ──────────────────────────────────────────────

    @Test
    fun `isLspAvailable returns true when language-server exists in node_modules`() {
        val lspDir = tempDir.resolve("node_modules/@rescript/language-server")
        Files.createDirectories(lspDir)

        assertTrue(RescriptLspDetector.isLspAvailable(tempDir.toString()))
    }

    @Test
    fun `isLspAvailable returns false when node_modules does not exist`() {
        assertFalse(RescriptLspDetector.isLspAvailable(tempDir.toString()))
    }

    @Test
    fun `isLspAvailable returns false when language-server is not in node_modules`() {
        val nodeModules = tempDir.resolve("node_modules/@rescript")
        Files.createDirectories(nodeModules)

        assertFalse(RescriptLspDetector.isLspAvailable(tempDir.toString()))
    }

    @Test
    fun `isLspAvailable searches parent directories`() {
        val lspDir = tempDir.resolve("node_modules/@rescript/language-server")
        Files.createDirectories(lspDir)
        val subDir = tempDir.resolve("packages/my-app")
        Files.createDirectories(subDir)

        assertTrue(RescriptLspDetector.isLspAvailable(subDir.toString()))
    }

    @Test
    fun `isLspAvailable returns false for null projectBasePath`() {
        assertFalse(RescriptLspDetector.isLspAvailable(null))
    }

    @Test
    fun `isLspAvailable returns false when path is a file not a directory`() {
        val lspPath = tempDir.resolve("node_modules/@rescript/language-server")
        Files.createDirectories(lspPath.parent)
        Files.createFile(lspPath)

        assertFalse(RescriptLspDetector.isLspAvailable(tempDir.toString()))
    }

    // ── isRescriptProject ───────────────────────────────────────────

    @Test
    fun `isRescriptProject returns true when rescript json exists`() {
        Files.createFile(tempDir.resolve("rescript.json"))

        assertTrue(RescriptLspDetector.isRescriptProject(tempDir.toString()))
    }

    @Test
    fun `isRescriptProject returns true when bsconfig json exists`() {
        Files.createFile(tempDir.resolve("bsconfig.json"))

        assertTrue(RescriptLspDetector.isRescriptProject(tempDir.toString()))
    }

    @Test
    fun `isRescriptProject returns false when no config file exists`() {
        assertFalse(RescriptLspDetector.isRescriptProject(tempDir.toString()))
    }

    @Test
    fun `isRescriptProject searches parent directories`() {
        Files.createFile(tempDir.resolve("rescript.json"))
        val subDir = tempDir.resolve("packages/my-app")
        Files.createDirectories(subDir)

        assertTrue(RescriptLspDetector.isRescriptProject(subDir.toString()))
    }

    @Test
    fun `isRescriptProject returns false for null projectBasePath`() {
        assertFalse(RescriptLspDetector.isRescriptProject(null))
    }

    @Test
    fun `isRescriptProject returns false when rescript json is a directory`() {
        Files.createDirectories(tempDir.resolve("rescript.json"))

        assertFalse(RescriptLspDetector.isRescriptProject(tempDir.toString()))
    }

    // ── isLspConfigured ──────────────────────────────────────────────

    @Test
    fun `isLspConfigured returns true when lspServerPath is set`() {
        val project = mock(Project::class.java)
        val settings = RescriptProjectSettings()
        settings.lspServerPath = "/usr/local/bin/rescript-lsp"
        `when`(project.getService(RescriptProjectSettings::class.java)).thenReturn(settings)

        assertTrue(RescriptLspDetector.isLspConfigured(project))
    }

    @Test
    fun `isLspConfigured returns false when lspServerPath is empty`() {
        val project = mock(Project::class.java)
        val settings = RescriptProjectSettings()
        `when`(project.getService(RescriptProjectSettings::class.java)).thenReturn(settings)

        assertFalse(RescriptLspDetector.isLspConfigured(project))
    }

    @Test
    fun `isLspConfigured returns false with default settings`() {
        val project = mock(Project::class.java)
        val settings = RescriptProjectSettings()
        `when`(project.getService(RescriptProjectSettings::class.java)).thenReturn(settings)

        // Default lspServerPath is empty string
        assertFalse(RescriptLspDetector.isLspConfigured(project))
    }
}
