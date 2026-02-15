package com.rescript.plugin.lang

import com.intellij.psi.tree.IElementType
import org.junit.Assert.assertEquals
import org.junit.Test

class RescriptLexerTest {
    private fun tokenize(input: String): List<Pair<IElementType, String>> {
        val lexer = RescriptLexer()
        lexer.start(input)
        val tokens = mutableListOf<Pair<IElementType, String>>()
        while (lexer.tokenType != null) {
            tokens.add(lexer.tokenType!! to lexer.tokenText)
            lexer.advance()
        }
        return tokens
    }

    private fun tokenTypes(input: String): List<IElementType> = tokenize(input).map { it.first }

    @Test
    fun `JSX opening tag - div`() {
        val tokens = tokenize("<div>")
        assertEquals(
            listOf(
                RescriptTokenTypes.TAG_LT to "<",
                RescriptTokenTypes.JSX_TAG_NAME to "div",
                RescriptTokenTypes.TAG_GT to ">",
            ),
            tokens,
        )
    }

    @Test
    fun `JSX closing tag - div`() {
        val tokens = tokenize("</div>")
        assertEquals(
            listOf(
                RescriptTokenTypes.TAG_LT_SLASH to "</",
                RescriptTokenTypes.JSX_TAG_NAME to "div",
                RescriptTokenTypes.TAG_GT to ">",
            ),
            tokens,
        )
    }

    @Test
    fun `JSX component tag - uppercase`() {
        val tokens = tokenize("<Component>")
        assertEquals(
            listOf(
                RescriptTokenTypes.TAG_LT to "<",
                RescriptTokenTypes.JSX_COMPONENT_NAME to "Component",
                RescriptTokenTypes.TAG_GT to ">",
            ),
            tokens,
        )
    }

    @Test
    fun `JSX module path tag`() {
        val tokens = tokenize("<Module.Sub.Comp>")
        assertEquals(
            listOf(
                RescriptTokenTypes.TAG_LT to "<",
                RescriptTokenTypes.JSX_COMPONENT_NAME to "Module",
                RescriptTokenTypes.DOT to ".",
                RescriptTokenTypes.JSX_COMPONENT_NAME to "Sub",
                RescriptTokenTypes.DOT to ".",
                RescriptTokenTypes.JSX_COMPONENT_NAME to "Comp",
                RescriptTokenTypes.TAG_GT to ">",
            ),
            tokens,
        )
    }

    @Test
    fun `JSX self-closing tag`() {
        val tokens = tokenize("<br />")
        val types = tokens.map { it.first }
        assertEquals(RescriptTokenTypes.TAG_LT, types[0])
        assertEquals(RescriptTokenTypes.JSX_TAG_NAME, types[1])
        assertEquals("br", tokens[1].second)
        assertEquals(RescriptTokenTypes.TAG_AUTO_CLOSE, types.last())
    }

    @Test
    fun `JSX closing module tag`() {
        val tokens = tokenize("</Module.Comp>")
        assertEquals(
            listOf(
                RescriptTokenTypes.TAG_LT_SLASH to "</",
                RescriptTokenTypes.JSX_COMPONENT_NAME to "Module",
                RescriptTokenTypes.DOT to ".",
                RescriptTokenTypes.JSX_COMPONENT_NAME to "Comp",
                RescriptTokenTypes.TAG_GT to ">",
            ),
            tokens,
        )
    }

    @Test
    fun `JSX tag with props`() {
        val tokens = tokenize("<Comp name>")
        val types = tokens.map { it.first }
        assertEquals(RescriptTokenTypes.TAG_LT, types[0])
        assertEquals(RescriptTokenTypes.JSX_COMPONENT_NAME, types[1])
        assertEquals("Comp", tokens[1].second)
        // After tag name, space triggers exit from IN_JSX_TAG_NAME
        // "name" should be LIDENT (not JSX_TAG_NAME)
        assertEquals(RescriptTokenTypes.LIDENT, types[3]) // index 3 because whitespace is 2
    }

    @Test
    fun `plain less-than is not JSX`() {
        val tokens = tokenize("1 < 2")
        val types = tokens.map { it.first }
        // "<" followed by space, not a letter - should stay as LT, no JSX
        assertEquals(RescriptTokenTypes.LT, types[2])
    }

    @Test
    fun `let binding not affected`() {
        val types = tokenTypes("let x = 5")
        assertEquals(RescriptTokenTypes.LET, types[0])
    }
}
