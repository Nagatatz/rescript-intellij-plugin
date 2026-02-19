package com.rescript.plugin.spellcheck

import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy
import com.rescript.plugin.RescriptTestUtils
import com.rescript.plugin.lang.RescriptTokenTypes
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Test

class RescriptSpellcheckingStrategyTest {
    private val strategy = RescriptSpellcheckingStrategy()

    @Test
    fun `single comment returns TEXT_TOKENIZER`() {
        val element = RescriptTestUtils.SimpleStubElement(RescriptTokenTypes.SINGLE_COMMENT, "// comment")
        val tokenizer = strategy.getTokenizer(element)
        assertSame(SpellcheckingStrategy.TEXT_TOKENIZER, tokenizer)
    }

    @Test
    fun `multi comment returns TEXT_TOKENIZER`() {
        val element = RescriptTestUtils.SimpleStubElement(RescriptTokenTypes.MULTI_COMMENT, "/* comment */")
        val tokenizer = strategy.getTokenizer(element)
        assertSame(SpellcheckingStrategy.TEXT_TOKENIZER, tokenizer)
    }

    @Test
    fun `string value returns TEXT_TOKENIZER`() {
        val element = RescriptTestUtils.SimpleStubElement(RescriptTokenTypes.STRING_VALUE, "\"hello\"")
        val tokenizer = strategy.getTokenizer(element)
        assertSame(SpellcheckingStrategy.TEXT_TOKENIZER, tokenizer)
    }

    @Test
    fun `lident returns identifier tokenizer`() {
        val element = RescriptTestUtils.SimpleStubElement(RescriptTokenTypes.LIDENT, "myVariable")
        val tokenizer = strategy.getTokenizer(element)
        assertNotSame(SpellcheckingStrategy.EMPTY_TOKENIZER, tokenizer)
        assertNotSame(SpellcheckingStrategy.TEXT_TOKENIZER, tokenizer)
    }

    @Test
    fun `uident returns identifier tokenizer`() {
        val element = RescriptTestUtils.SimpleStubElement(RescriptTokenTypes.UIDENT, "MyModule")
        val tokenizer = strategy.getTokenizer(element)
        assertNotSame(SpellcheckingStrategy.EMPTY_TOKENIZER, tokenizer)
        assertNotSame(SpellcheckingStrategy.TEXT_TOKENIZER, tokenizer)
    }

    @Test
    fun `keyword returns EMPTY_TOKENIZER`() {
        val element = RescriptTestUtils.SimpleStubElement(RescriptTokenTypes.LET, "let")
        val tokenizer = strategy.getTokenizer(element)
        assertSame(SpellcheckingStrategy.EMPTY_TOKENIZER, tokenizer)
    }

    @Test
    fun `operator returns EMPTY_TOKENIZER`() {
        val element = RescriptTestUtils.SimpleStubElement(RescriptTokenTypes.PLUS, "+")
        val tokenizer = strategy.getTokenizer(element)
        assertSame(SpellcheckingStrategy.EMPTY_TOKENIZER, tokenizer)
    }

    @Test
    fun `int value returns EMPTY_TOKENIZER`() {
        val element = RescriptTestUtils.SimpleStubElement(RescriptTokenTypes.INT_VALUE, "42")
        val tokenizer = strategy.getTokenizer(element)
        assertSame(SpellcheckingStrategy.EMPTY_TOKENIZER, tokenizer)
    }
}
