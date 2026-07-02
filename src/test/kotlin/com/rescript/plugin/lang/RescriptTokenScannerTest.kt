package com.rescript.plugin.lang

import com.intellij.psi.TokenType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** Verifies that [RescriptTokenScanner] filters trivia and classifies ignorable tokens. */
class RescriptTokenScannerTest {
    @Test
    fun `tokenize drops whitespace comments and eol`() {
        val source =
            """
            // leading comment
            let x = 1 /* block */
            """.trimIndent()
        val types = RescriptTokenScanner.tokenize(source).map { it.type }

        assertFalse(types.contains(TokenType.WHITE_SPACE), "whitespace must be filtered")
        assertFalse(types.contains(RescriptTokenTypes.EOL), "EOL must be filtered")
        assertFalse(types.contains(RescriptTokenTypes.SINGLE_COMMENT), "single comments must be filtered")
        assertFalse(types.contains(RescriptTokenTypes.MULTI_COMMENT), "block comments must be filtered")
    }

    @Test
    fun `tokenize preserves significant tokens with offsets and text`() {
        val source = "let x = 1"
        val tokens = RescriptTokenScanner.tokenize(source)

        // let, x, =, 1
        assertEquals(4, tokens.size)
        assertEquals("let", tokens.first().text)
        assertEquals(0, tokens.first().start)
        assertEquals(3, tokens.first().end)
        assertEquals("1", tokens.last().text)
        assertEquals(source.length, tokens.last().end)
    }

    @Test
    fun `isIgnorable truth table`() {
        assertTrue(RescriptTokenScanner.isIgnorable(TokenType.WHITE_SPACE))
        assertTrue(RescriptTokenScanner.isIgnorable(RescriptTokenTypes.EOL))
        assertTrue(RescriptTokenScanner.isIgnorable(RescriptTokenTypes.SINGLE_COMMENT))
        assertTrue(RescriptTokenScanner.isIgnorable(RescriptTokenTypes.MULTI_COMMENT))

        assertFalse(RescriptTokenScanner.isIgnorable(RescriptTokenTypes.PIPE))
        assertFalse(RescriptTokenScanner.isIgnorable(RescriptTokenTypes.LIDENT))
        assertFalse(RescriptTokenScanner.isIgnorable(RescriptTokenTypes.SWITCH))
        assertFalse(RescriptTokenScanner.isIgnorable(RescriptTokenTypes.ARROW))
    }
}
