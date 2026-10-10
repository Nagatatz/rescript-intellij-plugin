package com.rescript.plugin.refactor

import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.rescript.plugin.lang.RescriptLexer
import com.rescript.plugin.lang.RescriptTokenTypes as T

/** A validated extraction whose references resolve to supported lexical bindings. */
internal data class RescriptExtractFunctionPlan(
    val name: String,
    val body: String,
    val parameters: List<String>,
    val insertOffset: Int,
    val indent: String,
    val topLevel: Boolean,
) {
    /** One significant token and its enclosing brace ancestry. */
    private data class Token(
        val type: IElementType,
        val start: Int,
        val end: Int,
        val scope: List<Int>,
    )

    /** A declaration's lexical identity, not merely its spelling. */
    private data class Binding(
        val name: String,
        val position: Int,
        val visibleFrom: Int,
        val scope: List<Int>,
        val argumentSafe: Boolean,
        val parameter: Boolean = false,
    )

    companion object {
        /**
         * Resolves selected references and validates insertion without modifying the document.
         *
         * @param text the complete source snapshot
         * @param start inclusive selection offset
         * @param end exclusive selection offset
         * @return a safe plan, or null when binding or selection syntax is unresolved
         */
        fun create(
            text: String,
            start: Int,
            end: Int,
        ): RescriptExtractFunctionPlan? {
            if (start < 0 || end > text.length || start >= end) return null
            val tokens = mutableListOf<Token>()
            val scope = mutableListOf<Int>()
            val stack = mutableListOf<Int>()
            val pairs = mutableMapOf<Int, Int>()
            var nextScope = 0
            val privateAnnotations = Regex("@private[ \t]*\\r?\\n[ \t]*let\\b").findAll(text).toList()
            val lexer = RescriptLexer()
            lexer.start(text)
            while (lexer.tokenType != null) {
                val type = lexer.tokenType!!
                val from = lexer.tokenStart
                val to = lexer.tokenEnd
                val raw = text.substring(from, to)
                val privateMarker =
                    (type == T.ARROBASE || type == T.ANNOTATION_NAME) &&
                        privateAnnotations.any { from in it.range }
                if ((type in unsupported && !privateMarker) || type == TokenType.BAD_CHARACTER) return null
                if (type == T.STRING_VALUE && !closedString(raw)) return null
                if (type == T.MULTI_COMMENT && !closedComment(raw)) return null
                val commentLineEnd =
                    type == T.SINGLE_COMMENT && end in from until to &&
                        text.substring(end, to).all { it == '\n' || it == '\r' }
                if ((from < start && to > start) || (from < end && to > end && !commentLineEnd)) {
                    if (type != TokenType.WHITE_SPACE && type != T.EOL) return null
                }
                if (type == T.RBRACE) {
                    if (scope.isEmpty()) return null
                    scope.removeLast()
                }
                if (type != TokenType.WHITE_SPACE && type != T.EOL && !T.COMMENTS.contains(type)) {
                    val index = tokens.size
                    tokens.add(Token(type, from, to, scope.toList()))
                    if (type in openings) stack.add(index)
                    if (type in closings) {
                        val open = stack.removeLastOrNull() ?: return null
                        if (closings[type] != tokens[open].type) return null
                        pairs[open] = index
                        pairs[index] = open
                    }
                }
                if (type == T.LBRACE) scope.add(nextScope++)
                lexer.advance()
            }
            if (stack.isNotEmpty() || scope.isNotEmpty()) return null
            val selected = tokens.indices.filter { tokens[it].start >= start && tokens[it].end <= end }
            if (selected.isEmpty()) return null
            val first = selected.first()
            val last = selected.last()
            val selectedRange = first..last
            if (tokens[first].scope != tokens[last].scope) return null
            // The selection must contain every delimiter it opens or closes.
            if (selected.any { index -> pairs[index]?.let { it !in selectedRange } == true }) return null
            if (tokens.getOrNull(first - 1)?.type in setOf(T.DOT, T.TILDE, T.LET) ||
                tokens.getOrNull(last + 1)?.type in setOf(T.DOT, T.COLON, T.EQ) ||
                T.OPERATORS.contains(tokens[last].type) || tokens[last].type in setOf(T.COMMA, T.LET, T.ARROW)
            ) {
                return null
            }

            val bindings = mutableListOf<Binding>()
            val declarations = mutableSetOf<Int>()
            val blocks = mutableSetOf<List<Int>>()
            for ((index, token) in tokens.withIndex()) {
                if (token.type == T.LET) {
                    val name = tokens.getOrNull(index + 1) ?: return null
                    if (name.type != T.LIDENT || tokens.getOrNull(index + 2)?.type != T.EQ) return null
                    val spelling = text.substring(name.start, name.end)
                    if (spelling == "_") return null
                    val rhs = index + 3
                    val rhsType = tokens.getOrNull(rhs)?.type
                    // A generalized function/alias can lose polymorphism when passed as an argument.
                    // Lambda parameters, complete literals and invariant ref cells are supported.
                    // Even call results can be generalized through the relaxed value restriction.
                    val initializerEnd =
                        when (rhsType) {
                            T.INT_VALUE, T.FLOAT_VALUE, T.STRING_VALUE, T.CHAR_VALUE, T.BOOL_VALUE -> rhs
                            T.REF -> if (tokens.getOrNull(rhs + 1)?.type == T.LPAREN) pairs[rhs + 1] else null
                            else -> null
                        }
                    val safe =
                        initializerEnd?.let { final ->
                            val next = tokens.getOrNull(final + 1)
                            val lineEnd = text.indexOf('\n', tokens[final].end).let { if (it < 0) text.length else it }
                            val trailing = text.substring(tokens[final].end, lineEnd).trim()
                            (trailing.isEmpty() || trailing.startsWith("//")) &&
                                (
                                    next == null ||
                                        (
                                            !T.OPERATORS.contains(next.type) &&
                                                next.type !in setOf(T.DOT, T.LPAREN, T.LBRACKET, T.QUESTION_MARK)
                                        )
                                )
                        } == true
                    bindings.add(Binding(spelling, name.start, name.end, name.scope, safe))
                    declarations.add(index + 1)
                    blocks.add(token.scope)
                }
                if (token.type == T.ARROW) {
                    // Only block-body lambdas have a proven parameter visibility boundary.
                    val open = index + 1
                    if (tokens.getOrNull(open)?.type != T.LBRACE) return null
                    val bodyScope = tokens.getOrNull(open + 1)?.scope ?: return null
                    blocks.add(bodyScope)
                    val parameters =
                        when (tokens.getOrNull(index - 1)?.type) {
                            T.LIDENT -> {
                                listOf(index - 1)
                            }

                            T.RPAREN -> {
                                val parameterOpen = pairs[index - 1] ?: return null
                                val indices = (parameterOpen + 1 until index - 1).toList()
                                if (indices.any { tokens[it].type !in setOf(T.LIDENT, T.COMMA) }) return null
                                if (indices.isNotEmpty() &&
                                    (
                                        indices.size % 2 == 0 ||
                                            indices.withIndex().any { (position, i) ->
                                                tokens[i].type != if (position % 2 == 0) T.LIDENT else T.COMMA
                                            }
                                    )
                                ) {
                                    return null
                                }
                                indices.filter { tokens[it].type == T.LIDENT }
                            }

                            else -> {
                                return null
                            }
                        }
                    for (parameter in parameters) {
                        if (parameter in selectedRange && index !in selectedRange) return null
                        val p = tokens[parameter]
                        bindings.add(
                            Binding(text.substring(p.start, p.end), p.start, tokens[open].end, bodyScope, true, true),
                        )
                        declarations.add(parameter)
                    }
                }
            }
            val byName = bindings.groupBy { it.name }
            for (group in byName.values) {
                if (group.map { it.scope }.toSet().size != group.size) return null
                val declarationsWithName = group.filter { !it.parameter }
                if (declarationsWithName.size > 1) return null
                val declarationWithName = declarationsWithName.singleOrNull() ?: continue
                if (group.any {
                        it.parameter && (
                            it.position < declarationWithName.position ||
                                it.scope.take(declarationWithName.scope.size) != declarationWithName.scope
                        )
                    }
                ) {
                    return null
                }
            }
            val references = mutableListOf<Pair<Int, Binding>>()
            for ((index, token) in tokens.withIndex()) {
                if (token.type != T.LIDENT || index in declarations) continue
                val previous = tokens.getOrNull(index - 1)?.type
                val next = tokens.getOrNull(index + 1)?.type
                if (previous == T.DOT) continue
                if (previous == T.TILDE) {
                    if (next != T.EQ) return null // A shorthand label also reads a value, including escaped locals.
                    continue
                }
                if (next == T.COLON) continue // Record labels are not value reads.
                val spelling = text.substring(token.start, token.end)
                if (spelling == "eval") return null
                if (token.scope.isNotEmpty() && token.scope !in blocks) return null // Record shorthand/pattern.
                val resolved =
                    byName[spelling]
                        ?.filter {
                            it.visibleFrom <= token.start &&
                                token.scope.take(it.scope.size) == it.scope
                        }?.maxByOrNull { it.scope.size }
                if (index in selectedRange && resolved == null) return null
                if (resolved != null) references.add(index to resolved)
            }
            val local = bindings.filter { it.position in start until end }.toSet()
            // Removing a top-level declaration could break references in another module.
            if (local.any { it.scope.isEmpty() }) return null
            if (references.any { (index, binding) -> binding in local && index !in selectedRange }) return null
            if (references.any { (index, binding) ->
                    index in selectedRange && binding !in local &&
                        !binding.argumentSafe
                }
            ) {
                return null
            }
            val parameters =
                references
                    .filter { (index, binding) -> index in selectedRange && binding !in local }
                    .map { it.second.name }
                    .distinct()
                    .sorted()

            val firstLine = text.lastIndexOf('\n', (start - 1).coerceAtLeast(-1)) + 1
            val prefix = text.substring(firstLine, start)
            val indent = prefix.takeWhile { it == ' ' || it == '\t' }
            val lineTokens = tokens.filter { it.start in firstLine until start }
            // Never insert a function outside the block containing the selected expression.
            if (lineTokens.any {
                    it.scope != tokens[first].scope || it.type == T.ARROW || it.type == T.LBRACE
                }
            ) {
                return null
            }
            val base = "extractedFunction"
            val names = tokens.filter { it.type == T.LIDENT }.map { text.substring(it.start, it.end) }.toSet()
            var name = base
            var suffix = 2
            while (name in names) name = base + suffix++
            return RescriptExtractFunctionPlan(
                name,
                text.substring(start, end),
                parameters,
                firstLine,
                indent,
                tokens[first].scope.isEmpty(),
            )
        }

        private val openings = setOf(T.LPAREN, T.LBRACKET, T.LBRACE)
        private val closings = mapOf(T.RPAREN to T.LPAREN, T.RBRACKET to T.LBRACKET, T.RBRACE to T.LBRACE)
        private val unsupported =
            setOf(
                T.TYPE,
                T.MODULE,
                T.OPEN,
                T.INCLUDE,
                T.EXTERNAL,
                T.REC,
                T.SWITCH,
                T.TRY,
                T.FOR,
                T.WHILE,
                T.JS_STRING_OPEN,
                T.RAW,
                T.FFI,
                T.ARROBASE,
                T.ANNOTATION_NAME,
                T.TAG_LT,
                T.JSX_TAG_NAME,
            )

        /** Checks that a string terminates with an unescaped quote. */
        private fun closedString(raw: String): Boolean {
            if (raw.length < 2 || !raw.endsWith('"')) return false
            val slashes = raw.dropLast(1).takeLastWhile { it == '\\' }.length
            return slashes % 2 == 0
        }

        /** Refuses incomplete nested block comments. */
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
