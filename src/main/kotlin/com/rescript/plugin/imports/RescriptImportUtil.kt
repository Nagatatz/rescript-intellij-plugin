package com.rescript.plugin.imports

import com.intellij.psi.PsiElement
import com.rescript.plugin.lang.RescriptTokenTypes

/**
 * Utility for extracting and analyzing `open` statements in ReScript files.
 *
 * Provides shared helpers for open statement regex matching (used by quick fixes
 * and intentions) and PSI-level module path extraction (used by inspections
 * and import optimization).
 *
 * @see com.rescript.plugin.inspection.RescriptDuplicateOpenInspection
 * @see com.rescript.plugin.imports.RescriptImportOptimizer
 * @see com.rescript.plugin.quickfix.RescriptAddOpenQuickFix
 * @see com.rescript.plugin.quickfix.RescriptQualifyReferenceQuickFix
 * @see com.rescript.plugin.intention.RescriptRemoveQualifierIntention
 */
object RescriptImportUtil {
    // Matches top-level "open ModuleName" lines
    private val OPEN_PATTERN = Regex("""(?m)^open\s+\S+""")

    // Same pattern with a capture group for the module name
    private val OPEN_CAPTURE_PATTERN = Regex("""(?m)^open\s+(\S+)""")

    /**
     * Extracts the module path from an OPEN_STATEMENT PsiElement.
     * e.g., "open Belt.Array" -> "Belt.Array"
     */
    fun extractModulePath(openStmt: PsiElement): String {
        val tokens =
            buildList {
                var child = openStmt.firstChild
                var pastOpen = false
                while (child != null) {
                    val type = child.node?.elementType
                    if (type == RescriptTokenTypes.OPEN) {
                        pastOpen = true
                    } else if (pastOpen && (type == RescriptTokenTypes.UIDENT || type == RescriptTokenTypes.DOT)) {
                        add(child.text)
                    }
                    child = child.nextSibling
                }
            }
        return tokens.joinToString("")
    }

    /**
     * Finds the offset where a new open statement should be inserted.
     *
     * Inserts after the last existing open statement, or at the top of the file
     * after any leading comments.
     *
     * @param text the document text
     * @return the insertion offset
     */
    fun findOpenInsertOffset(text: String): Int {
        val matches = OPEN_PATTERN.findAll(text).toList()
        if (matches.isNotEmpty()) {
            val lastMatch = matches.last()
            val lineEnd = text.indexOf('\n', lastMatch.range.last)
            return if (lineEnd >= 0) lineEnd + 1 else text.length
        }

        // No open statements found; insert after leading comments/blank lines
        val lines = text.lines()
        var offset = 0
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("//") || trimmed.startsWith("/*")) {
                offset += line.length + 1
            } else {
                break
            }
        }
        return offset
    }

    /**
     * Collects module names from top-level open statements in the document.
     *
     * @param text the document text
     * @return list of module names from open statements
     */
    fun collectOpenModules(text: String): List<String> =
        OPEN_CAPTURE_PATTERN.findAll(text).map { it.groupValues[1] }.toList()

    /**
     * Checks if a module is opened at the top level in the file.
     *
     * @param text the document text
     * @param moduleName the module name to check
     * @return true if `open ModuleName` exists at file scope
     */
    fun isModuleOpened(
        text: String,
        moduleName: String,
    ): Boolean {
        // Build a specific pattern for this module name to avoid partial matches
        val pattern = Regex("""(?m)^open\s+$moduleName\s*$""")
        return pattern.containsMatchIn(text)
    }
}
