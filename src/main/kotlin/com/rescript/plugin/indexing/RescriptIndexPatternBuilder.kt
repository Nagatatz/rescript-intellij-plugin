package com.rescript.plugin.indexing

import com.intellij.lexer.Lexer
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.search.IndexPatternBuilder
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import com.rescript.plugin.lang.RescriptLexer
import com.rescript.plugin.lang.RescriptTokenTypes
import com.rescript.plugin.lang.psi.RescriptFile

/**
 * Enables TODO/FIXME pattern and word index searching in ReScript comment tokens.
 *
 * Provides the ReScript lexer and comment token set to the IDE's index pattern
 * search infrastructure, complementing the existing [RescriptTodoIndexer] for
 * in-file pattern matching. Implements [IndexPatternBuilder].
 *
 * @see RescriptTodoIndexer
 */
class RescriptIndexPatternBuilder : IndexPatternBuilder {
    companion object {
        // Length of "//" prefix for single-line comments
        internal const val SINGLE_COMMENT_START_DELTA = 2

        // Length of "/*" prefix for multi-line comments
        internal const val MULTI_COMMENT_START_DELTA = 2

        // Length of "*/" suffix for multi-line comments
        internal const val MULTI_COMMENT_END_DELTA = 2
    }

    override fun getIndexingLexer(file: PsiFile): Lexer? {
        if (file !is RescriptFile) return null
        return RescriptLexer()
    }

    override fun getCommentTokenSet(file: PsiFile): TokenSet? {
        if (file !is RescriptFile) return null
        return RescriptTokenTypes.COMMENTS
    }

    override fun getCommentStartDelta(tokenType: IElementType?): Int =
        when (tokenType) {
            RescriptTokenTypes.SINGLE_COMMENT -> SINGLE_COMMENT_START_DELTA
            RescriptTokenTypes.MULTI_COMMENT -> MULTI_COMMENT_START_DELTA
            else -> 0
        }

    override fun getCommentEndDelta(tokenType: IElementType?): Int =
        when (tokenType) {
            RescriptTokenTypes.MULTI_COMMENT -> MULTI_COMMENT_END_DELTA
            else -> 0
        }
}
