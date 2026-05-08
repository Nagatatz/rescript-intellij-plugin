package com.rescript.plugin.notebook

import com.intellij.openapi.project.Project
import com.intellij.testFramework.LightVirtualFile
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.rescript.plugin.IntelliJPlatformExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Verifies that a Notebook document survives a full editor round-trip:
 * load → snapshot → serialise → re-parse should yield the original
 * cells. Backs the post-merge open/edit/save/reopen acceptance check
 * from `.steering/20260508-003-notebook-worksheet/requirements.md`.
 */
@ExtendWith(IntelliJPlatformExtension::class)
class RescriptNotebookFileEditorIntegrationTest {
    private lateinit var myFixture: CodeInsightTestFixture
    private lateinit var project: Project

    @Test
    fun `empty file opens with one auto-added empty cell`() {
        val file = LightVirtualFile("empty.resnb", RescriptNotebookFileType, "")
        val editor = RescriptNotebookFileEditor(project, file)
        try {
            val snapshot = editor.panel.snapshot()
            // The panel auto-adds an empty cell when none are present.
            assertEquals(1, snapshot.cells.size)
            assertEquals("", snapshot.cells.single().code)
            assertEquals("", snapshot.cells.single().lastOutput)
        } finally {
            editor.dispose()
        }
    }

    @Test
    fun `existing cells are preserved through serializer round-trip`() {
        val original =
            NotebookDocument(
                cells =
                    listOf(
                        NotebookCell("let x = 1", "1"),
                        NotebookCell("let y = x + 2", ""),
                        NotebookCell("Js.log(y)", "3"),
                    ),
            )
        val file = LightVirtualFile("nb.resnb", RescriptNotebookFileType, RescriptNotebookSerializer.toJson(original))
        val editor = RescriptNotebookFileEditor(project, file)
        try {
            val snapshot = editor.panel.snapshot()
            // Serialise the snapshot back and parse it; the result should match.
            val text = RescriptNotebookSerializer.toJson(snapshot)
            val parsed = RescriptNotebookSerializer.fromJson(text)
            assertEquals(original.cells, parsed.cells)
        } finally {
            editor.dispose()
        }
    }

    @Test
    fun `invalid JSON falls back to empty notebook without crashing`() {
        val file = LightVirtualFile("bad.resnb", RescriptNotebookFileType, "this is not json")
        val editor = RescriptNotebookFileEditor(project, file)
        try {
            val snapshot = editor.panel.snapshot()
            // The fallback path appends one empty cell so the user can keep
            // editing without losing access to the file.
            assertEquals(1, snapshot.cells.size)
            assertTrue(
                snapshot.cells
                    .single()
                    .code
                    .isEmpty(),
            )
        } finally {
            editor.dispose()
        }
    }
}
