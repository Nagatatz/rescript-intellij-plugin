package com.rescript.plugin.editor

import com.intellij.openapi.editor.actions.lists.DefaultListSplitJoinContext
import com.intellij.openapi.editor.actions.lists.ListWithElements
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import com.rescript.plugin.lang.RescriptTokenTypes

/**
 * Enables Split/Join List action for comma-separated constructs in ReScript.
 *
 * Supports converting between single-line and multi-line formats for:
 * - Function arguments: `(a, b, c)` ↔ multi-line with trailing comma
 * - Array literals: `[1, 2, 3]` ↔ multi-line
 * - Record fields: `{name: "foo", age: 25}` ↔ multi-line
 * - Tuple elements: `(1, "a", true)` ↔ multi-line
 *
 * Follows ReScript conventions: trailing comma on split, 2-space indent.
 */
class RescriptListSplitJoinContext : DefaultListSplitJoinContext() {
    override fun extractData(context: PsiElement): ListWithElements? {
        // Find the nearest enclosing bracket pair
        val parent = findListParent(context) ?: return null
        val children = parent.children.toList()

        val elements =
            children.filter { child ->
                val type = child.node?.elementType
                type != RescriptTokenTypes.COMMA &&
                    type != TokenType.WHITE_SPACE &&
                    type != RescriptTokenTypes.EOL &&
                    type != RescriptTokenTypes.LPAREN &&
                    type != RescriptTokenTypes.RPAREN &&
                    type != RescriptTokenTypes.LBRACKET &&
                    type != RescriptTokenTypes.RBRACKET &&
                    type != RescriptTokenTypes.LBRACE &&
                    type != RescriptTokenTypes.RBRACE
            }

        if (elements.size < 2) return null

        return ListWithElements(parent, elements)
    }

    /**
     * Finds the nearest parent PSI element that represents a comma-separated list.
     *
     * @param element the starting element
     * @return the list parent element, or null if none found
     */
    override fun isSeparator(element: PsiElement): Boolean = element.node?.elementType == RescriptTokenTypes.COMMA

    /**
     * Finds the nearest parent PSI element that represents a comma-separated list.
     *
     * @param element the starting element
     * @return the list parent element, or null if none found
     */
    private fun findListParent(element: PsiElement): PsiElement? {
        var current: PsiElement? = element
        while (current != null) {
            val children = current.children
            if (children.any { it.node?.elementType == RescriptTokenTypes.COMMA }) {
                return current
            }
            current = current.parent
        }
        return null
    }
}
