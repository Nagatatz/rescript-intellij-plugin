package com.rescript.plugin.documentation

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.psi.PsiElement
import com.rescript.plugin.lang.psi.RescriptElementTypes
import com.rescript.plugin.lang.psi.RescriptFile
import com.rescript.plugin.lang.psi.RescriptPsiUtils

/**
 * Provides Quick Documentation (Ctrl+Q / hover) for ReScript elements.
 *
 * When the LSP server is connected, documentation is primarily served via the LSP's
 * `textDocument/hover` response. This provider supplies a PSI-based fallback that
 * shows declaration type, name, and file location for cases where LSP is unavailable.
 *
 * @see com.rescript.plugin.lsp.RescriptLspServerDescriptor for LSP integration
 */
class RescriptDocumentationProvider : AbstractDocumentationProvider() {
    override fun generateDoc(
        element: PsiElement?,
        originalElement: PsiElement?,
    ): String? {
        if (element == null) return null
        return generateDocumentation(element)
    }

    override fun generateHoverDoc(
        element: PsiElement,
        originalElement: PsiElement?,
    ): String? = generateDoc(element, originalElement)

    override fun getQuickNavigateInfo(
        element: PsiElement?,
        originalElement: PsiElement?,
    ): String? {
        if (element == null) return null
        val name = RescriptPsiUtils.extractName(element)
        val type = getDeclarationType(element) ?: return null
        return "$type $name"
    }

    companion object {
        /**
         * Generates HTML documentation for a ReScript element.
         *
         * Produces a simple documentation panel showing the declaration keyword,
         * name, and containing file.
         *
         * @param element the PSI element to document
         * @return HTML documentation string, or null if not documentable
         */
        internal fun generateDocumentation(element: PsiElement): String? {
            val declType = getDeclarationType(element) ?: return null
            val name = RescriptPsiUtils.extractName(element)
            val fileName = element.containingFile?.name ?: "unknown"

            return buildString {
                append("<div class='definition'><pre>")
                append("<b>$declType</b> $name")
                append("</pre></div>")
                append("<div class='content'>")
                append("<p>Defined in <code>$fileName</code></p>")
                append("</div>")
            }
        }

        /**
         * Returns a human-readable declaration type label for the element.
         *
         * @param element the PSI element
         * @return the declaration type string (e.g., "let", "type"), or null if not a declaration
         */
        internal fun getDeclarationType(element: PsiElement): String? {
            if (element is RescriptFile) return "file"
            return when (element.node?.elementType) {
                RescriptElementTypes.LET_DECLARATION -> "let"
                RescriptElementTypes.TYPE_DECLARATION -> "type"
                RescriptElementTypes.MODULE_DECLARATION -> "module"
                RescriptElementTypes.EXTERNAL_DECLARATION -> "external"
                RescriptElementTypes.EXCEPTION_DECLARATION -> "exception"
                RescriptElementTypes.OPEN_STATEMENT -> "open"
                RescriptElementTypes.INCLUDE_STATEMENT -> "include"
                else -> null
            }
        }
    }
}
