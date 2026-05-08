package com.rescript.plugin.lsp

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

/**
 * Unit tests for [RescriptWorkspaceFileParser] covering pnpm-workspace.yaml and
 * package.json#workspaces parsing.
 */
class RescriptWorkspaceFileParserTest {
    private lateinit var tempDir: Path

    @BeforeEach
    fun setUp() {
        tempDir = Files.createTempDirectory("rescript-ws-parser-test")
    }

    @AfterEach
    fun tearDown() {
        tempDir.toFile().deleteRecursively()
    }

    // ── pnpm-workspace.yaml ─────────────────────────────────────────

    @Test
    fun `readPnpmWorkspaces parses canonical packages list`() {
        val file =
            writePnpm(
                """
                packages:
                  - "packages/*"
                  - "examples/*"
                """.trimIndent(),
            )

        val result = RescriptWorkspaceFileParser.readPnpmWorkspaces(file)

        assertEquals(listOf("packages/*", "examples/*"), result)
    }

    @Test
    fun `readPnpmWorkspaces handles single quotes and unquoted entries`() {
        val file =
            writePnpm(
                """
                packages:
                  - 'packages/*'
                  - apps/web
                """.trimIndent(),
            )

        val result = RescriptWorkspaceFileParser.readPnpmWorkspaces(file)

        assertEquals(listOf("packages/*", "apps/web"), result)
    }

    @Test
    fun `readPnpmWorkspaces ignores comments and blank lines`() {
        val file =
            writePnpm(
                """
                # a comment

                packages:
                  # nested comment
                  - "packages/*"
                  - "apps/*" # trailing
                """.trimIndent(),
            )

        val result = RescriptWorkspaceFileParser.readPnpmWorkspaces(file)

        assertEquals(listOf("packages/*", "apps/*"), result)
    }

    @Test
    fun `readPnpmWorkspaces stops at the next top level key`() {
        val file =
            writePnpm(
                """
                packages:
                  - "packages/*"
                catalog:
                  some: thing
                """.trimIndent(),
            )

        val result = RescriptWorkspaceFileParser.readPnpmWorkspaces(file)

        assertEquals(listOf("packages/*"), result)
    }

    @Test
    fun `readPnpmWorkspaces returns empty list for missing file`() {
        val result = RescriptWorkspaceFileParser.readPnpmWorkspaces(tempDir.resolve("missing.yaml"))
        assertTrue(result.isEmpty())
    }

    @Test
    fun `readPnpmWorkspaces returns empty list when packages key absent`() {
        val file = writePnpm("catalog:\n  foo: bar\n")
        val result = RescriptWorkspaceFileParser.readPnpmWorkspaces(file)
        assertTrue(result.isEmpty())
    }

    // ── package.json#workspaces ─────────────────────────────────────

    @Test
    fun `readPackageJsonWorkspaces parses array form`() {
        val file = writePackageJson("""{"workspaces": ["packages/*", "apps/*"]}""")

        val result = RescriptWorkspaceFileParser.readPackageJsonWorkspaces(file)

        assertEquals(listOf("packages/*", "apps/*"), result)
    }

    @Test
    fun `readPackageJsonWorkspaces parses yarn classic object form`() {
        val file =
            writePackageJson(
                """{"workspaces": {"packages": ["packages/*"], "nohoist": ["**/foo"]}}""",
            )

        val result = RescriptWorkspaceFileParser.readPackageJsonWorkspaces(file)

        assertEquals(listOf("packages/*"), result)
    }

    @Test
    fun `readPackageJsonWorkspaces skips non-string entries`() {
        val file = writePackageJson("""{"workspaces": ["packages/*", 42, null]}""")

        val result = RescriptWorkspaceFileParser.readPackageJsonWorkspaces(file)

        assertEquals(listOf("packages/*"), result)
    }

    @Test
    fun `readPackageJsonWorkspaces returns empty list when workspaces field missing`() {
        val file = writePackageJson("""{"name": "foo"}""")
        val result = RescriptWorkspaceFileParser.readPackageJsonWorkspaces(file)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `readPackageJsonWorkspaces returns empty list for malformed JSON`() {
        val file = writePackageJson("not valid json")
        val result = RescriptWorkspaceFileParser.readPackageJsonWorkspaces(file)
        assertTrue(result.isEmpty())
    }

    // ── readGlobs precedence ────────────────────────────────────────

    @Test
    fun `readGlobs prefers pnpm-workspace yaml over package json`() {
        writePnpm("packages:\n  - \"pnpm-only/*\"\n")
        writePackageJson("""{"workspaces": ["pkg-json/*"]}""")

        val result = RescriptWorkspaceFileParser.readGlobs(tempDir)

        assertEquals(listOf("pnpm-only/*"), result)
    }

    @Test
    fun `readGlobs falls back to package json when pnpm yaml missing`() {
        writePackageJson("""{"workspaces": ["pkg-json/*"]}""")

        val result = RescriptWorkspaceFileParser.readGlobs(tempDir)

        assertEquals(listOf("pkg-json/*"), result)
    }

    private fun writePnpm(content: String): Path {
        val file = tempDir.resolve("pnpm-workspace.yaml")
        Files.writeString(file, content)
        return file
    }

    private fun writePackageJson(content: String): Path {
        val file = tempDir.resolve("package.json")
        Files.writeString(file, content)
        return file
    }
}
