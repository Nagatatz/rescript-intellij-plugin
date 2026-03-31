package com.rescript.plugin.completion

import com.intellij.codeInsight.completion.CompletionConfidence
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.util.ThreeState
import com.rescript.plugin.RescriptLanguage
import com.rescript.plugin.lang.RescriptTokenTypes

/**
 * Controls when the auto-completion popup should be suppressed in ReScript files.
 *
 * Prevents the popup from appearing inside comments, string literals, and
 * template string delimiters where code completions are not useful.
 *
 * @see RescriptPostfixTemplateProvider for the shared non-applicable token set
 */
class RescriptCompletionConfidence : CompletionConfidence() {
    // shouldSkipAutopopup() is deprecated but no replacement API exists;
    // required to suppress popups inside comments and strings.
    @Suppress("OVERRIDE_DEPRECATION")
    override fun shouldSkipAutopopup(
        contextElement: PsiElement,
        psiFile: PsiFile,
        offset: Int,
    ): ThreeState {
        if (psiFile.language != RescriptLanguage) return ThreeState.UNSURE

        val tokenType = contextElement.node?.elementType ?: return ThreeState.UNSURE
        return if (tokenType in NON_CODE_TOKENS) {
            ThreeState.YES
        } else {
            ThreeState.UNSURE
        }
    }

    companion object {
        private val NON_CODE_TOKENS =
            setOf(
                RescriptTokenTypes.SINGLE_COMMENT,
                RescriptTokenTypes.MULTI_COMMENT,
                RescriptTokenTypes.STRING_VALUE,
                RescriptTokenTypes.JS_STRING_OPEN,
                RescriptTokenTypes.JS_STRING_CLOSE,
            )
    }
}
