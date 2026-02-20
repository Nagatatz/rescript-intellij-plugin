package com.rescript.plugin.completion

import com.intellij.codeInsight.template.TemplateActionContext
import com.intellij.codeInsight.template.TemplateContextType
import com.rescript.plugin.RescriptLanguage
import com.rescript.plugin.lang.RescriptTokenTypes

/**
 * Defines the context in which ReScript Live Templates are applicable.
 *
 * Templates are available only inside ReScript files and not within
 * comments or string literals. This replaces the generic "OTHER" context
 * that was previously used for all ReScript templates.
 *
 * @see RescriptModuleNameMacro for template variable macros
 */
class RescriptTemplateContextType : TemplateContextType("ReScript") {
    override fun isInContext(templateActionContext: TemplateActionContext): Boolean {
        val file = templateActionContext.file
        if (file.language != RescriptLanguage) return false

        val offset = templateActionContext.startOffset
        val element = file.findElementAt(offset) ?: return true
        val tokenType = element.node?.elementType ?: return true

        return tokenType !in NON_TEMPLATE_TOKENS
    }

    companion object {
        private val NON_TEMPLATE_TOKENS =
            setOf(
                RescriptTokenTypes.SINGLE_COMMENT,
                RescriptTokenTypes.MULTI_COMMENT,
                RescriptTokenTypes.STRING_VALUE,
                RescriptTokenTypes.JS_STRING_OPEN,
                RescriptTokenTypes.JS_STRING_CLOSE,
            )
    }
}
