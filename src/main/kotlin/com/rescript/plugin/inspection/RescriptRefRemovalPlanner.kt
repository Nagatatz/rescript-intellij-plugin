package com.rescript.plugin.inspection

import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.rescript.plugin.lang.RescriptLexer
import com.rescript.plugin.lang.RescriptTokenTypes as T

/**
 * Proves a conservative local ref removal using lexer tokens and complete source snapshots.
 * Only scalar initializers in braced function bodies with direct contents reads are supported;
 * writes, box escapes, shadowing and closure boundaries are refused without semantic guesses.
 */
internal object RescriptRefRemovalPlanner {
    /** An immutable edit result that can only be applied to the exact inspected document. */
    class Plan(
        val name: String,
        val bindingOffset: Int,
        private val snapshot: String,
        private val replacement: String,
    ) {
        /** Returns the complete replacement, or null when any inspected input has changed. */
        fun replacementFor(currentText: String): String? = replacement.takeIf { currentText == snapshot }
    }

    /** Finds independently safe ref bindings; unsupported files have no proposed edits. */
    fun plans(text: String): List<Plan> {
        val source = Source.create(text) ?: return emptyList()
        // Opaque interop can access a local box through eval-like calls without a visible name token.
        if (source.tokens.any {
                it.type in setOf(T.OPEN, T.INCLUDE, T.EXTERNAL, T.PERCENT, T.ANNOTATION_NAME)
            }
        ) {
            return emptyList()
        }
        // Unqualified ref calls are the only constructor uses we can establish syntactically.
        if (source.tokens.indices.any {
                source.isRef(it) &&
                    (source.type(it - 1) in setOf(T.DOT, T.LET, T.REC, T.EXTERNAL) || source.type(it + 1) != T.LPAREN)
            }
        ) {
            return emptyList()
        }
        return source.tokens.indices.mapNotNull { source.plan(it) }
    }

    /** Significant token offsets and their nearest brace scope. */
    private data class Token(
        val type: IElementType,
        val start: Int,
        val end: Int,
        val scope: Int?,
        val parent: Int?,
    )

    /** A source-preserving replacement range. */
    private data class Edit(
        val start: Int,
        val end: Int,
        val replacement: String,
    )

    /** Balanced significant tokens with original trivia available for every edit. */
    private class Source(
        val text: String,
        val tokens: List<Token>,
        val pairs: Map<Int, Int>,
    ) {
        fun type(index: Int): IElementType? = tokens.getOrNull(index)?.type

        private fun word(index: Int): String = tokens.getOrNull(index)?.let { text.substring(it.start, it.end) } ?: ""

        // The lexer emits LIDENT for binding names, including a shadowing declaration named ref.
        fun isRef(index: Int): Boolean = type(index) in setOf(T.REF, T.LIDENT) && word(index) == "ref"

        /** Proves constructor identity, local scalar scope, and every use before collecting edits. */
        fun plan(start: Int): Plan? {
            if (type(start) != T.LET || type(start + 1) != T.LIDENT || type(start + 2) != T.EQ ||
                !isRef(start + 3) || type(start + 4) != T.LPAREN
            ) {
                return null
            }
            val scope = tokens[start].scope ?: return null
            if (tokens[start].parent != scope) return null
            if (start > scope + 1 && type(start - 1) != T.SEMI &&
                '\n' !in text.substring(tokens[start - 1].end, tokens[start].start)
            ) {
                return null
            }
            val scopeEnd = pairs[scope] ?: return null
            if (!functionBody(scope) ||
                (scope + 1 until scopeEnd).any {
                    type(it) in setOf(T.ARROW, T.MODULE, T.EXTERNAL, T.PERCENT, T.ANNOTATION_NAME)
                }
            ) {
                return null
            }
            val open = start + 4
            val close = pairs[open] ?: return null
            if (!scalar(open + 1, close - 1)) return null
            val name = word(start + 1)
            // The initializer must end at a real statement boundary, not a suffix such as ref(0).contents.
            val next = tokens.getOrNull(close + 1)
            if (next != null && next.type !in setOf(T.SEMI, T.RBRACE)) {
                if ('\n' !in text.substring(tokens[close].end, next.start)) return null
                if (next.type != T.LET && word(close + 1) != name && !callStartsAt(close + 1)) return null
            }
            if (name == "contents") return null
            val edits = mutableListOf<Edit>()
            edits +=
                Edit(
                    tokens[start + 3].start,
                    tokens[close].end,
                    text.substring(tokens[start + 3].end, tokens[open].start) +
                        text.substring(tokens[open].end, tokens[close].start),
                )
            for (index in tokens.indices) {
                if (type(index) != T.LIDENT || word(index) != name || index == start + 1) continue
                // Refuse other scopes, declarations, bare box arguments/aliases, and qualified names.
                if (index <= close || index >= scopeEnd || tokens[index].scope != scope ||
                    type(index - 1) == T.DOT || type(index + 1) != T.DOT || word(index + 2) != "contents"
                ) {
                    return null
                }
                var after = index + 3
                while (type(after) == T.RPAREN && (pairs[after] ?: Int.MAX_VALUE) < index) after++
                if (type(after) in setOf(T.EQ, T.COLON_EQ, T.LEFT_ARROW, T.DOT, T.LBRACKET, T.COLON)) return null
                if (type(after + 1) == T.EQ) return null
                if (!readBoundary(index + 2, after)) return null
                edits +=
                    Edit(
                        tokens[index].end,
                        tokens[index + 2].end,
                        text.substring(tokens[index].end, tokens[index + 1].start) +
                            text.substring(tokens[index + 1].end, tokens[index + 2].start),
                    )
            }
            val result = StringBuilder(text)
            edits.sortedByDescending { it.start }.forEach { result.replace(it.start, it.end, it.replacement) }
            return Plan(name, tokens[start].start, text, result.toString())
        }

        /**
         * Accepts only known value-read continuations after enclosing parentheses.
         *
         * @param field the contents field token
         * @param after the first token after the completed read
         * @return whether the continuation cannot be a supported mutation or unknown suffix
         */
        private fun readBoundary(
            field: Int,
            after: Int,
        ): Boolean {
            val next = tokens.getOrNull(after) ?: return false
            if (next.type in setOf(T.COMMA, T.SEMI, T.RBRACE, T.RBRACKET, T.RPAREN, T.RIGHT_ARROW)) return true
            if (next.type == T.LET) return '\n' in text.substring(tokens[field].end, next.start)
            return next.type in setOf(T.PLUS, T.MINUS, T.STAR, T.SLASH, T.EQEQ, T.EQEQEQ, T.NOT_EQ, T.NOT_EQEQ)
        }

        /** Recognizes plain lambda parameter lists without assuming unknown syntax is a function. */
        private fun functionBody(open: Int): Boolean {
            if (type(open - 1) != T.ARROW) return false
            val last = open - 2
            if (type(last) == T.LIDENT) return parameterBoundary(last)
            if (type(last) != T.RPAREN) return false
            val first = pairs[last] ?: return false
            if (!parameterBoundary(first)) return false
            if (last == first + 1) return true
            val params = (first + 1 until last).toList()
            if (params.size % 2 == 0) return false
            return params.withIndex().all { (i, token) -> type(token) == if (i % 2 == 0) T.LIDENT else T.COMMA } &&
                params
                    .filterIndexed { i, _ -> i % 2 == 0 }
                    .map(::word)
                    .distinct()
                    .size == (params.size + 1) / 2
        }

        private fun parameterBoundary(first: Int): Boolean =
            type(first - 1) in setOf(T.LPAREN, T.COMMA) ||
                (type(first - 1) == T.EQ && type(first - 2) == T.LIDENT && type(first - 3) == T.LET)

        private fun callStartsAt(start: Int): Boolean {
            if (type(start) !in setOf(T.LIDENT, T.UIDENT)) return false
            var end = start
            while (type(end + 1) == T.DOT && type(end + 2) in setOf(T.LIDENT, T.UIDENT)) end += 2
            return type(end) == T.LIDENT && type(end + 1) == T.LPAREN
        }

        /**
         * Restricts initializers to literals whose type and value survive box removal.
         *
         * @param start the first initializer token
         * @param end the last initializer token
         * @return whether the complete range is a supported scalar literal
         */
        private fun scalar(
            start: Int,
            end: Int,
        ): Boolean {
            var first = start
            var last = end
            while (type(first) == T.LPAREN && pairs[first] == last) {
                if (last == first + 1) return true
                first++
                last--
            }
            if (first > last) return false
            if (first == last) {
                return type(first) in setOf(T.INT_VALUE, T.FLOAT_VALUE, T.BOOL_VALUE, T.STRING_VALUE, T.CHAR_VALUE)
            }
            return last == first + 1 && type(first) == T.MINUS && type(last) in setOf(T.INT_VALUE, T.FLOAT_VALUE)
        }

        /** Creates source proofs only from complete lexer input with balanced delimiters. */
        companion object {
            /** Records lexical scopes while rejecting incomplete strings, comments, and delimiters. */
            fun create(text: String): Source? {
                val lexer = RescriptLexer()
                lexer.start(text)
                val tokens = mutableListOf<Token>()
                val stack = mutableListOf<Int>()
                val braces = mutableListOf<Int>()
                val pairs = mutableMapOf<Int, Int>()
                while (lexer.tokenType != null) {
                    val type = lexer.tokenType!!
                    val raw = text.substring(lexer.tokenStart, lexer.tokenEnd)
                    if (type == TokenType.BAD_CHARACTER || type == T.JS_STRING_OPEN || type == T.TAG_LT ||
                        (type == T.STRING_VALUE && !closedString(raw)) ||
                        (type == T.MULTI_COMMENT && !closedComment(raw))
                    ) {
                        return null
                    }
                    if (type != TokenType.WHITE_SPACE && type != T.EOL && !T.COMMENTS.contains(type)) {
                        val index = tokens.size
                        tokens += Token(type, lexer.tokenStart, lexer.tokenEnd, braces.lastOrNull(), stack.lastOrNull())
                        if (type in setOf(T.LPAREN, T.LBRACKET, T.LBRACE)) {
                            stack += index
                            if (type == T.LBRACE) braces += index
                        }
                        if (type in setOf(T.RPAREN, T.RBRACKET, T.RBRACE)) {
                            val open = stack.removeLastOrNull() ?: return null
                            val expected =
                                when (tokens[open].type) {
                                    T.LPAREN -> T.RPAREN
                                    T.LBRACKET -> T.RBRACKET
                                    else -> T.RBRACE
                                }
                            if (type != expected) return null
                            pairs[open] = index
                            pairs[index] = open
                            if (type == T.RBRACE) braces.removeLast()
                        }
                    }
                    lexer.advance()
                }
                return if (stack.isEmpty()) Source(text, tokens, pairs) else null
            }

            private fun closedString(raw: String): Boolean {
                if (raw.length < 2 || raw.first() != '"' || raw.last() != '"') return false
                var backslashes = 0
                var index = raw.lastIndex - 1
                while (index >= 0 && raw[index--] == '\\') backslashes++
                return backslashes % 2 == 0
            }

            private fun closedComment(raw: String): Boolean {
                var depth = 0
                var index = 0
                while (index < raw.length - 1) {
                    when (raw.substring(index, index + 2)) {
                        "/*" -> {
                            depth++
                            index += 2
                        }

                        "*/" -> {
                            depth--
                            index += 2
                        }

                        else -> {
                            index++
                        }
                    }
                    if (depth < 0) return false
                }
                return depth == 0 && raw.endsWith("*/")
            }
        }
    }
}
