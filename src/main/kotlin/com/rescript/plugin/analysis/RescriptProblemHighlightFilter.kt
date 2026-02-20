package com.rescript.plugin.analysis

import com.intellij.codeInsight.daemon.ProblemHighlightFilter
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.rescript.plugin.RescriptFileType
import com.rescript.plugin.RescriptInterfaceFileType

/**
 * Suppresses error highlighting for ReScript files in non-project directories.
 *
 * Files under `node_modules/` and ReScript compiler output directories
 * (`lib/bs/`, `lib/ocaml/`) are excluded from problem analysis to avoid
 * noisy diagnostics on third-party or generated code.
 */
class RescriptProblemHighlightFilter : ProblemHighlightFilter() {
    override fun shouldHighlight(file: PsiFile): Boolean {
        val virtualFile = file.virtualFile ?: return true
        val fileType = virtualFile.fileType
        if (fileType != RescriptFileType && fileType != RescriptInterfaceFileType) {
            return true
        }
        return !isExcludedPath(virtualFile)
    }

    companion object {
        private val EXCLUDED_PATH_SEGMENTS =
            listOf(
                "/node_modules/",
                "/lib/bs/",
                "/lib/ocaml/",
            )

        /**
         * Checks if a file resides in an excluded directory.
         *
         * @param file the virtual file to check
         * @return true if the file is in node_modules or compiler output
         */
        fun isExcludedPath(file: VirtualFile): Boolean {
            val path = file.path
            return EXCLUDED_PATH_SEGMENTS.any { path.contains(it) }
        }
    }
}
