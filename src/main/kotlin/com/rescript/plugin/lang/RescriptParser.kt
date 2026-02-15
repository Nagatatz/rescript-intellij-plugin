package com.rescript.plugin.lang

import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.psi.tree.IElementType
import com.rescript.plugin.lang.psi.RescriptElementTypes

/**
 * Lightweight parser for ReScript files.
 *
 * Provides minimal PSI structure for IDE features (structure view, folding).
 * Semantic analysis is delegated to the LSP (rescript-language-server).
 */
class RescriptParser : PsiParser {
    override fun parse(
        root: IElementType,
        builder: PsiBuilder,
    ): ASTNode {
        val rootMarker = builder.mark()
        while (!builder.eof()) {
            parseTopLevel(builder)
        }
        rootMarker.done(root)
        return builder.treeBuilt
    }

    private fun parseTopLevel(b: PsiBuilder) {
        when (b.tokenType) {
            RescriptTokenTypes.LET -> parseDeclaration(b, RescriptElementTypes.LET_DECLARATION, consumeRec = true)
            RescriptTokenTypes.TYPE -> parseDeclaration(b, RescriptElementTypes.TYPE_DECLARATION, consumeRec = true)
            RescriptTokenTypes.MODULE -> parseModuleDeclaration(b)
            RescriptTokenTypes.EXTERNAL -> parseDeclaration(b, RescriptElementTypes.EXTERNAL_DECLARATION)
            RescriptTokenTypes.OPEN -> parseSimple(b, RescriptElementTypes.OPEN_STATEMENT)
            RescriptTokenTypes.INCLUDE -> parseSimple(b, RescriptElementTypes.INCLUDE_STATEMENT)
            RescriptTokenTypes.EXCEPTION -> parseDeclaration(b, RescriptElementTypes.EXCEPTION_DECLARATION)
            RescriptTokenTypes.ARROBASE -> parseAnnotation(b)
            RescriptTokenTypes.EOL -> b.advanceLexer()
            RescriptTokenTypes.TAG_LT -> {
                if (!tryParseJsx(b)) {
                    // Not valid JSX — skip tokens silently
                    skipNonTopLevel(b)
                }
            }
            RescriptTokenTypes.LT -> {
                // Fragment opening: <> (lexer produces LT + GT, not TAG_LT + TAG_GT)
                if (!tryParseJsxFragment(b)) {
                    skipNonTopLevel(b)
                }
            }
            else -> {
                // Skip non-top-level tokens silently.
                // This lightweight parser only carves out top-level declarations;
                // expression-level validation is delegated to the LSP.
                skipNonTopLevel(b)
            }
        }
    }

    /** Parse `let [rec] name ...` or `type [rec] name ...` etc. */
    private fun parseDeclaration(
        b: PsiBuilder,
        elementType: IElementType,
        consumeRec: Boolean = false,
    ) {
        val m = b.mark()
        b.advanceLexer() // consume keyword

        if (consumeRec && b.tokenType == RescriptTokenTypes.REC) {
            b.advanceLexer()
        }

        // consume identifier (lident or uident or _)
        if (b.tokenType in
            listOf(RescriptTokenTypes.LIDENT, RescriptTokenTypes.UIDENT, RescriptTokenTypes.UNDERSCORE)
        ) {
            b.advanceLexer()
        } else if (!b.eof() && !isTopLevelStart(b.tokenType)) {
            // R2: Report missing identifier
            b.error("Expected identifier")
        }

        skipToEndOfDeclaration(b)
        m.done(elementType)
    }

    private fun parseModuleDeclaration(b: PsiBuilder) {
        val m = b.mark()
        b.advanceLexer() // consume 'module'

        // optional 'type' or 'rec'
        if (b.tokenType == RescriptTokenTypes.TYPE || b.tokenType == RescriptTokenTypes.REC) {
            b.advanceLexer()
        }

        // consume module name (UIDENT)
        if (b.tokenType == RescriptTokenTypes.UIDENT) {
            b.advanceLexer()
        } else if (!b.eof() && b.tokenType != RescriptTokenTypes.LBRACE && !isTopLevelStart(b.tokenType)) {
            // R3: Report missing module name
            b.error("Expected module name")
        }

        // module body: recursively parse declarations inside { ... }
        skipToOpenBrace(b)
        if (b.tokenType == RescriptTokenTypes.LBRACE) {
            b.advanceLexer() // consume '{'
            while (!b.eof() && b.tokenType != RescriptTokenTypes.RBRACE) {
                parseTopLevel(b)
            }
            if (b.tokenType == RescriptTokenTypes.RBRACE) {
                b.advanceLexer() // consume '}'
            } else if (!b.eof()) {
                // R4: Report missing closing brace
                b.error("Expected '}'")
            }
        } else {
            skipToEndOfDeclaration(b)
        }

        m.done(RescriptElementTypes.MODULE_DECLARATION)
    }

    /** For `open ...` / `include ...` — just skip to end. */
    private fun parseSimple(
        b: PsiBuilder,
        elementType: IElementType,
    ) {
        val m = b.mark()
        b.advanceLexer()
        skipToEndOfDeclaration(b)
        m.done(elementType)
    }

    private fun parseAnnotation(b: PsiBuilder) {
        val m = b.mark()
        b.advanceLexer() // consume '@'

        // consume annotation name: either a single ANNOTATION_NAME token (from lexer's IN_ANNOTATION state)
        // or individual LIDENT/UIDENT/DOT tokens
        if (b.tokenType == RescriptTokenTypes.ANNOTATION_NAME) {
            b.advanceLexer()
        } else {
            while (!b.eof()) {
                val t = b.tokenType
                if (t == RescriptTokenTypes.LIDENT || t == RescriptTokenTypes.UIDENT || t == RescriptTokenTypes.DOT) {
                    b.advanceLexer()
                } else {
                    break
                }
            }
        }

        // optional arguments in parens
        if (b.tokenType == RescriptTokenTypes.LPAREN) {
            skipBalanced(b, RescriptTokenTypes.LPAREN, RescriptTokenTypes.RPAREN)
        }

        m.done(RescriptElementTypes.ANNOTATION)
    }

    // ── JSX parsing ───────────────────────────────────────────────────

    /**
     * Try to parse a JSX element starting at TAG_LT.
     * Returns true if a JSX node was created, false if rolled back.
     */
    private fun tryParseJsx(b: PsiBuilder): Boolean {
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
     * Try to parse a JSX fragment starting at LT (not TAG_LT).
     * The lexer produces LT + GT for `<>` since `>` is not a letter.
     * Returns true if a JSX_FRAGMENT node was created, false if rolled back.
     */
    private fun tryParseJsxFragment(b: PsiBuilder): Boolean {
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

    /** Parse a named JSX tag (opening+closing or self-closing). TAG_LT already consumed. */
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
     * Parse JSX children between opening and closing tags.
     * Children can be: nested JSX, expression containers {expr}, or text tokens.
     */
    private fun parseJsxChildren(b: PsiBuilder) {
        while (!b.eof()) {
            when (b.tokenType) {
                RescriptTokenTypes.TAG_LT_SLASH -> return // closing tag starts
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
                    skipBalanced(b, RescriptTokenTypes.LBRACE, RescriptTokenTypes.RBRACE)
                }
                else -> b.advanceLexer()
            }
        }
    }

    /** Skip tokens inside an opening tag until `>` or `/>` is found. */
    private fun skipJsxAttributes(b: PsiBuilder) {
        while (!b.eof()) {
            when (b.tokenType) {
                RescriptTokenTypes.TAG_GT, RescriptTokenTypes.GT, RescriptTokenTypes.TAG_AUTO_CLOSE -> return
                RescriptTokenTypes.LBRACE -> {
                    skipBalanced(b, RescriptTokenTypes.LBRACE, RescriptTokenTypes.RBRACE)
                }
                else -> b.advanceLexer()
            }
        }
    }

    /** Consume a closing tag `</tagName>`. Tolerant of missing parts. */
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

    // ── helpers ───────────────────────────────────────────────────────

    /** Skip non-top-level tokens silently until a top-level keyword or EOL. */
    private fun skipNonTopLevel(b: PsiBuilder) {
        while (!b.eof() && !isTopLevelStart(b.tokenType) && b.tokenType != RescriptTokenTypes.EOL) {
            b.advanceLexer()
        }
    }

    /**
     * Skip tokens until '{' is found, or stop if a top-level keyword
     * is encountered (for brace-less module aliases like `module X = Y`).
     */
    private fun skipToOpenBrace(b: PsiBuilder) {
        while (!b.eof()) {
            val t = b.tokenType
            if (t == RescriptTokenTypes.LBRACE) return
            if (isTopLevelStart(t)) return
            b.advanceLexer()
        }
    }

    /**
     * Skip tokens until the next top-level keyword at brace-depth 0.
     * This is a heuristic that lets us carve out top-level declarations
     * without fully parsing expressions. JSX elements encountered during
     * skipping are parsed into PSI nodes.
     */
    private fun skipToEndOfDeclaration(b: PsiBuilder) {
        var braceDepth = 0
        var parenDepth = 0

        while (!b.eof()) {
            when (b.tokenType) {
                RescriptTokenTypes.LBRACE -> {
                    braceDepth++
                    b.advanceLexer()
                }
                RescriptTokenTypes.RBRACE -> {
                    if (braceDepth > 0) {
                        braceDepth--
                        b.advanceLexer()
                        if (braceDepth == 0 && parenDepth == 0) return
                    } else {
                        return
                    }
                }
                RescriptTokenTypes.LPAREN -> {
                    parenDepth++
                    b.advanceLexer()
                }
                RescriptTokenTypes.RPAREN -> {
                    if (parenDepth > 0) parenDepth--
                    b.advanceLexer()
                }
                RescriptTokenTypes.TAG_LT -> {
                    // Try to parse JSX inside declaration body
                    if (!tryParseJsx(b)) {
                        b.advanceLexer()
                    }
                }
                RescriptTokenTypes.LT -> {
                    // Try to parse JSX fragment (<> ... </>)
                    if (!tryParseJsxFragment(b)) {
                        if (braceDepth == 0 && parenDepth == 0 && isTopLevelStart(b.tokenType)) return
                        b.advanceLexer()
                    }
                }
                else -> {
                    if (braceDepth == 0 && parenDepth == 0 && isTopLevelStart(b.tokenType)) return
                    b.advanceLexer()
                }
            }
        }
    }

    private fun skipBalanced(
        b: PsiBuilder,
        open: IElementType,
        close: IElementType,
    ) {
        var depth = 0
        while (!b.eof()) {
            when (b.tokenType) {
                open -> depth++
                close -> {
                    depth--
                    if (depth == 0) {
                        b.advanceLexer()
                        return
                    }
                }
            }
            b.advanceLexer()
        }
    }

    private fun isTopLevelStart(token: IElementType?): Boolean =
        token != null &&
            RescriptTokenTypes.TOP_LEVEL_KEYWORDS.contains(token) ||
            token == RescriptTokenTypes.ARROBASE
}
