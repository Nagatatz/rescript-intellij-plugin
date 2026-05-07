package com.rescript.plugin.notebook

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Snapshots the Markdown output produced for representative
 * notebooks so any unintentional formatting drift is caught here.
 */
class RescriptNotebookMarkdownExporterTest {
    @Test
    fun `empty notebook produces empty string`() {
        assertEquals("", RescriptNotebookMarkdownExporter.toMarkdown(NotebookDocument.empty()))
    }

    @Test
    fun `single cell with output renders both code and output blocks`() {
        val doc =
            NotebookDocument(
                cells = listOf(NotebookCell(code = "let x = 1\nJs.log(x)", lastOutput = "1")),
            )
        val expected =
            """
            ## Cell 1

            ```rescript
            let x = 1
            Js.log(x)
            ```

            ```
            1
            ```

            """.trimIndent()
        assertEquals(expected, RescriptNotebookMarkdownExporter.toMarkdown(doc))
    }

    @Test
    fun `cell with no output omits the output block`() {
        val doc = NotebookDocument(cells = listOf(NotebookCell("let x = 1", "")))
        val expected =
            """
            ## Cell 1

            ```rescript
            let x = 1
            ```

            """.trimIndent()
        assertEquals(expected, RescriptNotebookMarkdownExporter.toMarkdown(doc))
    }

    @Test
    fun `multi-cell notebook separates cells with a blank line`() {
        val doc =
            NotebookDocument(
                cells =
                    listOf(
                        NotebookCell("let x = 1", "1"),
                        NotebookCell("let y = x + 2", "3"),
                    ),
            )
        val markdown = RescriptNotebookMarkdownExporter.toMarkdown(doc)
        // Cells should be numbered 1, 2 and have a blank line between them.
        assertEquals(true, markdown.contains("## Cell 1"))
        assertEquals(true, markdown.contains("## Cell 2"))
        // The blank line between cells is what separates output of cell 1
        // from the heading of cell 2.
        assertEquals(true, markdown.contains("```\n\n## Cell 2"))
    }
}
