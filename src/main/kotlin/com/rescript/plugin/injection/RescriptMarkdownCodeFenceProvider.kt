package com.rescript.plugin.injection

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.Language
import com.rescript.plugin.RescriptLanguage
import org.intellij.plugins.markdown.injection.CodeFenceLanguageProvider

/**
 * Provides ReScript language injection for Markdown code fences.
 *
 * Recognizes `rescript`, `res`, and `resi` as info-string identifiers
 * to enable ReScript syntax highlighting within Markdown documents.
 */
class RescriptMarkdownCodeFenceProvider : CodeFenceLanguageProvider {
    override fun getLanguageByInfoString(infoString: String): Language? {
        val trimmed = infoString.trim().lowercase()
        return if (trimmed in RESCRIPT_IDENTIFIERS) {
            RescriptLanguage
        } else {
            null
        }
    }

    override fun getCompletionVariantsForInfoString(completionParameters: CompletionParameters): List<LookupElement> =
        listOf(LookupElementBuilder.create("rescript"))

    companion object {
        private val RESCRIPT_IDENTIFIERS = setOf("rescript", "res", "resi")
    }
}
