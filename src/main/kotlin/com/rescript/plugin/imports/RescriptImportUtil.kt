package com.rescript.plugin.imports

import com.intellij.psi.PsiElement
import com.rescript.plugin.lang.RescriptTokenTypes

/**
 * Utility for extracting module path information from `open` statement PSI elements.
 *
 * @see com.rescript.plugin.inspection.RescriptDuplicateOpenInspection
 * @see com.rescript.plugin.imports.RescriptImportOptimizer
 */
object RescriptImportUtil {
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
}
