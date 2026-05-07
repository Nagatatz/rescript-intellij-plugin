package com.rescript.plugin.notebook

/**
 * Renders a [NotebookDocument] as Markdown for sharing in PR
 * descriptions, blog posts, or chat. Cells are emitted in order, each
 * with a numbered heading, a `rescript` code fence, and (when
 * available) an output fence.
 */
object RescriptNotebookMarkdownExporter {
    /**
     * Converts [doc] to a Markdown string. The result always ends with
     * a single trailing newline so it can be appended to existing
     * Markdown files without surgical formatting.
     */
    fun toMarkdown(doc: NotebookDocument): String =
        buildString {
            doc.cells.forEachIndexed { index, cell ->
                if (index > 0) appendLine()
                appendLine("## Cell ${index + 1}")
                appendLine()
                appendLine("```rescript")
                appendLine(cell.code.trimEnd('\n'))
                appendLine("```")
                if (cell.lastOutput.isNotBlank()) {
                    appendLine()
                    appendLine("```")
                    appendLine(cell.lastOutput.trimEnd('\n'))
                    appendLine("```")
                }
            }
        }
}
