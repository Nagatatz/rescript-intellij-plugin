package com.rescript.plugin.lsp

import com.rescript.plugin.util.RescriptPaths
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Tests the pure path-derivation helpers on [RescriptWorkspaceLayout]:
 * presence detection, `node_modules` resolution, and LSP binary / package
 * candidate paths. No filesystem interaction — purely string-level joins.
 */
class RescriptWorkspaceLayoutTest {
    @Test
    fun `EMPTY layout is not a ReScript project`() {
        assertFalse(RescriptWorkspaceLayout.EMPTY.isRescriptProject())
        assertTrue(RescriptWorkspaceLayout.EMPTY.packageRoots.isEmpty())
    }

    @Test
    fun `single root layout is a ReScript project`() {
        val layout = RescriptWorkspaceLayout(listOf(Paths.get("/work/proj")))
        assertTrue(layout.isRescriptProject())
        assertEquals(1, layout.packageRoots.size)
    }

    @Test
    fun `nodeModulesDirs appends node_modules per root`() {
        val layout =
            RescriptWorkspaceLayout(
                listOf(Paths.get("/work/a"), Paths.get("/work/b")),
            )
        assertEquals(
            listOf<Path>(
                Paths.get("/work/a/node_modules"),
                Paths.get("/work/b/node_modules"),
            ),
            layout.nodeModulesDirs(),
        )
    }

    @Test
    fun `lspBinCandidates resolves NODE_MODULES_BIN slash LSP_BIN_NAME per root`() {
        val layout = RescriptWorkspaceLayout(listOf(Paths.get("/work/proj")))
        val expected =
            Paths.get("/work/proj").resolve("${RescriptPaths.NODE_MODULES_BIN}/${RescriptPaths.LSP_BIN_NAME}")
        assertEquals(listOf(expected), layout.lspBinCandidates())
    }

    @Test
    fun `lspPackageDirs resolves LSP_PACKAGE_DIR per root`() {
        val layout = RescriptWorkspaceLayout(listOf(Paths.get("/work/proj")))
        val expected = Paths.get("/work/proj").resolve(RescriptPaths.LSP_PACKAGE_DIR)
        assertEquals(listOf(expected), layout.lspPackageDirs())
    }

    @Test
    fun `multi-root layouts return one entry per root`() {
        val roots = listOf("/work/a", "/work/b", "/work/c").map { Paths.get(it) }
        val layout = RescriptWorkspaceLayout(roots)
        assertEquals(roots.size, layout.nodeModulesDirs().size)
        assertEquals(roots.size, layout.lspBinCandidates().size)
        assertEquals(roots.size, layout.lspPackageDirs().size)
    }
}
