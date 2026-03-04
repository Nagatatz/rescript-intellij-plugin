package com.rescript.plugin.imports

import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.impl.DocumentMarkupModel
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.rescript.plugin.lang.psi.RescriptElementTypes

/**
 * Detects unused `open` statements by querying LSP diagnostic warnings
 * from the document markup model.
 *
 * The ReScript language server reports unused opens as warnings with
 * specific message patterns (e.g., "unused open Belt"). This detector
 * matches those warnings to OPEN_STATEMENT PSI elements for removal
 * by [RescriptImportOptimizer].
 *
 * @see RescriptImportOptimizer
 * @see DocumentMarkupModel
 */
object RescriptUnusedOpenDetector {
    // Pattern to match LSP warning messages for unused open statements.
    // The ReScript language server typically reports "unused open <ModuleName>".
    private val UNUSED_OPEN_PATTERN = Regex("unused open\\b", RegexOption.IGNORE_CASE)

    /**
     * Finds unused open statements in the given file by checking LSP diagnostics.
     *
     * Scans the [DocumentMarkupModel] for WARNING-severity highlights whose
     * description matches the "unused open" pattern, then maps those highlight
     * ranges back to OPEN_STATEMENT PSI elements.
     *
     * @param file the ReScript PSI file to analyze
     * @return list of PSI elements representing unused open statements,
     *         or empty list if LSP is not running or no unused opens are found
     */
    fun findUnusedOpens(file: PsiFile): List<PsiElement> {
        val project = file.project
        val document = file.viewProvider.document ?: return emptyList()

        val unusedOpens = mutableListOf<PsiElement>()

        // Scan the document markup model for diagnostic highlighters
        try {
            val markupModel =
                DocumentMarkupModel.forDocument(document, project, false)
                    ?: return emptyList()

            for (highlighter in markupModel.allHighlighters) {
                val info = HighlightInfo.fromRangeHighlighter(highlighter) ?: continue
                if (isUnusedOpenWarning(info)) {
                    // Find the OPEN_STATEMENT PSI element at the highlight range
                    val element = findOpenStatementAt(file, info.startOffset, info.endOffset)
                    if (element != null) {
                        unusedOpens.add(element)
                    }
                }
            }
        } catch (_: Exception) {
            // DocumentMarkupModel may not be available (e.g., in test or headless mode).
            // Return empty list gracefully.
            return emptyList()
        }

        return unusedOpens
    }

    /**
     * Checks if a [HighlightInfo] represents an unused open warning from the LSP.
     *
     * Matches against WARNING severity and description containing "unused open".
     * The check is case-insensitive to accommodate potential variations in
     * LSP message formatting.
     *
     * @param info the highlight info to check
     * @return true if this highlight represents an unused open warning
     */
    internal fun isUnusedOpenWarning(info: HighlightInfo): Boolean {
        if (info.severity != HighlightSeverity.WARNING) {
            return false
        }
        val description = info.description ?: return false
        return UNUSED_OPEN_PATTERN.containsMatchIn(description)
    }

    /**
     * Finds the OPEN_STATEMENT PSI element that contains or overlaps the given offset range.
     *
     * Walks up the PSI tree from the element at [startOffset] to find the nearest
     * OPEN_STATEMENT ancestor. Falls back to checking [endOffset] if the start
     * position does not resolve to an open statement.
     *
     * @param file the PSI file to search in
     * @param startOffset the start offset of the highlight range
     * @param endOffset the end offset of the highlight range
     * @return the OPEN_STATEMENT element, or null if not found
     */
    private fun findOpenStatementAt(
        file: PsiFile,
        startOffset: Int,
        endOffset: Int,
    ): PsiElement? {
        // Try finding from start offset first
        val elementAtStart = file.findElementAt(startOffset)
        if (elementAtStart != null) {
            val openStmt = findOpenStatementParent(elementAtStart)
            if (openStmt != null) return openStmt
        }

        // Fall back to end offset if start didn't resolve
        if (endOffset > startOffset) {
            val elementAtEnd = file.findElementAt(endOffset - 1)
            if (elementAtEnd != null) {
                val openStmt = findOpenStatementParent(elementAtEnd)
                if (openStmt != null) return openStmt
            }
        }

        return null
    }

    /**
     * Walks up the PSI tree to find the nearest OPEN_STATEMENT ancestor.
     *
     * @param element the starting PSI element
     * @return the OPEN_STATEMENT ancestor, or null if not found
     */
    private fun findOpenStatementParent(element: PsiElement): PsiElement? {
        var current: PsiElement? = element
        while (current != null && current !is PsiFile) {
            if (current.node?.elementType == RescriptElementTypes.OPEN_STATEMENT) {
                return current
            }
            current = current.parent
        }
        return null
    }
}
