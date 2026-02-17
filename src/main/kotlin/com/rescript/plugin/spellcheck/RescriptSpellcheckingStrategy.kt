package com.rescript.plugin.spellcheck

import com.intellij.psi.PsiElement
import com.intellij.spellchecker.inspections.PlainTextSplitter
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy
import com.intellij.spellchecker.tokenizer.Tokenizer
import com.intellij.spellchecker.tokenizer.TokenizerBase
import com.rescript.plugin.lang.RescriptTokenTypes

class RescriptSpellcheckingStrategy : SpellcheckingStrategy() {
    private val identifierTokenizer: Tokenizer<PsiElement> =
        TokenizerBase(PlainTextSplitter.getInstance())

    override fun getTokenizer(element: PsiElement): Tokenizer<PsiElement> {
        val elementType = element.node?.elementType ?: return EMPTY_TOKENIZER

        return when (elementType) {
            RescriptTokenTypes.SINGLE_COMMENT,
            RescriptTokenTypes.MULTI_COMMENT,
            -> TEXT_TOKENIZER

            RescriptTokenTypes.STRING_VALUE -> TEXT_TOKENIZER

            RescriptTokenTypes.LIDENT,
            RescriptTokenTypes.UIDENT,
            -> identifierTokenizer

            else -> EMPTY_TOKENIZER
        }
    }
}
