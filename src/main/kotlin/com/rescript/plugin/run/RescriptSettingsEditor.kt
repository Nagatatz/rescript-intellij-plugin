package com.rescript.plugin.run

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.util.ui.FormBuilder
import javax.swing.DefaultComboBoxModel
import javax.swing.JComponent
import javax.swing.JTextField

/**
 * Settings editor UI for [RescriptRunConfiguration].
 *
 * Provides form fields for selecting the ReScript command (build/build-watch/clean),
 * working directory, and additional CLI arguments.
 */
class RescriptSettingsEditor(
    private val project: Project,
) : SettingsEditor<RescriptRunConfiguration>() {
    private val commandComboBox =
        ComboBox(
            DefaultComboBoxModel(RescriptCommand.entries.toTypedArray()),
        ).apply {
            val defaultRenderer = javax.swing.DefaultListCellRenderer()
            setRenderer { list, value, index, isSelected, cellHasFocus ->
                defaultRenderer.getListCellRendererComponent(
                    list,
                    value?.displayName ?: "",
                    index,
                    isSelected,
                    cellHasFocus,
                )
            }
        }

    private val workingDirectoryField =
        TextFieldWithBrowseButton().apply {
            addBrowseFolderListener(
                project,
                FileChooserDescriptorFactory
                    .createSingleFolderDescriptor()
                    .withTitle("Working Directory")
                    .withDescription("Select the working directory for the ReScript build"),
            )
        }

    private val additionalArgumentsField = JTextField()

    override fun resetEditorFrom(config: RescriptRunConfiguration) {
        commandComboBox.selectedItem = RescriptCommand.fromId(config.command)
        workingDirectoryField.text = config.workingDirectory ?: project.basePath ?: ""
        additionalArgumentsField.text = config.additionalArguments ?: ""
    }

    override fun applyEditorTo(config: RescriptRunConfiguration) {
        config.command = (commandComboBox.selectedItem as? RescriptCommand)?.id ?: RescriptCommand.BUILD.id
        config.workingDirectory = workingDirectoryField.text.ifBlank { null }
        config.additionalArguments = additionalArgumentsField.text.ifBlank { null }
    }

    override fun createEditor(): JComponent =
        FormBuilder
            .createFormBuilder()
            .addLabeledComponent("Command:", commandComboBox)
            .addLabeledComponent("Working directory:", workingDirectoryField)
            .addLabeledComponent("Additional arguments:", additionalArgumentsField)
            .panel
}
