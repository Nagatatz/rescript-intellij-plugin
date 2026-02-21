package com.rescript.plugin.editor

import com.intellij.codeInsight.editorActions.moveLeftRight.MoveElementLeftRightHandler
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import com.rescript.plugin.lang.RescriptTokenTypes

/**
 * Enables Move Element Left/Right (Alt+Shift+Cmd+Left/Right) for comma-separated lists.
 *
 * Identifies movable sub-elements within parenthesized, bracketed, or braced groups,
 * such as function arguments `(a, b, c)`, array literals `[1, 2, 3]`,
 * and record fields `{name: "foo", age: 25}`.
 */
class RescriptMoveElementHandler : MoveElementLeftRightHandler() {
    override fun getMovableSubElements(element: PsiElement): Array<PsiElement> {
        val children = element.children
        if (children.isEmpty()) return PsiElement.EMPTY_ARRAY

        // Check if this element contains comma-separated items
        val hasComma = children.any { it.node?.elementType == RescriptTokenTypes.COMMA }
        if (!hasComma) return PsiElement.EMPTY_ARRAY

        // Collect elements between commas (excluding commas and whitespace)
        return children
            .filter { child ->
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
            }.toTypedArray()
    }
}
