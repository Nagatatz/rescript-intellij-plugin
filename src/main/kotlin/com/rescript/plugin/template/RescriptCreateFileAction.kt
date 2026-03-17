package com.rescript.plugin.template

import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.rescript.plugin.RescriptIcons

/**
 * "New > ReScript File" action in the Project tool window context menu.
 *
 * Offers three file templates: Module (.res), Interface (.resi), and Component (.res).
 * Automatically capitalizes the file name to follow ReScript module naming conventions.
 */
class RescriptCreateFileAction :
    CreateFileFromTemplateAction("ReScript File", "Create a new ReScript file", RescriptIcons.FILE),
    DumbAware {
    override fun buildDialog(
        project: Project,
        directory: PsiDirectory,
        builder: CreateFileFromTemplateDialog.Builder,
    ) {
        builder
            .setTitle("New ReScript File")
            .addKind("Module", RescriptIcons.FILE, "ReScript Module")
            .addKind("Interface", RescriptIcons.INTERFACE_FILE, "ReScript Interface")
            .addKind("Component", RescriptIcons.FILE, "ReScript Component")
    }

    override fun getActionName(
        directory: PsiDirectory,
        newName: String,
        templateName: String,
    ): String = formatActionName(newName)

    override fun createFileFromTemplate(
        name: String,
        template: com.intellij.ide.fileTemplates.FileTemplate,
        dir: PsiDirectory,
    ): PsiFile? {
        val capitalizedName = capitalizeFileName(name)
        return super.createFileFromTemplate(capitalizedName, template, dir)
    }

    companion object {
        /**
         * Capitalizes the first character of a file name to follow ReScript module naming conventions.
         *
         * @param name the raw file name entered by the user
         * @return the file name with its first character uppercased
         */
        internal fun capitalizeFileName(name: String): String = name.replaceFirstChar { it.uppercaseChar() }

        /**
         * Formats the action name shown in the undo/redo history.
         *
         * @param newName the file name entered by the user
         * @return the formatted action description
         */
        internal fun formatActionName(newName: String): String = "Create ReScript File: $newName"
    }
}
