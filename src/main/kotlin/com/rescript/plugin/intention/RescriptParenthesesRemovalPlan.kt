package com.rescript.plugin.intention

import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.rescript.plugin.lang.RescriptLexer
import com.rescript.plugin.lang.RescriptTokenTypes as T

/** Immutable proof for a scalar group forming a complete simple let initializer. */
internal data class RescriptParenthesesRemovalPlan(
    val source: String,
    val open: Int,
    val close: Int,
) {
    companion object {
        /** Returns no edit unless both expression and parent context are proven. */
        fun create(
            text: String,
            caret: Int,
        ): RescriptParenthesesRemovalPlan? {
            val stream = lex(text) ?: return null
            if (stream.unsafe || !stream.codeCaret(caret)) return null
            val (open, close) = stream.enclosing(caret) ?: return null
            val tokens = stream.tokens
            if (close != open + 2 || tokens[open + 1].type !in scalars ||
                text.substring(tokens[open + 1].start, tokens[open + 1].end) == "_"
            ) {
                return null
            }
            // A bare (value) may instead be a call argument, lambda parameter or pattern.
            if (tokens.getOrNull(open - 1)?.type != T.EQ ||
                tokens.getOrNull(open - 2)?.type != T.LIDENT ||
                tokens.getOrNull(open - 3)?.type != T.LET
            ) {
                return null
            }
            val next = tokens.getOrNull(close + 1)
            if (next != null && next.type != T.RBRACE) {
                val gap = text.substring(tokens[close].end, next.start)
                if ('\n' !in gap || next.type !in setOf(T.LET, T.LIDENT, T.UIDENT)) return null
            }
            return RescriptParenthesesRemovalPlan(text, tokens[open].start, tokens[close].start)
        }

        /** Locates token delimiters rather than parenthesis characters in strings/comments. */
        fun enclosing(
            text: String,
            caret: Int,
        ): Pair<Int, Int>? {
            val stream = lex(text) ?: return null
            if (stream.unsafe || !stream.codeCaret(caret)) return null
            val range = stream.enclosing(caret) ?: return null
            return stream.tokens[range.first].start to stream.tokens[range.second].start
        }

        /** Retained helper for lexer-aware tests; malformed input is conservatively unsafe. */
        fun topLevelContains(
            text: String,
            comma: Boolean,
        ): Boolean {
            val stream = lex(text) ?: return true
            var index = 0
            while (index < stream.tokens.size) {
                val type = stream.tokens[index].type
                if (type in openers) {
                    index = (stream.pairs[index] ?: return true) + 1
                    continue
                }
                if (if (comma) type == T.COMMA else T.OPERATORS.contains(type) || type in setOf(T.LT, T.GT)) return true
                index++
            }
            return false
        }

        private val scalars = setOf(T.LIDENT, T.INT_VALUE, T.FLOAT_VALUE, T.STRING_VALUE, T.CHAR_VALUE, T.BOOL_VALUE)
        private val openers = setOf(T.LPAREN, T.LBRACKET, T.LBRACE)
        private val closers = mapOf(T.RPAREN to T.LPAREN, T.RBRACKET to T.LBRACKET, T.RBRACE to T.LBRACE)
        private val unsupported =
            setOf(
                T.TYPE,
                T.EXTERNAL,
                T.MODULE,
                T.ARROBASE,
                T.ANNOTATION_NAME,
                T.JS_STRING_OPEN,
                T.JS_STRING_CLOSE,
                T.TAG_LT,
                T.TAG_LT_SLASH,
                T.TAG_GT,
            )

        /** Offset-preserving significant token; trivia is kept separately for caret exclusion. */
        private data class Token(
            val type: IElementType,
            val start: Int,
            val end: Int,
        )

        /** Balanced tokens and literal/comment ranges from the unchanged source snapshot. */
        private data class Stream(
            val tokens: List<Token>,
            val pairs: Map<Int, Int>,
            val trivia: List<Token>,
            val unsafe: Boolean,
        ) {
            fun codeCaret(caret: Int): Boolean =
                caret >= 0 &&
                    (tokens + trivia).none {
                        caret in it.start until it.end &&
                            (T.COMMENTS.contains(it.type) || it.type in setOf(T.STRING_VALUE, T.CHAR_VALUE))
                    }

            fun enclosing(caret: Int): Pair<Int, Int>? =
                pairs.entries
                    .filter {
                        tokens[it.key].type == T.LPAREN && caret in tokens[it.key].start..tokens[it.value].start
                    }.minByOrNull { it.value - it.key }
                    ?.let { it.key to it.value }
        }

        private fun lex(text: String): Stream? {
            val lexer = RescriptLexer()
            lexer.start(text)
            val tokens = mutableListOf<Token>()
            val trivia = mutableListOf<Token>()
            val stack = mutableListOf<Int>()
            val pairs = mutableMapOf<Int, Int>()
            var unsafe = false
            while (lexer.tokenType != null) {
                val type = lexer.tokenType!!
                val token = Token(type, lexer.tokenStart, lexer.tokenEnd)
                val raw = text.substring(token.start, token.end)
                if (type == TokenType.BAD_CHARACTER ||
                    (type == T.STRING_VALUE && (raw.length < 2 || !raw.endsWith('"'))) ||
                    (type == T.CHAR_VALUE && (raw.length < 3 || !raw.endsWith('\''))) ||
                    (type == T.MULTI_COMMENT && !raw.endsWith("*/"))
                ) {
                    return null
                }
                if (type in unsupported) unsafe = true
                if (type == TokenType.WHITE_SPACE || type == T.EOL || T.COMMENTS.contains(type)) {
                    trivia.add(token)
                } else {
                    val index = tokens.size
                    tokens.add(token)
                    if (type in openers) stack.add(index)
                    if (type in closers) {
                        val open = stack.removeLastOrNull() ?: return null
                        if (tokens[open].type != closers[type]) return null
                        pairs[open] = index
                    }
                }
                lexer.advance()
            }
            if (stack.isNotEmpty()) return null
            return Stream(tokens, pairs, trivia, unsafe)
        }
    }
}
