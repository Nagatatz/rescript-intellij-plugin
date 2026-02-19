package com.rescript.plugin.wizard

import com.intellij.ide.util.projectWizard.ModuleWizardStep
import java.awt.BorderLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.JCheckBox
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class RescriptProjectWizardStep(
    private val builder: RescriptModuleBuilder,
) : ModuleWizardStep() {
    private val packageManagerCombo = JComboBox(PackageManager.entries.toTypedArray())
    private val reactCheckBox = JCheckBox("Include React support")

    override fun getComponent(): JComponent {
        val panel = JPanel(BorderLayout())
        val innerPanel = JPanel(GridBagLayout())
        val gbc =
            GridBagConstraints().apply {
                fill = GridBagConstraints.HORIZONTAL
                insets = Insets(5, 5, 5, 5)
                anchor = GridBagConstraints.WEST
            }

        gbc.gridx = 0
        gbc.gridy = 0
        innerPanel.add(JLabel("Package manager:"), gbc)

        gbc.gridx = 1
        gbc.weightx = 1.0
        innerPanel.add(packageManagerCombo, gbc)

        gbc.gridx = 0
        gbc.gridy = 1
        gbc.gridwidth = 2
        gbc.weightx = 0.0
        innerPanel.add(reactCheckBox, gbc)

        panel.add(innerPanel, BorderLayout.NORTH)
        return panel
    }

    override fun updateDataModel() {
        builder.packageManager = packageManagerCombo.selectedItem as PackageManager
        builder.includeReact = reactCheckBox.isSelected
    }
}
