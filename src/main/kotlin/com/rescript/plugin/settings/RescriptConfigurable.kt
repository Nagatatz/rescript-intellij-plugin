package com.rescript.plugin.settings

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.util.ui.FormBuilder
import com.rescript.plugin.errorlens.RescriptErrorLensSeverity
import com.rescript.plugin.util.RescriptSecurityUtils
import java.io.File
import javax.swing.DefaultComboBoxModel
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
    private var incrementalAcrossFilesCheckbox: JCheckBox? = null
    private var errorLensCheckbox: JCheckBox? = null
    private var errorLensMinSeverityCombo: ComboBox<String>? = null
    private var removeUnusedOpensCheckbox: JCheckBox? = null
    private var rescriptBinaryPathField: TextFieldWithBrowseButton? = null
    private var platformPathField: TextFieldWithBrowseButton? = null
    private var runtimePathField: TextFieldWithBrowseButton? = null
    private var logLevelCombo: ComboBox<String>? = null

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

        val acrossFilesCheckbox = JCheckBox("Cross-file incremental type checking (experimental)", false)
        incrementalAcrossFilesCheckbox = acrossFilesCheckbox

        val elCheckbox = JCheckBox("Enable Error Lens (inline diagnostic display)", true)
        errorLensCheckbox = elCheckbox

        val severityCombo =
            ComboBox(DefaultComboBoxModel(RescriptErrorLensSeverity.SEVERITY_NAMES.toTypedArray()))
        errorLensMinSeverityCombo = severityCombo

        val unusedOpensCheckbox = JCheckBox("Remove unused open statements (requires LSP)", true)
        removeUnusedOpensCheckbox = unusedOpensCheckbox

        val binaryPathField =
            TextFieldWithBrowseButton().apply {
                @Suppress("DialogTitleCapitalization")
                addBrowseFolderListener(
                    project,
                    FileChooserDescriptorFactory
                        .singleFile()
                        .withTitle("ReScript Binary Path")
                        .withDescription("Select the ReScript compiler binary"),
                )
            }
        rescriptBinaryPathField = binaryPathField

        val platPathField =
            TextFieldWithBrowseButton().apply {
                @Suppress("DialogTitleCapitalization")
                addBrowseFolderListener(
                    project,
                    FileChooserDescriptorFactory
                        .createSingleFolderDescriptor()
                        .withTitle("Platform Path")
                        .withDescription("Select the ReScript platform directory"),
                )
            }
        platformPathField = platPathField

        val rtPathField =
            TextFieldWithBrowseButton().apply {
                @Suppress("DialogTitleCapitalization")
                addBrowseFolderListener(
                    project,
                    FileChooserDescriptorFactory
                        .createSingleFolderDescriptor()
                        .withTitle("Runtime Path")
                        .withDescription("Select the ReScript runtime directory"),
                )
            }
        runtimePathField = rtPathField

        val logCombo = ComboBox(DefaultComboBoxModel(LOG_LEVELS))
        logLevelCombo = logCombo

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
                ).addComponent(acrossFilesCheckbox)
                .addTooltip(
                    "Enable cross-file incremental type checking. Experimental feature. Requires LSP server restart.",
                ).addSeparator()
                .addComponent(elCheckbox)
                .addTooltip("Show diagnostic messages inline at the end of editor lines. Requires reopening files.")
                .addLabeledComponent("Minimum severity:", severityCombo)
                .addTooltip("Only show diagnostics at or above this severity level.")
                .addSeparator()
                .addComponent(unusedOpensCheckbox)
                .addTooltip(
                    "When enabled, Optimize Imports also removes unused open statements detected by the LSP server.",
                ).addSeparator()
                .addLabeledComponent("ReScript binary path:", binaryPathField)
                .addTooltip("Leave empty to auto-detect. Path to the ReScript compiler binary.")
                .addLabeledComponent("Platform path:", platPathField)
                .addTooltip("Leave empty to auto-detect. Path to the ReScript platform directory.")
                .addLabeledComponent("Runtime path:", rtPathField)
                .addTooltip("Leave empty to auto-detect. Path to the ReScript runtime directory.")
                .addLabeledComponent("Log level:", logCombo)
                .addTooltip("LSP server log verbosity level.")
                .addComponentFillVertically(JPanel(), 0)
                .panel

        panel = formPanel
        return formPanel
    }

    override fun isModified(): Boolean {
        val settings = RescriptProjectSettings.getInstance(project)
        return lspServerPathField?.text != settings.lspServerPath ||
            nodePathField?.text != settings.nodePath ||
            incrementalTypecheckingCheckbox?.isSelected != settings.incrementalTypecheckingEnabled ||
            incrementalAcrossFilesCheckbox?.isSelected != settings.incrementalTypecheckingAcrossFiles ||
            errorLensCheckbox?.isSelected != settings.errorLensEnabled ||
            errorLensMinSeverityCombo?.selectedItem != settings.errorLensMinSeverity ||
            removeUnusedOpensCheckbox?.isSelected != settings.removeUnusedOpensEnabled ||
            rescriptBinaryPathField?.text != settings.rescriptBinaryPath ||
            platformPathField?.text != settings.platformPath ||
            runtimePathField?.text != settings.runtimePath ||
            logLevelCombo?.selectedItem != settings.logLevel
    }

    @Throws(ConfigurationException::class)
    override fun apply() {
        val lspPath = lspServerPathField?.text?.trim() ?: ""
        val nodePath = nodePathField?.text?.trim() ?: ""
        val binaryPath = rescriptBinaryPathField?.text?.trim() ?: ""
        val platPath = platformPathField?.text?.trim() ?: ""
        val rtPath = runtimePathField?.text?.trim() ?: ""

        if (lspPath.isNotEmpty()) {
            if (!File(lspPath).exists()) {
                throw ConfigurationException("Language server path does not exist: $lspPath")
            }
            // Non-.js server paths must be executable binaries
            if (!lspPath.endsWith(".js") && !RescriptSecurityUtils.isValidExecutable(lspPath)) {
                throw ConfigurationException("Language server path is not an executable file: $lspPath")
            }
        }
        if (nodePath.isNotEmpty()) {
            if (!File(nodePath).exists()) {
                throw ConfigurationException("Node.js interpreter path does not exist: $nodePath")
            }
            if (!RescriptSecurityUtils.isValidExecutable(nodePath)) {
                throw ConfigurationException("Node.js interpreter path is not an executable file: $nodePath")
            }
        }
        if (binaryPath.isNotEmpty() && !File(binaryPath).exists()) {
            throw ConfigurationException("ReScript binary path does not exist: $binaryPath")
        }
        if (platPath.isNotEmpty() && !File(platPath).exists()) {
            throw ConfigurationException("Platform path does not exist: $platPath")
        }
        if (rtPath.isNotEmpty() && !File(rtPath).exists()) {
            throw ConfigurationException("Runtime path does not exist: $rtPath")
        }

        val settings = RescriptProjectSettings.getInstance(project)
        settings.lspServerPath = lspPath
        settings.nodePath = nodePath
        settings.incrementalTypecheckingEnabled = incrementalTypecheckingCheckbox?.isSelected ?: true
        settings.incrementalTypecheckingAcrossFiles = incrementalAcrossFilesCheckbox?.isSelected ?: false
        settings.errorLensEnabled = errorLensCheckbox?.isSelected ?: true
        settings.errorLensMinSeverity = errorLensMinSeverityCombo?.selectedItem as? String ?: "WARNING"
        settings.removeUnusedOpensEnabled = removeUnusedOpensCheckbox?.isSelected ?: true
        settings.rescriptBinaryPath = binaryPath
        settings.platformPath = platPath
        settings.runtimePath = rtPath
        settings.logLevel = logLevelCombo?.selectedItem as? String ?: "info"

        com.intellij.platform.lsp.api.LspServerManager
            .getInstance(project)
            .stopAndRestartIfNeeded(com.rescript.plugin.lsp.RescriptLspServerSupportProvider::class.java)
    }

    override fun reset() {
        val settings = RescriptProjectSettings.getInstance(project)
        lspServerPathField?.text = settings.lspServerPath
        nodePathField?.text = settings.nodePath
        incrementalTypecheckingCheckbox?.isSelected = settings.incrementalTypecheckingEnabled
        incrementalAcrossFilesCheckbox?.isSelected = settings.incrementalTypecheckingAcrossFiles
        errorLensCheckbox?.isSelected = settings.errorLensEnabled
        errorLensMinSeverityCombo?.selectedItem = settings.errorLensMinSeverity
        removeUnusedOpensCheckbox?.isSelected = settings.removeUnusedOpensEnabled
        rescriptBinaryPathField?.text = settings.rescriptBinaryPath
        platformPathField?.text = settings.platformPath
        runtimePathField?.text = settings.runtimePath
        logLevelCombo?.selectedItem = settings.logLevel
    }

    override fun disposeUIResources() {
        panel = null
        lspServerPathField = null
        nodePathField = null
        incrementalTypecheckingCheckbox = null
        incrementalAcrossFilesCheckbox = null
        errorLensCheckbox = null
        errorLensMinSeverityCombo = null
        removeUnusedOpensCheckbox = null
        rescriptBinaryPathField = null
        platformPathField = null
        runtimePathField = null
        logLevelCombo = null
    }

    companion object {
        private val LOG_LEVELS = arrayOf("error", "warn", "info", "log")
    }
}
