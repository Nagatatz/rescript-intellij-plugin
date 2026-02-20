package com.rescript.plugin.lang

import com.intellij.lang.cacheBuilder.DefaultWordsScanner
import com.intellij.lang.cacheBuilder.WordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet
import com.rescript.plugin.lang.psi.RescriptElementTypes
import com.rescript.plugin.lang.psi.RescriptPsiUtils

/**
 * Provides Find Usages support for ReScript files.
 *
 * Supplies a [WordsScanner] that tokenizes ReScript code into identifiers,
 * comments, and string literals for indexing. Also provides human-readable
 * type names and descriptions for symbols displayed in usage search results.
 *
 * @see RescriptPsiUtils for name extraction and icon resolution
 */
class RescriptFindUsagesProvider : FindUsagesProvider {
    companion object {
        private val IDENTIFIER_TOKENS =
            TokenSet.create(
                RescriptTokenTypes.LIDENT,
                RescriptTokenTypes.UIDENT,
            )
    }

    override fun getWordsScanner(): WordsScanner =
        DefaultWordsScanner(
            RescriptLexer(),
            IDENTIFIER_TOKENS,
            RescriptTokenTypes.COMMENTS,
            RescriptTokenTypes.STRINGS,
        )

    override fun canFindUsagesFor(element: PsiElement): Boolean {
        val elementType = element.node?.elementType ?: return false
        return elementType in RescriptPsiUtils.NAVIGABLE_TYPES
    }

    override fun getHelpId(element: PsiElement): String? = null

    override fun getType(element: PsiElement): String =
        when (element.node?.elementType) {
            RescriptElementTypes.LET_DECLARATION -> "function"
            RescriptElementTypes.TYPE_DECLARATION -> "type"
            RescriptElementTypes.MODULE_DECLARATION -> "module"
            RescriptElementTypes.EXTERNAL_DECLARATION -> "external"
            RescriptElementTypes.EXCEPTION_DECLARATION -> "exception"
            else -> "element"
        }

    override fun getDescriptiveName(element: PsiElement): String = RescriptPsiUtils.extractName(element)

    override fun getNodeText(
        element: PsiElement,
        useFullName: Boolean,
    ): String {
        val text = element.text ?: return ""
        // Return first line only for brevity
        val firstLine = text.lineSequence().firstOrNull() ?: ""
        return if (firstLine.length > 80) firstLine.take(80) + "..." else firstLine
    }
}
