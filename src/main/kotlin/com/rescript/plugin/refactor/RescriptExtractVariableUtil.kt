package com.rescript.plugin.refactor

import com.rescript.plugin.util.RescriptRegexPatterns

/**
 * Pure utility functions for the Extract Variable refactoring.
 *
 * Provides text-based algorithms for determining insertion points,
 * suggesting variable names, and building let bindings. All functions
 * are stateless and testable without IDE dependencies.
 *
 * @see RescriptExtractVariableHandler for the orchestrating handler
 */
object RescriptExtractVariableUtil {
    /** Top-level declaration keywords that start a ReScript statement. */
    private val STATEMENT_KEYWORDS =
        setOf("let", "type", "module", "external", "open", "include", "exception")

    /** Pattern matching a simple function call: `func(...)` */
    private val SIMPLE_CALL_REGEX = Regex("""^([a-z_][a-zA-Z0-9_']*)\s*\(""")

    /** Pattern matching a qualified function call: `Module.func(...)` */
    private val QUALIFIED_CALL_REGEX = Regex("""^([A-Z][a-zA-Z0-9_']*\.)*([a-z_][a-zA-Z0-9_']*)\s*\(""")

    /** Pattern matching a function call after `->`: `Module.func(...)` or `func` */
    private val PIPE_FUNC_REGEX = Regex("""^([A-Z][a-zA-Z0-9_']*\.)*([a-z_][a-zA-Z0-9_']*)""")

    /** Pattern for a valid ReScript lident (lowercase identifier). */
    private val LIDENT_REGEX = RescriptRegexPatterns.LIDENT

    /** ReScript keywords that cannot be used as variable names. */
    private val KEYWORDS =
        setOf(
            "and",
            "as",
            "assert",
            "async",
            "await",
            "begin",
            "catch",
            "class",
            "constraint",
            "do",
            "done",
            "downto",
            "else",
            "end",
            "exception",
            "external",
            "false",
            "for",
            "functor",
            "if",
            "in",
            "include",
            "inherit",
            "initializer",
            "lazy",
            "let",
            "list",
            "module",
            "mutable",
            "new",
            "nonrec",
            "object",
            "of",
            "open",
            "or",
            "pri",
            "pub",
            "raw",
            "rec",
            "ref",
            "raise",
            "sig",
            "struct",
            "switch",
            "then",
            "true",
            "try",
            "type",
            "unpack",
            "val",
            "virtual",
            "when",
            "while",
            "with",
        )

    /**
     * Finds the line number where the statement containing the selection starts.
     *
     * Scans upward from the selection's line to find the nearest statement boundary,
     * identified by a top-level keyword (`let`, `type`, `module`, etc.) or an
     * indentation change indicating a block boundary.
     *
     * @param documentText the full document text
     * @param selectionStartOffset the start offset of the user's selection
     * @return the 0-based line number for the statement start
     */
    fun findStatementStartLine(
        documentText: String,
        selectionStartOffset: Int,
    ): Int {
        val lines = documentText.lines()
        if (lines.isEmpty()) return 0

        // Find which line the selection starts on
        var charCount = 0
        var selectionLine = 0
        for ((i, line) in lines.withIndex()) {
            if (charCount + line.length >= selectionStartOffset) {
                selectionLine = i
                break
            }
            charCount += line.length + 1 // +1 for newline
        }

        val currentIndent = getIndentation(lines[selectionLine]).length

        // Scan upward from the selection line
        for (i in selectionLine downTo 0) {
            val line = lines[i]
            val trimmed = line.trimStart()

            // Skip empty lines
            if (trimmed.isEmpty()) continue

            val lineIndent = getIndentation(line).length

            // Check if this line starts with a statement keyword at the same or lower indent
            val firstWord = trimmed.split(RescriptRegexPatterns.WHITESPACE, limit = 2).firstOrNull() ?: ""
            if (firstWord in STATEMENT_KEYWORDS && lineIndent <= currentIndent) {
                return i
            }

            // A line at a shallower indent ending with `{` or `=>` means the next line is the start
            if (lineIndent < currentIndent && (trimmed.endsWith("{") || trimmed.endsWith("=>"))) {
                return (i + 1).coerceAtMost(selectionLine)
            }
        }

        return 0
    }

    /**
     * Returns the leading whitespace of a line.
     *
     * @param lineText a single line of text
     * @return the whitespace prefix (spaces and tabs)
     */
    fun getIndentation(lineText: String): String {
        val idx = lineText.indexOfFirst { it != ' ' && it != '\t' }
        return if (idx < 0) lineText else lineText.substring(0, idx)
    }

    /**
     * Suggests a variable name based on the selected expression text.
     *
     * Uses heuristics to derive a meaningful name:
     * - Function calls `func(...)` → `funcResult`
     * - Qualified calls `Module.func(...)` → `funcResult`
     * - Pipe chains `...->func(...)` → `funcResult`
     * - Simple identifiers → the identifier itself
     * - Everything else → `"extracted"`
     *
     * The result is validated against ReScript's lident pattern and keyword list.
     *
     * @param selectedText the user-selected expression text
     * @return a valid ReScript variable name suggestion
     */
    fun suggestVariableName(selectedText: String): String {
        val trimmed = selectedText.trim()

        // Try pipe chain: find the last `->` and extract the function name after it
        val lastPipeIndex = trimmed.lastIndexOf("->")
        if (lastPipeIndex >= 0) {
            val afterPipe = trimmed.substring(lastPipeIndex + 2).trimStart()
            val pipeMatch = PIPE_FUNC_REGEX.find(afterPipe)
            if (pipeMatch != null) {
                val funcName = pipeMatch.groupValues[2]
                return validateName("${funcName}Result")
            }
        }

        // Try qualified function call: Module.func(...)
        val qualifiedMatch = QUALIFIED_CALL_REGEX.find(trimmed)
        if (qualifiedMatch != null) {
            val funcName = qualifiedMatch.groupValues[2]
            return validateName("${funcName}Result")
        }

        // Try simple function call: func(...)
        val simpleMatch = SIMPLE_CALL_REGEX.find(trimmed)
        if (simpleMatch != null) {
            val funcName = simpleMatch.groupValues[1]
            return validateName("${funcName}Result")
        }

        // Simple identifier
        if (LIDENT_REGEX.matches(trimmed) && trimmed !in KEYWORDS) {
            return trimmed
        }

        return "extracted"
    }

    /**
     * Builds a let binding string with proper indentation.
     *
     * @param name the variable name
     * @param expression the expression to bind
     * @param indent the whitespace prefix to use
     * @return a complete let binding line ending with newline
     */
    fun buildLetBinding(
        name: String,
        expression: String,
        indent: String,
    ): String = "${indent}let $name = ${expression}\n"

    /**
     * Validates and sanitizes a suggested name to ensure it is a valid ReScript lident.
     *
     * @param name the candidate name
     * @return the name if valid, otherwise "extracted"
     */
    internal fun validateName(name: String): String {
        if (name.isEmpty()) return "extracted"
        if (!LIDENT_REGEX.matches(name)) return "extracted"
        if (name in KEYWORDS) return "extracted"
        return name
    }
}
