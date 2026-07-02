package com.rescript.plugin.lang

import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType

/**
 * Lightweight lexer record produced by walking the JFlex tokens once and
 * caching them; lets callers iterate by index without re-running the lexer
 * for every brace/comma lookup.
 *
 * @property type the token's element type
 * @property start source offset of the token's first character
 * @property end exclusive source offset just after the token
 * @property text the verbatim token text
 */
data class LexedToken(
    val type: IElementType,
    val start: Int,
    val end: Int,
    val text: String,
)

/**
 * Shared, PSI-free tokenizer that runs [RescriptLexer] over a source string
 * and returns the non-trivia tokens.
 *
 * Multiple lexer-walking analyzers (switch arm collector, nested-switch
 * flattener, merge/split intentions) need the same "tokenize, dropping
 * whitespace and comments" primitive; centralising it here keeps their
 * behavior identical and avoids duplicating the trivia filter.
 *
 * @see LexedToken for the record shape emitted by [tokenize]
 */
object RescriptTokenScanner {
    /**
     * Tokenizes [source] through [RescriptLexer], discarding trivia
     * (whitespace, end-of-line, and comment tokens per [isIgnorable]).
     *
     * @param source full text to tokenize
     * @return the significant tokens in source order
     */
    fun tokenize(source: String): List<LexedToken> {
        val lexer = RescriptLexer()
        lexer.start(source)
        val tokens = mutableListOf<LexedToken>()
        while (lexer.tokenType != null) {
            val type = lexer.tokenType!!
            if (!isIgnorable(type)) {
                tokens.add(LexedToken(type, lexer.tokenStart, lexer.tokenEnd, lexer.tokenText))
            }
            lexer.advance()
        }
        return tokens
    }

    /**
     * Reports whether [type] is trivia that [tokenize] filters out:
     * whitespace, end-of-line, single-line comments, and block comments.
     *
     * @param type the token type to classify
     * @return `true` if the token carries no structural meaning
     */
    fun isIgnorable(type: IElementType): Boolean =
        type == TokenType.WHITE_SPACE ||
            type == RescriptTokenTypes.EOL ||
            type == RescriptTokenTypes.SINGLE_COMMENT ||
            type == RescriptTokenTypes.MULTI_COMMENT
}
