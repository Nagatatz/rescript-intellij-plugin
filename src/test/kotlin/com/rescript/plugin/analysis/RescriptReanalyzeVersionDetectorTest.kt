package com.rescript.plugin.analysis

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class RescriptReanalyzeVersionDetectorTest {
    @get:Rule
    val tempFolder = TemporaryFolder()

    // --- parseSemver tests ---

    @Test
    fun `parseSemver parses standard version`() {
        val result = RescriptReanalyzeVersionDetector.parseSemver("12.1.0")
        assertNotNull(result)
        assertEquals(Triple(12, 1, 0), result)
    }

    @Test
    fun `parseSemver parses version with pre-release suffix`() {
        val result = RescriptReanalyzeVersionDetector.parseSemver("12.1.0-alpha.1")
        assertNotNull(result)
        assertEquals(Triple(12, 1, 0), result)
    }

    @Test
    fun `parseSemver parses version with rc suffix`() {
        val result = RescriptReanalyzeVersionDetector.parseSemver("13.0.0-rc.2")
        assertNotNull(result)
        assertEquals(Triple(13, 0, 0), result)
    }

    @Test
    fun `parseSemver parses version with beta suffix`() {
        val result = RescriptReanalyzeVersionDetector.parseSemver("12.2.0-beta.3")
        assertNotNull(result)
        assertEquals(Triple(12, 2, 0), result)
    }

    @Test
    fun `parseSemver returns null for empty string`() {
        assertNull(RescriptReanalyzeVersionDetector.parseSemver(""))
    }

    @Test
    fun `parseSemver returns null for invalid version`() {
        assertNull(RescriptReanalyzeVersionDetector.parseSemver("not-a-version"))
    }

    @Test
    fun `parseSemver returns null for partial version`() {
        assertNull(RescriptReanalyzeVersionDetector.parseSemver("12.1"))
    }

    @Test
    fun `parseSemver handles leading whitespace`() {
        val result = RescriptReanalyzeVersionDetector.parseSemver("  12.1.0  ")
        assertNotNull(result)
        assertEquals(Triple(12, 1, 0), result)
    }

    @Test
    fun `parseSemver handles zero version`() {
        val result = RescriptReanalyzeVersionDetector.parseSemver("0.0.0")
        assertNotNull(result)
        assertEquals(Triple(0, 0, 0), result)
    }

    @Test
    fun `parseSemver handles large version numbers`() {
        val result = RescriptReanalyzeVersionDetector.parseSemver("100.200.300")
        assertNotNull(result)
        assertEquals(Triple(100, 200, 300), result)
    }

    // --- isVersionAtLeast tests ---

    @Test
    fun `isVersionAtLeast returns true for exact match`() {
        assertTrue(
            RescriptReanalyzeVersionDetector.isVersionAtLeast(
                Triple(12, 1, 0),
                Triple(12, 1, 0),
            ),
        )
    }

    @Test
    fun `isVersionAtLeast returns true for higher major`() {
        assertTrue(
            RescriptReanalyzeVersionDetector.isVersionAtLeast(
                Triple(13, 0, 0),
                Triple(12, 1, 0),
            ),
        )
    }

    @Test
    fun `isVersionAtLeast returns true for higher minor`() {
        assertTrue(
            RescriptReanalyzeVersionDetector.isVersionAtLeast(
                Triple(12, 2, 0),
                Triple(12, 1, 0),
            ),
        )
    }

    @Test
    fun `isVersionAtLeast returns true for higher patch`() {
        assertTrue(
            RescriptReanalyzeVersionDetector.isVersionAtLeast(
                Triple(12, 1, 1),
                Triple(12, 1, 0),
            ),
        )
    }

    @Test
    fun `isVersionAtLeast returns false for lower major`() {
        assertFalse(
            RescriptReanalyzeVersionDetector.isVersionAtLeast(
                Triple(11, 9, 9),
                Triple(12, 1, 0),
            ),
        )
    }

    @Test
    fun `isVersionAtLeast returns false for lower minor`() {
        assertFalse(
            RescriptReanalyzeVersionDetector.isVersionAtLeast(
                Triple(12, 0, 9),
                Triple(12, 1, 0),
            ),
        )
    }

    @Test
    fun `isVersionAtLeast returns false for lower patch`() {
        assertFalse(
            RescriptReanalyzeVersionDetector.isVersionAtLeast(
                Triple(12, 0, 99),
                Triple(12, 1, 0),
            ),
        )
    }

    // --- parseVersionFromPackageJson tests ---

    @Test
    fun `parseVersionFromPackageJson reads version field`() {
        val pkgDir = tempFolder.newFolder("pkg")
        val pkgJson = java.io.File(pkgDir, "package.json")
        pkgJson.writeText("""{"name": "rescript", "version": "12.1.0"}""")

        val result = RescriptReanalyzeVersionDetector.parseVersionFromPackageJson(pkgJson.toPath())
        assertEquals("12.1.0", result)
    }

    @Test
    fun `parseVersionFromPackageJson handles pre-release version`() {
        val pkgDir = tempFolder.newFolder("pkg2")
        val pkgJson = java.io.File(pkgDir, "package.json")
        pkgJson.writeText("""{"name": "rescript", "version": "12.1.0-alpha.1"}""")

        val result = RescriptReanalyzeVersionDetector.parseVersionFromPackageJson(pkgJson.toPath())
        assertEquals("12.1.0-alpha.1", result)
    }

    @Test
    fun `parseVersionFromPackageJson returns null for missing version field`() {
        val pkgDir = tempFolder.newFolder("pkg3")
        val pkgJson = java.io.File(pkgDir, "package.json")
        pkgJson.writeText("""{"name": "rescript"}""")

        val result = RescriptReanalyzeVersionDetector.parseVersionFromPackageJson(pkgJson.toPath())
        assertNull(result)
    }

    @Test
    fun `parseVersionFromPackageJson returns null for malformed JSON`() {
        val pkgDir = tempFolder.newFolder("pkg4")
        val pkgJson = java.io.File(pkgDir, "package.json")
        pkgJson.writeText("not valid json {{{")

        val result = RescriptReanalyzeVersionDetector.parseVersionFromPackageJson(pkgJson.toPath())
        assertNull(result)
    }

    @Test
    fun `parseVersionFromPackageJson returns null for JSON array instead of object`() {
        val pkgDir = tempFolder.newFolder("pkg-array")
        val pkgJson = java.io.File(pkgDir, "package.json")
        pkgJson.writeText("""[1, 2, 3]""")

        val result = RescriptReanalyzeVersionDetector.parseVersionFromPackageJson(pkgJson.toPath())
        assertNull(result)
    }

    @Test
    fun `parseVersionFromPackageJson returns null for nonexistent file`() {
        val nonexistent =
            java.nio.file.Path
                .of("/nonexistent/package.json")
        val result = RescriptReanalyzeVersionDetector.parseVersionFromPackageJson(nonexistent)
        assertNull(result)
    }

    // --- readRescriptVersion tests ---

    @Test
    fun `readRescriptVersion finds version in project root`() {
        val root = tempFolder.newFolder("project")
        val rescriptDir = java.io.File(root, "node_modules/rescript")
        rescriptDir.mkdirs()
        java.io.File(rescriptDir, "package.json").writeText(
            """{"name": "rescript", "version": "12.1.0"}""",
        )

        val result = RescriptReanalyzeVersionDetector.readRescriptVersion(root.absolutePath)
        assertEquals("12.1.0", result)
    }

    @Test
    fun `readRescriptVersion returns null when rescript not installed`() {
        val root = tempFolder.newFolder("empty-project")
        val result = RescriptReanalyzeVersionDetector.readRescriptVersion(root.absolutePath)
        assertNull(result)
    }

    // --- isServerModeSupported tests ---

    @Test
    fun `isServerModeSupported returns true for 12_1_0`() {
        val root = tempFolder.newFolder("project-supported")
        val rescriptDir = java.io.File(root, "node_modules/rescript")
        rescriptDir.mkdirs()
        java.io.File(rescriptDir, "package.json").writeText(
            """{"name": "rescript", "version": "12.1.0"}""",
        )

        assertTrue(RescriptReanalyzeVersionDetector.isServerModeSupported(root.absolutePath))
    }

    @Test
    fun `isServerModeSupported returns true for 13_0_0`() {
        val root = tempFolder.newFolder("project-v13")
        val rescriptDir = java.io.File(root, "node_modules/rescript")
        rescriptDir.mkdirs()
        java.io.File(rescriptDir, "package.json").writeText(
            """{"name": "rescript", "version": "13.0.0"}""",
        )

        assertTrue(RescriptReanalyzeVersionDetector.isServerModeSupported(root.absolutePath))
    }

    @Test
    fun `isServerModeSupported returns false for 11_0_0`() {
        val root = tempFolder.newFolder("project-old")
        val rescriptDir = java.io.File(root, "node_modules/rescript")
        rescriptDir.mkdirs()
        java.io.File(rescriptDir, "package.json").writeText(
            """{"name": "rescript", "version": "11.0.0"}""",
        )

        assertFalse(RescriptReanalyzeVersionDetector.isServerModeSupported(root.absolutePath))
    }

    @Test
    fun `isServerModeSupported returns false for 12_0_99`() {
        val root = tempFolder.newFolder("project-almost")
        val rescriptDir = java.io.File(root, "node_modules/rescript")
        rescriptDir.mkdirs()
        java.io.File(rescriptDir, "package.json").writeText(
            """{"name": "rescript", "version": "12.0.99"}""",
        )

        assertFalse(RescriptReanalyzeVersionDetector.isServerModeSupported(root.absolutePath))
    }

    @Test
    fun `isServerModeSupported returns true for 12_1_0-alpha_1`() {
        val root = tempFolder.newFolder("project-alpha")
        val rescriptDir = java.io.File(root, "node_modules/rescript")
        rescriptDir.mkdirs()
        java.io.File(rescriptDir, "package.json").writeText(
            """{"name": "rescript", "version": "12.1.0-alpha.1"}""",
        )

        assertTrue(RescriptReanalyzeVersionDetector.isServerModeSupported(root.absolutePath))
    }

    @Test
    fun `isServerModeSupported returns false when rescript not installed`() {
        val root = tempFolder.newFolder("project-no-rescript")
        assertFalse(RescriptReanalyzeVersionDetector.isServerModeSupported(root.absolutePath))
    }

    // --- MIN_SERVER_VERSION constant ---

    @Test
    fun `MIN_SERVER_VERSION is 12_1_0`() {
        assertEquals(Triple(12, 1, 0), RescriptReanalyzeVersionDetector.MIN_SERVER_VERSION)
    }
}
