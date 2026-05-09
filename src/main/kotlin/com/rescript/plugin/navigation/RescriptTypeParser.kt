package com.rescript.plugin.navigation

/**
 * Recursive-descent parser that turns a textual ReScript type expression
 * into a [RescriptTypeAst]. The grammar is intentionally a small subset
 * of the real language — enough to parse top-level annotations like
 * `(int, string) => result<int, string>` or `option<'a> => option<'b>`.
 *
 * The parser is pure (`String → RescriptTypeAst?`) so the entire test
 * matrix runs without an IntelliJ Platform fixture. Inputs the parser
 * cannot understand (records, polymorphic variants, labeled arguments,
 * GADTs) yield `null` — callers treat that as "ineligible candidate"
 * rather than crashing.
 *
 * Grammar sketch:
 *
 * ```
 * type        := arrow
 * arrow       := primary ("=>" arrow)?     // right-associative
 * primary     := tvar
 *              | ctorOrApp
 *              | "(" ")"                   // unit
 *              | "(" type ")"              // paren / tuple
 *              | "(" type ("," type)+ ")"
 * ctorOrApp   := IDENT ("<" type ("," type)* ">")?
 * tvar        := "'" IDENT
 * returnQuery := "=>" type
 * ```
 */
object RescriptTypeParser {
    /**
     * Parses [text] as a ReScript type. Returns `null` when the input
     * cannot be tokenised or the grammar fails. A leading `=>` switches
     * the parser into "return query" mode, producing a
     * [RescriptTypeAst.ReturnQuery] node that the unifier handles
     * specially.
     */
    fun parse(text: String): RescriptTypeAst? {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return null
        val state = ParserState(trimmed)
        return try {
            if (state.peekArrow()) {
                state.consumeArrow()
                val target = parseType(state) ?: return null
                if (!state.atEnd()) return null
                RescriptTypeAst.ReturnQuery(target)
            } else {
                val ast = parseType(state) ?: return null
                if (!state.atEnd()) return null
                ast
            }
        } catch (_: ParseException) {
            null
        }
    }

    /**
     * Sentinel thrown by token consumers when the input doesn't match
     * the expected shape. The top-level [parse] catches it and returns
     * `null`, so partial / malformed inputs surface as a clean
     * "ineligible candidate" rather than a stack trace.
     */
    private class ParseException : RuntimeException()

    /**
     * Mutable cursor over the parser input. All parsing helpers operate
     * on this state instead of threading offsets through every recursive
     * call.
     */
    private class ParserState(
        val source: String,
    ) {
        var index: Int = 0

        fun atEnd(): Boolean {
            skipWhitespace()
            return index >= source.length
        }

        fun skipWhitespace() {
            while (index < source.length && source[index].isWhitespace()) index++
        }

        fun peekChar(): Char? {
            skipWhitespace()
            return if (index < source.length) source[index] else null
        }

        fun peekArrow(): Boolean {
            skipWhitespace()
            return index + 1 < source.length && source[index] == '=' && source[index + 1] == '>'
        }

        fun consumeArrow() {
            skipWhitespace()
            check(index + 1 < source.length && source[index] == '=' && source[index + 1] == '>')
            index += 2
        }

        fun consumeChar(expected: Char) {
            skipWhitespace()
            if (index >= source.length || source[index] != expected) throw ParseException()
            index++
        }

        fun tryConsumeChar(expected: Char): Boolean {
            skipWhitespace()
            if (index < source.length && source[index] == expected) {
                index++
                return true
            }
            return false
        }

        fun consumeIdent(): String {
            skipWhitespace()
            val start = index
            if (index >= source.length || !isIdentStart(source[index])) throw ParseException()
            index++
            while (index < source.length && isIdentCont(source[index])) index++
            return source.substring(start, index)
        }

        private fun isIdentStart(ch: Char): Boolean = ch.isLetter() || ch == '_'

        private fun isIdentCont(ch: Char): Boolean = ch.isLetterOrDigit() || ch == '_' || ch == '\''
    }

    private fun parseType(state: ParserState): RescriptTypeAst? {
        val from = parsePrimary(state) ?: return null
        if (state.peekArrow()) {
            state.consumeArrow()
            val to = parseType(state) ?: return null
            return RescriptTypeAst.Arrow(from, to)
        }
        return from
    }

    private fun parsePrimary(state: ParserState): RescriptTypeAst? {
        val ch = state.peekChar() ?: return null
        return when (ch) {
            '\'' -> parseTypeVar(state)
            '(' -> parseParenOrTupleOrUnit(state)
            else -> parseCtorOrApp(state)
        }
    }

    private fun parseTypeVar(state: ParserState): RescriptTypeAst {
        state.consumeChar('\'')
        val name = state.consumeIdent()
        return RescriptTypeAst.TypeVar(name)
    }

    private fun parseParenOrTupleOrUnit(state: ParserState): RescriptTypeAst? {
        state.consumeChar('(')
        if (state.tryConsumeChar(')')) {
            return RescriptTypeAst.UnitT
        }
        val first = parseType(state) ?: return null
        val rest = mutableListOf<RescriptTypeAst>()
        while (state.tryConsumeChar(',')) {
            rest.add(parseType(state) ?: return null)
        }
        state.consumeChar(')')
        return if (rest.isEmpty()) first else RescriptTypeAst.Tuple(listOf(first) + rest)
    }

    private fun parseCtorOrApp(state: ParserState): RescriptTypeAst? {
        val name = state.consumeIdent()
        if (state.tryConsumeChar('<')) {
            val args = mutableListOf<RescriptTypeAst>()
            args.add(parseType(state) ?: return null)
            while (state.tryConsumeChar(',')) {
                args.add(parseType(state) ?: return null)
            }
            state.consumeChar('>')
            return RescriptTypeAst.App(name, args)
        }
        return RescriptTypeAst.Ctor(name)
    }
}
