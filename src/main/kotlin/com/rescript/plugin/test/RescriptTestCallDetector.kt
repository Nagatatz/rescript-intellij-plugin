package com.rescript.plugin.test

import com.intellij.openapi.util.TextRange
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.rescript.plugin.lang.RescriptLexer
import com.rescript.plugin.lang.RescriptTokenTypes

/**
 * A single detected test-framework call (`describe` / `it` / `test`) carrying enough
 * positional information to anchor a gutter run marker and to derive a `-t <name>` filter.
 *
 * @property functionName the framework function name (`describe`, `it`, or `test`)
 * @property testName the literal text of the first string argument, with surrounding quotes stripped
 * @property callOffset the start offset of the function-name identifier (gutter marker anchor)
 * @property nameArgRange the full range of the first string-literal argument
 */
data class TestCall(
    val functionName: String,
    val testName: String,
    val callOffset: Int,
    val nameArgRange: TextRange,
)

/**
 * Pure-syntax detector for ReScript test-framework calls of the form
 * `describe("...", ...)`, `it("...", ...)`, and `test("...", ...)`.
 *
 * Detection is LSP-independent: the input is tokenized with [RescriptLexer] and a call is
 * recognized when a `describe` / `it` / `test` identifier is immediately followed by `(` and a
 * plain string-literal first argument. Calls whose first argument is a backtick/interpolated
 * template string are skipped, because a stable run-filter name cannot be derived from them.
 * Nested calls (e.g. an `it` inside a `describe` body) are each reported independently.
 *
 * @see RescriptTestRunLineMarkerContributor for the gutter run/debug integration that consumes the results
 */
object RescriptTestCallDetector {
    private val TEST_FUNCTION_NAMES = setOf("describe", "it", "test")

    /** Lightweight token record retaining the element type, source offsets and raw text. */
    private data class Tok(
        val type: IElementType,
        val start: Int,
        val end: Int,
        val text: String,
    )

    /**
     * Scans [text] and returns every `describe` / `it` / `test` call with a plain string-literal
     * first argument, in source order.
     *
     * @param text the ReScript source to scan
     * @return the detected test calls; empty when none match
     */
    fun detect(text: CharSequence): List<TestCall> {
        val tokens = lexNonTrivia(text)
        val result = mutableListOf<TestCall>()
        var i = 0
        while (i < tokens.size) {
            val tok = tokens[i]
            if (tok.type == RescriptTokenTypes.LIDENT && tok.text in TEST_FUNCTION_NAMES) {
                // Expect: LIDENT "(" STRING_VALUE
                val lparen = tokens.getOrNull(i + 1)
                val arg = tokens.getOrNull(i + 2)
                if (lparen != null && lparen.type == RescriptTokenTypes.LPAREN &&
                    arg != null && arg.type == RescriptTokenTypes.STRING_VALUE
                ) {
                    result.add(
                        TestCall(
                            functionName = tok.text,
                            testName = stripQuotes(arg.text),
                            callOffset = tok.start,
                            nameArgRange = TextRange(arg.start, arg.end),
                        ),
                    )
                    // Skip past the consumed argument so a name like `test` inside the string
                    // cannot be re-matched.
                    i += 3
                    continue
                }
            }
            i++
        }
        return result
    }

    /**
     * Tokenizes [text] with [RescriptLexer], discarding whitespace and comments while retaining
     * each significant token's type, offsets and text.
     */
    private fun lexNonTrivia(text: CharSequence): List<Tok> {
        val source = text.toString()
        val lexer = RescriptLexer()
        lexer.start(source)
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

    /**
     * Removes the surrounding double-quote characters from a `STRING_VALUE` token's text.
     * The lexer includes the quotes in the token text (e.g. `"hello"` → text `"hello"`),
     * so the first and last characters are dropped when present.
     */
    private fun stripQuotes(raw: String): String {
        if (raw.length >= 2 && raw.first() == '"' && raw.last() == '"') {
            return raw.substring(1, raw.length - 1)
        }
        return raw
    }
}
