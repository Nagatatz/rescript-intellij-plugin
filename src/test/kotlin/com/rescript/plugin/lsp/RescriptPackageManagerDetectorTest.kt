package com.rescript.plugin.lsp

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path

class RescriptPackageManagerDetectorTest {
    private lateinit var tempDir: Path

    @Before
    fun setUp() {
        tempDir = Files.createTempDirectory("rescript-pm-detector-test")
    }

    @After
    fun tearDown() {
        tempDir.toFile().deleteRecursively()
    }

    // ── Lock file detection ─────────────────────────────────────────

    @Test
    fun `detect returns NPM when package-lock json exists`() {
        Files.createFile(tempDir.resolve("package-lock.json"))

        val result = RescriptPackageManagerDetector.detect(tempDir.toString())
        assertEquals(PackageManager.NPM, result.packageManager)
        assertEquals(tempDir.toString(), result.workingDirectory)
    }

    @Test
    fun `detect returns YARN when yarn lock exists`() {
        Files.createFile(tempDir.resolve("yarn.lock"))

        val result = RescriptPackageManagerDetector.detect(tempDir.toString())
        assertEquals(PackageManager.YARN, result.packageManager)
        assertEquals(tempDir.toString(), result.workingDirectory)
    }

    @Test
    fun `detect returns PNPM when pnpm-lock yaml exists`() {
        Files.createFile(tempDir.resolve("pnpm-lock.yaml"))

        val result = RescriptPackageManagerDetector.detect(tempDir.toString())
        assertEquals(PackageManager.PNPM, result.packageManager)
        assertEquals(tempDir.toString(), result.workingDirectory)
    }

    // ── Priority ────────────────────────────────────────────────────

    @Test
    fun `detect prefers PNPM over YARN when both lock files exist`() {
        Files.createFile(tempDir.resolve("pnpm-lock.yaml"))
        Files.createFile(tempDir.resolve("yarn.lock"))

        val result = RescriptPackageManagerDetector.detect(tempDir.toString())
        assertEquals(PackageManager.PNPM, result.packageManager)
    }

    @Test
    fun `detect prefers YARN over NPM when both lock files exist`() {
        Files.createFile(tempDir.resolve("yarn.lock"))
        Files.createFile(tempDir.resolve("package-lock.json"))

        val result = RescriptPackageManagerDetector.detect(tempDir.toString())
        assertEquals(PackageManager.YARN, result.packageManager)
    }

    @Test
    fun `detect prefers PNPM over NPM when both lock files exist`() {
        Files.createFile(tempDir.resolve("pnpm-lock.yaml"))
        Files.createFile(tempDir.resolve("package-lock.json"))

        val result = RescriptPackageManagerDetector.detect(tempDir.toString())
        assertEquals(PackageManager.PNPM, result.packageManager)
    }

    // ── Default behavior ────────────────────────────────────────────

    @Test
    fun `detect defaults to NPM when no lock file exists`() {
        val result = RescriptPackageManagerDetector.detect(tempDir.toString())
        assertEquals(PackageManager.NPM, result.packageManager)
        assertEquals(tempDir.toString(), result.workingDirectory)
    }

    @Test
    fun `detect defaults to NPM with dot directory for null path`() {
        val result = RescriptPackageManagerDetector.detect(null)
        assertEquals(PackageManager.NPM, result.packageManager)
        assertEquals(".", result.workingDirectory)
    }

    // ── Parent directory search (monorepo) ──────────────────────────

    @Test
    fun `detect finds lock file in parent directory`() {
        Files.createFile(tempDir.resolve("pnpm-lock.yaml"))
        val subDir = tempDir.resolve("packages/my-app")
        Files.createDirectories(subDir)

        val result = RescriptPackageManagerDetector.detect(subDir.toString())
        assertEquals(PackageManager.PNPM, result.packageManager)
        assertEquals(tempDir.toString(), result.workingDirectory)
    }

    @Test
    fun `detect prefers project root lock file over parent`() {
        // Parent has pnpm, project root has yarn
        Files.createFile(tempDir.resolve("pnpm-lock.yaml"))
        val subDir = tempDir.resolve("packages/my-app")
        Files.createDirectories(subDir)
        Files.createFile(subDir.resolve("yarn.lock"))

        val result = RescriptPackageManagerDetector.detect(subDir.toString())
        assertEquals(PackageManager.YARN, result.packageManager)
        assertEquals(subDir.toString(), result.workingDirectory)
    }

    // ── PackageManager enum properties ──────────────────────────────

    @Test
    fun `NPM has correct install command`() {
        assertEquals(listOf("npm", "install", "--save-dev"), PackageManager.NPM.installCommand)
    }

    @Test
    fun `YARN has correct install command`() {
        assertEquals(listOf("yarn", "add", "--dev"), PackageManager.YARN.installCommand)
    }

    @Test
    fun `PNPM has correct install command`() {
        assertEquals(listOf("pnpm", "add", "--save-dev"), PackageManager.PNPM.installCommand)
    }

    @Test
    fun `PackageManager display names are correct`() {
        assertEquals("npm", PackageManager.NPM.displayName)
        assertEquals("Yarn", PackageManager.YARN.displayName)
        assertEquals("pnpm", PackageManager.PNPM.displayName)
    }

    @Test
    fun `PackageManager lock file names are correct`() {
        assertEquals("package-lock.json", PackageManager.NPM.lockFileName)
        assertEquals("yarn.lock", PackageManager.YARN.lockFileName)
        assertEquals("pnpm-lock.yaml", PackageManager.PNPM.lockFileName)
    }
}
