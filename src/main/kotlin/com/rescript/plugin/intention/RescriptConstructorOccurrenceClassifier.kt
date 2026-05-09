package com.rescript.plugin.intention

import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.rescript.plugin.lang.RescriptLexer
import com.rescript.plugin.lang.RescriptTokenTypes

/**
 * Decides how a UIDENT token at a given offset is being used in ReScript
 * source code: as a constructor invocation / arm pattern / module-qualified
 * tail, or as something else (JSX, type-position name, etc.) that the
 * variant-constructor rename must skip.
 *
 * The classifier is a pure tokeniser-only object so it can be unit-tested
 * exhaustively and re-used both from the intention's `isAvailable`
 * (single-file lookup) and from the project-wide finder (per-match
 * verdict). It deliberately does not consult the language server — this
 * is what `Shift+F6` already covers; the goal here is to give users a
 * working rename even when LSP is offline.
 */
object RescriptConstructorOccurrenceClassifier {
    /**
     * Classifies the UIDENT token covering [offset] in [source] and
     * returns the result. If [offset] does not land on a UIDENT, returns
     * [ConstructorOccurrenceKind.OTHER].
     *
     * @param source full source text of the ReScript file
     * @param offset 0-based offset within [source]
     */
    fun classifyAt(
        source: String,
        offset: Int,
    ): ConstructorOccurrenceKind {
        val tokens = lex(source)
        val index = tokens.indexOfFirst { offset >= it.start && offset < it.end }
        if (index < 0) return ConstructorOccurrenceKind.OTHER
        val token = tokens[index]
        if (token.type != RescriptTokenTypes.UIDENT) return ConstructorOccurrenceKind.OTHER

        val prev = previousNonTrivia(tokens, index)
        val next = nextNonTrivia(tokens, index)

        // `Module.Foo` — leading `.` is the strongest signal; the UIDENT is
        // the trailing constructor name regardless of what comes after it.
        if (prev?.type == RescriptTokenTypes.DOT) return ConstructorOccurrenceKind.MODULE_QUALIFIED_TAIL

        // `| Foo` / `| Foo(_)` — both `switch` arm and `type` arm.
        if (prev?.type == RescriptTokenTypes.PIPE) return ConstructorOccurrenceKind.PATTERN

        // `Foo(x)` — constructor invocation when followed by an opening paren.
        if (next?.type == RescriptTokenTypes.LPAREN) return ConstructorOccurrenceKind.CONSTRUCTOR

        // 0-ary `Foo` used in expression / argument position. Allow only after
        // a small set of "expression-starter" tokens so we don't false-match
        // type-position names like `: Foo` or JSX `<Foo />`.
        if (prev == null || prev.type in EXPRESSION_STARTERS) return ConstructorOccurrenceKind.CONSTRUCTOR

        return ConstructorOccurrenceKind.OTHER
    }

    /**
     * Tokens that, when they immediately precede a 0-ary UIDENT, are a
     * reliable signal that the UIDENT is being used as a constructor in
     * expression position. Covers `let x = Foo`, `[Foo, Bar]`, `(Foo)`,
     * `f(Foo)` argument lists, `Foo |> g`, etc.
     */
    private val EXPRESSION_STARTERS: Set<IElementType> =
        setOf(
            RescriptTokenTypes.EQ,
            RescriptTokenTypes.COMMA,
            RescriptTokenTypes.LPAREN,
            RescriptTokenTypes.LBRACKET,
            RescriptTokenTypes.LBRACE,
            RescriptTokenTypes.ARROW,
            RescriptTokenTypes.SEMI,
        )

    /**
     * Lightweight cache of one lexer hit — the classifier needs to walk
     * tokens by index to look at the previous / next non-trivia neighbours,
     * which the streaming `RescriptLexer` itself can't replay.
     */
    private data class LexedToken(
        val type: IElementType,
        val start: Int,
        val end: Int,
    )

    private fun lex(source: String): List<LexedToken> {
        val lexer = RescriptLexer()
        lexer.start(source)
        val out = mutableListOf<LexedToken>()
        while (lexer.tokenType != null) {
            out.add(LexedToken(lexer.tokenType!!, lexer.tokenStart, lexer.tokenEnd))
            lexer.advance()
        }
        return out
    }

    private fun previousNonTrivia(
        tokens: List<LexedToken>,
        index: Int,
    ): LexedToken? {
        var i = index - 1
        while (i >= 0) {
            if (!isTrivia(tokens[i].type)) return tokens[i]
            i--
        }
        return null
    }

    private fun nextNonTrivia(
        tokens: List<LexedToken>,
        index: Int,
    ): LexedToken? {
        var i = index + 1
        while (i < tokens.size) {
            if (!isTrivia(tokens[i].type)) return tokens[i]
            i++
        }
        return null
    }

    private fun isTrivia(type: IElementType): Boolean =
        type == TokenType.WHITE_SPACE ||
            type == RescriptTokenTypes.EOL ||
            type == RescriptTokenTypes.SINGLE_COMMENT ||
            type == RescriptTokenTypes.MULTI_COMMENT
}
