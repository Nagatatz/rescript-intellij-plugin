package com.rescript.plugin.debug

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JTextField

/**
 * Settings editor UI for [RescriptDebugRunConfiguration].
 *
 * Provides form fields for selecting the ReScript source file,
 * Node.js executable path, additional Node.js arguments, and working directory.
 */
class RescriptDebugSettingsEditor(
    private val project: Project,
) : SettingsEditor<RescriptDebugRunConfiguration>() {
    private val sourceFileField =
        TextFieldWithBrowseButton().apply {
            addBrowseFolderListener(
                project,
                FileChooserDescriptorFactory
                    .createSingleFileNoJarsDescriptor()
                    .withTitle("Source File")
                    .withDescription("Select the ReScript source file (.res or .resi)"),
            )
        }

    private val nodeExecutableField =
        TextFieldWithBrowseButton().apply {
            addBrowseFolderListener(
                project,
                FileChooserDescriptorFactory
                    .createSingleFileNoJarsDescriptor()
                    .withTitle("Node.js Executable")
                    .withDescription("Select the Node.js executable"),
            )
        }

    private val nodeArgsField = JTextField()

    private val workingDirectoryField =
        TextFieldWithBrowseButton().apply {
            addBrowseFolderListener(
                project,
                FileChooserDescriptorFactory
                    .createSingleFolderDescriptor()
                    .withTitle("Working Directory")
                    .withDescription("Select the working directory for the debug session"),
            )
        }

    override fun resetEditorFrom(config: RescriptDebugRunConfiguration) {
        sourceFileField.text = config.sourceFilePath ?: ""
        nodeExecutableField.text = config.nodeExecutable
        nodeArgsField.text = config.nodeArgs ?: ""
        workingDirectoryField.text = config.workingDirectory ?: project.basePath ?: ""
    }

    override fun applyEditorTo(config: RescriptDebugRunConfiguration) {
        config.sourceFilePath = sourceFileField.text.ifBlank { null }
        config.nodeExecutable = nodeExecutableField.text.ifBlank { "node" }
        config.nodeArgs = nodeArgsField.text.ifBlank { null }
        config.workingDirectory = workingDirectoryField.text.ifBlank { null }
    }

    override fun createEditor(): JComponent =
        FormBuilder
            .createFormBuilder()
            .addLabeledComponent("Source file:", sourceFileField)
            .addLabeledComponent("Node.js executable:", nodeExecutableField)
            .addLabeledComponent("Additional arguments:", nodeArgsField)
            .addLabeledComponent("Working directory:", workingDirectoryField)
            .panel
}
