package com.rescript.plugin.settings

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.util.ui.FormBuilder
import java.io.File
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * Settings UI for the ReScript plugin, accessible via
 * Settings > Languages & Frameworks > ReScript.
 *
 * Allows users to configure the LSP server path, Node.js interpreter path,
 * and incremental type-checking. Applying settings triggers an LSP server restart.
 */
class RescriptConfigurable(
    private val project: Project,
) : Configurable {
    private var panel: JPanel? = null
    private var lspServerPathField: TextFieldWithBrowseButton? = null
    private var nodePathField: TextFieldWithBrowseButton? = null
    private var incrementalTypecheckingCheckbox: JCheckBox? = null
    private var removeUnusedOpensCheckbox: JCheckBox? = null

    override fun getDisplayName(): String = "ReScript"

    override fun createComponent(): JComponent {
        val lspField =
            TextFieldWithBrowseButton().apply {
                @Suppress("DialogTitleCapitalization")
                addBrowseFolderListener(
                    project,
                    FileChooserDescriptorFactory
                        .singleFile()
                        .withTitle("Language Server Path")
                        .withDescription("Select the rescript-language-server executable or cli.js"),
                )
            }

        val nodeField =
            TextFieldWithBrowseButton().apply {
                @Suppress("DialogTitleCapitalization")
                addBrowseFolderListener(
                    project,
                    FileChooserDescriptorFactory
                        .singleFile()
                        .withTitle("Node.js Interpreter Path")
                        .withDescription("Select the Node.js interpreter"),
                )
            }

        lspServerPathField = lspField
        nodePathField = nodeField

        val incrementalCheckbox = JCheckBox("Enable incremental type checking", true)
        incrementalTypecheckingCheckbox = incrementalCheckbox

        val unusedOpensCheckbox = JCheckBox("Remove unused open statements (requires LSP)", true)
        removeUnusedOpensCheckbox = unusedOpensCheckbox

        val formPanel =
            FormBuilder
                .createFormBuilder()
                .addLabeledComponent("Language server path:", lspField)
                .addTooltip("Leave empty to auto-detect from node_modules or PATH.")
                .addLabeledComponent("Node.js interpreter path:", nodeField)
                .addTooltip("Leave empty to use \"node\" from PATH.")
                .addSeparator()
                .addComponent(incrementalCheckbox)
                .addTooltip(
                    "When enabled, the LSP server uses incremental type checking for faster feedback. Requires LSP server restart.",
                ).addComponent(unusedOpensCheckbox)
                .addTooltip(
                    "When enabled, Optimize Imports also removes unused open statements detected by the LSP server.",
                ).addComponentFillVertically(JPanel(), 0)
                .panel

        panel = formPanel
        return formPanel
    }

    override fun isModified(): Boolean {
        val settings = RescriptProjectSettings.getInstance(project)
        return lspServerPathField?.text != settings.lspServerPath ||
            nodePathField?.text != settings.nodePath ||
            incrementalTypecheckingCheckbox?.isSelected != settings.incrementalTypecheckingEnabled ||
            removeUnusedOpensCheckbox?.isSelected != settings.removeUnusedOpensEnabled
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
        settings.incrementalTypecheckingEnabled = incrementalTypecheckingCheckbox?.isSelected ?: true
        settings.removeUnusedOpensEnabled = removeUnusedOpensCheckbox?.isSelected ?: true

        com.intellij.platform.lsp.api.LspServerManager
            .getInstance(project)
            .stopAndRestartIfNeeded(com.rescript.plugin.lsp.RescriptLspServerSupportProvider::class.java)
    }

    override fun reset() {
        val settings = RescriptProjectSettings.getInstance(project)
        lspServerPathField?.text = settings.lspServerPath
        nodePathField?.text = settings.nodePath
        incrementalTypecheckingCheckbox?.isSelected = settings.incrementalTypecheckingEnabled
        removeUnusedOpensCheckbox?.isSelected = settings.removeUnusedOpensEnabled
    }

    override fun disposeUIResources() {
        panel = null
        lspServerPathField = null
        nodePathField = null
        incrementalTypecheckingCheckbox = null
        removeUnusedOpensCheckbox = null
    }
}
