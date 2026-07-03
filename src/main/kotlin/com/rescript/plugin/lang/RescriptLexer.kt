package com.rescript.plugin.lang

import com.intellij.lexer.LexerBase
import com.intellij.psi.tree.IElementType

/**
 * Lexer for ReScript source code, driving the JFlex-generated [RescriptFlexLexer].
 *
 * Implemented directly on [LexerBase] (rather than `FlexAdapter`) so that the state
 * returned by [getState] fully and *consistently* captures the lexer's restart context.
 * A JSX open tag keeps context in the flex lexer's `inJsxOpenTag` / `jsxAttrBraceDepth`
 * fields while it stays in the `INITIAL` lexical state across several attribute tokens.
 * IntelliJ's incremental highlighter snapshots [getState] at token boundaries and restarts
 * lexing from there, so that context is encoded into the state integer above the JFlex
 * lexical-state bits. Crucially, the snapshot is taken *before* each token is lexed — the
 * value that must be restored to reproduce it — which `FlexAdapter` cannot express because
 * its `getState()` reports the JFlex state before the token but reads the custom fields
 * only after it, mis-restarting at `{`/`}` boundaries inside a tag.
 *
 * @see RescriptFlexLexer the generated JFlex scanner
 */
class RescriptLexer : LexerBase() {
    private val flex = RescriptFlexLexer(null)

    private var buffer: CharSequence = ""
    private var bufferEndOffset = 0

    private var tokenType: IElementType? = null
    private var tokenStartOffset = 0
    private var tokenEndOffset = 0

    /** Packed (JFlex state + JSX fields) as they were *entering* the current token. */
    private var stateBeforeToken = 0

    /** Whether [tokenType]/offsets/[stateBeforeToken] describe the current token. */
    private var located = false

    override fun start(
        buffer: CharSequence,
        startOffset: Int,
        endOffset: Int,
        initialState: Int,
    ) {
        this.buffer = buffer
        this.bufferEndOffset = endOffset
        flex.reset(buffer, startOffset, endOffset, initialState and STATE_MASK)
        flex.isInJsxOpenTag = (initialState ushr JSX_FLAG_SHIFT) and 1 == 1
        flex.jsxAttrBraceDepth = (initialState ushr DEPTH_SHIFT) and DEPTH_MASK
        located = false
        tokenType = null
    }

    override fun getState(): Int {
        locateToken()
        return stateBeforeToken
    }

    override fun getTokenType(): IElementType? {
        locateToken()
        return tokenType
    }

    override fun getTokenStart(): Int {
        locateToken()
        return tokenStartOffset
    }

    override fun getTokenEnd(): Int {
        locateToken()
        return tokenEndOffset
    }

    override fun advance() {
        locateToken()
        located = false
    }

    override fun getBufferSequence(): CharSequence = buffer

    override fun getBufferEnd(): Int = bufferEndOffset

    /**
     * Lexes the current token if not already located, snapshotting the pre-token state.
     *
     * The snapshot is captured *before* [RescriptFlexLexer.advance] so that
     * `start(buffer, tokenStart, end, getState())` reproduces this exact token during an
     * incremental restart.
     */
    private fun locateToken() {
        if (located) return
        stateBeforeToken = packState(flex.yystate(), flex.isInJsxOpenTag, flex.jsxAttrBraceDepth)
        tokenType = flex.advance()
        tokenStartOffset = flex.tokenStart
        tokenEndOffset = flex.tokenEnd
        located = true
    }

    private fun packState(
        lexicalState: Int,
        inJsxOpenTag: Boolean,
        jsxAttrBraceDepth: Int,
    ): Int {
        val jsxFlag = if (inJsxOpenTag) 1 else 0
        val depth = jsxAttrBraceDepth.coerceIn(0, MAX_BRACE_DEPTH)
        return (lexicalState and STATE_MASK) or (jsxFlag shl JSX_FLAG_SHIFT) or (depth shl DEPTH_SHIFT)
    }

    companion object {
        // JFlex lexical states occupy the low bits (the generated max is 18, i.e. 5 bits).
        private const val STATE_MASK = 0x1F
        private const val JSX_FLAG_SHIFT = 5
        private const val DEPTH_SHIFT = 6
        private const val DEPTH_MASK = 0x7

        /**
         * Maximum JSX-attribute brace depth encoded into the restart state (3 bits).
         * Deeper nesting — vanishingly rare in real JSX — is clamped; the only effect is
         * that a restart at such an extreme depth may fall back to the pre-fix behavior.
         */
        private const val MAX_BRACE_DEPTH = 7
    }
}
