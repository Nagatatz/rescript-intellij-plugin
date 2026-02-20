package com.rescript.plugin.injection

import com.intellij.lang.Language
import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import com.rescript.plugin.lang.RescriptTokenTypes
import com.rescript.plugin.lang.psi.RescriptStringLiteral

/**
 * Injects JavaScript language into `%raw("...")`, `%%raw(`...`)`,
 * `%ffi("...")`, and `%%ffi(`...`)` blocks in ReScript files,
 * enabling JS syntax highlighting within raw and FFI expressions.
 */
class RescriptRawJsInjector : MultiHostInjector {
    override fun elementsToInjectIn(): List<Class<out PsiElement>> = listOf(RescriptStringLiteral::class.java)

    override fun getLanguagesToInject(
        registrar: MultiHostRegistrar,
        context: PsiElement,
    ) {
        if (context !is RescriptStringLiteral) return

        val jsLanguage = findJsLanguage() ?: return
        if (!isInsideRawBlock(context)) return

        val range = getInjectionRange(context) ?: return

        registrar.startInjecting(jsLanguage)
        registrar.addPlace(null, null, context, range)
        registrar.doneInjecting()
    }

    private fun findJsLanguage(): Language? =
        Language.findLanguageByID("JavaScript")
            ?: Language.findLanguageByID("ECMAScript 6")

    /**
     * Checks if the given element is a string literal inside a `%raw()`, `%%raw()`,
     * `%ffi()`, or `%%ffi()` block by walking backwards through PSI siblings to find
     * the PERCENT -> (RAW|FFI) -> LPAREN pattern.
     */
    private fun isInsideRawBlock(element: PsiElement): Boolean {
        var current = element.prevSibling

        // Skip whitespace/EOL
        current = skipWhitespace(current)

        // For template string content, skip past JS_STRING_OPEN token
        if (current != null && current.node.elementType == RescriptTokenTypes.JS_STRING_OPEN) {
            current = skipWhitespace(current.prevSibling)
        }

        // Expect LPAREN
        if (current == null || current.node.elementType != RescriptTokenTypes.LPAREN) return false
        current = skipWhitespace(current.prevSibling)

        // Expect RAW or FFI
        if (current == null) return false
        val keywordType = current.node.elementType
        if (keywordType != RescriptTokenTypes.RAW && keywordType != RescriptTokenTypes.FFI) return false
        current = skipWhitespace(current.prevSibling)

        // Expect at least one PERCENT
        return current != null && current.node.elementType == RescriptTokenTypes.PERCENT
    }

    /**
     * Returns the text range within the element to inject JavaScript into.
     * For regular strings ("..."), trims the enclosing quotes.
     * For template string content (inside backticks), uses the full text.
     */
    private fun getInjectionRange(element: RescriptStringLiteral): TextRange? {
        val text = element.text
        if (text.isEmpty()) return null

        // Regular string: "content" -> trim quotes
        if (text.startsWith("\"") && text.endsWith("\"") && text.length >= 2) {
            if (text.length == 2) return null // empty string ""
            return TextRange(1, text.length - 1)
        }

        // Template string content (no quotes, backtick is a separate token)
        return TextRange(0, text.length)
    }

    private fun skipWhitespace(start: PsiElement?): PsiElement? {
        var current = start
        while (current != null) {
            val type = current.node.elementType
            if (type != TokenType.WHITE_SPACE && type != RescriptTokenTypes.EOL) {
                return current
            }
            current = current.prevSibling
        }
        return null
    }
}
