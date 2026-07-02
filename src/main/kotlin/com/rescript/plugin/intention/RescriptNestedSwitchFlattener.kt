package com.rescript.plugin.intention

import com.rescript.plugin.lang.LexedToken
import com.rescript.plugin.lang.RescriptTokenScanner
import com.rescript.plugin.lang.RescriptTokenTypes

/**
 * A single inner arm of the nested `switch`: the source text of its
 * pattern and the verbatim source text of its body.
 *
 * @property patternText the inner arm pattern (e.g. `Some(z)`, `None`)
 * @property bodyText the inner arm body, copied verbatim from source
 */
internal data class InnerArm(
    val patternText: String,
    val bodyText: String,
)

/**
 * A computed flatten transformation for a nested `switch`.
 *
 * Describes the text range of the outer arm to replace (from its leading
 * `|` through the end of its body) and the replacement text holding the
 * arms produced by folding the inner patterns into the outer pattern.
 *
 * @property replaceStart offset of the outer arm's leading `|`
 * @property replaceEnd exclusive end offset of the outer arm body
 * @property replacementText the flattened arms, newline-joined
 * @property armCount number of generated arms
 */
data class FlattenPlan(
    val replaceStart: Int,
    val replaceEnd: Int,
    val replacementText: String,
    val armCount: Int,
)

/**
 * Pure, LSP-independent analyzer that decides whether the `switch` arm at
 * the caret can be flattened with the nested `switch` in its body, and if
 * so produces the [FlattenPlan] describing the rewrite.
 *
 * The transformation collapses two `switch` levels into one by folding
 * each inner pattern into the position of the single binding introduced by
 * the outer pattern. For example
 * `| Some(y) => switch y { | Some(z) => a | None => b }`
 * becomes `| Some(Some(z)) => a` and `| Some(None) => b`.
 *
 * Like [com.rescript.plugin.narrowing.RescriptSwitchArmCollector], this
 * walks the [RescriptTokenScanner] output directly and tracks brace/paren
 * depth, so it never touches PSI and is trivially unit-testable.
 *
 * @see RescriptFlattenNestedSwitchIntention for the editor-facing wrapper
 */
object RescriptNestedSwitchFlattener {
    /**
     * Computes a [FlattenPlan] for the outer `switch` arm at [caretOffset].
     *
     * Returns `null` when the caret is not on a flattenable arm — i.e. any
     * of the availability checks (no `when` guard, exactly one binding,
     * body is solely the inner `switch`, inner scrutinee equals the outer
     * binding, no inner or-patterns/`when`) fails.
     *
     * @param source full text of the file
     * @param caretOffset caret position within [source]
     * @return the rewrite plan, or `null` if not applicable
     */
    fun plan(
        source: String,
        caretOffset: Int,
    ): FlattenPlan? {
        val tokens = RescriptTokenScanner.tokenize(source)
        val arms = collectArms(tokens)
        // The outer arm is the widest arm whose range contains the caret:
        // when the caret sits inside the nested switch, both the inner and
        // the outer arm contain it, and we always want the outer one.
        val outer =
            arms
                .filter {
                    it.bodyTokenEndIdx > 0 &&
                        caretOffset >= it.pipeStart &&
                        caretOffset <= it.bodyEndOffset
                }.maxByOrNull { it.bodyEndOffset - it.pipeStart }
                ?: return null
        return buildPlan(source, tokens, outer)
    }

    private fun buildPlan(
        source: String,
        tokens: List<LexedToken>,
        outer: ArmRecord,
    ): FlattenPlan? {
        val patternTokens = tokens.subList(outer.patternStartIdx, outer.patternEndIdx)
        if (patternTokens.isEmpty()) return null
        // Check 1: no `when` guard on the outer arm.
        if (patternTokens.any { it.type == RescriptTokenTypes.WHEN }) return null
        // Check 2: exactly one lowercase binding. A bare LIDENT catch-all
        // counts as one binding; `None`/UIDENT-only is zero, `Loaded(a, b)`
        // is two — both rejected.
        val bindings = patternTokens.filter { it.type == RescriptTokenTypes.LIDENT }
        if (bindings.size != 1) return null
        val binding = bindings.single()

        val rawBody = tokens.subList(outer.bodyTokenStartIdx, outer.bodyTokenEndIdx)
        val body = stripOuterBraces(rawBody)
        // Checks 3-5: body is solely the inner switch whose scrutinee is the
        // outer binding, and inner arms have no or-patterns or `when`.
        val innerArms = parseInnerArms(source, body, binding.text) ?: return null

        // Split the outer pattern at the binding position: prefix + <inner> + suffix.
        val prefix = source.substring(patternTokens.first().start, binding.start)
        val suffix = source.substring(binding.end, patternTokens.last().end)

        val indent = leadingIndent(source, outer.pipeStart)
        val replacementText =
            innerArms
                .mapIndexed { idx, arm ->
                    val newPattern = prefix + arm.patternText + suffix
                    // The leading indent before the first `|` is left in the
                    // document (replacement starts at the `|`); only the
                    // continuation lines need the indent prepended.
                    val linePrefix = if (idx == 0) "" else indent
                    "$linePrefix| $newPattern => ${arm.bodyText}"
                }.joinToString("\n")

        return FlattenPlan(
            replaceStart = outer.pipeStart,
            replaceEnd = tokens[outer.bodyTokenEndIdx - 1].end,
            replacementText = replacementText,
            armCount = innerArms.size,
        )
    }

    /**
     * Parses [body] as a lone inner `switch` and extracts its arms.
     *
     * @param source full file text, for slicing arm pattern/body text
     * @param body the outer arm body tokens (one redundant brace wrap
     *   already stripped by the caller)
     * @param bindingText text of the outer binding the inner scrutinee
     *   must equal
     * @return the inner arms, or `null` if [body] is not exactly an inner
     *   `switch` on the binding, or an inner arm uses an or-pattern/`when`
     */
    private fun parseInnerArms(
        source: String,
        body: List<LexedToken>,
        bindingText: String,
    ): List<InnerArm>? {
        // Check 3 (start): body must begin with `switch`.
        if (body.isEmpty() || body.first().type != RescriptTokenTypes.SWITCH) return null

        // Locate the inner switch's body `{` at paren depth 0.
        var j = 1
        var parenDepth = 0
        var braceIdx = -1
        while (j < body.size) {
            when (body[j].type) {
                RescriptTokenTypes.LPAREN -> parenDepth++
                RescriptTokenTypes.RPAREN -> if (parenDepth > 0) parenDepth--
                RescriptTokenTypes.LBRACE -> if (parenDepth == 0) braceIdx = j
            }
            if (braceIdx >= 0) break
            j++
        }
        if (braceIdx < 0) return null

        // Check 4: inner scrutinee is a single LIDENT equal to the binding.
        val scrutinee = body.subList(1, braceIdx)
        if (scrutinee.size != 1 ||
            scrutinee[0].type != RescriptTokenTypes.LIDENT ||
            scrutinee[0].text != bindingText
        ) {
            return null
        }

        // Check 3 (end): the inner switch's closing `}` is the last token.
        val closeIdx = matchBrace(body, braceIdx)
        if (closeIdx != body.size - 1) return null

        return extractArms(source, body.subList(braceIdx + 1, closeIdx))
    }

    /**
     * Extracts inner arms from the token content between the inner switch
     * braces. Rejects (returns `null`) on or-patterns or `when` guards.
     *
     * @param source full file text, for slicing arm text
     * @param content tokens between the inner switch's `{` and `}`
     * @return the inner arms, or `null` if any arm is malformed or uses an
     *   or-pattern/`when`
     */
    private fun extractArms(
        source: String,
        content: List<LexedToken>,
    ): List<InnerArm>? {
        val arms = mutableListOf<InnerArm>()
        var braceDepth = 0
        var parenDepth = 0
        var inArm = false
        var arrowSeen = false
        var patStart = -1
        var arrowIdx = -1
        var bodyStart = -1

        // Finalizes the arm currently being accumulated, slicing its
        // pattern and body text. Returns false to signal a rejection.
        fun finalize(endExclusive: Int): Boolean {
            if (!inArm) return true
            if (!arrowSeen) return false
            val patternTokens = content.subList(patStart, arrowIdx)
            if (patternTokens.isEmpty()) return false
            // Check 5: no `when` guard inside an inner arm pattern.
            if (patternTokens.any { it.type == RescriptTokenTypes.WHEN }) return false
            val patternText =
                source.substring(patternTokens.first().start, patternTokens.last().end)
            val bodyTokens = content.subList(bodyStart, endExclusive)
            if (bodyTokens.isEmpty()) return false
            val bodyText = source.substring(bodyTokens.first().start, bodyTokens.last().end)
            arms.add(InnerArm(patternText, bodyText))
            return true
        }

        var m = 0
        while (m < content.size) {
            val t = content[m]
            when (t.type) {
                RescriptTokenTypes.LBRACE -> {
                    braceDepth++
                }

                RescriptTokenTypes.RBRACE -> {
                    if (braceDepth > 0) braceDepth--
                }

                RescriptTokenTypes.LPAREN -> {
                    parenDepth++
                }

                RescriptTokenTypes.RPAREN -> {
                    if (parenDepth > 0) parenDepth--
                }

                RescriptTokenTypes.PIPE -> {
                    if (braceDepth == 0 && parenDepth == 0) {
                        when {
                            !inArm -> {
                                inArm = true
                                arrowSeen = false
                                patStart = m + 1
                            }

                            // A `|` before the arm's `=>` is an or-pattern
                            // separator — Check 5 rejects it.
                            !arrowSeen -> {
                                return null
                            }

                            else -> {
                                if (!finalize(m)) return null
                                inArm = true
                                arrowSeen = false
                                patStart = m + 1
                            }
                        }
                    }
                }

                RescriptTokenTypes.ARROW -> {
                    if (braceDepth == 0 && parenDepth == 0 && inArm && !arrowSeen) {
                        arrowSeen = true
                        arrowIdx = m
                        bodyStart = m + 1
                    }
                }

                else -> {}
            }
            m++
        }
        if (!finalize(content.size)) return null
        return arms.ifEmpty { null }
    }

    /**
     * Strips a single redundant `{ ... }` wrap around an arm body so that
     * `{ switch y { ... } }` is treated like `switch y { ... }`.
     */
    private fun stripOuterBraces(body: List<LexedToken>): List<LexedToken> =
        if (body.size >= 2 &&
            body.first().type == RescriptTokenTypes.LBRACE &&
            body.last().type == RescriptTokenTypes.RBRACE &&
            matchBrace(body, 0) == body.size - 1
        ) {
            body.subList(1, body.size - 1)
        } else {
            body
        }

    /**
     * Returns the index of the `}` that closes the `{` at [openIdx], or
     * `-1` if the braces are unbalanced.
     */
    private fun matchBrace(
        tokens: List<LexedToken>,
        openIdx: Int,
    ): Int {
        var depth = 0
        for (idx in openIdx until tokens.size) {
            when (tokens[idx].type) {
                RescriptTokenTypes.LBRACE -> {
                    depth++
                }

                RescriptTokenTypes.RBRACE -> {
                    depth--
                    if (depth == 0) return idx
                }
            }
        }
        return -1
    }

    /** Returns the whitespace prefix of the line containing [offset]. */
    private fun leadingIndent(
        source: String,
        offset: Int,
    ): String {
        val lineStart = source.lastIndexOf('\n', offset - 1) + 1
        val sb = StringBuilder()
        var k = lineStart
        while (k < offset && (source[k] == ' ' || source[k] == '\t')) {
            sb.append(source[k])
            k++
        }
        return sb.toString()
    }

    /**
     * Walks [tokens] and records every `switch` arm (across all nesting
     * levels) so the caret-containing arm can be selected by span.
     */
    private fun collectArms(tokens: List<LexedToken>): List<ArmRecord> {
        val arms = mutableListOf<ArmRecord>()
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

    /**
     * Processes a single `switch ... { ... }` starting at [start],
     * appending its arms (and any nested switches' arms) to [arms].
     *
     * @return the index immediately after the closing `}`, or the token
     *   count if the switch is unterminated
     */
    private fun processSwitch(
        tokens: List<LexedToken>,
        start: Int,
        arms: MutableList<ArmRecord>,
    ): Int {
        val scrutineeStart = start + 1
        if (scrutineeStart >= tokens.size) return tokens.size

        var i = scrutineeStart
        var parenDepth = 0
        var bodyBraceIdx = -1
        while (i < tokens.size) {
            when (tokens[i].type) {
                RescriptTokenTypes.LPAREN -> parenDepth++
                RescriptTokenTypes.RPAREN -> if (parenDepth > 0) parenDepth--
                RescriptTokenTypes.LBRACE -> if (parenDepth == 0) bodyBraceIdx = i
            }
            if (bodyBraceIdx >= 0) break
            i++
        }
        if (bodyBraceIdx < 0) return tokens.size

        i = bodyBraceIdx + 1
        var braceDepth = 1
        var armPipeStart: Int? = null
        var armPatternStartIdx = -1
        var pendingArmIdx: Int? = null
        while (i < tokens.size) {
            val t = tokens[i]
            when (t.type) {
                RescriptTokenTypes.LBRACE -> {
                    braceDepth++
                }

                RescriptTokenTypes.RBRACE -> {
                    braceDepth--
                    if (braceDepth == 0) {
                        if (pendingArmIdx != null) {
                            arms[pendingArmIdx] =
                                arms[pendingArmIdx].copy(
                                    bodyTokenEndIdx = i,
                                    bodyEndOffset = tokens[i - 1].end,
                                )
                        }
                        return i + 1
                    }
                }

                RescriptTokenTypes.SWITCH -> {
                    i = processSwitch(tokens, i, arms)
                    continue
                }

                RescriptTokenTypes.PIPE -> {
                    if (braceDepth == 1 && armPipeStart == null) {
                        if (pendingArmIdx != null) {
                            arms[pendingArmIdx] =
                                arms[pendingArmIdx].copy(
                                    bodyTokenEndIdx = i,
                                    bodyEndOffset = tokens[i - 1].end,
                                )
                            pendingArmIdx = null
                        }
                        armPipeStart = t.start
                        armPatternStartIdx = i + 1
                    }
                }

                RescriptTokenTypes.ARROW -> {
                    if (braceDepth == 1 && armPipeStart != null) {
                        arms.add(
                            ArmRecord(
                                pipeStart = armPipeStart,
                                patternStartIdx = armPatternStartIdx,
                                patternEndIdx = i,
                                bodyTokenStartIdx = i + 1,
                                bodyTokenEndIdx = -1,
                                bodyEndOffset = -1,
                            ),
                        )
                        pendingArmIdx = arms.size - 1
                        armPipeStart = null
                    }
                }

                else -> {}
            }
            i++
        }
        return tokens.size
    }

    /**
     * A `switch` arm located during the lexer walk, recorded with token
     * indices so pattern/body slices can be taken later.
     *
     * @property pipeStart source offset of the arm's leading `|`
     * @property patternStartIdx token index just after the `|`
     * @property patternEndIdx token index of the `=>` (exclusive pattern end)
     * @property bodyTokenStartIdx token index just after the `=>`
     * @property bodyTokenEndIdx token index of the next `|`/`}` (exclusive
     *   body end); `-1` while still open
     * @property bodyEndOffset source offset of the end of the body; `-1`
     *   while still open
     */
    private data class ArmRecord(
        val pipeStart: Int,
        val patternStartIdx: Int,
        val patternEndIdx: Int,
        val bodyTokenStartIdx: Int,
        val bodyTokenEndIdx: Int,
        val bodyEndOffset: Int,
    )
}
