package com.rescript.plugin.completion

import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.rescript.plugin.lang.RescriptLexer
import com.rescript.plugin.lang.RescriptTokenTypes

/**
 * Detected type-annotation context at the caret: the head identifier of the
 * annotated type and whether the caret already sits after an opening `{`.
 *
 * @property typeName the head type identifier with type arguments stripped
 *   (e.g. `person`, `option`, `Belt` for `Belt.Map.t`)
 * @property afterOpenBrace `true` when an opening `{` is already typed after
 *   the `=`, so the caller should suppress the record-literal candidate to
 *   avoid producing a doubled brace
 */
data class TypeAnnotationDetection(
    val typeName: String,
    val afterOpenBrace: Boolean,
)

/**
 * Pure, LSP-independent analyzer that decides whether the caret sits at the
 * value position of a type-annotated `let` binding (`let <name>: <T> = `) and,
 * if so, extracts the head identifier of the annotated type.
 *
 * The detection walks the [RescriptLexer] token stream of the text before the
 * caret, anchors on the binding's `=`, validates the preceding
 * `let [rec] <name>:` shape, and lifts the head identifier out of the raw
 * annotation text. It never touches PSI and is trivially unit-testable.
 *
 * @see RescriptPlaceholderCompletionContributor for the editor-facing wrapper
 */
object RescriptTypeAnnotationContext {
    /** Matches a complete ReScript value/type identifier. */
    private val IDENTIFIER = Regex("^[A-Za-z_][A-Za-z0-9_']*$")

    /** Matches the leading identifier segment of a type annotation. */
    private val LEADING_IDENTIFIER = Regex("^[A-Za-z_][A-Za-z0-9_']*")

    /** Closing bracket tokens that increase nesting depth on a backward scan. */
    private val CLOSERS =
        setOf(
            RescriptTokenTypes.RPAREN,
            RescriptTokenTypes.RBRACKET,
            RescriptTokenTypes.RBRACE,
            RescriptTokenTypes.GT,
        )

    /** Opening bracket tokens that decrease nesting depth on a backward scan. */
    private val OPENERS =
        setOf(
            RescriptTokenTypes.LPAREN,
            RescriptTokenTypes.LBRACKET,
            RescriptTokenTypes.LBRACE,
            RescriptTokenTypes.LT,
        )

    /**
     * Detects a type-annotated `let` value position ending at the caret.
     *
     * @param textBeforeCaret document text from file start to the caret offset
     * @return the detected type context, or `null` if the caret is not at the
     *   value position of a type-annotated `let` binding
     */
    fun detectExpectedType(textBeforeCaret: String): TypeAnnotationDetection? {
        val tokens = tokenize(textBeforeCaret)
        if (tokens.isEmpty()) return null

        // Anchor on the last `=`: everything after it is the (still empty or
        // partially typed) value, everything before is the binding header.
        val eqIdx = tokens.indexOfLast { it.type == RescriptTokenTypes.EQ }
        if (eqIdx < 0) return null

        val tail = tokens.subList(eqIdx + 1, tokens.size)
        val afterOpenBrace = classifyTail(tail) ?: return null

        val colonIdx = findAnnotationColon(tokens, eqIdx) ?: return null

        // The binding name is the LIDENT immediately before the colon, with an
        // optional `rec` and the mandatory `let` keyword preceding it.
        // Need at least `let <name> :`, so the colon must be at index >= 2.
        if (colonIdx < 2 || tokens[colonIdx - 1].type != RescriptTokenTypes.LIDENT) return null
        val beforeName = tokens[colonIdx - 2].typeOrNull()
        val isLetBinding =
            when (beforeName) {
                RescriptTokenTypes.LET -> true
                RescriptTokenTypes.REC -> tokens.getOrNull(colonIdx - 3)?.type == RescriptTokenTypes.LET
                else -> false
            }
        if (!isLetBinding) return null

        val annotationText = textBeforeCaret.substring(tokens[colonIdx].end, tokens[eqIdx].start)
        val head = extractHeadIdentifier(annotationText) ?: return null
        return TypeAnnotationDetection(head, afterOpenBrace)
    }

    /**
     * Validates the tokens that follow the binding's `=` and reports whether
     * an opening `{` is already present.
     *
     * @return `true`/`false` for the `afterOpenBrace` flag, or `null` if the
     *   tail is anything other than empty, a lone `{`, or a single identifier
     *   prefix (which would mean the caret is not at a fresh value position)
     */
    private fun classifyTail(tail: List<LexedToken>): Boolean? =
        when {
            tail.isEmpty() -> false
            tail.size == 1 && tail[0].type == RescriptTokenTypes.LBRACE -> true
            tail.size == 1 && IDENTIFIER.matches(tail[0].text) -> false
            else -> null
        }

    /**
     * Walks backward from the binding's `=` to find the annotation `:` at
     * bracket depth 0, skipping colons nested inside `<>`, `()`, `[]`, `{}`.
     *
     * @return the token index of the annotation colon, or `null` if none
     */
    private fun findAnnotationColon(
        tokens: List<LexedToken>,
        eqIdx: Int,
    ): Int? {
        var depth = 0
        for (i in eqIdx - 1 downTo 0) {
            when (tokens[i].type) {
                in CLOSERS -> {
                    depth++
                }

                in OPENERS -> {
                    if (depth > 0) depth--
                }

                RescriptTokenTypes.COLON -> {
                    if (depth == 0) return i
                }

                RescriptTokenTypes.LET -> {
                    if (depth == 0) return null // crossed into prior statement
                }

                else -> {}
            }
        }
        return null
    }

    /**
     * Extracts the head identifier from a raw type annotation, stripping type
     * arguments and dotted qualifiers (the first segment is kept).
     *
     * Examples: `person` → `person`, `option<int>` → `option`,
     * `Belt.Map.t` → `Belt`, `(int, string)` → `null`.
     */
    private fun extractHeadIdentifier(annotationText: String): String? {
        val trimmed = annotationText.trim()
        return LEADING_IDENTIFIER.find(trimmed)?.value
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

    private fun LexedToken?.typeOrNull(): IElementType? = this?.type

    private fun List<LexedToken>.getOrNull(index: Int): LexedToken? = if (index in indices) this[index] else null

    /**
     * Lightweight lexer record produced by walking the JFlex tokens once.
     */
    private data class LexedToken(
        val type: IElementType,
        val start: Int,
        val end: Int,
        val text: String,
    )
}
