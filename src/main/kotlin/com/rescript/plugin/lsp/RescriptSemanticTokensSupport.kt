package com.rescript.plugin.lsp

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.platform.lsp.api.customization.LspSemanticTokensSupport
import com.intellij.psi.PsiFile
import com.rescript.plugin.highlight.RescriptSyntaxHighlighter

class RescriptSemanticTokensSupport : LspSemanticTokensSupport() {
    override fun shouldAskServerForSemanticTokens(psiFile: PsiFile): Boolean = true

    override fun getTextAttributesKey(
        tokenType: String,
        modifiers: List<String>,
    ): TextAttributesKey? =
        when (tokenType) {
            "variable" -> RescriptSyntaxHighlighter.SEMANTIC_VARIABLE
            "type" -> RescriptSyntaxHighlighter.SEMANTIC_TYPE
            "namespace" -> RescriptSyntaxHighlighter.SEMANTIC_NAMESPACE
            "enumMember" -> RescriptSyntaxHighlighter.SEMANTIC_ENUM_MEMBER
            "property" -> RescriptSyntaxHighlighter.SEMANTIC_PROPERTY
            "interface" -> RescriptSyntaxHighlighter.SEMANTIC_INTERFACE
            "operator" -> RescriptSyntaxHighlighter.SEMANTIC_OPERATOR
            "modifier" -> RescriptSyntaxHighlighter.SEMANTIC_MODIFIER
            else -> null // Preserve lexer-based highlighting for unknown token types
        }
}
