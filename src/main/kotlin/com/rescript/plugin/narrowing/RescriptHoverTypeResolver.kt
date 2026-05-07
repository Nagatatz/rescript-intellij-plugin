package com.rescript.plugin.narrowing

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.rescript.plugin.lsp.RescriptLspUtils

/**
 * Resolves the narrowed type for a given offset by delegating to the
 * ReScript LSP server's `textDocument/hover` response.
 *
 * Modeled as a single-method functional interface so that
 * [RescriptNarrowingHintProvider] can be unit-tested without a live
 * LSP — tests pass an in-memory stub that returns canned responses,
 * while production code uses [forFile] to obtain the LSP-backed
 * implementation already bound to a specific project + file.
 */
fun interface RescriptHoverTypeResolver {
    /**
     * Returns the type string for the given offset within the file
     * this resolver is bound to, or `null` if LSP is unavailable, the
     * request fails, or the response carries no type information.
     *
     * @param offset zero-based character offset in the file
     * @return the type string, or `null` when no narrowing info is
     *   available
     */
    fun resolveAt(offset: Int): String?

    companion object {
        /**
         * Returns a resolver bound to the given project + file that
         * delegates to [RescriptLspUtils.getHoverType] for each call.
         * The returned lambda makes a synchronous LSP request, so
         * callers must invoke it off the EDT.
         */
        fun forFile(
            project: Project,
            file: VirtualFile,
        ): RescriptHoverTypeResolver =
            RescriptHoverTypeResolver { offset ->
                RescriptLspUtils.getHoverType(project, file, offset)
            }
    }
}
