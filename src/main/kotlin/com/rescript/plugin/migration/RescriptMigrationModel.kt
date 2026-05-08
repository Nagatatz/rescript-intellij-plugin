package com.rescript.plugin.migration

import com.intellij.openapi.vfs.VirtualFile

/**
 * One `.re` or `.rei` file that the migration pilot can convert.
 *
 * @property file the underlying [VirtualFile]
 * @property relativePath path relative to the project root, used as
 *   the user-visible label in the migration list
 */
data class MigrationCandidate(
    val file: VirtualFile,
    val relativePath: String,
)

/** Outcome of a single conversion attempt. */
enum class ConversionStatus { SUCCESS, FAILED }

/**
 * Result returned by the converter for one candidate. The summary
 * captures whatever the `rescript convert` CLI wrote to stdout/stderr,
 * so the panel can surface the exact reason a conversion failed.
 *
 * @property candidate the file that was attempted
 * @property status whether the conversion succeeded
 * @property message stdout (on success, may be empty) or stderr
 *   (on failure)
 */
data class ConversionResult(
    val candidate: MigrationCandidate,
    val status: ConversionStatus,
    val message: String,
)
