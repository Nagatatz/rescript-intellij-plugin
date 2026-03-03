package com.rescript.plugin.documentation

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.psi.PsiElement
import com.rescript.plugin.RescriptLanguage
import com.rescript.plugin.lang.RescriptTokenTypes
import com.rescript.plugin.lang.psi.RescriptElementTypes
import com.rescript.plugin.lang.psi.RescriptFile
import com.rescript.plugin.lang.psi.RescriptPsiUtils
import com.rescript.plugin.util.RescriptSecurityUtils

/**
 * Provides Quick Documentation (Ctrl+Q / hover) and external documentation URLs for ReScript elements.
 *
 * When the LSP server is connected, documentation is primarily served via the LSP's
 * `textDocument/hover` response. This provider supplies a PSI-based fallback that
 * shows declaration type, name, and file location for cases where LSP is unavailable.
 * External documentation (Shift+F1) opens the corresponding page on rescript-lang.org
 * for standard library modules (Belt.*, Js.*).
 *
 * Additionally provides operator precedence information when hovering over operators
 * (e.g., `->`, `++`, `===`).
 *
 * Operator documentation is delegated to [RescriptOperatorDocumentation] and
 * external URL mappings to [RescriptExternalDocUrls].
 *
 * @see com.rescript.plugin.lsp.RescriptLspServerDescriptor for LSP integration
 * @see RescriptOperatorDocumentation
 * @see RescriptExternalDocUrls
 */
class RescriptDocumentationProvider : AbstractDocumentationProvider() {
    override fun generateDoc(
        element: PsiElement?,
        originalElement: PsiElement?,
    ): String? {
        if (element == null) return null

        // Check for operator documentation
        val operatorDoc = RescriptOperatorDocumentation.generateOperatorDoc(originalElement ?: element)
        if (operatorDoc != null) return operatorDoc

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

    override fun getUrlFor(
        element: PsiElement?,
        originalElement: PsiElement?,
    ): List<String>? {
        if (element == null) return null
        if (element.language != RescriptLanguage) return null

        val modulePath = resolveModulePath(element, originalElement) ?: return null
        val urlPath = RescriptExternalDocUrls.MODULE_URL_MAP[modulePath] ?: return null

        return listOf("${RescriptExternalDocUrls.BASE_URL}/$urlPath")
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
            val name = RescriptSecurityUtils.escapeHtml(RescriptPsiUtils.extractName(element))
            val fileName = RescriptSecurityUtils.escapeHtml(element.containingFile?.name ?: "unknown")

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

        /**
         * Resolves the module path from the PSI element at the caret position.
         *
         * Builds a dot-separated path from consecutive uppercase identifiers
         * (e.g., `Belt.Array` from `Belt` `.` `Array`).
         *
         * @param element the resolved element
         * @param originalElement the element at the caret position
         * @return the module path string, or null if not resolvable
         */
        internal fun resolveModulePath(
            element: PsiElement,
            originalElement: PsiElement?,
        ): String? {
            val target = originalElement ?: element
            val tokenType = target.node?.elementType ?: return null

            // Must be an uppercase identifier (module name)
            if (tokenType != RescriptTokenTypes.UIDENT) return null

            // Build the full module path by looking at dot-separated identifiers
            val parts = mutableListOf(target.text)

            // Scan backward for "Dot UIDENT" patterns
            var prev = target.prevSibling
            while (prev != null) {
                if (prev.node?.elementType == RescriptTokenTypes.DOT) {
                    val beforeDot = prev.prevSibling
                    if (beforeDot?.node?.elementType == RescriptTokenTypes.UIDENT) {
                        parts.add(0, beforeDot.text)
                        prev = beforeDot.prevSibling
                        continue
                    }
                }
                break
            }

            // Scan forward for "DOT UIDENT" patterns
            var next = target.nextSibling
            while (next != null) {
                if (next.node?.elementType == RescriptTokenTypes.DOT) {
                    val afterDot = next.nextSibling
                    if (afterDot?.node?.elementType == RescriptTokenTypes.UIDENT) {
                        parts.add(afterDot.text)
                        next = afterDot.nextSibling
                        continue
                    }
                }
                break
            }

            // Try progressively shorter paths to find a match
            val fullPath = parts.joinToString(".")
            if (fullPath in RescriptExternalDocUrls.MODULE_URL_MAP) return fullPath

            // Try without the last segment (might be a function, not a module)
            for (i in parts.size downTo 1) {
                val subPath = parts.subList(0, i).joinToString(".")
                if (subPath in RescriptExternalDocUrls.MODULE_URL_MAP) return subPath
            }

            return null
        }
    }
}
