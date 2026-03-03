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
 *
 * Declaration parsing is delegated to [RescriptDeclarationParser] and
 * JSX parsing to [RescriptJsxParser].
 *
 * @see RescriptDeclarationParser
 * @see RescriptJsxParser
 */
class RescriptParser : PsiParser {
    companion object {
        /** Set of token types that represent identifiers (lident, uident, underscore). */
        internal val IDENTIFIER_TOKENS =
            setOf(RescriptTokenTypes.LIDENT, RescriptTokenTypes.UIDENT, RescriptTokenTypes.UNDERSCORE)

        /**
         * Checks whether the given token type starts a top-level declaration.
         *
         * @param token the token type to check
         * @return true if the token can begin a top-level declaration
         */
        internal fun isTopLevelStart(token: IElementType?): Boolean =
            token != null &&
                (
                    RescriptTokenTypes.TOP_LEVEL_KEYWORDS.contains(token) ||
                        token == RescriptTokenTypes.ARROBASE
                )

        /**
         * Skips balanced pairs of open/close tokens (e.g., parentheses, braces).
         *
         * @param b the PsiBuilder
         * @param open the opening token type
         * @param close the closing token type
         */
        internal fun skipBalanced(
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
    }

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
            RescriptTokenTypes.LET ->
                RescriptDeclarationParser.parseDeclaration(
                    b,
                    RescriptElementTypes.LET_DECLARATION,
                    consumeRec = true,
                    skipToEnd = ::skipToEndOfDeclaration,
                )
            RescriptTokenTypes.TYPE ->
                RescriptDeclarationParser.parseDeclaration(
                    b,
                    RescriptElementTypes.TYPE_DECLARATION,
                    consumeRec = true,
                    skipToEnd = ::skipToEndOfDeclaration,
                )
            RescriptTokenTypes.MODULE -> parseModuleDeclaration(b)
            RescriptTokenTypes.EXTERNAL ->
                RescriptDeclarationParser.parseDeclaration(
                    b,
                    RescriptElementTypes.EXTERNAL_DECLARATION,
                    skipToEnd = ::skipToEndOfDeclaration,
                )
            RescriptTokenTypes.OPEN ->
                RescriptDeclarationParser.parseSimple(
                    b,
                    RescriptElementTypes.OPEN_STATEMENT,
                    ::skipToEndOfDeclaration,
                )
            RescriptTokenTypes.INCLUDE ->
                RescriptDeclarationParser.parseSimple(
                    b,
                    RescriptElementTypes.INCLUDE_STATEMENT,
                    ::skipToEndOfDeclaration,
                )
            RescriptTokenTypes.EXCEPTION ->
                RescriptDeclarationParser.parseDeclaration(
                    b,
                    RescriptElementTypes.EXCEPTION_DECLARATION,
                    skipToEnd = ::skipToEndOfDeclaration,
                )
            RescriptTokenTypes.ARROBASE -> RescriptDeclarationParser.parseAnnotation(b)
            RescriptTokenTypes.EOL -> b.advanceLexer()
            RescriptTokenTypes.TAG_LT -> {
                if (!RescriptJsxParser.tryParseJsx(b)) {
                    // Not valid JSX — skip tokens silently
                    skipNonTopLevel(b)
                }
            }
            RescriptTokenTypes.LT -> {
                // Fragment opening: <> (lexer produces LT + GT, not TAG_LT + TAG_GT)
                if (!RescriptJsxParser.tryParseJsxFragment(b)) {
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
                    if (!RescriptJsxParser.tryParseJsx(b)) {
                        b.advanceLexer()
                    }
                }
                RescriptTokenTypes.LT -> {
                    // Try to parse JSX fragment (<> ... </>)
                    if (!RescriptJsxParser.tryParseJsxFragment(b)) {
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
}
