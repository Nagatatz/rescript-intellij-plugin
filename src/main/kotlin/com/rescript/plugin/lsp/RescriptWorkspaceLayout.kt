package com.rescript.plugin.lsp

import com.rescript.plugin.util.RescriptPaths
import java.nio.file.Path

/**
 * Snapshot of the ReScript project layout discovered for an IntelliJ project.
 *
 * In a single-package project [packageRoots] holds the project base directory
 * itself; in a monorepo it holds the directories that contain a `rescript.json`
 * or `bsconfig.json`. Consumers use this aggregate view instead of walking the
 * filesystem ad-hoc.
 *
 * @see RescriptWorkspaceDiscovery
 */
data class RescriptWorkspaceLayout(
    val packageRoots: List<Path>,
) {
    /** Returns whether at least one ReScript package root was discovered. */
    fun isRescriptProject(): Boolean = packageRoots.isNotEmpty()

    /** Returns the `node_modules` directories of every discovered package root. */
    fun nodeModulesDirs(): List<Path> = packageRoots.map { it.resolve("node_modules") }

    /** Returns the `node_modules/.bin/rescript-language-server` candidates per package root. */
    fun lspBinCandidates(): List<Path> =
        packageRoots.map { it.resolve("${RescriptPaths.NODE_MODULES_BIN}/${RescriptPaths.LSP_BIN_NAME}") }

    /** Returns the `node_modules/@rescript/language-server` directory candidates per package root. */
    fun lspPackageDirs(): List<Path> = packageRoots.map { it.resolve(RescriptPaths.LSP_PACKAGE_DIR) }

    companion object {
        /** Empty layout used when no ReScript projects are detected. */
        val EMPTY = RescriptWorkspaceLayout(emptyList())
    }
}
