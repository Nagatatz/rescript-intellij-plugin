package com.rescript.plugin.documentation

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.psi.PsiElement
import com.rescript.plugin.RescriptLanguage
import com.rescript.plugin.lang.RescriptTokenTypes
import com.rescript.plugin.lang.psi.RescriptElementTypes
import com.rescript.plugin.lang.psi.RescriptFile
import com.rescript.plugin.lang.psi.RescriptPsiUtils

/**
 * Provides Quick Documentation (Ctrl+Q / hover) and external documentation URLs for ReScript elements.
 *
 * When the LSP server is connected, documentation is primarily served via the LSP's
 * `textDocument/hover` response. This provider supplies a PSI-based fallback that
 * shows declaration type, name, and file location for cases where LSP is unavailable.
 * External documentation (Shift+F1) opens the corresponding page on rescript-lang.org
 * for standard library modules (Belt.*, Js.*).
 *
 * @see com.rescript.plugin.lsp.RescriptLspServerDescriptor for LSP integration
 * @see <a href="https://rescript-lang.org/docs/manual/latest/api">ReScript API Docs</a>
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

    override fun getUrlFor(
        element: PsiElement?,
        originalElement: PsiElement?,
    ): List<String>? {
        if (element == null) return null
        if (element.language != RescriptLanguage) return null

        val modulePath = resolveModulePath(element, originalElement) ?: return null
        val urlPath = MODULE_URL_MAP[modulePath] ?: return null

        return listOf("$BASE_URL/$urlPath")
    }

    companion object {
        private const val BASE_URL = "https://rescript-lang.org/docs/manual/latest/api"

        /**
         * Maps ReScript standard library module paths to their documentation URL segments.
         */
        val MODULE_URL_MAP =
            mapOf(
                // Belt modules
                "Belt" to "belt",
                "Belt.Array" to "belt/array",
                "Belt.List" to "belt/list",
                "Belt.Map" to "belt/map",
                "Belt.Map.Dict" to "belt/map-dict",
                "Belt.Map.Int" to "belt/map-int",
                "Belt.Map.String" to "belt/map-string",
                "Belt.Set" to "belt/set",
                "Belt.Set.Dict" to "belt/set-dict",
                "Belt.Set.Int" to "belt/set-int",
                "Belt.Set.String" to "belt/set-string",
                "Belt.HashMap" to "belt/hash-map",
                "Belt.HashMap.Int" to "belt/hash-map-int",
                "Belt.HashMap.String" to "belt/hash-map-string",
                "Belt.HashSet" to "belt/hash-set",
                "Belt.HashSet.Int" to "belt/hash-set-int",
                "Belt.HashSet.String" to "belt/hash-set-string",
                "Belt.MutableMap" to "belt/mutable-map",
                "Belt.MutableMap.Int" to "belt/mutable-map-int",
                "Belt.MutableMap.String" to "belt/mutable-map-string",
                "Belt.MutableSet" to "belt/mutable-set",
                "Belt.MutableSet.Int" to "belt/mutable-set-int",
                "Belt.MutableSet.String" to "belt/mutable-set-string",
                "Belt.MutableQueue" to "belt/mutable-queue",
                "Belt.MutableStack" to "belt/mutable-stack",
                "Belt.SortArray" to "belt/sort-array",
                "Belt.SortArray.Int" to "belt/sort-array-int",
                "Belt.SortArray.String" to "belt/sort-array-string",
                "Belt.Int" to "belt/int",
                "Belt.Float" to "belt/float",
                "Belt.Option" to "belt/option",
                "Belt.Result" to "belt/result",
                "Belt.Range" to "belt/range",
                "Belt.Id" to "belt/id",
                // Js modules
                "Js" to "js",
                "Js.Array" to "js/array",
                "Js.Array2" to "js/array-2",
                "Js.String" to "js/string",
                "Js.String2" to "js/string-2",
                "Js.Promise" to "js/promise",
                "Js.Promise2" to "js/promise-2",
                "Js.Json" to "js/json",
                "Js.Math" to "js/math",
                "Js.Date" to "js/date",
                "Js.Re" to "js/re",
                "Js.Dict" to "js/dict",
                "Js.Null" to "js/null",
                "Js.Nullable" to "js/nullable",
                "Js.Undefined" to "js/undefined",
                "Js.Exn" to "js/exn",
                "Js.Console" to "js/console",
                "Js.Float" to "js/float",
                "Js.Int" to "js/int",
                "Js.Obj" to "js/obj",
                "Js.Option" to "js/option",
                "Js.Result" to "js/result",
                "Js.TypedArray2" to "js/typed-array-2",
                "Js.Types" to "js/types",
                "Js.Global" to "js/global",
            )

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
            if (fullPath in MODULE_URL_MAP) return fullPath

            // Try without the last segment (might be a function, not a module)
            for (i in parts.size downTo 1) {
                val subPath = parts.subList(0, i).joinToString(".")
                if (subPath in MODULE_URL_MAP) return subPath
            }

            return null
        }
    }
}
