package com.rescript.plugin.editor

import com.intellij.lang.Language
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.impl.PsiBasedStripTrailingSpacesFilter
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.rescript.plugin.RescriptLanguage
import com.rescript.plugin.lang.RescriptTokenTypes

/**
 * Provides a trailing-space stripping filter for ReScript files.
 *
 * Enforces trailing whitespace removal on save while protecting content
 * inside string literals from being modified. Implements
 * [PsiBasedStripTrailingSpacesFilter.Factory].
 */
class RescriptStripTrailingSpacesFilterFactory : PsiBasedStripTrailingSpacesFilter.Factory() {
    override fun createFilter(document: Document): PsiBasedStripTrailingSpacesFilter =
        RescriptStripTrailingSpacesFilter(document)

    override fun isApplicableTo(language: Language): Boolean = language.isKindOf(RescriptLanguage)
}

/**
 * Filter that disables trailing-space stripping on ranges that contain
 * string literals, preserving intentional whitespace inside strings.
 *
 * Extends [PsiBasedStripTrailingSpacesFilter] to scan PSI tokens and
 * mark string-containing ranges as non-strippable via [disableRange].
 */
private class RescriptStripTrailingSpacesFilter(
    document: Document,
) : PsiBasedStripTrailingSpacesFilter(document) {
    companion object {
        /** Token types representing string content that should be protected. */
        internal val STRING_TOKEN_TYPES =
            setOf(
                RescriptTokenTypes.STRING_VALUE,
                RescriptTokenTypes.JS_STRING_OPEN,
                RescriptTokenTypes.JS_STRING_CLOSE,
            )
    }

    override fun process(psiFile: PsiFile) {
        // Walk leaf elements and disable stripping on ranges with string tokens
        walkLeaves(psiFile.firstChild)
    }

    private fun walkLeaves(root: PsiElement?) {
        var current = root
        while (current != null) {
            if (current.firstChild != null) {
                walkLeaves(current.firstChild)
            } else {
                val tokenType = current.node?.elementType
                if (tokenType in STRING_TOKEN_TYPES) {
                    disableRange(current.textRange, false)
                }
            }
            current = current.nextSibling
        }
    }
}
