package com.rescript.plugin.lang

import com.intellij.lang.PsiBuilder
import com.rescript.plugin.lang.psi.RescriptElementTypes

/**
 * Parses JSX elements and fragments within ReScript source files.
 *
 * Handles opening/closing tags, self-closing elements, JSX fragments
 * (`<> ... </>`), nested JSX children, and expression containers.
 * Extracted from [RescriptParser] for focused responsibility.
 *
 * @see RescriptParser
 */
internal object RescriptJsxParser {
    /**
     * Tries to parse a JSX element starting at TAG_LT.
     *
     * @param b the PsiBuilder
     * @return true if a JSX node was created, false if rolled back
     */
    fun tryParseJsx(b: PsiBuilder): Boolean {
        if (b.tokenType != RescriptTokenTypes.TAG_LT) return false

        val m = b.mark()
        b.advanceLexer() // consume '<'

        return when (b.tokenType) {
            RescriptTokenTypes.JSX_TAG_NAME, RescriptTokenTypes.JSX_COMPONENT_NAME -> {
                parseJsxTagOrSelfClosing(b, m)
            }

            else -> {
                m.rollbackTo()
                false
            }
        }
    }

    /**
     * Tries to parse a JSX fragment starting at LT (not TAG_LT).
     * The lexer produces LT + GT for `<>` since `>` is not a letter.
     *
     * @param b the PsiBuilder
     * @return true if a JSX_FRAGMENT node was created, false if rolled back
     */
    fun tryParseJsxFragment(b: PsiBuilder): Boolean {
        if (b.tokenType != RescriptTokenTypes.LT) return false

        val m = b.mark()
        b.advanceLexer() // consume '<'

        if (b.tokenType != RescriptTokenTypes.GT) {
            m.rollbackTo()
            return false
        }

        b.advanceLexer() // consume '>'
        parseJsxChildren(b)
        // expect </> closing
        if (b.tokenType == RescriptTokenTypes.TAG_LT_SLASH) {
            b.advanceLexer() // consume '</'
            if (b.tokenType == RescriptTokenTypes.TAG_GT) {
                b.advanceLexer() // consume '>'
            }
        }
        m.done(RescriptElementTypes.JSX_FRAGMENT)
        return true
    }

    /** Parses a named JSX tag (opening+closing or self-closing). TAG_LT already consumed. */
    private fun parseJsxTagOrSelfClosing(
        b: PsiBuilder,
        m: PsiBuilder.Marker,
    ): Boolean {
        b.advanceLexer() // consume tag name

        // Consume dotted module path: <Module.SubModule.component ...>
        while (!b.eof() && b.tokenType == RescriptTokenTypes.DOT) {
            b.advanceLexer() // consume '.'
            if (b.tokenType == RescriptTokenTypes.JSX_TAG_NAME ||
                b.tokenType == RescriptTokenTypes.JSX_COMPONENT_NAME
            ) {
                b.advanceLexer() // consume next part
            }
        }

        skipJsxAttributes(b)

        return when (b.tokenType) {
            RescriptTokenTypes.TAG_AUTO_CLOSE -> {
                b.advanceLexer() // consume '/>'
                m.done(RescriptElementTypes.JSX_SELF_CLOSING_ELEMENT)
                true
            }

            RescriptTokenTypes.TAG_GT, RescriptTokenTypes.GT -> {
                b.advanceLexer() // consume '>'
                parseJsxChildren(b)
                consumeClosingTag(b)
                m.done(RescriptElementTypes.JSX_ELEMENT)
                true
            }

            else -> {
                m.rollbackTo()
                false
            }
        }
    }

    /**
     * Parses JSX children between opening and closing tags.
     * Children can be: nested JSX, expression containers {expr}, or text tokens.
     */
    private fun parseJsxChildren(b: PsiBuilder) {
        while (!b.eof()) {
            when (b.tokenType) {
                RescriptTokenTypes.TAG_LT_SLASH -> {
                    return
                }

                // closing tag starts
                RescriptTokenTypes.TAG_LT -> {
                    if (!tryParseJsx(b)) {
                        b.advanceLexer() // skip if not valid JSX
                    }
                }

                RescriptTokenTypes.LT -> {
                    // Nested fragment: <> ... </>
                    if (!tryParseJsxFragment(b)) {
                        b.advanceLexer()
                    }
                }

                RescriptTokenTypes.LBRACE -> {
                    RescriptParser.skipBalanced(b, RescriptTokenTypes.LBRACE, RescriptTokenTypes.RBRACE)
                }

                else -> {
                    b.advanceLexer()
                }
            }
        }
    }

    /** Skips tokens inside an opening tag until `>` or `/>` is found. */
    private fun skipJsxAttributes(b: PsiBuilder) {
        while (!b.eof()) {
            when (b.tokenType) {
                RescriptTokenTypes.TAG_GT, RescriptTokenTypes.GT, RescriptTokenTypes.TAG_AUTO_CLOSE -> {
                    return
                }

                RescriptTokenTypes.LBRACE -> {
                    RescriptParser.skipBalanced(b, RescriptTokenTypes.LBRACE, RescriptTokenTypes.RBRACE)
                }

                else -> {
                    b.advanceLexer()
                }
            }
        }
    }

    /** Consumes a closing tag `</tagName>`. Tolerant of missing parts. */
    private fun consumeClosingTag(b: PsiBuilder) {
        if (b.tokenType != RescriptTokenTypes.TAG_LT_SLASH) return
        b.advanceLexer() // consume '</'

        // consume tag name (and optional dotted path)
        while (!b.eof()) {
            if (b.tokenType == RescriptTokenTypes.JSX_TAG_NAME ||
                b.tokenType == RescriptTokenTypes.JSX_COMPONENT_NAME
            ) {
                b.advanceLexer()
                if (b.tokenType == RescriptTokenTypes.DOT) {
                    b.advanceLexer()
                } else {
                    break
                }
            } else {
                break
            }
        }

        if (b.tokenType == RescriptTokenTypes.TAG_GT) {
            b.advanceLexer() // consume '>'
        }
    }
}
