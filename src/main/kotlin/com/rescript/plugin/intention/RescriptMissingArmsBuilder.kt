package com.rescript.plugin.intention

import com.intellij.psi.TokenType
import com.rescript.plugin.lang.RescriptLexer
import com.rescript.plugin.lang.RescriptTokenTypes
import com.rescript.plugin.lsp.RescriptLspUtils
import com.rescript.plugin.narrowing.RescriptSwitchArmCollector
import com.rescript.plugin.narrowing.SwitchArm

/**
 * Outcome of computing a missing-arms suggestion for a `switch` expression.
 *
 * @property insertOffset the offset (just after the last existing arm body) at
 *   which the new arm text should be spliced in
 * @property insertText the verbatim text to insert; always ends in a newline
 *   so the closing `}` stays on its own line
 * @property missingNames the constructor names that were missing, in the order
 *   they appear in [insertText]
 */
data class MissingArmsResult(
    val insertOffset: Int,
    val insertText: String,
    val missingNames: List<String>,
)

/**
 * Pure helper that, given a ReScript source file and a caret offset, finds
 * the enclosing `switch` expression and computes which variant constructors
 * still need an arm.
 *
 * The object has no IDE dependencies, so it is fully unit-testable without
 * an IntelliJ fixture. The intention class
 * [RescriptAddMissingSwitchArmsIntention] is a thin wrapper that pipes LSP
 * hover results into this builder and applies the result via a write action.
 *
 * @see RescriptSwitchArmCollector for the underlying lexer-based arm walker
 * @see RescriptLspUtils.parseVariantConstructors for the constructor parser
 */
object RescriptMissingArmsBuilder {
    /** Result of scanning existing arm patterns for already-covered constructor names. */
    internal sealed class CoveredSet {
        /** A wildcard `_` or a bare LIDENT binding makes the switch exhaustive — no suggestion. */
        object WildcardSeen : CoveredSet()

        /** Set of capitalised constructor names that have an arm (or-patterns count individually). */
        data class Names(
            val names: Set<String>,
        ) : CoveredSet()
    }

    /**
     * Compute the arms that are missing from the switch enclosing [offset].
     *
     * Returns `null` if there is no enclosing switch, the switch is already
     * exhaustive (wildcard or all constructors covered), the switch is
     * unterminated, or [constructors] is empty.
     *
     * @param source the full text of the file
     * @param offset the caret offset within [source]
     * @param constructors variant constructors of the scrutinee, as parsed
     *   from an LSP hover response
     */
    fun computeMissing(
        source: String,
        offset: Int,
        constructors: List<RescriptLspUtils.VariantInfo>,
    ): MissingArmsResult? {
        if (constructors.isEmpty()) return null
        val arms = findEnclosingSwitchArms(source, offset) ?: return null
        if (arms.any { it.bodyEndOffset < 0 }) return null

        val covered =
            when (val c = extractCoveredNames(source, arms)) {
                is CoveredSet.WildcardSeen -> return null
                is CoveredSet.Names -> c.names
            }
        val missing = constructors.filter { it.name !in covered }
        if (missing.isEmpty()) return null

        val insertText = buildInsertion(source, arms, missing)
        val insertOffset = arms.last().bodyEndOffset
        return MissingArmsResult(
            insertOffset = insertOffset,
            insertText = insertText,
            missingNames = missing.map { it.name },
        )
    }

    /**
     * Returns true when [offset] lies inside the body or scrutinee of some
     * `switch` expression, regardless of whether arms are already exhaustive.
     *
     * Used by intention availability to avoid an LSP round-trip for offsets
     * that are clearly outside any switch.
     */
    fun isInsideSwitch(
        source: String,
        offset: Int,
    ): Boolean = findEnclosingSwitchArms(source, offset) != null

    /**
     * Returns true when the switch enclosing [offset] already has a wildcard
     * arm or a bare LIDENT binding arm.
     *
     * The intention should not show in this case, even before LSP hover.
     */
    fun hasWildcardArm(
        source: String,
        offset: Int,
    ): Boolean {
        val arms = findEnclosingSwitchArms(source, offset) ?: return false
        return extractCoveredNames(source, arms) is CoveredSet.WildcardSeen
    }

    /** Offset of the scrutinee's first non-whitespace character, for hover queries. */
    fun scrutineeOffset(
        source: String,
        offset: Int,
    ): Int? {
        val arms = findEnclosingSwitchArms(source, offset) ?: return null
        return arms.first().scrutineeRange.startOffset
    }

    // ── internals ──────────────────────────────────────────────────────

    /**
     * Group arms by their shared `scrutineeRange` and pick the innermost
     * group whose span contains [offset].
     *
     * The collector's `scrutineeRange` covers only the scrutinee expression
     * (the `x` in `switch x { ... }`), so we widen the lower bound to the
     * position of the preceding `switch` keyword via [findSwitchKeywordStart]
     * so a caret on the keyword itself is considered "inside" the switch.
     *
     * For nested switches, the most-deeply-nested enclosing switch is the
     * one whose scrutinee starts latest in the source, so we pick the
     * candidate with the maximum `startOffset`.
     */
    internal fun findEnclosingSwitchArms(
        source: String,
        offset: Int,
    ): List<SwitchArm>? {
        val all = RescriptSwitchArmCollector.collect(source)
        if (all.isEmpty()) return null
        val groups = all.groupBy { it.scrutineeRange }
        val candidates =
            groups.filter { (range, arms) ->
                val end = arms.maxOfOrNull { it.bodyEndOffset.coerceAtLeast(it.arrowOffset) } ?: return@filter false
                val lower = findSwitchKeywordStart(source, range.startOffset) ?: range.startOffset
                offset >= lower && offset <= end
            }
        if (candidates.isEmpty()) return null
        return candidates.maxBy { (range, _) -> range.startOffset }.value
    }

    private fun findSwitchKeywordStart(
        source: String,
        scrutineeStart: Int,
    ): Int? {
        var i = scrutineeStart - 1
        while (i >= 0 && source[i].isWhitespace()) i--
        val keywordEndExclusive = i + 1
        val start = keywordEndExclusive - 6
        if (start < 0) return null
        return if (source.regionMatches(start, "switch", 0, 6)) start else null
    }

    /**
     * Walk each arm's pattern range, re-lex the substring, and collect
     * constructor names. Bails out with [CoveredSet.WildcardSeen] as soon as
     * a wildcard or bare lowercase binding pattern is found.
     */
    internal fun extractCoveredNames(
        source: String,
        arms: List<SwitchArm>,
    ): CoveredSet {
        val names = mutableSetOf<String>()
        for (arm in arms) {
            // arrowOffset is positioned just after `=>`, so subtracting 2
            // gives the offset of the opening `=` of `=>`. Patterns end at
            // (or just before) that position; clamp to be defensive.
            val patternEnd = (arm.arrowOffset - 2).coerceAtLeast(arm.patternOffset)
            val patternText = source.substring(arm.patternOffset, patternEnd)
            when (val r = classifyPattern(patternText)) {
                is CoveredSet.WildcardSeen -> return r
                is CoveredSet.Names -> names.addAll(r.names)
            }
        }
        return CoveredSet.Names(names)
    }

    private fun classifyPattern(patternText: String): CoveredSet {
        val lexer = RescriptLexer()
        lexer.start(patternText)
        val collected = mutableSetOf<String>()
        var sawConstructorOrWildcard = false
        var sawWildcard = false
        var sawBareLident = false
        var sawNonTrivial = false // anything other than top-level LIDENT / UNDERSCORE / pipes / whitespace
        var depth = 0
        while (lexer.tokenType != null) {
            val type = lexer.tokenType
            // Stop scanning once a `when` guard begins; the rest is
            // expression code, not pattern structure.
            if (type == RescriptTokenTypes.WHEN) break
            if (!isIgnorable(type)) {
                when (type) {
                    RescriptTokenTypes.LPAREN, RescriptTokenTypes.LBRACE, RescriptTokenTypes.LBRACKET -> {
                        depth++
                        sawNonTrivial = true
                    }

                    RescriptTokenTypes.RPAREN, RescriptTokenTypes.RBRACE, RescriptTokenTypes.RBRACKET -> {
                        depth = (depth - 1).coerceAtLeast(0)
                    }

                    RescriptTokenTypes.UIDENT, RescriptTokenTypes.SOME, RescriptTokenTypes.NONE -> {
                        if (depth == 0) {
                            collected.add(lexer.tokenText)
                            sawConstructorOrWildcard = true
                        }
                    }

                    RescriptTokenTypes.POLY_VARIANT -> {
                        if (depth == 0) {
                            collected.add(lexer.tokenText)
                            sawConstructorOrWildcard = true
                        }
                    }

                    RescriptTokenTypes.UNDERSCORE -> {
                        if (depth == 0) {
                            sawWildcard = true
                            sawConstructorOrWildcard = true
                        }
                    }

                    RescriptTokenTypes.LIDENT -> {
                        if (depth == 0) {
                            sawBareLident = true
                        }
                    }

                    RescriptTokenTypes.PIPE -> { /* or-pattern separator at top level */ }

                    else -> {
                        if (depth == 0) sawNonTrivial = true
                    }
                }
            }
            lexer.advance()
        }
        // A bare top-level `_` arm makes the switch exhaustive.
        if (sawWildcard && collected.isEmpty()) return CoveredSet.WildcardSeen
        // A bare top-level LIDENT (e.g. `| any => ...`) with no constructors
        // is a binding that matches everything — also exhaustive.
        if (sawBareLident && !sawConstructorOrWildcard && !sawNonTrivial) return CoveredSet.WildcardSeen
        return CoveredSet.Names(collected)
    }

    private fun isIgnorable(type: com.intellij.psi.tree.IElementType?): Boolean =
        type == null ||
            type == TokenType.WHITE_SPACE ||
            type == RescriptTokenTypes.EOL ||
            type == RescriptTokenTypes.SINGLE_COMMENT ||
            type == RescriptTokenTypes.MULTI_COMMENT

    /**
     * Build the text to splice in just before the closing `}` of the switch.
     *
     * The new arms reuse the indentation of the first existing arm so the
     * insertion blends in with hand-written code.
     */
    internal fun buildInsertion(
        source: String,
        arms: List<SwitchArm>,
        missing: List<RescriptLspUtils.VariantInfo>,
    ): String {
        val firstArm = arms.first()
        // patternOffset points just after the leading `|`, so the `|` itself
        // sits at patternOffset - 1. Walk back to the start of that line to
        // capture the indent characters.
        val pipeOffset = (firstArm.patternOffset - 1).coerceAtLeast(0)
        val lineStart = source.lastIndexOf('\n', pipeOffset - 1).let { if (it < 0) 0 else it + 1 }
        val indent = source.substring(lineStart, pipeOffset)
        // If the existing last-arm body does not end with a newline (inline
        // switches like `switch x { | Some(v) => v }`), prepend one so the
        // inserted arms start on a fresh line.
        val lastArmEnd = arms.last().bodyEndOffset
        val needsLeadingNewline = lastArmEnd > 0 && source[lastArmEnd - 1] != '\n'
        return buildString {
            if (needsLeadingNewline) append('\n')
            for (info in missing) {
                val pat = if (info.hasPayload) "${info.name}(_)" else info.name
                append(indent).append("| ").append(pat).append(" => todo\n")
            }
        }
    }
}
