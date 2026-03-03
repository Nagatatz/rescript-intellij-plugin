package com.rescript.plugin.lsp

import com.intellij.lang.ExpressionTypeProvider
import com.intellij.psi.PsiElement
import com.rescript.plugin.RescriptLanguage
import com.rescript.plugin.util.RescriptOffsetUtils
import org.eclipse.lsp4j.HoverParams
import org.eclipse.lsp4j.TextDocumentIdentifier

/**
 * Provides expression type information for ReScript via LSP hover.
 *
 * Triggered by Ctrl+Shift+P to show the inferred type of the expression
 * at the caret position. Extracts type information from the LSP
 * `textDocument/hover` response.
 *
 * @see RescriptLspServerDescriptor for LSP server configuration
 */
class RescriptExpressionTypeProvider : ExpressionTypeProvider<PsiElement>() {
    override fun getInformationHint(element: PsiElement): String {
        val file = element.containingFile ?: return NO_TYPE
        val project = file.project
        val virtualFile = file.virtualFile ?: return NO_TYPE

        try {
            val server = RescriptLspUtils.getServer(project) ?: return NO_TYPE

            val document = file.viewProvider.document ?: return NO_TYPE
            val offset = element.textRange.startOffset
            val position = RescriptOffsetUtils.offsetToPosition(document, offset)

            val uri = RescriptLspUtils.toLspUri(virtualFile)

            val params =
                HoverParams(
                    TextDocumentIdentifier(uri),
                    position,
                )

            val hoverResult =
                server.sendRequestSync { languageServer ->
                    languageServer.textDocumentService.hover(params)
                } ?: return NO_TYPE

            val content = hoverResult.contents ?: return NO_TYPE

            val typeText =
                when {
                    content.isLeft ->
                        content.left
                            .firstOrNull()
                            ?.let { if (it.isLeft) it.left else it.right.value }
                    content.isRight -> {
                        val markdown = content.right.value
                        extractTypeFromMarkdown(markdown)
                    }
                    else -> null
                }

            return typeText?.let { escapeHtml(it) } ?: NO_TYPE
        } catch (_: Exception) {
            return NO_TYPE
        }
    }

    override fun getExpressionsAt(elementAt: PsiElement): List<PsiElement> {
        if (elementAt.language != RescriptLanguage) return emptyList()
        return listOf(elementAt)
    }

    override fun getErrorHint(): String = NO_TYPE

    companion object {
        private const val NO_TYPE = "No type information available"

        /** Pattern matching a Markdown code fence block (with optional `rescript` language tag). */
        private val CODE_FENCE_PATTERN =
            Regex("```(?:rescript)?\\s*\\n(.+?)\\n```", RegexOption.DOT_MATCHES_ALL)

        /** Pattern matching an inline code span in Markdown. */
        private val INLINE_CODE_PATTERN = Regex("`(.+?)`")

        /**
         * Extracts type information from LSP hover markdown content.
         *
         * The hover response typically contains a code block with the type signature.
         * This method extracts the text within the first code fence.
         *
         * @param markdown the raw markdown from LSP hover
         * @return the extracted type text, or the raw content if no code fence found
         */
        internal fun extractTypeFromMarkdown(markdown: String): String {
            // Try to extract from code fence
            CODE_FENCE_PATTERN.find(markdown)?.let { return it.groupValues[1].trim() }

            // Try inline code
            INLINE_CODE_PATTERN.find(markdown)?.let { return it.groupValues[1].trim() }

            return markdown.trim()
        }

        private fun escapeHtml(text: String): String =
            text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
    }
}
