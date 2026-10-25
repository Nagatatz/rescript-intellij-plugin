package com.rescript.plugin.refactor

import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.rescript.plugin.lang.RescriptLexer
import com.rescript.plugin.lang.RescriptTokenTypes as T

/**
 * A conservative edit plan for a single-line constant binding.
 *
 * The lexer supplies reference boundaries, function parameter positions and block ancestry.
 * Ambiguous patterns, opens, modules, templates and rebinding are rejected until semantic
 * reference resolution is available. No expression with variable reads, calls or
 * potentially throwing division is moved or duplicated.
 */
internal data class RescriptInlinePlan(
    val name: String,
    val value: String,
    val declarationStart: Int,
    val declarationEnd: Int,
    val usages: List<Int>,
    val removeDeclaration: Boolean,
) {
    /** One significant lexer token together with its enclosing block ancestry. */
    private data class Token(
        val type: IElementType,
        val start: Int,
        val end: Int,
        val scope: List<Int>,
    )

    companion object {
        private val declaration = Regex("""^\s*let\s+([a-z_][a-zA-Z0-9_']*)\s*=\s*(.+)$""")
        private val unsupported =
            setOf(
                T.TYPE,
                T.MODULE,
                T.OPEN,
                T.INCLUDE,
                T.EXTERNAL,
                T.REC,
                T.RAW,
                T.FFI,
                T.JS_STRING_OPEN,
                T.SWITCH,
                T.FOR,
                T.WHILE,
                T.TRY,
                T.ANNOTATION_NAME,
                T.ARROBASE,
                T.TAG_LT,
                T.JSX_TAG_NAME,
                T.JSX_COMPONENT_NAME,
                TokenType.BAD_CHARACTER,
            )
        private val literals = setOf(T.INT_VALUE, T.FLOAT_VALUE, T.STRING_VALUE, T.CHAR_VALUE, T.BOOL_VALUE)
        private val arithmetic = setOf(T.PLUS, T.MINUS, T.STAR, T.PLUSDOT, T.MINUSDOT, T.STARDOT, T.STRING_CONCAT)

        /**
         * Validates a declaration and constructs reference edits without changing text.
         *
         * @param text the complete document snapshot
         * @param offset a position on the declaration line
         * @return a safe constant plan, or null for unsupported or ambiguous input
         */
        fun create(
            text: String,
            offset: Int,
        ): RescriptInlinePlan? {
            if (offset !in 0..text.length) return null
            val start = text.lastIndexOf('\n', (offset - 1).coerceAtLeast(-1)) + 1
            val end = text.indexOf('\n', offset).let { if (it < 0) text.length else it }
            val match = declaration.matchEntire(text.substring(start, end)) ?: return null
            val name = match.groupValues[1]
            if (name == "_") return null
            val value = match.groupValues[2].trim()
            val allTokens = lex(text) ?: return null
            // Moving a trailing line comment would consume the replacement's closing parenthesis.
            if (allTokens.any { T.COMMENTS.contains(it.type) && it.start < end && it.end > start }) return null
            val tokens = allTokens.filter { !T.COMMENTS.contains(it.type) }
            if (tokens.any { it.type in unsupported }) return null
            if (tokens.any { it.type == T.LIDENT && text.substring(it.start, it.end) == "eval" }) return null
            val parameters = parameterPositions(tokens) ?: return null
            if (tokens.withIndex().any { (index, token) ->
                    token.type == T.LET &&
                        (tokens.getOrNull(index + 1)?.type != T.LIDENT || tokens.getOrNull(index + 2)?.type != T.EQ)
                }
            ) {
                return null
            }
            val line = tokens.filter { it.start in start until end }
            if (line.any { it.end > end }) return null
            if (line.size < 4 || line[0].type != T.LET || line[1].type != T.LIDENT || line[2].type != T.EQ) return null
            if (!constantExpression(line.drop(3))) return null
            val declarationToken = line[1]
            val names =
                tokens.withIndex().filter { (_, token) ->
                    token.type == T.LIDENT &&
                        text.substring(token.start, token.end) == name
                }
            // A second spelling in a binding position requires semantic resolution.
            if (names.count { (index, _) -> tokens.getOrNull(index - 1)?.type == T.LET } != 1) return null
            val after = tokens.firstOrNull { it.start >= end }
            if (after != null &&
                (T.OPERATORS.contains(after.type) || after.type in setOf(T.DOT, T.LPAREN, T.COMMA, T.COLON, T.SEMI))
            ) {
                return null
            }
            val blockScopes = tokens.filter { it.type == T.LET }.map { it.scope }.toMutableSet()
            tokens
                .withIndex()
                .filter { (index, token) ->
                    token.type == T.LBRACE && tokens.getOrNull(index - 1)?.type == T.ARROW
                }.forEach { (index, _) -> tokens.getOrNull(index + 1)?.scope?.let { blockScopes.add(it) } }
            val usages = mutableListOf<Int>()
            for ((index, token) in names) {
                if (token.start <= declarationToken.start) continue
                if (token.scope.take(declarationToken.scope.size) != declarationToken.scope) continue
                if (index in parameters) return null
                val previous = tokens.getOrNull(index - 1)?.type
                val next = tokens.getOrNull(index + 1)?.type
                if (previous == T.DOT) continue // Qualified fields belong to another binding.
                if (previous == T.TILDE || next in setOf(T.EQ, T.COLON, T.COLON_EQ, T.ARROW, T.LPAREN)) return null
                // A brace without declarations could be a record shorthand or a pattern.
                if (token.scope.isNotEmpty() && token.scope !in blockScopes) return null
                usages.add(token.start)
            }
            if (usages.isEmpty()) return null
            val deleteEnd = if (end < text.length) end + 1 else end
            return RescriptInlinePlan(
                name = name,
                value = value,
                declarationStart = start,
                declarationEnd = deleteEnd,
                usages = usages,
                removeDeclaration = declarationToken.scope.isNotEmpty(),
            )
        }

        /**
         * Locates possible parameter bindings, refusing unmatched or unknown function syntax.
         *
         * @param tokens significant source tokens
         * @return parameter token indices, or null if delimiters or parameter forms are unresolved
         */
        private fun parameterPositions(tokens: List<Token>): Set<Int>? {
            val closing = mapOf(T.RPAREN to T.LPAREN, T.RBRACKET to T.LBRACKET, T.RBRACE to T.LBRACE)
            val stack = mutableListOf<Int>()
            val matched = mutableMapOf<Int, Int>()
            for ((index, token) in tokens.withIndex()) {
                when {
                    token.type in closing.values -> {
                        stack.add(index)
                    }

                    token.type in closing -> {
                        val open = stack.removeLastOrNull() ?: return null
                        if (tokens[open].type != closing[token.type]) return null
                        matched[index] = open
                    }
                }
            }
            if (stack.isNotEmpty()) return null
            val result = mutableSetOf<Int>()
            for ((index, token) in tokens.withIndex()) {
                if (token.type != T.ARROW) continue
                when (tokens.getOrNull(index - 1)?.type) {
                    T.LIDENT -> {
                        result.add(index - 1)
                    }

                    T.RPAREN -> {
                        val open = matched[index - 1] ?: return null
                        for (parameter in open + 1 until index - 1) {
                            if (tokens[parameter].type == T.LIDENT) result.add(parameter)
                        }
                    }

                    else -> {
                        return null
                    }
                }
            }
            return result
        }

        /**
         * Tokenizes a snapshot while retaining balanced brace ancestry.
         *
         * @param text source text to inspect
         * @return non-whitespace tokens, or null for unbalanced blocks
         */
        private fun lex(text: String): List<Token>? {
            val result = mutableListOf<Token>()
            val scope = mutableListOf<Int>()
            var nextScope = 0
            val lexer = RescriptLexer()
            lexer.start(text)
            while (lexer.tokenType != null) {
                val type = lexer.tokenType!!
                if (type == T.RBRACE) {
                    if (scope.isEmpty()) return null
                    scope.removeAt(scope.lastIndex)
                }
                if (type != TokenType.WHITE_SPACE && type != T.EOL) {
                    result.add(Token(type, lexer.tokenStart, lexer.tokenEnd, scope.toList()))
                }
                if (type == T.LBRACE) scope.add(nextScope++)
                lexer.advance()
            }
            return result.takeIf { scope.isEmpty() }
        }

        /**
         * Recognizes literal-only arithmetic with balanced parentheses.
         *
         * @param tokens the right-hand side tokens
         * @return whether moving or duplicating the expression is safe
         */
        private fun constantExpression(tokens: List<Token>): Boolean {
            var depth = 0
            var expectsValue = true
            for (token in tokens) {
                when {
                    token.type == T.LPAREN && expectsValue -> depth++
                    token.type == T.RPAREN && !expectsValue && depth > 0 -> depth--
                    token.type in literals && expectsValue -> expectsValue = false
                    token.type in setOf(T.MINUS, T.MINUSDOT) && expectsValue -> Unit
                    token.type in arithmetic && !expectsValue -> expectsValue = true
                    else -> return false
                }
            }
            return tokens.isNotEmpty() && depth == 0 && !expectsValue
        }
    }
}
