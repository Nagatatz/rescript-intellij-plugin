package com.rescript.plugin.navigation

import com.intellij.codeInsight.folding.impl.ElementSignatureProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.rescript.plugin.lang.psi.RescriptPsiUtils

/**
 * Provides unique signatures for ReScript declaration elements to support
 * fold state persistence and element tracking across file modifications.
 *
 * Generates signatures in the format `<TYPE>#<name>#<offset>` (e.g.,
 * `LET_DECLARATION#myFunc#42`) and restores elements by offset lookup.
 * Implements [ElementSignatureProvider].
 *
 * @see RescriptQualifiedNameProvider
 */
class RescriptElementSignatureProvider : ElementSignatureProvider {
    companion object {
        private const val SEPARATOR = "#"

        /**
         * Creates a signature string for a declaration element.
         *
         * @param element the PSI element to create a signature for
         * @return a signature in the format `TYPE#name#offset`, or null if not a declaration
         */
        internal fun createSignature(element: PsiElement): String? {
            val elementType = element.node?.elementType ?: return null
            if (elementType !in RescriptPsiUtils.NAVIGABLE_TYPES) return null

            val typeName = elementType.toString()
            val name = RescriptPsiUtils.extractName(element)
            val offset = element.textRange?.startOffset ?: return null

            return "$typeName$SEPARATOR$name$SEPARATOR$offset"
        }

        /**
         * Parses a signature string into its components.
         *
         * @param signature the signature to parse
         * @return a triple of (typeName, name, offset), or null if malformed
         */
        internal fun parseSignature(signature: String): Triple<String, String, Int>? {
            val parts = signature.split(SEPARATOR)
            if (parts.size != 3) return null
            val offset = parts[2].toIntOrNull() ?: return null
            return Triple(parts[0], parts[1], offset)
        }
    }

    override fun getSignature(element: PsiElement): String? = createSignature(element)

    override fun restoreBySignature(
        file: PsiFile,
        signature: String,
        processingInfoStorage: StringBuilder?,
    ): PsiElement? {
        val (_, _, offset) = parseSignature(signature) ?: return null
        if (offset < 0 || offset >= file.textLength) return null

        val elementAtOffset = file.findElementAt(offset) ?: return null
        return RescriptPsiUtils.findEnclosingDeclaration(elementAtOffset, RescriptPsiUtils.NAVIGABLE_TYPES)
    }
}
