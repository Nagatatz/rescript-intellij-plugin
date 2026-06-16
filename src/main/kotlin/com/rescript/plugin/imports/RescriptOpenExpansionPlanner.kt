package com.rescript.plugin.imports

import com.intellij.openapi.util.TextRange
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.rescript.plugin.lang.RescriptLexer
import com.rescript.plugin.lang.RescriptTokenTypes

/**
 * The concrete edits required to expand an `open M` back into qualified references.
 *
 * @property prefixInsertOffsets the offsets at which a `M.` prefix must be inserted, one per
 *   unqualified occurrence of a member of `M` that follows the open statement
 * @property openDeletionRange the range of the `open M` statement (including its trailing
 *   newline when present) that should be removed once every reference is qualified
 */
data class ExpansionPlan(
    val prefixInsertOffsets: List<Int>,
    val openDeletionRange: TextRange,
)

/**
 * Pure-syntax planner that computes how to rewrite the references brought into scope by
 * `open M` into their explicit `M.name` form so the `open` statement can be removed.
 *
 * The planner is LSP-independent. It tokenizes the source with [RescriptLexer] and, for the
 * region after the open statement, finds every bare identifier that matches one of [memberNames].
 * Occurrences that are already qualified (preceded by `.`) are skipped to avoid producing
 * `M.M.name`. To stay conservative, a member name that is re-bound locally by a `let <name>`
 * after the open statement is excluded entirely, because qualifying its other uses could change
 * the program's meaning.
 *
 * @see RescriptModuleMemberExtractor which supplies the [memberNames] set
 * @see com.rescript.plugin.intention.RescriptExpandOpenQualifierIntention which applies the plan
 */
object RescriptOpenExpansionPlanner {
    /** Lightweight token record retaining the element type, source offsets and raw text. */
    private data class Tok(
        val type: IElementType,
        val start: Int,
        val end: Int,
        val text: String,
    )

    /**
     * Computes the qualifier-insertion offsets and the open-statement deletion range for
     * expanding `open [moduleName]`.
     *
     * @param text the full source of the file containing the open statement
     * @param moduleName the module being opened (used as the inserted `moduleName.` prefix)
     * @param memberNames the top-level member names of the opened module
     * @param openStatementRange the range of the `open moduleName` statement within [text]
     * @return the plan; [ExpansionPlan.prefixInsertOffsets] is empty when nothing needs qualifying
     */
    fun plan(
        text: CharSequence,
        moduleName: String,
        memberNames: Set<String>,
        openStatementRange: TextRange,
    ): ExpansionPlan {
        val deletionRange = expandToTrailingNewline(text, openStatementRange)
        if (memberNames.isEmpty()) {
            return ExpansionPlan(emptyList(), deletionRange)
        }

        val tokens = lexNonTrivia(text)
        // Only references that appear after the open statement are in its scope.
        val scopeStart = openStatementRange.endOffset
        val shadowed = locallyShadowedNames(tokens, memberNames, scopeStart)

        val offsets = mutableListOf<Int>()
        for (idx in tokens.indices) {
            val tok = tokens[idx]
            if (tok.start < scopeStart) continue
            if (tok.type != RescriptTokenTypes.LIDENT && tok.type != RescriptTokenTypes.UIDENT) continue
            if (tok.text !in memberNames) continue
            if (tok.text in shadowed) continue
            // Skip occurrences already qualified by a preceding `.` (e.g. `Other.name`),
            // which would otherwise become `Other.M.name`.
            if (tokens.getOrNull(idx - 1)?.type == RescriptTokenTypes.DOT) continue
            offsets.add(tok.start)
        }
        return ExpansionPlan(offsets, deletionRange)
    }

    /**
     * Collects member names that are re-bound by a `let <name>` appearing at or after
     * [scopeStart]. Such names are excluded from qualification because a local binding shadows
     * the opened module's member, so rewriting the references would be unsafe.
     */
    private fun locallyShadowedNames(
        tokens: List<Tok>,
        memberNames: Set<String>,
        scopeStart: Int,
    ): Set<String> {
        val shadowed = mutableSetOf<String>()
        for (idx in tokens.indices) {
            val tok = tokens[idx]
            if (tok.start < scopeStart) continue
            if (tok.type != RescriptTokenTypes.LET) continue
            var next = idx + 1
            if (tokens.getOrNull(next)?.type == RescriptTokenTypes.REC) next++
            val bound = tokens.getOrNull(next) ?: continue
            if (bound.type == RescriptTokenTypes.LIDENT && bound.text in memberNames) {
                shadowed.add(bound.text)
            }
        }
        return shadowed
    }

    /**
     * Extends [range] to swallow a single trailing newline (and any trailing spaces/tabs before
     * it) so removing the open statement does not leave a blank line behind.
     */
    private fun expandToTrailingNewline(
        text: CharSequence,
        range: TextRange,
    ): TextRange {
        var end = range.endOffset
        while (end < text.length && (text[end] == ' ' || text[end] == '\t')) end++
        if (end < text.length && text[end] == '\n') {
            end++
        }
        return TextRange(range.startOffset, end)
    }

    /**
     * Tokenizes [text] with [RescriptLexer], discarding whitespace and comments while retaining
     * each significant token's type, offsets and text.
     */
    private fun lexNonTrivia(text: CharSequence): List<Tok> {
        val lexer = RescriptLexer()
        lexer.start(text.toString())
        val out = mutableListOf<Tok>()
        while (lexer.tokenType != null) {
            val type = lexer.tokenType!!
            if (!isIgnorable(type)) {
                out.add(Tok(type, lexer.tokenStart, lexer.tokenEnd, lexer.tokenText))
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
