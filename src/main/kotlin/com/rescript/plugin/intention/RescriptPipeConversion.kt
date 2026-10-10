package com.rescript.plugin.intention

import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.rescript.plugin.lang.RescriptLexer
import com.rescript.plugin.lang.RescriptTokenTypes as T

/**
 * Locates a conservative subset of pipe/call expressions using lexer tokens and balanced
 * delimiters. Source slices retain trivia; strings and comments never become delimiters.
 * Unsupported operators, JSX/templates, incomplete delimiters and ambiguous expression
 * boundaries fail closed instead of rewriting a suffix of a larger expression.
 */
internal object RescriptPipeConversion {
    /** Finds a `->` conversion at the caret. `|>` has different semantics and is rejected. */
    fun pipe(
        text: String,
        offset: Int,
    ): RescriptConvertPipeToFunctionCallIntention.PipeExpression? {
        val source = Source.create(text) ?: return null
        if (!source.codeCaret(offset)) return null
        return source.tokens.indices
            .mapNotNull { arrow ->
                if (source.type(arrow) != T.RIGHT_ARROW) return@mapNotNull null
                val start = source.valueStart(arrow - 1) ?: return@mapNotNull null
                if (!source.leftBoundary(start) || !source.supportedExpression(start, arrow - 1)) return@mapNotNull null
                val nameEnd = source.pathEnd(arrow + 1) ?: return@mapNotNull null
                var end = nameEnd
                val open = nameEnd + 1
                val args =
                    if (source.type(open) == T.LPAREN) {
                        end = source.pairs[open] ?: return@mapNotNull null
                        if (!source.validArguments(open, end) ||
                            (end == open + 1 && source.sliceInside(open, end).isNotBlank())
                        ) {
                            return@mapNotNull null
                        }
                        source.sliceInside(open, end)
                    } else {
                        null
                    }
                if (!source.rightBoundary(end) || !source.contains(start, end, offset)) return@mapNotNull null
                val leading = text.substring(source.tokens[arrow].end, source.tokens[arrow + 1].start)
                val lhs = source.clean(leading + text.substring(source.tokens[start].start, source.tokens[arrow].start))
                val nameTrivia =
                    if (args != null) text.substring(source.tokens[nameEnd].end, source.tokens[open].start) else ""
                RescriptConvertPipeToFunctionCallIntention.PipeExpression(
                    lhs = lhs,
                    funcName = source.slice(arrow + 1, nameEnd) + nameTrivia,
                    args = args,
                    fullStart = source.tokens[start].start,
                    fullEnd = source.tokens[end].end,
                )
            }.minByOrNull { it.fullEnd - it.fullStart }
    }

    /** Finds a qualified call whose first argument is safe to move to a pipe's LHS. */
    fun call(
        text: String,
        offset: Int,
    ): RescriptConvertFunctionCallToPipeIntention.FunctionCall? {
        val source = Source.create(text) ?: return null
        if (!source.codeCaret(offset)) return null
        return source.tokens.indices
            .mapNotNull { start ->
                if (source.type(start) != T.UIDENT || !source.leftBoundary(start)) return@mapNotNull null
                val nameEnd = source.pathEnd(start) ?: return@mapNotNull null
                if (nameEnd <= start || source.type(nameEnd + 1) != T.LPAREN) return@mapNotNull null
                val open = nameEnd + 1
                val end = source.pairs[open] ?: return@mapNotNull null
                if (!source.rightBoundary(end) || !source.contains(start, end, offset)) return@mapNotNull null
                if (!source.validArguments(open, end)) return@mapNotNull null
                val ranges = source.arguments(open, end) ?: return@mapNotNull null
                val first = ranges.firstOrNull() ?: return@mapNotNull null
                if (source.valueStart(first.second) != first.first) return@mapNotNull null
                val lastDot = nameEnd - 1
                val raw = source.rawArguments(open, end) ?: return@mapNotNull null
                RescriptConvertFunctionCallToPipeIntention.FunctionCall(
                    text.substring(source.tokens[start].start, source.tokens[lastDot].start),
                    text.substring(source.tokens[lastDot].end, source.tokens[open].start),
                    source.clean(raw.first()),
                    raw.drop(1).map(source::clean),
                    source.tokens[start].start,
                    source.tokens[end].end,
                )
            }.minByOrNull { it.fullEnd - it.fullStart }
    }

    /** Splits only commas at delimiter depth zero; invalid input has no safe split. */
    fun splitArguments(text: String): List<String> {
        val source = Source.create("($text)") ?: return emptyList()
        return source.rawArguments(0, source.tokens.lastIndex) ?: emptyList()
    }

    /** A lexer token with offsets into the unmodified document. */
    private data class Token(
        val type: IElementType,
        val start: Int,
        val end: Int,
    )

    /** Significant tokens and paired delimiters, with original offsets for lossless slices. */
    private class Source(
        val text: String,
        val tokens: List<Token>,
        val pairs: Map<Int, Int>,
        val trivia: List<Token>,
    ) {
        fun type(index: Int): IElementType? = tokens.getOrNull(index)?.type

        fun slice(
            start: Int,
            end: Int,
        ): String = text.substring(tokens[start].start, tokens[end].end)

        fun sliceInside(
            open: Int,
            close: Int,
        ): String = text.substring(tokens[open].end, tokens[close].start)

        fun contains(
            start: Int,
            end: Int,
            offset: Int,
        ): Boolean = offset in tokens[start].start..tokens[end].end

        fun codeCaret(offset: Int): Boolean =
            offset in 0..text.length &&
                (trivia + tokens).none {
                    offset >= it.start && offset < it.end &&
                        (T.COMMENTS.contains(it.type) || it.type == T.STRING_VALUE || it.type == T.CHAR_VALUE)
                }

        // Keep a terminating newline when a moved slice ends with a line comment.
        fun clean(raw: String): String {
            val trimmed = raw.trim()
            val lexer = RescriptLexer()
            lexer.start(trimmed)
            var last: IElementType? = null
            while (lexer.tokenType != null) {
                last = lexer.tokenType
                lexer.advance()
            }
            return if (last == T.SINGLE_COMMENT) "$trimmed\n" else trimmed
        }

        fun pathEnd(start: Int): Int? {
            if (type(start) !in setOf(T.LIDENT, T.UIDENT)) return null
            var end = start
            while (type(end + 1) == T.DOT && type(end + 2) in setOf(T.LIDENT, T.UIDENT)) end += 2
            return if (type(end) == T.LIDENT) end else null
        }

        private fun pathStart(end: Int): Int? {
            if (type(end) !in setOf(T.LIDENT, T.UIDENT)) return null
            var start = end
            while (type(start - 1) == T.DOT && type(start - 2) in setOf(T.LIDENT, T.UIDENT)) start -= 2
            return start
        }

        fun safeGroup(
            open: Int,
            close: Int,
        ): Boolean =
            (open..close).none {
                type(it) == TokenType.BAD_CHARACTER ||
                    type(it) in
                    setOf(
                        T.JS_STRING_OPEN,
                        T.JS_STRING_CLOSE,
                        T.TAG_LT,
                        T.TAG_LT_SLASH,
                        T.TAG_GT,
                        T.PIPE_FORWARD,
                        T.QUESTION_MARK,
                        T.TILDE,
                        T.UNDERSCORE,
                    )
            }

        /**
         * Validates a deliberately limited expression grammar, not just balanced brackets.
         * Every significant token must be consumed, so adjacent values such as `a b` are
         * refused even inside calls, arrays, records or lambda bodies. Validation never
         * reprints expressions or guesses unsupported syntax.
         */
        fun supportedExpression(
            start: Int,
            end: Int,
            depth: Int = 0,
        ): Boolean {
            if (start > end || depth > 128) return false
            var i = start
            // An unparenthesized lambda spans the entire expression at this depth.
            while (i <= end) {
                if (type(i) in OPENERS) {
                    val close = pairs[i] ?: return false
                    if (close > end) return false
                    i = close
                } else if (type(i) == T.ARROW) {
                    return lambdaParameters(start, i - 1) && supportedExpression(i + 1, end, depth + 1)
                }
                i++
            }
            i = operandEnd(start, end, depth + 1) ?: return false
            while (i <= end) {
                if (type(i) == T.RIGHT_ARROW) {
                    val nameEnd = pathEnd(i + 1) ?: return false
                    if (nameEnd > end) return false
                    i = nameEnd + 1
                    if (type(i) == T.LPAREN) {
                        val close = pairs[i] ?: return false
                        if (close > end || !validArguments(i, close, depth + 1)) return false
                        i = close + 1
                    }
                } else if (type(i) in BINARY_OPERATORS) {
                    i = operandEnd(i + 1, end, depth + 1) ?: return false
                } else {
                    return false
                }
            }
            return i == end + 1
        }

        fun validArguments(
            open: Int,
            close: Int,
            depth: Int = 0,
        ): Boolean {
            if (depth > 128) return false
            val ranges = arguments(open, close) ?: return false
            return ranges.all { supportedExpression(it.first, it.second, depth + 1) }
        }

        private fun lambdaParameters(
            start: Int,
            end: Int,
        ): Boolean {
            if (start == end) return type(start) == T.LIDENT
            if (type(start) != T.LPAREN || pairs[start] != end) return false
            val parameters = arguments(start, end) ?: return false
            val names =
                parameters.map { (first, last) ->
                    if (first != last || type(first) != T.LIDENT) return false
                    slice(first, last)
                }
            return names.size == names.distinct().size
        }

        // Returns the first token after one operand, including its nested call suffixes.
        private fun operandEnd(
            start: Int,
            end: Int,
            depth: Int,
        ): Int? {
            if (start > end || depth > 128) return null
            var i = start
            if (type(i) in setOf(T.MINUS, T.PLUS, T.EXCLAMATION_MARK)) {
                return operandEnd(i + 1, end, depth + 1)
            }
            when (type(i)) {
                T.LIDENT, T.UIDENT, T.SOME, T.NONE, T.POLY_VARIANT -> {
                    i++
                    while (type(i) == T.DOT && type(i + 1) in setOf(T.LIDENT, T.UIDENT)) i += 2
                }

                T.INT_VALUE, T.FLOAT_VALUE, T.BOOL_VALUE, T.STRING_VALUE, T.CHAR_VALUE -> {
                    i++
                }

                T.LPAREN, T.LBRACKET -> {
                    val close = pairs[i] ?: return null
                    if (close > end || !validArguments(i, close, depth + 1)) return null
                    i = close + 1
                }

                T.LBRACE -> {
                    val close = pairs[i] ?: return null
                    if (close > end || !recordOrBlock(i, close, depth + 1)) return null
                    i = close + 1
                }

                else -> {
                    return null
                }
            }
            while (i <= end && type(i) == T.LPAREN) {
                val close = pairs[i] ?: return null
                if (close > end || !validArguments(i, close, depth + 1)) return null
                i = close + 1
            }
            return i.takeIf { it <= end + 1 }
        }

        private fun recordOrBlock(
            open: Int,
            close: Int,
            depth: Int,
        ): Boolean {
            val fields = arguments(open, close) ?: return false
            if (fields.isEmpty()) return true
            if (fields.size == 1 && type(fields[0].first + 1) != T.COLON) {
                return supportedExpression(fields[0].first, fields[0].second, depth + 1)
            }
            return fields.all { (first, last) ->
                when {
                    first == last -> {
                        type(first) == T.LIDENT
                    }

                    // Record field punning.
                    type(first) in setOf(T.LIDENT, T.STRING_VALUE) && type(first + 1) == T.COLON -> {
                        supportedExpression(first + 2, last, depth + 1)
                    }

                    else -> {
                        false
                    }
                }
            }
        }

        fun valueStart(
            end: Int,
            depth: Int = 0,
        ): Int? {
            if (end < 0 || depth > 128) return null
            var start =
                when (type(end)) {
                    T.RPAREN, T.RBRACKET, T.RBRACE -> {
                        val open = pairs[end] ?: return null
                        if (!safeGroup(open, end)) return null
                        if (type(open) == T.LPAREN && type(open - 1) in setOf(T.LIDENT, T.UIDENT, T.RPAREN)) {
                            valueStart(open - 1, depth + 1) ?: return null
                        } else {
                            open
                        }
                    }

                    T.LIDENT, T.UIDENT -> {
                        pathStart(end) ?: return null
                    }

                    T.INT_VALUE, T.FLOAT_VALUE, T.BOOL_VALUE, T.STRING_VALUE, T.CHAR_VALUE -> {
                        end
                    }

                    else -> {
                        return null
                    }
                }
            // Include earlier pipe stages instead of capturing just the final callee.
            if (type(start - 1) == T.RIGHT_ARROW) start = valueStart(start - 2, depth + 1) ?: return null
            return start
        }

        fun leftBoundary(
            start: Int,
            depth: Int = 0,
        ): Boolean {
            if (depth > 128) return false
            val previous = type(start - 1) ?: return true
            if (previous == T.ARROW) {
                val parameterEnd = start - 2
                val parameterStart =
                    if (type(parameterEnd) ==
                        T.RPAREN
                    ) {
                        pairs[parameterEnd] ?: return false
                    } else {
                        parameterEnd
                    }
                return lambdaParameters(parameterStart, parameterEnd) && leftBoundary(parameterStart, depth + 1)
            }
            if (previous in setOf(T.EQ, T.COMMA, T.LPAREN, T.LBRACKET, T.LBRACE, T.SEMI)) return true
            // A new declaration/statement on a new line may follow a complete expression.
            return previous in setOf(T.RPAREN, T.RBRACE, T.RBRACKET, T.LIDENT, T.INT_VALUE) &&
                text.substring(tokens[start - 1].end, tokens[start].start).contains('\n')
        }

        fun rightBoundary(end: Int): Boolean {
            val next = type(end + 1) ?: return true
            if (next in setOf(T.COMMA, T.RPAREN, T.RBRACKET, T.RBRACE, T.SEMI, T.RIGHT_ARROW)) return true
            return next in setOf(T.LET, T.TYPE, T.MODULE) &&
                text.substring(tokens[end].end, tokens[end + 1].start).contains('\n')
        }

        fun arguments(
            open: Int,
            close: Int,
        ): List<Pair<Int, Int>>? {
            if (close == open + 1) return emptyList()
            val result = mutableListOf<Pair<Int, Int>>()
            var start = open + 1
            var i = start
            while (i < close) {
                if (type(i) == T.COMMA) {
                    if (i == start) return null
                    result += start to i - 1
                    start = i + 1
                } else if (type(i) in setOf(T.LPAREN, T.LBRACKET, T.LBRACE)) {
                    val end = pairs[i] ?: return null
                    if (end >= close || !safeGroup(i, end)) return null
                    i = end
                } else if (!safeGroup(i, i)) {
                    return null
                }
                i++
            }
            if (start == close) return null // A trailing comma is intentionally unsupported.
            result += start to close - 1
            return result
        }

        fun rawArguments(
            open: Int,
            close: Int,
        ): List<String>? {
            val args = arguments(open, close) ?: return null
            return args.mapIndexed { index, range ->
                val start = if (index == 0) tokens[open].end else tokens[range.first - 1].end
                val end = if (index == args.lastIndex) tokens[close].start else tokens[range.second + 1].start
                text.substring(start, end)
            }
        }

        companion object {
            private val OPENERS = setOf(T.LPAREN, T.LBRACKET, T.LBRACE)
            private val BINARY_OPERATORS =
                setOf(
                    T.PLUS,
                    T.MINUS,
                    T.STAR,
                    T.SLASH,
                    T.PERCENT,
                    T.PLUSDOT,
                    T.MINUSDOT,
                    T.STARDOT,
                    T.SLASHDOT,
                    T.STRING_CONCAT,
                    T.EQEQ,
                    T.EQEQEQ,
                    T.NOT_EQ,
                    T.NOT_EQEQ,
                    T.L_AND,
                    T.L_OR,
                    T.LT,
                    T.GT,
                    T.LT_OR_EQUAL,
                )

            private fun closedString(raw: String): Boolean {
                if (raw.length < 2 || !raw.startsWith('"') || !raw.endsWith('"')) return false
                var slashes = 0
                var i = raw.lastIndex - 1
                while (i >= 0 && raw[i] == '\\') {
                    slashes++
                    i--
                }
                return slashes % 2 == 0
            }

            private fun closedComment(raw: String): Boolean {
                var depth = 0
                var i = 0
                while (i < raw.length - 1) {
                    when (raw.substring(i, i + 2)) {
                        "/*" -> {
                            depth++
                            i += 2
                        }

                        "*/" -> {
                            depth--
                            i += 2
                        }

                        else -> {
                            i++
                        }
                    }
                    if (depth < 0) return false
                }
                return depth == 0 && raw.endsWith("*/")
            }

            fun create(text: String): Source? {
                val lexer = RescriptLexer()
                lexer.start(text)
                val tokens = mutableListOf<Token>()
                val trivia = mutableListOf<Token>()
                val stack = mutableListOf<Int>()
                val pairs = mutableMapOf<Int, Int>()
                while (lexer.tokenType != null) {
                    val type = lexer.tokenType!!
                    val token = Token(type, lexer.tokenStart, lexer.tokenEnd)
                    val raw = text.substring(token.start, token.end)
                    if (type == TokenType.BAD_CHARACTER ||
                        (type == T.STRING_VALUE && !closedString(raw)) ||
                        (type == T.MULTI_COMMENT && !closedComment(raw))
                    ) {
                        return null
                    }
                    if (type == TokenType.WHITE_SPACE || type == T.EOL || T.COMMENTS.contains(type)) {
                        trivia += token
                    } else {
                        val index = tokens.size
                        tokens += token
                        if (type in setOf(T.LPAREN, T.LBRACKET, T.LBRACE)) stack += index
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
                        }
                    }
                    lexer.advance()
                }
                return if (stack.isEmpty()) Source(text, tokens, pairs, trivia) else null
            }
        }
    }
}
