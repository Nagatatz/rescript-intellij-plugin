package com.rescript.plugin.intention

import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.rescript.plugin.lang.RescriptLexer
import com.rescript.plugin.lang.RescriptTokenTypes

/**
 * Pure, LSP-independent logic that enumerates un-annotated file-top-level
 * `let` declarations in a ReScript source string and builds a batch of
 * type-annotation insertions from an injected type resolver.
 *
 * The class is deliberately free of any IntelliJ Platform threading,
 * document, or LSP coupling so it can be fully unit-tested. The execution
 * glue (querying hover off the EDT, applying edits in a write action) lives
 * in [RescriptBatchAnnotationRunner].
 *
 * The `let` enumeration mirrors the depth-tracking heuristic in
 * `RescriptTypeCoverageScanner.topLevelLetRanges`, but additionally pins
 * down each binding's name token and the insertion offset for `: T`.
 */
object RescriptBatchAnnotationPlanner {
    /**
     * Maximum length of an inline type annotation. Hover responses longer
     * than this (e.g. large inferred record types) are skipped rather than
     * inlined, to avoid producing unreadable single-line annotations.
     */
    const val MAX_INLINE_TYPE_LENGTH = 200

    /**
     * A single un-annotated file-top-level `let` binding.
     *
     * @property letStart offset of the `let` keyword
     * @property nameStart offset of the binding name's first character
     * @property nameEnd offset just past the binding name — the insertion
     *   point for the `: T` annotation
     * @property bindingName the binding's identifier text
     */
    data class InferredLet(
        val letStart: Int,
        val nameStart: Int,
        val nameEnd: Int,
        val bindingName: String,
    )

    /**
     * A single annotation insertion to apply to the document.
     *
     * @property offset where to insert [text]
     * @property text the literal string to insert (e.g. `": int"`)
     */
    data class AnnotationEdit(
        val offset: Int,
        val text: String,
    )

    /**
     * The result of [buildPlan].
     *
     * @property edits insertions sorted by descending [AnnotationEdit.offset]
     *   so that applying them front-to-back never shifts a later offset
     * @property annotatedCount number of bindings that received an annotation
     * @property skippedCount number of bindings skipped because the resolver
     *   returned nothing or the type failed normalization
     */
    data class Plan(
        val edits: List<AnnotationEdit>,
        val annotatedCount: Int,
        val skippedCount: Int,
    )

    /** Internal lexer token record used for index-based look-ahead. */
    private data class Tok(val type: IElementType, val start: Int, val end: Int)

    /**
     * Enumerates every un-annotated file-top-level `let` binding in [text].
     *
     * Only simple bindings at brace/paren/bracket depth 0 are returned.
     * Destructuring binds (`let (a, b) = ...`), record-pattern binds
     * (`let {x} = ...`), wildcard binds (`let _ = ...`), already-annotated
     * binds (`let x: int = ...`), and module-internal or expression-internal
     * lets (depth > 0) are all excluded.
     *
     * @param text the ReScript source to scan
     * @return the inferred-let descriptors in source order
     */
    fun collectInferredLets(text: String): List<InferredLet> {
        val tokens = lex(text)
        val result = mutableListOf<InferredLet>()
        var depth = 0
        for (i in tokens.indices) {
            val tok = tokens[i]
            when (tok.type) {
                RescriptTokenTypes.LPAREN,
                RescriptTokenTypes.LBRACE,
                RescriptTokenTypes.LBRACKET,
                -> depth++

                RescriptTokenTypes.RPAREN,
                RescriptTokenTypes.RBRACE,
                RescriptTokenTypes.RBRACKET,
                -> depth = (depth - 1).coerceAtLeast(0)

                RescriptTokenTypes.LET -> if (depth == 0) collectAt(text, tokens, i)?.let(result::add)
            }
        }
        return result
    }

    /**
     * Attempts to read a single inferred-let starting at the `let` token at
     * index [letIndex]. Returns null when the binding is not a simple,
     * un-annotated identifier binding.
     */
    private fun collectAt(
        text: String,
        tokens: List<Tok>,
        letIndex: Int,
    ): InferredLet? {
        // Skip the optional `rec` keyword between `let` and the binding name.
        var nameIdx = nextSignificant(tokens, letIndex + 1)
        if (nameIdx != -1 && tokens[nameIdx].type == RescriptTokenTypes.REC) {
            nameIdx = nextSignificant(tokens, nameIdx + 1)
        }
        if (nameIdx == -1) return null
        val nameTok = tokens[nameIdx]
        // Only plain lowercase-identifier bindings qualify; tuple/record
        // destructuring and `_` wildcard bindings are excluded.
        if (nameTok.type != RescriptTokenTypes.LIDENT) return null

        // The next significant token decides annotated vs inferred.
        val afterIdx = nextSignificant(tokens, nameIdx + 1)
        if (afterIdx != -1 && tokens[afterIdx].type == RescriptTokenTypes.COLON) return null

        return InferredLet(
            letStart = tokens[letIndex].start,
            nameStart = nameTok.start,
            nameEnd = nameTok.end,
            bindingName = text.substring(nameTok.start, nameTok.end),
        )
    }

    /**
     * Normalizes a raw type string coming from an LSP hover response into a
     * single-line annotation, sanitizing it for safe insertion.
     *
     * Collapses any run of whitespace (including newlines and tabs) into one
     * space, strips ASCII control characters, and trims. Returns null when
     * the result is blank or exceeds [MAX_INLINE_TYPE_LENGTH].
     *
     * @param raw the unprocessed type text from hover
     * @return the inline-safe type string, or null if unusable
     */
    fun normalizeType(raw: String): String? {
        val sanitized =
            buildString(raw.length) {
                for (ch in raw) {
                    // Drop ASCII control chars (sanitize LSP-sourced text);
                    // whitespace is normalized below.
                    if (ch.isWhitespace()) {
                        append(' ')
                    } else if (ch.code >= 0x20 && ch.code != 0x7F) {
                        append(ch)
                    }
                }
            }
        val collapsed = WHITESPACE_RUN.replace(sanitized, " ").trim()
        if (collapsed.isEmpty()) return null
        if (collapsed.length > MAX_INLINE_TYPE_LENGTH) return null
        return collapsed
    }

    /**
     * Builds an annotation [Plan] for [text] using [resolver] to obtain a
     * type for each inferred binding.
     *
     * [resolver] is called once per binding with that binding's name-start
     * offset and should return the raw type text (or null when unknown). A
     * null/blank resolver result, or a type that fails [normalizeType],
     * counts toward [Plan.skippedCount]; otherwise an insertion is emitted.
     *
     * @param text the ReScript source to annotate
     * @param resolver maps a binding name-start offset to its raw type text
     * @return the plan with edits sorted descending by offset
     */
    fun buildPlan(
        text: String,
        resolver: (Int) -> String?,
    ): Plan {
        val edits = mutableListOf<AnnotationEdit>()
        var annotated = 0
        var skipped = 0
        for (let in collectInferredLets(text)) {
            val raw = resolver(let.nameStart)
            if (raw.isNullOrBlank()) {
                skipped++
                continue
            }
            val type = normalizeType(raw)
            if (type == null) {
                skipped++
                continue
            }
            edits.add(AnnotationEdit(let.nameEnd, ": $type"))
            annotated++
        }
        // Descending offset order: front-to-back application never shifts a
        // not-yet-applied (earlier) offset.
        edits.sortByDescending { it.offset }
        return Plan(edits = edits, annotatedCount = annotated, skippedCount = skipped)
    }

    /** Lexes [text] into a flat token list for index-based look-ahead. */
    private fun lex(text: String): List<Tok> {
        val tokens = mutableListOf<Tok>()
        val lexer = RescriptLexer()
        lexer.start(text)
        while (lexer.tokenType != null) {
            tokens.add(Tok(lexer.tokenType!!, lexer.tokenStart, lexer.tokenEnd))
            lexer.advance()
        }
        return tokens
    }

    /**
     * Returns the index of the first non-ignorable token at or after [from],
     * or -1 if none remains.
     */
    private fun nextSignificant(
        tokens: List<Tok>,
        from: Int,
    ): Int {
        var i = from
        while (i < tokens.size) {
            if (!isIgnorable(tokens[i].type)) return i
            i++
        }
        return -1
    }

    private fun isIgnorable(type: IElementType): Boolean =
        type == TokenType.WHITE_SPACE ||
            type == RescriptTokenTypes.EOL ||
            type == RescriptTokenTypes.SINGLE_COMMENT ||
            type == RescriptTokenTypes.MULTI_COMMENT

    /** Matches one or more whitespace characters for collapsing. */
    private val WHITESPACE_RUN = Regex("\\s+")
}
