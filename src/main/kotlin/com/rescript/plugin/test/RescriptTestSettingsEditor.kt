package com.rescript.plugin.test

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.util.ui.FormBuilder
import javax.swing.DefaultComboBoxModel
import javax.swing.JComponent
import javax.swing.JTextField

class RescriptTestSettingsEditor(
    private val project: Project,
) : SettingsEditor<RescriptTestRunConfiguration>() {
    private val frameworkComboBox =
        ComboBox(
            DefaultComboBoxModel(TestFramework.entries.toTypedArray()),
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
                    .withDescription("Select the working directory for test execution"),
            )
        }

    private val testFilePathField =
        TextFieldWithBrowseButton().apply {
            addBrowseFolderListener(
                project,
                FileChooserDescriptorFactory
                    .createSingleFileNoJarsDescriptor()
                    .withTitle("Test File")
                    .withDescription("Select a specific test file to run"),
            )
        }

    private val testNameField = JTextField()
    private val additionalArgumentsField = JTextField()

    override fun resetEditorFrom(config: RescriptTestRunConfiguration) {
        frameworkComboBox.selectedItem = config.framework
        workingDirectoryField.text = config.workingDirectory ?: project.basePath ?: ""
        testFilePathField.text = config.testFilePath ?: ""
        testNameField.text = config.testName ?: ""
        additionalArgumentsField.text = config.additionalArguments ?: ""
    }

    override fun applyEditorTo(config: RescriptTestRunConfiguration) {
        config.framework = frameworkComboBox.selectedItem as? TestFramework ?: TestFramework.JEST
        config.workingDirectory = workingDirectoryField.text.ifBlank { null }
        config.testFilePath = testFilePathField.text.ifBlank { null }
        config.testName = testNameField.text.ifBlank { null }
        config.additionalArguments = additionalArgumentsField.text.ifBlank { null }
    }

    override fun createEditor(): JComponent =
        FormBuilder
            .createFormBuilder()
            .addLabeledComponent("Framework:", frameworkComboBox)
            .addLabeledComponent("Working directory:", workingDirectoryField)
            .addLabeledComponent("Test file:", testFilePathField)
            .addLabeledComponent("Test name:", testNameField)
            .addLabeledComponent("Additional arguments:", additionalArgumentsField)
            .panel
}
