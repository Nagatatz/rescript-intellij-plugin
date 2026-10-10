package com.rescript.plugin.intention

import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.rescript.plugin.lang.RescriptLexer
import com.rescript.plugin.lang.RescriptTokenTypes as T

/**
 * Proves that an opened, directly declared local module exports the same value as a qualifier.
 * A deliberately small file grammar rules out nested scopes, aliases, competing opens and
 * ambiguous declarations. Unsupported files have no edits; source trivia is retained.
 */
internal object RescriptQualifierRemovalPlanner {
    /** An immutable result that refuses any change to the inspected source. */
    class Plan(
        private val snapshot: String,
        private val replacement: String,
    ) {
        /** Returns the replacement only for the complete original source. */
        fun replacementFor(text: String): String? = replacement.takeIf { text == snapshot }
    }

    /**
     * Locates a value qualifier whose removal has a local binding-identity proof.
     *
     * @param text the complete document
     * @param offset the caret within the leading module identifier
     * @return a snapshot-bound plan, or null for unsupported or ambiguous input
     */
    fun plan(
        text: String,
        offset: Int,
    ): Plan? {
        val source = Source.create(text) ?: return null
        val program = source.program() ?: return null
        val reference = source.tokens.indexOfFirst { offset >= it.start && offset < it.end }
        if (source.type(reference) != T.UIDENT || source.type(reference + 1) != T.DOT ||
            source.type(reference + 2) != T.LIDENT || source.type(reference - 1) == T.DOT ||
            source.type(reference + 3) == T.DOT
        ) {
            return null
        }
        val name = source.word(reference)
        val member = source.word(reference + 2)
        val module = program.modules[name] ?: return null
        val opened = program.opened ?: return null
        if (opened.name != name || module.end >= opened.start || opened.end >= reference ||
            member !in module.exports || member in program.bindings || reference !in program.valueTokens
        ) {
            return null
        }
        val prefix = source.tokens[reference]
        val dot = source.tokens[reference + 1]
        val replacement = text.replaceRange(prefix.start, dot.end, text.substring(prefix.end, dot.start))
        return Plan(text, replacement)
    }

    /** A significant lexer token with its original source range. */
    private data class Token(
        val type: IElementType,
        val start: Int,
        val end: Int,
    )

    /** A direct local module with unique, scalar value exports. */
    private data class Module(
        val end: Int,
        val exports: Set<String>,
    )

    /** A direct open and its exact token extent. */
    private data class Open(
        val name: String,
        val start: Int,
        val end: Int,
    )

    /** Fully consumed top-level declarations and value-expression token positions. */
    private data class Program(
        val modules: Map<String, Module>,
        val opened: Open?,
        val bindings: Set<String>,
        val valueTokens: Set<Int>,
    )

    /** Balanced tokens and a strict declaration/value grammar independent of coarse PSI ranges. */
    private class Source(
        val text: String,
        val tokens: List<Token>,
        val pairs: Map<Int, Int>,
    ) {
        fun type(index: Int): IElementType? = tokens.getOrNull(index)?.type

        fun word(index: Int): String = tokens.getOrNull(index)?.let { text.substring(it.start, it.end) } ?: ""

        /** Consumes every token in the supported file grammar, without assuming unknown RHS boundaries. */
        fun program(): Program? {
            val modules = mutableMapOf<String, Module>()
            val bindings = mutableSetOf<String>()
            val values = mutableSetOf<Int>()
            var opened: Open? = null
            var index = 0
            while (index < tokens.size) {
                val start = index
                when (type(index)) {
                    T.MODULE -> {
                        if (type(index + 1) != T.UIDENT || type(index + 2) != T.EQ ||
                            type(index + 3) != T.LBRACE
                        ) {
                            return null
                        }
                        val name = word(index + 1)
                        val close = pairs[index + 3] ?: return null
                        val exports = exports(index + 4, close) ?: return null
                        if (modules.put(name, Module(close, exports)) != null) return null
                        index = close + 1
                    }

                    T.OPEN -> {
                        if (opened != null || type(index + 1) != T.UIDENT) return null
                        opened = Open(word(index + 1), index, index + 1)
                        index += 2
                    }

                    T.LET -> {
                        if (type(index + 1) != T.LIDENT || type(index + 2) != T.EQ) return null
                        if (!bindings.add(word(index + 1))) return null
                        val end = declarationEnd(index + 3)
                        if (!expression(index + 3, end - 1)) return null
                        values.addAll(index + 3 until end)
                        index = end
                    }

                    else -> {
                        return null
                    }
                }
                if (type(index) == T.SEMI) {
                    index++
                } else if (index < tokens.size && !newline(index - 1, index)) {
                    return null
                }
                if (index <= start) return null
            }
            return Program(modules, opened, bindings, values)
        }

        /**
         * Accepts direct scalar exports only, never aliases/re-exports or declaration-looking RHS text.
         *
         * @param start the first module-body token
         * @param close the closing brace token
         * @return unique exported value names, or null for an unsupported body
         */
        private fun exports(
            start: Int,
            close: Int,
        ): Set<String>? {
            val names = mutableSetOf<String>()
            var index = start
            while (index < close) {
                if (type(index) != T.LET || type(index + 1) != T.LIDENT || type(index + 2) != T.EQ ||
                    !names.add(word(index + 1))
                ) {
                    return null
                }
                val first = index + 3
                val end = scalarEnd(first) ?: return null
                index = end + 1
                if (type(index) == T.SEMI) {
                    index++
                } else if (index < close && !newline(index - 1, index)) {
                    return null
                }
            }
            return names.takeIf { index == close }
        }

        private fun declarationEnd(start: Int): Int {
            var index = start
            while (index < tokens.size) {
                if (type(index) in setOf(T.LET, T.MODULE, T.OPEN, T.SEMI)) return index
                if (type(index) in OPENERS) index = pairs[index] ?: return index
                index++
            }
            return index
        }

        /**
         * Checks complete value expressions without accepting lambdas, records, patterns or annotations.
         *
         * @param start the first expression token
         * @param end the last expression token
         * @param depth the bounded nesting depth
         * @return whether every token belongs to the supported value grammar
         */
        private fun expression(
            start: Int,
            end: Int,
            depth: Int = 0,
        ): Boolean {
            if (start > end || depth > 128) return false
            var index = termEnd(start, end, depth) ?: return false
            while (index < end) {
                if (type(index + 1) !in OPERATORS) return false
                index = termEnd(index + 2, end, depth) ?: return false
            }
            return index == end
        }

        /**
         * Consumes an atom and optional call argument lists while retaining original syntax.
         *
         * @param start the first atom token
         * @param end the last allowed token
         * @param depth the bounded nesting depth
         * @return the atom/call end token, or null for unsupported input
         */
        private fun termEnd(
            start: Int,
            end: Int,
            depth: Int,
        ): Int? {
            if (start > end) return null
            var index = start
            when (type(index)) {
                in LITERALS -> {}

                T.LIDENT -> {}

                T.UIDENT -> {
                    while (type(index + 1) == T.DOT && type(index + 2) == T.UIDENT) index += 2
                    if (type(index + 1) != T.DOT || type(index + 2) != T.LIDENT) return null
                    index += 2
                }

                T.LPAREN -> {
                    val close = pairs[index] ?: return null
                    if (close > end || (close != index + 1 && !expression(index + 1, close - 1, depth + 1))) return null
                    index = close
                }

                else -> {
                    return null
                }
            }
            while (type(index + 1) == T.LPAREN) {
                val open = index + 1
                val close = pairs[open] ?: return null
                if (close > end) return null
                var arg = open + 1
                var cursor = arg
                while (cursor < close) {
                    if (type(cursor) in OPENERS) cursor = pairs[cursor] ?: return null
                    if (type(cursor) == T.COMMA) {
                        if (!expression(arg, cursor - 1, depth + 1)) return null
                        arg = cursor + 1
                    }
                    cursor++
                }
                if (arg < close && !expression(arg, close - 1, depth + 1)) return null
                if (arg == close && close != open + 1) return null
                index = close
            }
            return index.takeIf { it <= end }
        }

        private fun scalarEnd(start: Int): Int? {
            if (type(start) in LITERALS) return start
            if (type(start) == T.MINUS && type(start + 1) in setOf(T.INT_VALUE, T.FLOAT_VALUE)) return start + 1
            if (type(start) == T.LPAREN && pairs[start] == start + 1) return start + 1
            return null
        }

        /**
         * Checks whether two declaration boundary tokens are separated by a real source newline.
         *
         * @param before the preceding significant token
         * @param after the next significant token
         * @return whether the original trivia contains a newline
         */
        private fun newline(
            before: Int,
            after: Int,
        ): Boolean = before >= 0 && '\n' in text.substring(tokens[before].end, tokens[after].start)

        /** Builds paired tokens while validating lexical completeness. */
        companion object {
            private val OPENERS = setOf(T.LPAREN, T.LBRACKET, T.LBRACE)
            private val LITERALS = setOf(T.INT_VALUE, T.FLOAT_VALUE, T.STRING_VALUE, T.CHAR_VALUE, T.BOOL_VALUE)
            private val OPERATORS =
                setOf(T.PLUS, T.MINUS, T.STAR, T.SLASH, T.EQEQ, T.EQEQEQ, T.NOT_EQ, T.NOT_EQEQ)

            /** Tokenizes the complete file and pairs delimiters without counting comment/string trivia. */
            fun create(text: String): Source? {
                val lexer = RescriptLexer()
                lexer.start(text)
                val tokens = mutableListOf<Token>()
                val pairs = mutableMapOf<Int, Int>()
                val stack = mutableListOf<Int>()
                while (lexer.tokenType != null) {
                    val type = lexer.tokenType!!
                    val raw = text.substring(lexer.tokenStart, lexer.tokenEnd)
                    if (type == TokenType.BAD_CHARACTER ||
                        (type == T.STRING_VALUE && !closedString(raw)) ||
                        (type == T.MULTI_COMMENT && !closedComment(raw))
                    ) {
                        return null
                    }
                    if (type != TokenType.WHITE_SPACE && type != T.EOL && !T.COMMENTS.contains(type)) {
                        val index = tokens.size
                        tokens += Token(type, lexer.tokenStart, lexer.tokenEnd)
                        if (type in OPENERS) stack += index
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
                        }
                    }
                    lexer.advance()
                }
                return if (stack.isEmpty()) Source(text, tokens, pairs) else null
            }

            /** Rejects an unfinished quoted literal, including an escaped closing quote. */
            private fun closedString(raw: String): Boolean {
                if (raw.length < 2 || raw.first() != '"' || raw.last() != '"') return false
                var escaped = 0
                var index = raw.lastIndex - 1
                while (index >= 0 && raw[index--] == '\\') escaped++
                return escaped % 2 == 0
            }

            /** Verifies nested comment termination independently of lexer recovery behavior. */
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
