package com.rescript.plugin.navigation

import com.intellij.openapi.vfs.VirtualFile

/**
 * One row produced by the Hoogle-style ReScript Type Signature Search.
 *
 * The renderer shows `name: signatureDisplay  (relativePath:line)` and
 * `processSelectedItem` opens [file] at [declarationOffset]. Keeping the
 * raw AST out of the hit (only the textual signature is retained) lets
 * the unit tests verify the contributor flow without re-parsing every
 * candidate.
 *
 * @property name the binding name as it appears in the declaration
 * @property signatureDisplay the original signature text (used in the
 *   list cell — the AST is only used for matching)
 * @property file the file containing the matched declaration
 * @property declarationOffset offset of the binding name; used by
 *   `OpenFileDescriptor` so navigation jumps to the symbol, not the
 *   file's first byte
 * @property line 1-based line number of the declaration in [file]
 * @property relativePath project-relative path used for display only
 */
data class RescriptTypeSignatureSearchHit(
    val name: String,
    val signatureDisplay: String,
    val file: VirtualFile,
    val declarationOffset: Int,
    val line: Int,
    val relativePath: String,
)
