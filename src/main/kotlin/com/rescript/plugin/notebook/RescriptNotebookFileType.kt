package com.rescript.plugin.notebook

import com.intellij.openapi.fileTypes.FileType
import com.rescript.plugin.RescriptIcons
import javax.swing.Icon

/**
 * File type for `.resnb` ReScript Notebook files.
 *
 * Notebooks are JSON documents holding a sequence of cells, each
 * with ReScript code and the captured output of the last evaluation.
 * They are bound to [RescriptNotebookFileEditorProvider] so opening
 * a `.resnb` file launches the cell-based editor instead of plain
 * JSON view.
 *
 * @see FileType
 * @see RescriptNotebookFileEditorProvider
 */
object RescriptNotebookFileType : FileType {
    override fun getName(): String = "ReScript Notebook"

    override fun getDescription(): String = "ReScript notebook file (cell-based worksheet)"

    override fun getDefaultExtension(): String = "resnb"

    override fun getIcon(): Icon = RescriptIcons.FILE

    override fun isBinary(): Boolean = false
}
