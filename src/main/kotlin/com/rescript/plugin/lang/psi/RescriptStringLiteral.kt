package com.rescript.plugin.lang.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.LiteralTextEscaper
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.tree.IElementType

/**
 * Custom leaf PSI element for ReScript string literals that supports language injection.
 *
 * This enables IntelliJ's language injection framework to inject languages
 * (e.g., JavaScript in `%raw()` blocks) into ReScript string literals.
 */
class RescriptStringLiteral(
    type: IElementType,
    text: CharSequence,
) : LeafPsiElement(type, text),
    PsiLanguageInjectionHost {
    override fun isValidHost(): Boolean = true

    override fun updateText(text: String): PsiLanguageInjectionHost {
        val replacement = RescriptStringLiteral(elementType, text)
        return replace(replacement) as? PsiLanguageInjectionHost ?: this
    }

    override fun createLiteralTextEscaper(): LiteralTextEscaper<out PsiLanguageInjectionHost> {
        return object : LiteralTextEscaper<RescriptStringLiteral>(this) {
            override fun decode(
                rangeInsideHost: TextRange,
                outChars: StringBuilder,
            ): Boolean {
                outChars.append(rangeInsideHost.substring(myHost.text))
                return true
            }

            override fun getOffsetInHost(
                offsetInDecoded: Int,
                rangeInsideHost: TextRange,
            ): Int = rangeInsideHost.startOffset + offsetInDecoded

            override fun isOneLine(): Boolean = false
        }
    }
}
