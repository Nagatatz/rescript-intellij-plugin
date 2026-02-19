package com.rescript.plugin.imports

import com.intellij.lang.ImportOptimizer
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.rescript.plugin.lang.psi.RescriptElementTypes
import com.rescript.plugin.lang.psi.RescriptFile

/**
 * Optimizes imports in ReScript files by removing duplicate `open` statements.
 *
 * Invoked via Ctrl+Alt+O (Optimize Imports). Scans top-level `open` statements,
 * identifies duplicates by module path, and deletes them in reverse order to
 * preserve offsets during deletion.
 */
class RescriptImportOptimizer : ImportOptimizer {
    override fun supports(file: PsiFile): Boolean = file is RescriptFile

    override fun processFile(file: PsiFile): ImportOptimizer.CollectingInfoRunnable {
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

        return object : ImportOptimizer.CollectingInfoRunnable {
            override fun run() {
                for (element in duplicates.reversed()) {
                    element.delete()
                }
            }

            override fun getUserNotificationInfo(): String =
                if (duplicates.isEmpty()) {
                    "No duplicate open statements found"
                } else {
                    "Removed ${duplicates.size} duplicate open statement(s)"
                }
        }
    }
}
