package com.rescript.plugin.lang.psi

import com.intellij.icons.AllIcons
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.rescript.plugin.RescriptIcons
import com.rescript.plugin.lang.RescriptTokenTypes
import javax.swing.Icon

/**
 * Utility functions for working with ReScript PSI elements.
 *
 * Provides name extraction, icon mapping, and description generation for
 * top-level declarations. Used by structure view, breadcrumbs, and navigation features.
 */
object RescriptPsiUtils {
    /** Element types that represent navigable declarations in structure view and breadcrumbs. */
    val NAVIGABLE_TYPES: Set<IElementType> =
        setOf(
            RescriptElementTypes.LET_DECLARATION,
            RescriptElementTypes.TYPE_DECLARATION,
            RescriptElementTypes.MODULE_DECLARATION,
            RescriptElementTypes.EXTERNAL_DECLARATION,
            RescriptElementTypes.EXCEPTION_DECLARATION,
        )

    private val IDENTIFIER_TYPES: Set<IElementType> =
        setOf(
            RescriptTokenTypes.LIDENT,
            RescriptTokenTypes.UIDENT,
            RescriptTokenTypes.UNDERSCORE,
        )

    /**
     * Extracts the declared name from a ReScript PSI element.
     *
     * Walks through child AST nodes to find the first identifier token after a
     * top-level keyword (e.g., `let`, `type`, `module`), skipping `rec` keywords.
     *
     * @return the declared name, or "(anonymous)" if no identifier is found
     */
    fun extractName(element: PsiElement): String {
        if (element is RescriptFile) return element.name

        val node = element.node ?: return "(unknown)"
        var child = node.firstChildNode
        var afterKeyword = false
        while (child != null) {
            if (RescriptTokenTypes.TOP_LEVEL_KEYWORDS.contains(child.elementType)) {
                afterKeyword = true
            } else if (child.elementType == RescriptTokenTypes.REC) {
                // skip 'rec' keyword — name follows after it
            } else if (afterKeyword && child.elementType in IDENTIFIER_TYPES) {
                return child.text
            }
            child = child.treeNext
        }
        return "(anonymous)"
    }

    /** Returns the appropriate icon for a ReScript PSI element based on its declaration type. */
    fun getIcon(element: PsiElement): Icon? {
        if (element is RescriptFile) return RescriptIcons.FILE

        return when (element.node?.elementType) {
            RescriptElementTypes.LET_DECLARATION -> AllIcons.Nodes.Function
            RescriptElementTypes.TYPE_DECLARATION -> AllIcons.Nodes.Type
            RescriptElementTypes.MODULE_DECLARATION -> AllIcons.Nodes.Module
            RescriptElementTypes.EXTERNAL_DECLARATION -> AllIcons.Nodes.PluginJB
            RescriptElementTypes.EXCEPTION_DECLARATION -> AllIcons.Nodes.ExceptionClass
            else -> null
        }
    }

    /** Returns a human-readable description of the element's declaration type (e.g., "let declaration"). */
    fun getElementDescription(element: PsiElement): String? =
        when (element.node?.elementType) {
            RescriptElementTypes.LET_DECLARATION -> "let declaration"
            RescriptElementTypes.TYPE_DECLARATION -> "type declaration"
            RescriptElementTypes.MODULE_DECLARATION -> "module declaration"
            RescriptElementTypes.EXTERNAL_DECLARATION -> "external declaration"
            RescriptElementTypes.EXCEPTION_DECLARATION -> "exception declaration"
            else -> null
        }
}
