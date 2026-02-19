package com.rescript.plugin.imports

import com.intellij.lang.ImportOptimizer
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.rescript.plugin.lang.psi.RescriptElementTypes
import com.rescript.plugin.lang.psi.RescriptFile
import com.rescript.plugin.settings.RescriptProjectSettings

/**
 * Optimizes imports in ReScript files by removing duplicate and unused `open` statements.
 *
 * Invoked via Ctrl+Alt+O (Optimize Imports). Scans top-level `open` statements,
 * identifies duplicates by module path, and optionally detects unused opens via
 * LSP diagnostic warnings. Deletes matched statements in reverse offset order to
 * preserve offsets during deletion.
 *
 * @see RescriptUnusedOpenDetector for LSP-based unused open detection
 * @see RescriptImportUtil for module path extraction
 */
class RescriptImportOptimizer : ImportOptimizer {
    override fun supports(file: PsiFile): Boolean = file is RescriptFile

    override fun processFile(file: PsiFile): ImportOptimizer.CollectingInfoRunnable {
        // Phase 1: Detect duplicate open statements
        val openStatements = file.children.filter { it.node?.elementType == RescriptElementTypes.OPEN_STATEMENT }
        val seen = mutableSetOf<String>()
        val duplicates = mutableListOf<PsiElement>()

        for (openStmt in openStatements) {
            val modulePath = RescriptImportUtil.extractModulePath(openStmt)
            if (modulePath.isNotEmpty()) {
                if (!seen.add(modulePath)) {
                    duplicates.add(openStmt)
                }
            }
        }

        // Phase 2: Detect unused open statements via LSP diagnostics
        val unusedOpens = mutableListOf<PsiElement>()
        val removeUnusedEnabled =
            try {
                RescriptProjectSettings.getInstance(file.project).removeUnusedOpensEnabled
            } catch (_: Exception) {
                // Settings service may not be available in test context
                true
            }

        if (removeUnusedEnabled) {
            val detected = RescriptUnusedOpenDetector.findUnusedOpens(file)
            // Exclude any that are already in duplicates to avoid double-deletion
            val duplicateRanges = duplicates.mapTo(mutableSetOf()) { it.textRange }
            for (open in detected) {
                if (open.textRange !in duplicateRanges) {
                    unusedOpens.add(open)
                }
            }
        }

        // Merge both lists, deduplicate by text range
        val allToRemove = (duplicates + unusedOpens).distinctBy { it.textRange }

        return object : ImportOptimizer.CollectingInfoRunnable {
            override fun run() {
                // Delete in reverse offset order to preserve earlier offsets
                for (element in allToRemove.sortedByDescending { it.textRange.startOffset }) {
                    element.delete()
                }
            }

            override fun getUserNotificationInfo(): String = buildNotificationMessage(duplicates.size, unusedOpens.size)
        }
    }

    companion object {
        /**
         * Builds a human-readable notification message summarizing the optimization result.
         *
         * @param duplicateCount number of duplicate open statements removed
         * @param unusedCount number of unused open statements removed
         * @return descriptive message for the user notification
         */
        internal fun buildNotificationMessage(
            duplicateCount: Int,
            unusedCount: Int,
        ): String {
            val parts = mutableListOf<String>()
            if (duplicateCount > 0) parts.add("$duplicateCount duplicate")
            if (unusedCount > 0) parts.add("$unusedCount unused")
            return if (parts.isEmpty()) {
                "No open statements to remove"
            } else {
                "Removed ${parts.joinToString(" and ")} open statement(s)"
            }
        }
    }
}
