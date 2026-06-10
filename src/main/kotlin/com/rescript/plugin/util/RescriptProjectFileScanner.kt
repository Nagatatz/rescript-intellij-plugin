package com.rescript.plugin.util

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope

/**
 * Shared project-wide file scan loop used by the tool window scanners
 * (Type Coverage, JS Interop Risk Map).
 *
 * Each scanner used to repeat the same boilerplate: a read action wrapping
 * [FileTypeIndex] lookups over [GlobalSearchScope.projectScope], a cap check
 * at the top of the loop, and a guarded [VirtualFile.contentsToByteArray]
 * read. This helper keeps that loop in one tested place; what differs per
 * scanner (the cap predicate and the per-file processing) stays with the
 * caller.
 *
 * @see com.rescript.plugin.coverage.RescriptTypeCoverageScanner
 * @see com.rescript.plugin.interop.RescriptInteropScanner
 */
internal object RescriptProjectFileScanner {
    /**
     * Iterates every project file of the given [fileTypes] inside a read
     * action, reading each file's text safely and passing it to [onFile].
     * Files whose contents cannot be read are skipped silently, matching
     * the historical scanner behaviour.
     *
     * [shouldContinue] is evaluated before each file; when it returns
     * `false` while unvisited files remain, the scan stops and the result
     * is reported as truncated.
     *
     * Must be called off the EDT — file contents are read via
     * [VirtualFile.contentsToByteArray] inside the read action.
     *
     * @param project the project whose [GlobalSearchScope.projectScope] is scanned
     * @param fileTypes file types to collect, concatenated in order
     * @param shouldContinue cap predicate checked before each file
     * @param onFile invoked with each readable file and its decoded text
     * @return `true` when the scan stopped early because [shouldContinue]
     *   returned `false`, `false` when every file was visited
     */
    fun scanFiles(
        project: Project,
        fileTypes: List<FileType>,
        shouldContinue: () -> Boolean,
        onFile: (VirtualFile, String) -> Unit,
    ): Boolean {
        var truncated = false
        ApplicationManager.getApplication().runReadAction {
            val scope = GlobalSearchScope.projectScope(project)
            val files = fileTypes.flatMap { FileTypeIndex.getFiles(it, scope) }
            truncated = visitFiles(files, shouldContinue, onFile)
        }
        return truncated
    }

    /**
     * Pure visiting loop behind [scanFiles], exposed `internal` so unit
     * tests can drive it with in-memory files — the light test fixture
     * has no content roots, so [FileTypeIndex] cannot be populated there.
     *
     * @param files candidate files in iteration order
     * @param shouldContinue cap predicate checked before each file
     * @param onFile invoked with each readable file and its decoded text
     * @return `true` when the loop stopped early because [shouldContinue]
     *   returned `false`, `false` when every file was visited
     */
    internal fun visitFiles(
        files: List<VirtualFile>,
        shouldContinue: () -> Boolean,
        onFile: (VirtualFile, String) -> Unit,
    ): Boolean {
        for (file in files) {
            if (!shouldContinue()) {
                return true
            }
            val text =
                try {
                    String(file.contentsToByteArray(), file.charset)
                } catch (_: Exception) {
                    continue
                }
            onFile(file, text)
        }
        return false
    }
}
