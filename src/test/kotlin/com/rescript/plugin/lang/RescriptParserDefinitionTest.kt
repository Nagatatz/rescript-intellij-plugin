package com.rescript.plugin.lang

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptParserDefinitionTest {
    private val definition = RescriptParserDefinition()

    @Test
    fun testCreateLexerReturnsRescriptLexer() {
        val lexer = definition.createLexer(null)
        assertTrue(lexer is RescriptLexer)
    }

    @Test
    fun testCreateParserReturnsRescriptParser() {
        val parser = definition.createParser(null)
        assertTrue(parser is RescriptParser)
    }

    @Test
    fun testGetFileNodeType() {
        val fileType = definition.fileNodeType
        assertNotNull(fileType)
        assertEquals(RescriptParserDefinition.FILE, fileType)
    }

    @Test
    fun testGetCommentTokensContainsSingleComment() {
        assertTrue(definition.commentTokens.contains(RescriptTokenTypes.SINGLE_COMMENT))
    }

    @Test
    fun testGetCommentTokensContainsMultiComment() {
        assertTrue(definition.commentTokens.contains(RescriptTokenTypes.MULTI_COMMENT))
    }

    @Test
    fun testGetStringLiteralElementsContainsStringValue() {
        assertTrue(definition.stringLiteralElements.contains(RescriptTokenTypes.STRING_VALUE))
    }
}
