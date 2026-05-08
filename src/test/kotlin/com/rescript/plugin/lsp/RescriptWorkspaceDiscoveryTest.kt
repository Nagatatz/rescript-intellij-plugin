package com.rescript.plugin.lsp

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

/**
 * Unit tests for [RescriptWorkspaceDiscovery] covering the four discovery
 * layers (manual override, workspace files, depth-limited scan, parent walk)
 * plus the empty/edge cases.
 */
class RescriptWorkspaceDiscoveryTest {
    private lateinit var tempDir: Path

    @BeforeEach
    fun setUp() {
        tempDir = Files.createTempDirectory("rescript-discovery-test")
    }

    @AfterEach
    fun tearDown() {
        tempDir.toFile().deleteRecursively()
    }

    // ── Layer 1: manual override ─────────────────────────────────

    @Test
    fun `manual override is honoured when entries are valid`() {
        val pkg = tempDir.resolve("custom/pkg").also { Files.createDirectories(it) }
        Files.createFile(pkg.resolve("rescript.json"))

        val layout = RescriptWorkspaceDiscovery.discover(tempDir.toString(), listOf("custom/pkg"))

        assertEquals(listOf(pkg.toAbsolutePath().normalize()), layout.packageRoots)
        assertTrue(layout.isRescriptProject())
    }

    @Test
    fun `manual override silently filters invalid entries`() {
        val valid = tempDir.resolve("packages/core").also { Files.createDirectories(it) }
        Files.createFile(valid.resolve("rescript.json"))

        val layout =
            RescriptWorkspaceDiscovery.discover(
                tempDir.toString(),
                listOf("packages/core", "packages/missing", "../../escape"),
            )

        assertEquals(listOf(valid.toAbsolutePath().normalize()), layout.packageRoots)
    }

    @Test
    fun `manual override that resolves to nothing falls through to auto-detect`() {
        Files.createFile(tempDir.resolve("rescript.json"))

        val layout =
            RescriptWorkspaceDiscovery.discover(
                tempDir.toString(),
                listOf("nonexistent/path"),
            )

        // Falls through to depth-limited scan which finds the root config
        assertTrue(layout.isRescriptProject())
        assertEquals(listOf(tempDir.toAbsolutePath().normalize()), layout.packageRoots)
    }

    // ── Layer 2: workspace files ─────────────────────────────────

    @Test
    fun `pnpm workspace yaml drives detection`() {
        Files.writeString(
            tempDir.resolve("pnpm-workspace.yaml"),
            "packages:\n  - \"packages/*\"\n",
        )
        val core = tempDir.resolve("packages/core").also { Files.createDirectories(it) }
        Files.createFile(core.resolve("rescript.json"))
        // Sibling without config is filtered out
        Files.createDirectories(tempDir.resolve("packages/empty"))

        val layout = RescriptWorkspaceDiscovery.discover(tempDir.toString())

        assertEquals(listOf(core.toAbsolutePath().normalize()), layout.packageRoots)
    }

    @Test
    fun `package json workspaces array form is consumed`() {
        Files.writeString(
            tempDir.resolve("package.json"),
            """{"workspaces": ["apps/*"]}""",
        )
        val web = tempDir.resolve("apps/web").also { Files.createDirectories(it) }
        Files.createFile(web.resolve("bsconfig.json"))

        val layout = RescriptWorkspaceDiscovery.discover(tempDir.toString())

        assertEquals(listOf(web.toAbsolutePath().normalize()), layout.packageRoots)
    }

    @Test
    fun `workspace files plus root config produce both entries`() {
        Files.createFile(tempDir.resolve("rescript.json"))
        Files.writeString(
            tempDir.resolve("pnpm-workspace.yaml"),
            "packages:\n  - \"packages/*\"\n",
        )
        val core = tempDir.resolve("packages/core").also { Files.createDirectories(it) }
        Files.createFile(core.resolve("rescript.json"))

        val layout = RescriptWorkspaceDiscovery.discover(tempDir.toString())

        assertEquals(2, layout.packageRoots.size)
        assertTrue(layout.packageRoots.contains(tempDir.toAbsolutePath().normalize()))
        assertTrue(layout.packageRoots.contains(core.toAbsolutePath().normalize()))
    }

    // ── Layer 3: depth-limited scan ──────────────────────────────

    @Test
    fun `depth-limited scan finds root level config`() {
        Files.createFile(tempDir.resolve("rescript.json"))

        val layout = RescriptWorkspaceDiscovery.discover(tempDir.toString())

        assertEquals(listOf(tempDir.toAbsolutePath().normalize()), layout.packageRoots)
    }

    @Test
    fun `depth-limited scan finds nested rescript json without workspace files`() {
        val nested = tempDir.resolve("a/b/c").also { Files.createDirectories(it) }
        Files.createFile(nested.resolve("rescript.json"))

        val layout = RescriptWorkspaceDiscovery.discover(tempDir.toString())

        assertEquals(listOf(nested.toAbsolutePath().normalize()), layout.packageRoots)
    }

    @Test
    fun `depth-limited scan ignores node_modules subtree`() {
        val nodeModulesPkg =
            tempDir.resolve("node_modules/some-pkg").also { Files.createDirectories(it) }
        Files.createFile(nodeModulesPkg.resolve("rescript.json"))

        val layout = RescriptWorkspaceDiscovery.discover(tempDir.toString())

        assertFalse(layout.isRescriptProject())
    }

    @Test
    fun `depth-limited scan does not descend into matched package`() {
        val pkg = tempDir.resolve("packages/core").also { Files.createDirectories(it) }
        Files.createFile(pkg.resolve("rescript.json"))
        val nestedSubpkg =
            pkg.resolve("subpkg").also { Files.createDirectories(it) }
        Files.createFile(nestedSubpkg.resolve("rescript.json"))

        val layout = RescriptWorkspaceDiscovery.discover(tempDir.toString())

        assertEquals(listOf(pkg.toAbsolutePath().normalize()), layout.packageRoots)
    }

    // ── Layer 4: parent walk ─────────────────────────────────────

    @Test
    fun `parent walk recovers config when opening a sub-directory`() {
        Files.createFile(tempDir.resolve("rescript.json"))
        val sub = tempDir.resolve("src").also { Files.createDirectories(it) }
        // sub directory itself has no config; depth-limited from sub finds nothing;
        // expect parent walk to climb to tempDir.
        val layout = RescriptWorkspaceDiscovery.discover(sub.toString())

        // Either the depth-limited scan from `sub` won't find anything (empty subtree),
        // and then parent walk should resolve to tempDir.
        assertTrue(layout.isRescriptProject())
        assertEquals(listOf(tempDir.toAbsolutePath().normalize()), layout.packageRoots)
    }

    // ── Layer 5: empty / edge cases ──────────────────────────────

    @Test
    fun `empty layout when nothing exists`() {
        val layout = RescriptWorkspaceDiscovery.discover(tempDir.toString())
        assertFalse(layout.isRescriptProject())
        assertTrue(layout.packageRoots.isEmpty())
    }

    @Test
    fun `null base path returns empty layout`() {
        val layout = RescriptWorkspaceDiscovery.discover(null as String?)
        assertEquals(RescriptWorkspaceLayout.EMPTY, layout)
    }

    @Test
    fun `non-existent base path returns empty layout`() {
        val layout =
            RescriptWorkspaceDiscovery.discover(tempDir.resolve("does-not-exist").toString())

        assertFalse(layout.isRescriptProject())
    }
}
