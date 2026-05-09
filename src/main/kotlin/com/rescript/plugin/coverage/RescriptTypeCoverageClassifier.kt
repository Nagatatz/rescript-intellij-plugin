package com.rescript.plugin.coverage

import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.rescript.plugin.lang.RescriptLexer
import com.rescript.plugin.lang.RescriptTokenTypes

/**
 * Classifies a single top-level `let` declaration as either having an
 * explicit type annotation on the binding name or relying on inference.
 *
 * The classifier is intentionally tokeniser-only — it does not load
 * PSI or call the language server — so it can run synchronously inside
 * a project-wide scan without inflating start-up cost. A binding is
 * called [LetCoverage.ANNOTATED] when a depth-0 `:` token appears
 * between the `let` keyword and the first depth-0 `=`, where depth is
 * incremented by `(`, `{`, `[` and decremented by their counterparts.
 *
 * Parameter-only annotations (e.g. `let f = (x: int) => x`) and
 * return-type annotations placed after the parameter list (e.g.
 * `let f = (x): int => x`) keep the binding name itself untyped and
 * are therefore reported as [LetCoverage.INFERRED]. Capturing those
 * tiers as a separate result variant is left to a future revision.
 */
object RescriptTypeCoverageClassifier {
    /**
     * Classifies [letSource] — the textual range of one `let`
     * declaration — into one of the [LetCoverage] tiers.
     *
     * The input is expected to start at (or before) the `let`
     * keyword and end at or just past the binding's `=` token.
     * Trailing body text after `=` is ignored. Inputs that don't
     * contain a `let` keyword are reported as [LetCoverage.INFERRED]
     * (defensive default).
     */
    fun classifyLet(letSource: String): LetCoverage {
        val tokens = lexNonTrivia(letSource)
        val letIndex = tokens.indexOfFirst { it.type == RescriptTokenTypes.LET }
        if (letIndex < 0) return LetCoverage.INFERRED

        var depth = 0
        var i = letIndex + 1
        while (i < tokens.size) {
            val type = tokens[i].type
            when {
                type == RescriptTokenTypes.LPAREN ||
                    type == RescriptTokenTypes.LBRACE ||
                    type == RescriptTokenTypes.LBRACKET -> depth++

                type == RescriptTokenTypes.RPAREN ||
                    type == RescriptTokenTypes.RBRACE ||
                    type == RescriptTokenTypes.RBRACKET -> depth = (depth - 1).coerceAtLeast(0)

                depth == 0 && type == RescriptTokenTypes.COLON -> return LetCoverage.ANNOTATED

                depth == 0 && type == RescriptTokenTypes.EQ -> return LetCoverage.INFERRED
            }
            i++
        }
        return LetCoverage.INFERRED
    }

    /**
     * Lightweight wrapper around a single lexer hit. Captures only the
     * element type because the classifier never needs offsets or text;
     * keeping the record minimal lets the inner loop stay branch-light.
     */
    private data class Tok(
        val type: IElementType,
    )

    private fun lexNonTrivia(source: String): List<Tok> {
        val lexer = RescriptLexer()
        lexer.start(source)
        val out = mutableListOf<Tok>()
        while (lexer.tokenType != null) {
            val type = lexer.tokenType!!
            if (!isIgnorable(type)) {
                out.add(Tok(type))
            }
            lexer.advance()
        }
        return out
    }

    private fun isIgnorable(type: IElementType): Boolean =
        type == TokenType.WHITE_SPACE ||
            type == RescriptTokenTypes.EOL ||
            type == RescriptTokenTypes.SINGLE_COMMENT ||
            type == RescriptTokenTypes.MULTI_COMMENT
}
