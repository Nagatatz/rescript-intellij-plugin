package com.rescript.plugin.spellcheck

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy
import com.rescript.plugin.lang.RescriptTokenTypes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class RescriptSpellcheckingStrategyTest {
    private val strategy = RescriptSpellcheckingStrategy()

    private fun stubElement(type: IElementType): PsiElement =
        java.lang.reflect.Proxy.newProxyInstance(
            PsiElement::class.java.classLoader,
            arrayOf(PsiElement::class.java),
        ) { _, method, _ ->
            when (method.name) {
                "getNode" ->
                    java.lang.reflect.Proxy.newProxyInstance(
                        ASTNode::class.java.classLoader,
                        arrayOf(ASTNode::class.java),
                    ) { _, m, _ ->
                        when (m.name) {
                            "getElementType" -> type
                            "toString" -> "StubASTNode($type)"
                            "hashCode" -> type.hashCode()
                            "equals" -> false
                            else -> null
                        }
                    } as ASTNode

                "toString" -> "StubPsiElement($type)"
                "hashCode" -> type.hashCode()
                "equals" -> false
                else -> null
            }
        } as PsiElement

    private fun stubElementNullNode(): PsiElement =
        java.lang.reflect.Proxy.newProxyInstance(
            PsiElement::class.java.classLoader,
            arrayOf(PsiElement::class.java),
        ) { _, method, _ ->
            when (method.name) {
                "getNode" -> null
                "toString" -> "StubPsiElement(null)"
                "hashCode" -> 0
                "equals" -> false
                else -> null
            }
        } as PsiElement

    @Test
    fun `SINGLE_COMMENT returns TEXT_TOKENIZER`() {
        val tokenizer = strategy.getTokenizer(stubElement(RescriptTokenTypes.SINGLE_COMMENT))
        assertEquals(SpellcheckingStrategy.TEXT_TOKENIZER, tokenizer)
    }

    @Test
    fun `MULTI_COMMENT returns TEXT_TOKENIZER`() {
        val tokenizer = strategy.getTokenizer(stubElement(RescriptTokenTypes.MULTI_COMMENT))
        assertEquals(SpellcheckingStrategy.TEXT_TOKENIZER, tokenizer)
    }

    @Test
    fun `STRING_VALUE returns TEXT_TOKENIZER`() {
        val tokenizer = strategy.getTokenizer(stubElement(RescriptTokenTypes.STRING_VALUE))
        assertEquals(SpellcheckingStrategy.TEXT_TOKENIZER, tokenizer)
    }

    @Test
    fun `LIDENT returns identifier tokenizer`() {
        val tokenizer = strategy.getTokenizer(stubElement(RescriptTokenTypes.LIDENT))
        assertNotEquals(SpellcheckingStrategy.EMPTY_TOKENIZER, tokenizer)
        assertNotEquals(SpellcheckingStrategy.TEXT_TOKENIZER, tokenizer)
    }

    @Test
    fun `UIDENT returns identifier tokenizer`() {
        val tokenizer = strategy.getTokenizer(stubElement(RescriptTokenTypes.UIDENT))
        assertNotEquals(SpellcheckingStrategy.EMPTY_TOKENIZER, tokenizer)
        assertNotEquals(SpellcheckingStrategy.TEXT_TOKENIZER, tokenizer)
    }

    @Test
    fun `LBRACE returns EMPTY_TOKENIZER`() {
        val tokenizer = strategy.getTokenizer(stubElement(RescriptTokenTypes.LBRACE))
        assertEquals(SpellcheckingStrategy.EMPTY_TOKENIZER, tokenizer)
    }

    @Test
    fun `LET returns EMPTY_TOKENIZER`() {
        val tokenizer = strategy.getTokenizer(stubElement(RescriptTokenTypes.LET))
        assertEquals(SpellcheckingStrategy.EMPTY_TOKENIZER, tokenizer)
    }

    @Test
    fun `null node returns EMPTY_TOKENIZER`() {
        val tokenizer = strategy.getTokenizer(stubElementNullNode())
        assertEquals(SpellcheckingStrategy.EMPTY_TOKENIZER, tokenizer)
    }
}
