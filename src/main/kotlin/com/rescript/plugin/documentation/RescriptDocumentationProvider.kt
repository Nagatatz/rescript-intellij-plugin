package com.rescript.plugin.documentation

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
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
 * @see com.rescript.plugin.lsp.RescriptLspServerDescriptor for LSP integration
 * @see <a href="https://rescript-lang.org/docs/manual/latest/api">ReScript API Docs</a>
 */
class RescriptDocumentationProvider : AbstractDocumentationProvider() {
    override fun generateDoc(
        element: PsiElement?,
        originalElement: PsiElement?,
    ): String? {
        if (element == null) return null

        // Check for operator documentation
        val operatorDoc = generateOperatorDoc(originalElement ?: element)
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
        val urlPath = MODULE_URL_MAP[modulePath] ?: return null

        return listOf("$BASE_URL/$urlPath")
    }

    companion object {
        private const val BASE_URL = "https://rescript-lang.org/docs/manual/latest/api"

        /**
         * Generates HTML documentation for a ReScript operator token.
         *
         * Shows the operator symbol, its name, precedence level, associativity,
         * and a short description.
         *
         * @param element the PSI element (expected to be an operator token)
         * @return HTML documentation string, or null if not an operator
         */
        fun generateOperatorDoc(element: PsiElement): String? {
            val tokenType = element.node?.elementType ?: return null
            val info = OPERATOR_INFO[tokenType] ?: return null

            val escapedText = RescriptSecurityUtils.escapeHtml(element.text)
            val escapedName = RescriptSecurityUtils.escapeHtml(info.name)
            val escapedDesc = RescriptSecurityUtils.escapeHtml(info.description)

            return buildString {
                append("<div class='definition'><pre>")
                append("<b>$escapedText</b> — $escapedName")
                append("</pre></div>")
                append("<div class='content'>")
                append("<table>")
                append("<tr><td><b>Precedence:</b></td><td>${info.precedence}</td></tr>")
                append("<tr><td><b>Associativity:</b></td><td>${info.associativity}</td></tr>")
                append("</table>")
                append("<p>$escapedDesc</p>")
                append("</div>")
            }
        }

        /**
         * Information about a ReScript operator for documentation purposes.
         *
         * @param name human-readable name of the operator
         * @param precedence numeric precedence level (higher binds tighter)
         * @param associativity left, right, or none
         * @param description brief explanation of the operator
         */
        data class OperatorInfo(
            val name: String,
            val precedence: Int,
            val associativity: String,
            val description: String,
        )

        /**
         * Maps operator token types to their precedence and description information.
         *
         * Precedence levels follow ReScript's operator precedence table,
         * from lowest (1) to highest (16).
         */
        val OPERATOR_INFO: Map<IElementType, OperatorInfo> =
            mapOf(
                // Pipe operators
                RescriptTokenTypes.PIPE_FORWARD to
                    OperatorInfo(
                        "Pipe forward",
                        1,
                        "Left",
                        "Pipes the left operand as the last argument to the right function.",
                    ),
                RescriptTokenTypes.ARROW to
                    OperatorInfo(
                        "Pipe",
                        1,
                        "Left",
                        "Pipes the left operand as the first argument to the right function.",
                    ),
                // Logical operators
                RescriptTokenTypes.L_OR to
                    OperatorInfo(
                        "Logical OR",
                        2,
                        "Left",
                        "Short-circuit logical OR. Returns true if either operand is true.",
                    ),
                RescriptTokenTypes.L_AND to
                    OperatorInfo(
                        "Logical AND",
                        3,
                        "Left",
                        "Short-circuit logical AND. Returns true if both operands are true.",
                    ),
                // Comparison operators
                RescriptTokenTypes.EQEQEQ to
                    OperatorInfo(
                        "Strict equality",
                        4,
                        "Left",
                        "Compiles to JavaScript `===`. Checks referential equality.",
                    ),
                RescriptTokenTypes.EQEQ to
                    OperatorInfo("Structural equality", 4, "Left", "Compiles to a deep structural equality check."),
                RescriptTokenTypes.NOT_EQEQ to
                    OperatorInfo(
                        "Strict inequality",
                        4,
                        "Left",
                        "Compiles to JavaScript `!==`. Checks referential inequality.",
                    ),
                RescriptTokenTypes.NOT_EQ to
                    OperatorInfo("Structural inequality", 4, "Left", "Compiles to a deep structural inequality check."),
                RescriptTokenTypes.LT to
                    OperatorInfo("Less than", 4, "Left", "Numeric or string comparison: less than."),
                RescriptTokenTypes.GT to
                    OperatorInfo("Greater than", 4, "Left", "Numeric or string comparison: greater than."),
                RescriptTokenTypes.LT_OR_EQUAL to
                    OperatorInfo("Less than or equal", 4, "Left", "Numeric or string comparison: less than or equal."),
                // String concatenation
                RescriptTokenTypes.STRING_CONCAT to
                    OperatorInfo("String concatenation", 5, "Right", "Concatenates two strings using the ++ operator."),
                // Arithmetic operators
                RescriptTokenTypes.PLUS to
                    OperatorInfo("Addition", 6, "Left", "Integer addition."),
                RescriptTokenTypes.MINUS to
                    OperatorInfo("Subtraction", 6, "Left", "Integer subtraction."),
                RescriptTokenTypes.PLUSDOT to
                    OperatorInfo("Float addition", 6, "Left", "Floating-point addition."),
                RescriptTokenTypes.MINUSDOT to
                    OperatorInfo("Float subtraction", 6, "Left", "Floating-point subtraction."),
                RescriptTokenTypes.STAR to
                    OperatorInfo("Multiplication", 7, "Left", "Integer multiplication."),
                RescriptTokenTypes.SLASH to
                    OperatorInfo("Division", 7, "Left", "Integer division."),
                RescriptTokenTypes.STARDOT to
                    OperatorInfo("Float multiplication", 7, "Left", "Floating-point multiplication."),
                RescriptTokenTypes.SLASHDOT to
                    OperatorInfo("Float division", 7, "Left", "Floating-point division."),
                RescriptTokenTypes.PERCENT to
                    OperatorInfo("Modulo", 7, "Left", "Integer modulo (remainder)."),
                RescriptTokenTypes.CARRET to
                    OperatorInfo("Exponentiation", 8, "Right", "Raises a float to a power (via Js.Math.pow)."),
                // Bitwise operators
                RescriptTokenTypes.LAND to
                    OperatorInfo("Bitwise AND", 9, "Left", "Bitwise AND of two integers."),
                RescriptTokenTypes.LOR to
                    OperatorInfo("Bitwise OR", 9, "Left", "Bitwise OR of two integers."),
                RescriptTokenTypes.LXOR to
                    OperatorInfo("Bitwise XOR", 9, "Left", "Bitwise XOR of two integers."),
                RescriptTokenTypes.LSL to
                    OperatorInfo("Left shift", 10, "Left", "Bitwise left shift."),
                RescriptTokenTypes.LSR to
                    OperatorInfo("Logical right shift", 10, "Left", "Bitwise logical (unsigned) right shift."),
                RescriptTokenTypes.ASR to
                    OperatorInfo("Arithmetic right shift", 10, "Left", "Bitwise arithmetic (signed) right shift."),
                // Assignment
                RescriptTokenTypes.COLON_EQ to
                    OperatorInfo("Ref assignment", 11, "Right", "Assigns a new value to a ref cell."),
                // Arrow
                RescriptTokenTypes.RIGHT_ARROW to
                    OperatorInfo("Function arrow", 0, "Right", "Separates function parameters from the body."),
            )

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
