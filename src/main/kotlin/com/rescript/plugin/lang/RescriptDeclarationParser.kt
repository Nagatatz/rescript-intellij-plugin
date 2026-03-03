package com.rescript.plugin.lang

import com.intellij.lang.PsiBuilder
import com.intellij.psi.tree.IElementType
import com.rescript.plugin.lang.psi.RescriptElementTypes

/**
 * Parses top-level declarations (let, type, external, open, include,
 * exception, annotations) within ReScript source files.
 *
 * Extracted from [RescriptParser] for focused responsibility.
 * Declaration body skipping is provided via a callback to avoid
 * circular dependencies with JSX parsing.
 *
 * @see RescriptParser
 * @see RescriptJsxParser
 */
internal object RescriptDeclarationParser {
    /**
     * Parses `let [rec] name ...` or `type [rec] name ...` etc.
     *
     * @param b the PsiBuilder
     * @param elementType the PSI element type for this declaration
     * @param consumeRec whether to consume an optional `rec` keyword
     * @param skipToEnd callback to skip tokens until end of declaration
     */
    fun parseDeclaration(
        b: PsiBuilder,
        elementType: IElementType,
        consumeRec: Boolean = false,
        skipToEnd: (PsiBuilder) -> Unit,
    ) {
        val m = b.mark()
        b.advanceLexer() // consume keyword

        if (consumeRec && b.tokenType == RescriptTokenTypes.REC) {
            b.advanceLexer()
        }

        // consume identifier (lident or uident or _)
        if (b.tokenType in RescriptParser.IDENTIFIER_TOKENS) {
            b.advanceLexer()
        } else if (!b.eof() && !RescriptParser.isTopLevelStart(b.tokenType)) {
            // R2: Report missing identifier
            b.error("Expected identifier")
        }

        skipToEnd(b)
        m.done(elementType)
    }

    /**
     * Parses `open ...` / `include ...` — just skips to end.
     *
     * @param b the PsiBuilder
     * @param elementType the PSI element type for this statement
     * @param skipToEnd callback to skip tokens until end of declaration
     */
    fun parseSimple(
        b: PsiBuilder,
        elementType: IElementType,
        skipToEnd: (PsiBuilder) -> Unit,
    ) {
        val m = b.mark()
        b.advanceLexer()
        skipToEnd(b)
        m.done(elementType)
    }

    /** Parses an annotation starting with `@`. */
    fun parseAnnotation(b: PsiBuilder) {
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
            RescriptParser.skipBalanced(b, RescriptTokenTypes.LPAREN, RescriptTokenTypes.RPAREN)
        }

        m.done(RescriptElementTypes.ANNOTATION)
    }
}
