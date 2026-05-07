package com.rescript.plugin.narrowing

import com.intellij.openapi.util.TextRange
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.rescript.plugin.lang.RescriptLexer
import com.rescript.plugin.lang.RescriptTokenTypes

/**
 * Information about a single arm of a `switch` expression that the
 * type-narrowing visualizer needs in order to anchor an inlay hint
 * and resolve the narrowed type via LSP hover.
 *
 * @property scrutineeRange text range of the expression being matched
 *   (the `X` in `switch X { ... }`); used as the LSP hover offset
 * @property patternOffset offset just after the leading `|` of this arm
 * @property arrowOffset offset just after the arm's `=>` token; the
 *   inlay hint is anchored here
 * @property patternSummary short human-readable summary of the pattern
 *   (e.g. `Some(_)`, `None`, `[]`); used as a tooltip prefix
 */
data class SwitchArm(
    val scrutineeRange: TextRange,
    val patternOffset: Int,
    val arrowOffset: Int,
    val patternSummary: String,
)

/**
 * Locates every `switch` arm in a ReScript source file by walking the
 * lexer output directly, without relying on PSI.
 *
 * The existing [com.rescript.plugin.lang.RescriptParser] does not model
 * `switch` expressions in the PSI tree (expression-level constructs are
 * delegated to LSP). To keep the parser untouched, this collector
 * tokenises the source through [RescriptLexer] and tracks switch
 * boundaries with a small state machine that handles nested switches,
 * paren depth in scrutinees, and `|` characters used inside or-patterns.
 *
 * The function is pure (`String → List<SwitchArm>`), so it is trivially
 * cacheable and unit-testable.
 */
object RescriptSwitchArmCollector {
    private const val PATTERN_SUMMARY_MAX_LENGTH = 32

    /**
     * Scans [source] and returns one [SwitchArm] entry per arm of every
     * `switch` expression encountered (including nested switches).
     *
     * Arms whose `=>` is missing (e.g. while the user is still typing)
     * are skipped silently rather than producing partial entries.
     *
     * @param source full text of the file
     * @return arms in source order
     */
    fun collect(source: String): List<SwitchArm> {
        val tokens = tokenize(source)
        val arms = mutableListOf<SwitchArm>()
        var i = 0
        while (i < tokens.size) {
            if (tokens[i].type == RescriptTokenTypes.SWITCH) {
                i = processSwitch(tokens, i, arms)
            } else {
                i++
            }
        }
        return arms
    }

    private fun tokenize(source: String): List<LexedToken> {
        val lexer = RescriptLexer()
        lexer.start(source)
        val tokens = mutableListOf<LexedToken>()
        while (lexer.tokenType != null) {
            val type = lexer.tokenType!!
            if (!isIgnorable(type)) {
                tokens.add(LexedToken(type, lexer.tokenStart, lexer.tokenEnd, lexer.tokenText))
            }
            lexer.advance()
        }
        return tokens
    }

    private fun isIgnorable(type: IElementType): Boolean =
        type == TokenType.WHITE_SPACE ||
            type == RescriptTokenTypes.EOL ||
            type == RescriptTokenTypes.SINGLE_COMMENT ||
            type == RescriptTokenTypes.MULTI_COMMENT

    /**
     * Process a single `switch ... { ... }` block starting at index
     * [start], appending arms to [arms].
     *
     * @return the index immediately after the closing `}` (or [tokens]
     *   size if the switch is unterminated)
     */
    private fun processSwitch(
        tokens: List<LexedToken>,
        start: Int,
        arms: MutableList<SwitchArm>,
    ): Int {
        // Locate scrutinee start and the body's opening `{` (paren depth 0).
        val scrutineeStart = start + 1
        if (scrutineeStart >= tokens.size) return tokens.size

        var i = scrutineeStart
        var parenDepth = 0
        var bodyBraceIdx = -1
        while (i < tokens.size) {
            when (tokens[i].type) {
                RescriptTokenTypes.LPAREN -> {
                    parenDepth++
                }

                RescriptTokenTypes.RPAREN -> {
                    if (parenDepth > 0) parenDepth--
                }

                RescriptTokenTypes.LBRACE -> {
                    if (parenDepth == 0) {
                        bodyBraceIdx = i
                    }
                }
            }
            if (bodyBraceIdx >= 0) break
            i++
        }
        if (bodyBraceIdx < 0) return tokens.size

        val scrutineeRange =
            TextRange(tokens[scrutineeStart].start, tokens[bodyBraceIdx - 1].end)

        // Walk the body, recognising arm boundaries on `|` and `=>`.
        i = bodyBraceIdx + 1
        var braceDepth = 1
        var armPipeOffset: Int? = null
        var armPatternTokens = mutableListOf<LexedToken>()
        while (i < tokens.size) {
            val t = tokens[i]
            when (t.type) {
                RescriptTokenTypes.LBRACE -> {
                    braceDepth++
                    armPatternTokens.addIfInArm(armPipeOffset, braceDepth, t)
                }

                RescriptTokenTypes.RBRACE -> {
                    braceDepth--
                    if (braceDepth == 0) return i + 1
                    armPatternTokens.addIfInArm(armPipeOffset, braceDepth, t)
                }

                RescriptTokenTypes.SWITCH -> {
                    // Nested switch — recurse and resume after its closing `}`.
                    i = processSwitch(tokens, i, arms)
                    continue
                }

                RescriptTokenTypes.PIPE -> {
                    if (braceDepth == 1 && armPipeOffset == null) {
                        armPipeOffset = t.end
                        armPatternTokens = mutableListOf()
                    } else {
                        armPatternTokens.addIfInArm(armPipeOffset, braceDepth, t)
                    }
                }

                RescriptTokenTypes.ARROW -> {
                    if (braceDepth == 1 && armPipeOffset != null) {
                        arms.add(
                            SwitchArm(
                                scrutineeRange = scrutineeRange,
                                patternOffset = armPipeOffset,
                                arrowOffset = t.end,
                                patternSummary = summarize(armPatternTokens),
                            ),
                        )
                        armPipeOffset = null
                        armPatternTokens = mutableListOf()
                    } else {
                        armPatternTokens.addIfInArm(armPipeOffset, braceDepth, t)
                    }
                }

                else -> {
                    armPatternTokens.addIfInArm(armPipeOffset, braceDepth, t)
                }
            }
            i++
        }
        return tokens.size
    }

    private fun MutableList<LexedToken>.addIfInArm(
        armPipeOffset: Int?,
        braceDepth: Int,
        token: LexedToken,
    ) {
        if (armPipeOffset != null && braceDepth == 1) {
            add(token)
        }
    }

    /**
     * Build a short, single-line label such as `Some(_)` or `None`
     * from the pattern tokens. The result is always trimmed to at most
     * [PATTERN_SUMMARY_MAX_LENGTH] characters.
     */
    private fun summarize(patternTokens: List<LexedToken>): String {
        if (patternTokens.isEmpty()) return ""

        // Strip an optional leading `when` guard tail: anything from
        // `when` onward is excluded so the summary stays close to the
        // structural pattern itself.
        val effective = patternTokens.takeWhile { it.type != RescriptTokenTypes.WHEN }
        if (effective.isEmpty()) return patternTokens.first().text.trim()

        val head = effective.first()
        val needsCallShape =
            effective.size > 1 && effective[1].type == RescriptTokenTypes.LPAREN

        val raw =
            when (head.type) {
                RescriptTokenTypes.UIDENT,
                RescriptTokenTypes.SOME,
                RescriptTokenTypes.NONE,
                RescriptTokenTypes.LIDENT,
                RescriptTokenTypes.UNDERSCORE,
                RescriptTokenTypes.POLY_VARIANT,
                -> if (needsCallShape) "${head.text}(_)" else head.text

                else -> head.text.trim()
            }
        return if (raw.length <= PATTERN_SUMMARY_MAX_LENGTH) {
            raw
        } else {
            raw.substring(0, PATTERN_SUMMARY_MAX_LENGTH - 1) + "…"
        }
    }

    private data class LexedToken(
        val type: IElementType,
        val start: Int,
        val end: Int,
        val text: String,
    )
}
