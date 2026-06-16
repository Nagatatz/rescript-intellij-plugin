package com.rescript.plugin.imports

import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.rescript.plugin.lang.RescriptLexer
import com.rescript.plugin.lang.RescriptTokenTypes

/**
 * Pure-syntax extractor for the top-level member names declared by a ReScript module file.
 *
 * Given the source of a module file (`M.res` / `M.resi`), this returns the set of names a
 * sibling file could reference unqualified after `open M`: the identifiers introduced by
 * brace-depth-0 `let` / `let rec` / `type` / `module` / `external` / `exception` declarations.
 * Declarations nested inside a submodule body, a function body, or a record literal (brace
 * depth > 0) are intentionally excluded, because `open M` only brings the top-level bindings
 * of `M` into scope.
 *
 * The extraction is LSP-independent: the input is tokenized with [RescriptLexer] and the
 * brace nesting depth is tracked so that only depth-0 declaration keywords contribute a name.
 *
 * @see RescriptOpenExpansionPlanner which consumes the extracted names to plan a qualifier rewrite
 */
object RescriptModuleMemberExtractor {
    private val DECLARATION_KEYWORDS =
        setOf(
            RescriptTokenTypes.LET,
            RescriptTokenTypes.TYPE,
            RescriptTokenTypes.MODULE,
            RescriptTokenTypes.EXTERNAL,
            RescriptTokenTypes.EXCEPTION,
        )

    /** Lightweight token record retaining the element type and raw text. */
    private data class Tok(
        val type: IElementType,
        val text: String,
    )

    /**
     * Scans [text] and returns the set of brace-depth-0 declaration names.
     *
     * @param text the ReScript module source to scan
     * @return the top-level member names; empty when the module declares nothing
     */
    fun extractTopLevelNames(text: CharSequence): Set<String> {
        val tokens = lexNonTrivia(text)
        val names = mutableSetOf<String>()
        var depth = 0
        var i = 0
        while (i < tokens.size) {
            val tok = tokens[i]
            when (tok.type) {
                RescriptTokenTypes.LBRACE, RescriptTokenTypes.LPAREN, RescriptTokenTypes.LBRACKET -> {
                    depth++
                }

                RescriptTokenTypes.RBRACE, RescriptTokenTypes.RPAREN, RescriptTokenTypes.RBRACKET -> {
                    if (depth > 0) depth--
                }

                else -> {
                    if (depth == 0 && tok.type in DECLARATION_KEYWORDS) {
                        val name = nameAfterKeyword(tokens, i, tok.type)
                        if (name != null) names.add(name)
                    }
                }
            }
            i++
        }
        return names
    }

    /**
     * Resolves the declared name immediately following a depth-0 declaration keyword at index
     * [keywordIndex]. Handles `let rec <name>` by skipping an optional `rec` keyword, and accepts
     * both lower-case bindings (`let` / `external`) and capitalized names (`module` / `type` /
     * `exception` may be either, so any identifier token is accepted).
     *
     * @return the declared name, or null when the keyword is not followed by an identifier
     */
    private fun nameAfterKeyword(
        tokens: List<Tok>,
        keywordIndex: Int,
        keywordType: IElementType,
    ): String? {
        var next = keywordIndex + 1
        // `let rec name` — skip the optional rec marker.
        if (keywordType == RescriptTokenTypes.LET && tokens.getOrNull(next)?.type == RescriptTokenTypes.REC) {
            next++
        }
        val candidate = tokens.getOrNull(next) ?: return null
        return if (candidate.type == RescriptTokenTypes.LIDENT || candidate.type == RescriptTokenTypes.UIDENT) {
            candidate.text
        } else {
            null
        }
    }

    /**
     * Tokenizes [text] with [RescriptLexer], discarding whitespace and comments while retaining
     * each significant token's type and text.
     */
    private fun lexNonTrivia(text: CharSequence): List<Tok> {
        val lexer = RescriptLexer()
        lexer.start(text.toString())
        val out = mutableListOf<Tok>()
        while (lexer.tokenType != null) {
            val type = lexer.tokenType!!
            if (!isIgnorable(type)) {
                out.add(Tok(type, lexer.tokenText))
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
