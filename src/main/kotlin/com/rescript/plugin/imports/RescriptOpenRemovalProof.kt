package com.rescript.plugin.imports

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import com.rescript.plugin.lang.RescriptLexer
import com.rescript.plugin.lang.psi.RescriptElementTypes
import com.rescript.plugin.lang.RescriptTokenTypes as T

/** Proves a small subset of redundant opens without guessing module identity from spelling. */
internal object RescriptOpenRemovalProof {
    /**
     * Finds adjacent opens of the same proven local module within [scope].
     *
     * Supported modules export literal values only, so opening them cannot shadow their
     * own module name. Unknown declarations and opens invalidate the proof. Nested scopes
     * are deliberately independent, and repeated opens separated by code are preserved.
     */
    fun findRedundantOpens(scope: PsiElement): List<PsiElement> {
        // Attributes and embedded code can invalidate a source-only module shape proof.
        if (significantTokens(scope.text).any {
                it.first in setOf(T.ARROBASE, T.ANNOTATION_NAME, T.RAW, T.FFI, T.JS_STRING_OPEN)
            }
        ) {
            return emptyList()
        }
        val localModules = mutableSetOf<String>()
        val redundant = mutableListOf<PsiElement>()
        var previousOpen: String? = null
        for (child in scope.children) {
            val type = child.node?.elementType
            if (child.text.isBlank() || (type != null && T.COMMENTS.contains(type))) continue
            when (type) {
                RescriptElementTypes.MODULE_DECLARATION -> {
                    previousOpen = null
                    val name = literalModuleName(child)
                    if (name == null) localModules.clear() else localModules.add(name)
                }

                RescriptElementTypes.OPEN_STATEMENT -> {
                    val path = leadingOpen(child)?.first
                    if (path == null || path !in localModules) {
                        previousOpen = null
                        localModules.clear()
                    } else {
                        if (previousOpen == path) redundant.add(child)
                        if (significantTokens(child.text).map { it.first } == listOf(T.OPEN, T.UIDENT)) {
                            previousOpen = path
                        } else {
                            // The lightweight PSI can include expressions after the open line.
                            previousOpen = null
                            localModules.clear()
                        }
                    }
                }

                else -> {
                    previousOpen = null
                    localModules.clear()
                }
            }
        }
        return redundant
    }

    /** Returns only open syntax; callers must first prove the module identity. */
    fun removalRange(element: PsiElement): TextRange? = leadingOpen(element)?.second

    /** Validates a single-line open prefix without including following expressions or comments. */
    private fun leadingOpen(element: PsiElement): Pair<String, TextRange>? {
        val text = element.text.substringBefore('\n')
        val lexer = RescriptLexer()
        lexer.start(text)
        while (lexer.tokenType == TokenType.WHITE_SPACE) lexer.advance()
        if (lexer.tokenType != T.OPEN) return null
        val start = lexer.tokenStart
        lexer.advance()
        while (lexer.tokenType == TokenType.WHITE_SPACE) lexer.advance()
        if (lexer.tokenType != T.UIDENT) return null
        val name = text.substring(lexer.tokenStart, lexer.tokenEnd)
        val end = lexer.tokenEnd
        lexer.advance()
        while (lexer.tokenType != null) {
            val type = lexer.tokenType!!
            if (type != TokenType.WHITE_SPACE && type != T.EOL && !T.COMMENTS.contains(type)) return null
            lexer.advance()
        }
        val base = element.textRange.startOffset
        return name to TextRange(base + start, base + end)
    }

    /** Recognizes modules exporting only complete literal let bindings and no modules. */
    private fun literalModuleName(element: PsiElement): String? {
        if (PsiTreeUtil.findChildOfType(element, PsiErrorElement::class.java) != null) return null
        val tokens = significantTokens(element.text)
        if (tokens.size < 5 || tokens[0].first != T.MODULE || tokens[1].first != T.UIDENT ||
            tokens[2].first != T.EQ || tokens[3].first != T.LBRACE || tokens.last().first != T.RBRACE
        ) {
            return null
        }
        var index = 4
        while (index < tokens.lastIndex) {
            if (tokens.getOrNull(index)?.first != T.LET || tokens.getOrNull(index + 1)?.first != T.LIDENT ||
                tokens.getOrNull(index + 2)?.first != T.EQ
            ) {
                return null
            }
            val literal = tokens.getOrNull(index + 3) ?: return null
            if (literal.first !in
                setOf(T.INT_VALUE, T.FLOAT_VALUE, T.BOOL_VALUE, T.CHAR_VALUE, T.STRING_VALUE)
            ) {
                return null
            }
            if (literal.first == T.STRING_VALUE &&
                (
                    literal.second.length < 2 || !literal.second.endsWith('"') ||
                        literal.second
                            .dropLast(1)
                            .takeLastWhile { it == '\\' }
                            .length % 2 != 0
                )
            ) {
                return null
            }
            index += 4
        }
        return if (index == tokens.lastIndex) tokens[1].second else null
    }

    /** Reads lexical tokens without mistaking comments or string contents for declarations. */
    private fun significantTokens(text: String): List<Pair<IElementType, String>> {
        val lexer = RescriptLexer()
        lexer.start(text)
        return buildList {
            while (lexer.tokenType != null) {
                val type = lexer.tokenType!!
                if (type != TokenType.WHITE_SPACE && type != T.EOL && !T.COMMENTS.contains(type)) {
                    add(type to text.substring(lexer.tokenStart, lexer.tokenEnd))
                }
                lexer.advance()
            }
        }
    }
}
