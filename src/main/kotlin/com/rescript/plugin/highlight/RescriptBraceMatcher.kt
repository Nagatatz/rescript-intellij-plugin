package com.rescript.plugin.highlight

import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import com.rescript.plugin.lang.RescriptTokenTypes as T

/**
 * Brace matcher for ReScript that enables brace highlighting and navigation.
 *
 * Defines matched pairs for curly braces `{}`, square brackets `[]`, and
 * parentheses `()`. Curly braces are marked as structural (block-level),
 * enabling code folding at brace boundaries.
 */
class RescriptBraceMatcher : PairedBraceMatcher {
    companion object {
        private val PAIRS =
            arrayOf(
                BracePair(T.LBRACE, T.RBRACE, true),
                BracePair(T.LBRACKET, T.RBRACKET, false),
                BracePair(T.LPAREN, T.RPAREN, false),
            )
    }

    override fun getPairs(): Array<BracePair> = PAIRS

    override fun isPairedBracesAllowedBeforeType(
        lbraceType: IElementType,
        contextType: IElementType?,
    ): Boolean = true

    override fun getCodeConstructStart(
        file: PsiFile?,
        openingBraceOffset: Int,
    ): Int = openingBraceOffset
}
