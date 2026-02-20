package com.rescript.plugin.documentation

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.psi.PsiElement
import com.rescript.plugin.RescriptLanguage
import com.rescript.plugin.lang.RescriptTokenTypes

/**
 * Provides external documentation URLs for ReScript standard library modules.
 *
 * Triggered by Shift+F1 to open the corresponding page on rescript-lang.org.
 * Supports Belt and Js module hierarchies.
 *
 * @see <a href="https://rescript-lang.org/docs/manual/latest/api">ReScript API Docs</a>
 */
class RescriptDocumentationProvider : AbstractDocumentationProvider() {
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
