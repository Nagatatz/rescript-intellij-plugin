package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.ProjectTemplate
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TemplateDependencyVersionsTest {
    /**
     * Extracts a semver version string for a given package from a package.json string.
     *
     * @param packageJson the raw package.json content
     * @param packageName the npm package name to look up
     * @return the version range string (e.g., "^19.0.4"), or null if not found
     */
    private fun extractVersion(
        packageJson: String,
        packageName: String,
    ): String? {
        // Match "packageName": "^x.y.z" or "~x.y.z" — only semver ranges, not script values
        val regex = Regex(""""${Regex.escape(packageName)}"\s*:\s*"([~^]?\d+\.\d+\.\d+)"""")
        return regex.find(packageJson)?.groupValues?.get(1)
    }

    /**
     * Asserts that a version range string has a minimum version at or above the expected floor.
     *
     * Parses caret ranges like "^19.0.4" and verifies the base version meets the minimum.
     *
     * @param packageName used only for assertion messages
     * @param versionRange the version range string (e.g., "^19.0.4")
     * @param minMajor minimum expected major version
     * @param minMinor minimum expected minor version
     * @param minPatch minimum expected patch version
     */
    private fun assertMinVersion(
        packageName: String,
        versionRange: String?,
        minMajor: Int,
        minMinor: Int,
        minPatch: Int,
    ) {
        assertTrue(versionRange != null, "$packageName should be present")
        val cleaned = versionRange!!.removePrefix("^").removePrefix("~")
        val parts = cleaned.split(".").map { it.toInt() }
        val major = parts[0]
        val minor = parts.getOrElse(1) { 0 }
        val patch = parts.getOrElse(2) { 0 }
        val actual = major * 1_000_000 + minor * 1_000 + patch
        val expected = minMajor * 1_000_000 + minMinor * 1_000 + minPatch
        assertTrue(
            actual >= expected,
            "$packageName version $versionRange should be >= $minMajor.$minMinor.$minPatch",
        )
    }

    @Test
    fun `ViteReact has secure dependency versions`() {
        val files = ProjectTemplate.VITE_REACT.generateFiles("test")
        val pkg = files["package.json"]!!
        assertMinVersion("react", extractVersion(pkg, "react"), 19, 0, 4)
        assertMinVersion("react-dom", extractVersion(pkg, "react-dom"), 19, 0, 4)
        assertMinVersion("@vitejs/plugin-react", extractVersion(pkg, "@vitejs/plugin-react"), 6, 0, 0)
        // Vite+ is the active toolchain; vite-plus is pre-1.0 so we only assert presence.
        assertTrue(extractVersion(pkg, "vite-plus") != null, "vite-plus should be present")
    }

    @Test
    fun `Nextjs has secure dependency versions`() {
        val files = ProjectTemplate.NEXTJS.generateFiles("test")
        val pkg = files["package.json"]!!
        assertMinVersion("react", extractVersion(pkg, "react"), 19, 0, 4)
        assertMinVersion("react-dom", extractVersion(pkg, "react-dom"), 19, 0, 4)
        assertMinVersion("next", extractVersion(pkg, "next"), 16, 0, 0)
    }

    @Test
    fun `Electron has secure dependency versions`() {
        val files = ProjectTemplate.ELECTRON.generateFiles("test")
        val pkg = files["package.json"]!!
        assertMinVersion("react", extractVersion(pkg, "react"), 19, 0, 4)
        assertMinVersion("react-dom", extractVersion(pkg, "react-dom"), 19, 0, 4)
        assertMinVersion("@vitejs/plugin-react", extractVersion(pkg, "@vitejs/plugin-react"), 6, 0, 0)
        assertMinVersion("electron", extractVersion(pkg, "electron"), 40, 0, 0)
        assertTrue(extractVersion(pkg, "vite-plus") != null, "vite-plus should be present")
    }

    @Test
    fun `ReactNative has secure dependency versions`() {
        val files = ProjectTemplate.REACT_NATIVE.generateFiles("test")
        val pkg = files["package.json"]!!
        assertMinVersion("react", extractVersion(pkg, "react"), 19, 0, 4)
        assertMinVersion("react-native", extractVersion(pkg, "react-native"), 0, 84, 0)
        assertMinVersion("expo", extractVersion(pkg, "expo"), 55, 0, 0)
    }

    @Test
    fun `CloudflareWorkers has secure dependency versions`() {
        val files = ProjectTemplate.CLOUDFLARE_WORKERS.generateFiles("test")
        val pkg = files["package.json"]!!
        assertMinVersion("wrangler", extractVersion(pkg, "wrangler"), 4, 0, 0)
    }

    @Test
    fun `AwsLambda has secure dependency versions`() {
        val files = ProjectTemplate.AWS_LAMBDA.generateFiles("test")
        val pkg = files["package.json"]!!
        assertMinVersion("esbuild", extractVersion(pkg, "esbuild"), 0, 27, 0)
    }

    @Test
    fun `Monorepo has secure dependency versions`() {
        val files = ProjectTemplate.MONOREPO.generateFiles("test")
        val rootPkg = files["package.json"]!!
        assertMinVersion("concurrently", extractVersion(rootPkg, "concurrently"), 9, 0, 0)
        val clientPkg = files["packages/client/package.json"]!!
        assertMinVersion("react", extractVersion(clientPkg, "react"), 19, 0, 4)
        assertMinVersion("react-dom", extractVersion(clientPkg, "react-dom"), 19, 0, 4)
        assertMinVersion("@vitejs/plugin-react", extractVersion(clientPkg, "@vitejs/plugin-react"), 6, 0, 0)
        assertTrue(extractVersion(clientPkg, "vite-plus") != null, "client should depend on vite-plus")
    }
}
