package com.rescript.plugin.navigation

import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.EditorColorsScheme
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.ui.SimpleTextAttributes
import com.rescript.plugin.highlight.RescriptSyntaxHighlighter
import com.rescript.plugin.lang.RescriptLexer

/**
 * Tokenises a ReScript type signature string with the production
 * [RescriptLexer] and resolves each token to a [SimpleTextAttributes]
 * pair by consulting the global editor colour scheme through
 * [RescriptSyntaxHighlighter].
 *
 * Used by the Hoogle-style search cell renderer so that signatures
 * rendered into a `ColoredListCellRenderer` look identical to the
 * surrounding editor — keywords, type constructors, operators, type
 * variables and punctuation each receive the colour they would have
 * inside an open `.res` editor instead of being painted as one
 * grey-italic blob.
 *
 * Pure: the public [tokenize] entry point is a string-to-list helper
 * with no UI dependencies, so it can be exercised in unit tests
 * without spinning up a Swing fixture.
 */
internal object RescriptSignatureTokenColorizer {
    /**
     * One coloured run produced by [tokenize].
     *
     * @property text the substring covered by this token, ready to
     *   hand straight to `ColoredListCellRenderer.append`
     * @property attributes the resolved foreground / style attributes
     *   to render [text] with
     */
    data class Token(
        val text: String,
        val attributes: SimpleTextAttributes,
    )

    /**
     * Splits [signature] into coloured tokens.
     *
     * The lexer is the same JFlex-generated [RescriptLexer] the editor
     * uses, so any token the editor recognises maps onto the same
     * [TextAttributesKey] here. Tokens with no registered key (e.g.
     * `LIDENT`, whitespace, parens) fall back to
     * [SimpleTextAttributes.REGULAR_ATTRIBUTES] so the surrounding text
     * still renders in the list's default foreground.
     *
     * @param signature the type signature to colour, exactly as it
     *   should appear in the list cell
     * @return the coloured token sequence in source order; empty when
     *   [signature] is empty
     */
    fun tokenize(signature: String): List<Token> {
        if (signature.isEmpty()) return emptyList()
        val lexer = RescriptLexer().apply { start(signature) }
        val highlighter = RescriptSyntaxHighlighter()
        val scheme = EditorColorsManager.getInstance().globalScheme
        val result = mutableListOf<Token>()
        while (lexer.tokenType != null) {
            val tokenType = lexer.tokenType!!
            val text = signature.substring(lexer.tokenStart, lexer.tokenEnd)
            val keys = highlighter.getTokenHighlights(tokenType)
            result.add(Token(text, resolveAttributes(keys, scheme)))
            lexer.advance()
        }
        return result
    }

    private fun resolveAttributes(
        keys: Array<TextAttributesKey>,
        scheme: EditorColorsScheme,
    ): SimpleTextAttributes {
        if (keys.isEmpty()) return SimpleTextAttributes.REGULAR_ATTRIBUTES
        val resolved =
            scheme.getAttributes(keys.first())
                ?: return SimpleTextAttributes.REGULAR_ATTRIBUTES
        // If the scheme stores nothing useful (null foreground and no
        // style flags), fall back to the regular colour so we never
        // render an invisible token.
        if (resolved.foregroundColor == null && resolved.backgroundColor == null && resolved.fontType == 0) {
            return SimpleTextAttributes.REGULAR_ATTRIBUTES
        }
        return SimpleTextAttributes.fromTextAttributes(resolved)
    }
}
