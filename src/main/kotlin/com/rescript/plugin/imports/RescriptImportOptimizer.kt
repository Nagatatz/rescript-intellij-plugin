package com.rescript.plugin.imports

import com.intellij.lang.ImportOptimizer
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.rescript.plugin.lang.psi.RescriptFile

/**
 * Removes adjacent opens only when their identical local module target is proven.
 *
 * Repeated paths separated by code, unknown modules and unversioned diagnostic warnings
 * are preserved. The collected edit is discarded if the source changes before application.
 *
 * @see RescriptOpenRemovalProof
 */
class RescriptImportOptimizer : ImportOptimizer {
    override fun supports(file: PsiFile): Boolean = file is RescriptFile

    override fun processFile(file: PsiFile): ImportOptimizer.CollectingInfoRunnable {
        val snapshot = file.text
        val duplicates = RescriptOpenRemovalProof.findRedundantOpens(file)
        val removals = duplicates.mapNotNull { RescriptOpenRemovalProof.removalRange(it) }

        return object : ImportOptimizer.CollectingInfoRunnable {
            private var removedCount = 0

            override fun run() {
                val document = file.viewProvider.document ?: return
                if (!file.isValid || !PsiDocumentManager.getInstance(file.project).isCommitted(document) ||
                    file.text != snapshot || document.text != snapshot ||
                    duplicates.any { !it.isValid }
                ) {
                    return
                }
                // Delete in reverse offset order to preserve earlier offsets.
                for (range in removals.sortedByDescending { it.startOffset }) {
                    document.deleteString(range.startOffset, range.endOffset)
                    removedCount++
                }
            }

            override fun getUserNotificationInfo(): String = buildNotificationMessage(removedCount, 0)
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
