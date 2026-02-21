package com.rescript.plugin.imports

import com.intellij.application.options.editor.AutoImportOptionsProvider
import com.intellij.openapi.project.Project
import com.rescript.plugin.settings.RescriptProjectSettings
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextField

/**
 * Provides auto-import configuration options for ReScript in
 * Settings > Editor > General > Auto Import.
 *
 * Allows users to control:
 * - Automatic import optimization on the fly
 * - Automatic open statement insertion
 * - Module exclusion list (comma-separated)
 *
 * Settings are persisted in [RescriptProjectSettings].
 */
class RescriptAutoImportOptionsProvider(
    private val project: Project,
) : AutoImportOptionsProvider {
    private var optimizeCheckBox: JCheckBox? = null
    private var autoOpenCheckBox: JCheckBox? = null
    private var excludeField: JTextField? = null

    override fun createComponent(): JComponent {
        val panel = JPanel()
        panel.layout = javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS)

        optimizeCheckBox =
            JCheckBox("Optimize imports on the fly").also {
                panel.add(it)
            }
        autoOpenCheckBox =
            JCheckBox("Add open statements automatically").also {
                panel.add(it)
            }

        val excludePanel = JPanel(java.awt.FlowLayout(java.awt.FlowLayout.LEFT))
        excludePanel.add(javax.swing.JLabel("Exclude modules:"))
        excludeField =
            JTextField(30).also {
                excludePanel.add(it)
            }
        panel.add(excludePanel)

        return panel
    }

    override fun isModified(): Boolean {
        val settings = RescriptProjectSettings.getInstance(project)
        return optimizeCheckBox?.isSelected != settings.autoOptimizeImports ||
            autoOpenCheckBox?.isSelected != settings.autoAddOpenStatements ||
            excludeField?.text != settings.excludedModules
    }

    override fun apply() {
        val settings = RescriptProjectSettings.getInstance(project)
        optimizeCheckBox?.isSelected?.let { settings.autoOptimizeImports = it }
        autoOpenCheckBox?.isSelected?.let { settings.autoAddOpenStatements = it }
        excludeField?.text?.let { settings.excludedModules = it }
    }

    override fun reset() {
        val settings = RescriptProjectSettings.getInstance(project)
        optimizeCheckBox?.isSelected = settings.autoOptimizeImports
        autoOpenCheckBox?.isSelected = settings.autoAddOpenStatements
        excludeField?.text = settings.excludedModules
    }
}
