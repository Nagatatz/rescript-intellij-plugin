package com.rescript.plugin.settings

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.util.ui.FormBuilder
import java.io.File
import javax.swing.JComponent
import javax.swing.JPanel

class RescriptConfigurable(
    private val project: Project,
) : Configurable {
    private var panel: JPanel? = null
    private var lspServerPathField: TextFieldWithBrowseButton? = null
    private var nodePathField: TextFieldWithBrowseButton? = null

    override fun getDisplayName(): String = "ReScript"

    override fun createComponent(): JComponent {
        val lspField =
            TextFieldWithBrowseButton().apply {
                @Suppress("DEPRECATION")
                addBrowseFolderListener(
                    "Language Server Path",
                    "Select the rescript-language-server executable or cli.js",
                    project,
                    FileChooserDescriptorFactory.createSingleFileDescriptor(),
                )
            }

        val nodeField =
            TextFieldWithBrowseButton().apply {
                @Suppress("DEPRECATION")
                addBrowseFolderListener(
                    "Node.js Interpreter Path",
                    "Select the Node.js interpreter",
                    project,
                    FileChooserDescriptorFactory.createSingleFileDescriptor(),
                )
            }

        lspServerPathField = lspField
        nodePathField = nodeField

        val formPanel =
            FormBuilder
                .createFormBuilder()
                .addLabeledComponent("Language server path:", lspField)
                .addTooltip("Leave empty to auto-detect from node_modules or PATH.")
                .addLabeledComponent("Node.js interpreter path:", nodeField)
                .addTooltip("Leave empty to use \"node\" from PATH.")
                .addComponentFillVertically(JPanel(), 0)
                .panel

        panel = formPanel
        return formPanel
    }

    override fun isModified(): Boolean {
        val settings = RescriptProjectSettings.getInstance(project)
        return lspServerPathField?.text != settings.lspServerPath ||
            nodePathField?.text != settings.nodePath
    }

    @Throws(ConfigurationException::class)
    override fun apply() {
        val lspPath = lspServerPathField?.text?.trim() ?: ""
        val nodePath = nodePathField?.text?.trim() ?: ""

        if (lspPath.isNotEmpty() && !File(lspPath).exists()) {
            throw ConfigurationException("Language server path does not exist: $lspPath")
        }
        if (nodePath.isNotEmpty() && !File(nodePath).exists()) {
            throw ConfigurationException("Node.js interpreter path does not exist: $nodePath")
        }

        val settings = RescriptProjectSettings.getInstance(project)
        settings.lspServerPath = lspPath
        settings.nodePath = nodePath
    }

    override fun reset() {
        val settings = RescriptProjectSettings.getInstance(project)
        lspServerPathField?.text = settings.lspServerPath
        nodePathField?.text = settings.nodePath
    }

    override fun disposeUIResources() {
        panel = null
        lspServerPathField = null
        nodePathField = null
    }
}
