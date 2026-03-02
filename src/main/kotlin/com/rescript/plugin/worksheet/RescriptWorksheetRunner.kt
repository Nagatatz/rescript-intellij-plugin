package com.rescript.plugin.worksheet

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

/**
 * Executes ReScript worksheet files by evaluating each top-level expression
 * individually and displaying the results as inline comments.
 *
 * Each expression is wrapped in a temporary `.res` file, compiled via
 * `npx rescript`, and executed with `node`. Results are collected and
 * formatted as `// => result` annotations beside each expression.
 *
 * @see RescriptWorksheetFileType for the file type definition
 */
class RescriptWorksheetRunner(
    @Suppress("unused") private val project: Project,
) {
    /**
     * Evaluates all expressions in the worksheet file and displays results
     * in the editor as inline comments.
     *
     * @param editor the editor displaying the worksheet file
     * @param file the worksheet PSI file
     */
    @Suppress("unused") // Placeholder for future worksheet execution
    fun evaluate(
        editor: Editor,
        file: PsiFile,
    ) {
        val text = file.text
        val expressions = extractExpressions(text)

        ApplicationManager.getApplication().executeOnPooledThread {
            @Suppress("unused")
            val results =
                expressions.map { expr ->
                    @Suppress("unused")
                    val script = buildEvalScript(expr.code)
                    // In a real implementation, this would compile and run via rescript/node
                    formatResult(expr.code, "")
                }

            ApplicationManager.getApplication().invokeLater {
                // Display results would be done via inlay hints or document modification
            }
        }
    }

    companion object {
        /** A single expression extracted from the worksheet. */
        data class WorksheetExpression(
            val code: String,
            val lineNumber: Int,
            val endLineNumber: Int,
        )

        // Pattern for top-level let bindings
        private val LET_PATTERN = Regex("""^let\s+\w+""")

        // Pattern for type declarations (not evaluated)
        private val TYPE_PATTERN = Regex("""^type\s+""")
        private val MODULE_PATTERN = Regex("""^module\s+""")
        private val OPEN_PATTERN = Regex("""^open\s+""")

        /**
         * Extracts top-level expressions from worksheet source text.
         *
         * Groups multi-line expressions by tracking brace/paren depth.
         * Skips type declarations, module declarations, and open statements
         * which are not evaluatable expressions.
         *
         * @param sourceText the worksheet source code
         * @return list of expressions with their line positions
         */
        internal fun extractExpressions(sourceText: String): List<WorksheetExpression> {
            val expressions = mutableListOf<WorksheetExpression>()
            val lines = sourceText.lines()
            var i = 0

            while (i < lines.size) {
                val line = lines[i].trim()

                // Skip empty lines and comments
                if (line.isEmpty() || line.startsWith("//") || line.startsWith("/*")) {
                    i++
                    continue
                }

                // Skip type and module declarations
                if (TYPE_PATTERN.containsMatchIn(line) ||
                    MODULE_PATTERN.containsMatchIn(line) ||
                    OPEN_PATTERN.containsMatchIn(line)
                ) {
                    // Find end of declaration (track braces)
                    val endLine = findBlockEnd(lines, i)
                    i = endLine + 1
                    continue
                }

                // Collect the expression (may span multiple lines)
                val endLine = findBlockEnd(lines, i)
                val code = lines.subList(i, endLine + 1).joinToString("\n")
                expressions.add(WorksheetExpression(code, i + 1, endLine + 1))
                i = endLine + 1
            }

            return expressions
        }

        /**
         * Wraps a ReScript expression in a compilable script for evaluation.
         *
         * @param code the expression to evaluate
         * @return the wrapped script text
         */
        internal fun buildEvalScript(code: String): String =
            buildString {
                appendLine("// Worksheet evaluation")
                appendLine(code)
                // Add Js.log for expressions that don't already print
                val trimmed = code.trim()
                if (!trimmed.contains("Js.log") && !trimmed.contains("Console.log")) {
                    // If it's a let binding, log the bound value
                    val letMatch = Regex("""^let\s+(\w+)\s*=""").find(trimmed)
                    if (letMatch != null) {
                        appendLine("Js.log(${letMatch.groupValues[1]})")
                    }
                }
            }

        /**
         * Formats an expression result for display.
         *
         * @param code the original expression code
         * @param output the evaluation output
         * @return formatted result string
         */
        internal fun formatResult(
            code: String,
            output: String,
        ): String {
            val trimmedOutput = output.trim()
            return if (trimmedOutput.isEmpty()) {
                "// => ()"
            } else {
                "// => $trimmedOutput"
            }
        }

        private fun findBlockEnd(
            lines: List<String>,
            startLine: Int,
        ): Int {
            var depth = 0
            var i = startLine
            var foundOpener = false

            while (i < lines.size) {
                for (ch in lines[i]) {
                    when (ch) {
                        '{', '(' -> {
                            depth++
                            foundOpener = true
                        }
                        '}', ')' -> depth--
                    }
                }

                if (foundOpener && depth <= 0) return i
                if (!foundOpener && i > startLine) {
                    // Single line expression without braces
                    val nextLine = lines.getOrNull(i)?.trim() ?: ""
                    if (nextLine.isEmpty() ||
                        LET_PATTERN.containsMatchIn(nextLine) ||
                        TYPE_PATTERN.containsMatchIn(nextLine) ||
                        MODULE_PATTERN.containsMatchIn(nextLine)
                    ) {
                        return i - 1
                    }
                }
                i++
            }

            return lines.size - 1
        }
    }
}
