package com.rescript.plugin.notebook

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileEditor.FileEditorProvider
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

/**
 * Routes `.resnb` files to the cell-based [RescriptNotebookFileEditor]
 * instead of the platform's default JSON text editor.
 *
 * Registered as `com.intellij.fileEditorProvider` in `plugin.xml`.
 *
 * @see FileEditorProvider
 * @see RescriptNotebookFileType
 */
class RescriptNotebookFileEditorProvider :
    FileEditorProvider,
    DumbAware {
    override fun accept(
        project: Project,
        file: VirtualFile,
    ): Boolean = file.fileType == RescriptNotebookFileType

    override fun createEditor(
        project: Project,
        file: VirtualFile,
    ): FileEditor = RescriptNotebookFileEditor(project, file)

    override fun getEditorTypeId(): String = "rescript-notebook"

    override fun getPolicy(): FileEditorPolicy = FileEditorPolicy.HIDE_DEFAULT_EDITOR
}
