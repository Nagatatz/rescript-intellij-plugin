package com.rescript.plugin.injection

import com.intellij.lang.Language
import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.rescript.plugin.lang.RescriptTokenTypes
import com.rescript.plugin.lang.psi.RescriptStringLiteral
import com.rescript.plugin.util.RescriptBraceBalanceUtil

/**
 * Injects languages into ReScript extension expressions:
 * - `%raw("...")` / `%%raw(`...`)` / `%ffi("...")` / `%%ffi(`...`)` → JavaScript
 * - `%re("/pattern/flags")` → RegExp
 *
 * Detects the extension directive by walking backwards through PSI siblings
 * to find the `PERCENT -> keyword/identifier -> LPAREN` pattern.
 *
 * Implements [MultiHostInjector] for language injection support.
 */
class RescriptRawJsInjector : MultiHostInjector {
    override fun elementsToInjectIn(): List<Class<out PsiElement>> = listOf(RescriptStringLiteral::class.java)

    override fun getLanguagesToInject(
        registrar: MultiHostRegistrar,
        context: PsiElement,
    ) {
        if (context !is RescriptStringLiteral) return

        val injectionInfo = detectInjection(context) ?: return
        val range = getInjectionRange(context, injectionInfo) ?: return

        registrar.startInjecting(injectionInfo.language)
        registrar.addPlace(injectionInfo.prefix, injectionInfo.suffix, context, range)
        registrar.doneInjecting()
    }

    /**
     * Detects which language should be injected based on the extension directive
     * surrounding the string literal.
     *
     * @param element the string literal to check
     * @return injection info if inside a supported extension block, null otherwise
     */
    internal fun detectInjection(element: PsiElement): InjectionInfo? {
        val directiveName = detectDirective(element) ?: return null

        return when (directiveName) {
            "raw", "ffi" -> {
                val jsLanguage = findJsLanguage() ?: return null
                InjectionInfo(jsLanguage, isRegex = false)
            }

            "re" -> {
                val regexpLanguage = Language.findLanguageByID("RegExp") ?: return null
                InjectionInfo(regexpLanguage, isRegex = true)
            }

            else -> {
                null
            }
        }
    }

    /**
     * Detects the extension directive name by walking backwards through PSI siblings.
     * Returns the directive name ("raw", "ffi", or "re") if found, null otherwise.
     *
     * @param element the string literal to check
     * @return the directive name, or null if not inside a supported extension block
     */
    internal fun detectDirective(element: PsiElement): String? {
        var current = element.prevSibling

        // Skip whitespace/EOL
        current = RescriptBraceBalanceUtil.skipWhitespaceAndEolBackward(current)

        // For template string content, skip past JS_STRING_OPEN token
        if (current != null && current.node.elementType == RescriptTokenTypes.JS_STRING_OPEN) {
            current = RescriptBraceBalanceUtil.skipWhitespaceAndEolBackward(current.prevSibling)
        }

        // Expect LPAREN
        if (current == null || current.node.elementType != RescriptTokenTypes.LPAREN) return null
        current = RescriptBraceBalanceUtil.skipWhitespaceAndEolBackward(current.prevSibling)

        // Expect RAW, FFI, or LIDENT("re")
        if (current == null) return null
        val keywordType = current.node.elementType
        val directiveName =
            when (keywordType) {
                RescriptTokenTypes.RAW -> "raw"
                RescriptTokenTypes.FFI -> "ffi"
                RescriptTokenTypes.LIDENT -> if (current.text == "re") "re" else return null
                else -> return null
            }
        current = RescriptBraceBalanceUtil.skipWhitespaceAndEolBackward(current.prevSibling)

        // Expect at least one PERCENT
        if (current == null || current.node.elementType != RescriptTokenTypes.PERCENT) return null

        return directiveName
    }

    private fun findJsLanguage(): Language? =
        Language.findLanguageByID("JavaScript")
            ?: Language.findLanguageByID("ECMAScript 6")

    /**
     * Returns the text range within the element to inject the language into.
     * For regular strings ("..."), trims the enclosing quotes.
     * For template string content (inside backticks), uses the full text.
     * For regex strings ("/pattern/flags"), extracts the pattern between slashes.
     *
     * @param element the string literal element
     * @param info injection info describing the directive type
     * @return the text range to inject into, or null if the string is empty
     */
    private fun getInjectionRange(
        element: RescriptStringLiteral,
        info: InjectionInfo,
    ): TextRange? {
        val text = element.text
        if (text.isEmpty()) return null

        // Regular string: "content" -> trim quotes
        if (text.startsWith("\"") && text.endsWith("\"") && text.length >= 2) {
            if (text.length == 2) return null // empty string ""
            val innerStart = 1
            val innerEnd = text.length - 1
            val inner = text.substring(innerStart, innerEnd)

            // For %re: extract pattern from /pattern/flags
            if (info.isRegex) {
                return getRegexPatternRange(inner, innerStart)
            }

            return TextRange(innerStart, innerEnd)
        }

        // Template string content (no quotes, backtick is a separate token)
        if (info.isRegex) {
            return getRegexPatternRange(text, 0)
        }
        return TextRange(0, text.length)
    }

    /**
     * Extracts the regex pattern range from a `/pattern/flags` string.
     *
     * @param content the string content (without surrounding quotes)
     * @param baseOffset the base offset to add to the returned range
     * @return text range covering only the pattern portion, or null if invalid
     */
    internal fun getRegexPatternRange(
        content: String,
        baseOffset: Int,
    ): TextRange? {
        if (!content.startsWith("/")) return null

        // Find the closing slash (last / before optional flags)
        val lastSlash = content.lastIndexOf('/')
        if (lastSlash <= 0) return null

        val patternStart = baseOffset + 1
        val patternEnd = baseOffset + lastSlash
        if (patternStart >= patternEnd) return null

        return TextRange(patternStart, patternEnd)
    }

    /**
     * Describes a language injection including the target language and
     * optional prefix/suffix for the injected fragment.
     *
     * @param language the language to inject
     * @param isRegex true if this is a regex injection (affects range extraction)
     * @param prefix optional text to prepend to the injected fragment
     * @param suffix optional text to append to the injected fragment
     */
    data class InjectionInfo(
        val language: Language,
        val isRegex: Boolean,
        val prefix: String? = null,
        val suffix: String? = null,
    )
}
