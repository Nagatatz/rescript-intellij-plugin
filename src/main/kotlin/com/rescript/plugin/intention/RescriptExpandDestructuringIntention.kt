package com.rescript.plugin.intention

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.rescript.plugin.util.RescriptEditorUtils.getLineRangeAt
import com.rescript.plugin.util.RescriptEditorUtils.getLineTextAt
import com.rescript.plugin.util.RescriptEditorUtils.replaceInWriteAction

/**
 * Intention action to expand a record destructuring binding into
 * individual let bindings for each field.
 *
 * Detects single-line `let {field1, field2} = expr` patterns using
 * regex matching and transforms them into separate let bindings:
 * ```
 * let field1 = expr.field1
 * let field2 = expr.field2
 * ```
 *
 * Supports field aliasing (`name: n` -> `let n = expr.name`).
 * Does not support spread (`...rest`) or nested destructuring.
 *
 * Triggered via Alt+Enter > "Expand destructuring".
 *
 * Implements [RescriptBaseIntention] for intention action support.
 */
class RescriptExpandDestructuringIntention : RescriptBaseIntention() {
    override fun getText(): String = "Expand destructuring"

    override fun isAvailableInRescript(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ): Boolean {
        val document = editor?.document ?: return false
        val offset = element.textRange.startOffset

        val (lineStart, _) = document.getLineRangeAt(offset)
        val line = document.getLineTextAt(offset)

        // Check that this line is a destructuring pattern
        val match = DESTRUCTURING_PATTERN.find(line) ?: return false
        val fieldsStr = match.groupValues[1]

        // Reject spread patterns
        if (fieldsStr.contains("...")) return false

        // Reject nested destructuring
        if (fieldsStr.contains("{") || fieldsStr.contains("}")) return false

        // Check cursor is within the { ... } range
        val braceOpen = line.indexOf('{')
        val braceClose = line.indexOf('}')
        val posInLine = offset - lineStart
        return posInLine in braceOpen..braceClose
    }

    override fun invoke(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ) {
        editor ?: return
        val document = editor.document
        val offset = element.textRange.startOffset

        val (lineStart, lineEnd) = document.getLineRangeAt(offset)
        val line = document.getLineTextAt(offset)

        val expanded = expandDestructuring(line) ?: return

        document.replaceInWriteAction(project, lineStart, lineEnd, expanded)
    }

    companion object {
        // Matches: let {field1, field2, ...} = expr
        internal val DESTRUCTURING_PATTERN =
            Regex("""(\s*)let\s+\{([^}]+)}\s*=\s*(.+)""")

        /**
         * Expands a destructuring let binding into individual let bindings.
         *
         * @param line the source line containing `let {fields} = expr`
         * @return the expanded let bindings, or null if the line doesn't match
         */
        internal fun expandDestructuring(line: String): String? {
            val match = DESTRUCTURING_PATTERN.find(line) ?: return null
            val indent = match.groupValues[1]
            val fieldsStr = match.groupValues[2]
            val expr = match.groupValues[3].trim()

            // Reject spread patterns
            if (fieldsStr.contains("...")) return null

            // Reject nested destructuring
            if (fieldsStr.contains("{") || fieldsStr.contains("}")) return null

            val fields = parseFields(fieldsStr)
            if (fields.isEmpty()) return null

            return fields.joinToString("\n") { (originalName, bindingName) ->
                "${indent}let $bindingName = $expr.$originalName"
            }
        }

        /**
         * Parses destructuring field list into pairs of (original name, binding name).
         *
         * For simple fields like `name`, both values are the same.
         * For aliased fields like `name: n`, original is `name` and binding is `n`.
         *
         * @param fieldsStr the comma-separated field string from inside `{ ... }`
         * @return list of (originalName, bindingName) pairs
         */
        internal fun parseFields(fieldsStr: String): List<Pair<String, String>> =
            fieldsStr
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .map { field ->
                    if (field.contains(":")) {
                        // Alias pattern: name: n
                        val parts = field.split(":", limit = 2)
                        val originalName = parts[0].trim()
                        val bindingName = parts[1].trim()
                        originalName to bindingName
                    } else {
                        field to field
                    }
                }
    }
}
