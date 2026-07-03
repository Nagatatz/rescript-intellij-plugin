package com.rescript.plugin.lang

import com.intellij.psi.tree.IElementType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Characterization tests for incremental re-lexing (lexer restart).
 *
 * IntelliJ's [com.intellij.openapi.editor.ex.util.LexerEditorHighlighter]
 * snapshots [com.intellij.lexer.Lexer.getState] at token boundaries and restarts
 * lexing via `start(text, offset, end, state)` after an edit. Any lexer state not
 * captured by `getState()` — or captured with the wrong (post-token) value — is lost
 * on restart. These tests exercise that path for JSX open tags (whose context lives
 * in custom lexer fields spanning multiple tokens) and for block comments (a single
 * token, expected to be restart-safe with no field encoding).
 */
class RescriptLexerRestartTest {
    private data class Tok(
        val start: Int,
        val type: IElementType,
        val state: Int,
    )

    /** Fully lexes [text], recording each token's start offset, type, and restart state. */
    private fun lexWithStates(text: String): List<Tok> {
        val lexer = RescriptLexer()
        lexer.start(text, 0, text.length, 0)
        val out = mutableListOf<Tok>()
        while (lexer.tokenType != null) {
            out.add(Tok(lexer.tokenStart, lexer.tokenType!!, lexer.state))
            lexer.advance()
        }
        return out
    }

    /** Restarts the lexer at [offset]/[state] and returns the token type at [targetOffset]. */
    private fun tokenTypeAfterRestart(
        text: String,
        offset: Int,
        state: Int,
        targetOffset: Int,
    ): IElementType? {
        val lexer = RescriptLexer()
        lexer.start(text, offset, text.length, state)
        while (lexer.tokenType != null) {
            if (lexer.tokenStart == targetOffset) return lexer.tokenType
            lexer.advance()
        }
        return null
    }

    /** Asserts that restarting at every token boundary reproduces the full-lex token type. */
    private fun assertRestartConsistent(text: String) {
        val tokens = lexWithStates(text)
        for (tok in tokens) {
            val restarted = tokenTypeAfterRestart(text, tok.start, tok.state, tok.start)
            assertEquals(
                tok.type,
                restarted,
                "restart at offset ${tok.start} ('${text[tok.start]}') must reproduce ${tok.type}",
            )
        }
    }

    @Test
    fun `JSX closing angle bracket survives a restart inside the open tag`() {
        val text = "<div className={x} onClick={f}>foo</div>"
        val closeGt = text.indexOf('>') // the open tag's closing '>'
        val restartAt = text.indexOf("onClick")

        val tokens = lexWithStates(text)

        // Baseline: a full lex classifies the open tag's '>' as TAG_GT.
        assertEquals(
            RescriptTokenTypes.TAG_GT,
            tokens.first { it.start == closeGt }.type,
            "full lex should classify the open-tag '>' as TAG_GT",
        )

        // Restart at the 'onClick' attribute token, mimicking the highlighter.
        val restartState = tokens.first { it.start == restartAt }.state
        val afterRestart = tokenTypeAfterRestart(text, restartAt, restartState, closeGt)

        assertEquals(
            RescriptTokenTypes.TAG_GT,
            afterRestart,
            "after an incremental restart inside the JSX open tag, '>' must still be TAG_GT",
        )
    }

    @Test
    fun `restarting at a brace token inside a JSX tag keeps the closing angle a TAG_GT`() {
        // Restart at the '{' of an attribute value: the brace-depth field must be
        // captured as its pre-token value, or the closing '>' mis-lexes as GT.
        val text = "<div className={x} onClick={f}>foo</div>"
        val closeGt = text.indexOf('>')
        val braceAt = text.indexOf('{')

        val tokens = lexWithStates(text)
        val restartState = tokens.first { it.start == braceAt }.state
        val afterRestart = tokenTypeAfterRestart(text, braceAt, restartState, closeGt)

        assertEquals(RescriptTokenTypes.TAG_GT, afterRestart)
    }

    @Test
    fun `every token boundary is a consistent restart point for nested-brace JSX`() {
        assertRestartConsistent("<div style={{a: {b: 1}}} onClick={f}>x</div>")
    }

    @Test
    fun `block comment stays a comment after a restart at its start`() {
        val text = "let a = 1\n/* c1 /* nested */ c2 */\nlet b = 2"
        val commentStart = text.indexOf("/*")

        val tokens = lexWithStates(text)
        val restartState = tokens.first { it.start == commentStart }.state
        val afterRestart = tokenTypeAfterRestart(text, commentStart, restartState, commentStart)

        assertEquals(
            RescriptTokenTypes.MULTI_COMMENT,
            afterRestart,
            "the (single-token) block comment must remain MULTI_COMMENT after restart",
        )
    }
}
