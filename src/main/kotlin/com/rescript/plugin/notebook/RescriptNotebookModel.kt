package com.rescript.plugin.notebook

/**
 * One cell in a [NotebookDocument]: a snippet of ReScript code and the
 * output captured the last time it was run.
 *
 * @property code the ReScript source for this cell
 * @property lastOutput stdout/stderr captured at the last evaluation,
 *   or empty when the cell has not been run yet
 */
data class NotebookCell(
    val code: String,
    val lastOutput: String,
)

/**
 * Top-level value persisted to a `.resnb` file: a versioned list of
 * [NotebookCell]s.
 *
 * @property version schema version, currently always `1`. New
 *   versions can be introduced without breaking older files because
 *   [RescriptNotebookSerializer.fromJson] tolerates missing fields.
 * @property cells cells in display order
 */
data class NotebookDocument(
    val version: Int = 1,
    val cells: List<NotebookCell>,
) {
    companion object {
        /** Returns an empty notebook ready to be opened in the editor. */
        fun empty(): NotebookDocument = NotebookDocument(cells = emptyList())
    }
}
