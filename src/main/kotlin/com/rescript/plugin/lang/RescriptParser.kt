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
            else -> b.advanceLexer()
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

        // consume dotted name: react.component, module, etc.
        while (!b.eof()) {
            val t = b.tokenType
            if (t == RescriptTokenTypes.LIDENT || t == RescriptTokenTypes.UIDENT || t == RescriptTokenTypes.DOT) {
                b.advanceLexer()
            } else {
                break
            }
        }

        // optional arguments in parens
        if (b.tokenType == RescriptTokenTypes.LPAREN) {
            skipBalanced(b, RescriptTokenTypes.LPAREN, RescriptTokenTypes.RPAREN)
        }

        m.done(RescriptElementTypes.ANNOTATION)
    }

    // ── helpers ───────────────────────────────────────────────────────

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
     * without fully parsing expressions.
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
