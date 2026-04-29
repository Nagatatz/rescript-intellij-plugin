package com.rescript.plugin.wizard

import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.DefaultListCellRenderer
import javax.swing.DefaultListModel
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JTextArea
import javax.swing.ListSelectionModel
import javax.swing.border.EmptyBorder

/**
 * Wizard step UI for configuring a new ReScript project.
 *
 * Provides a template selection list grouped by [TemplateCategory],
 * a description panel showing details about the selected template,
 * a package manager combo box, and a validation library combo box.
 * The validation combo is hidden when the selected template opts out via
 * [ProjectTemplate.supportsValidationSelection]. Values are written back to
 * [RescriptModuleBuilder] via [updateDataModel].
 */
class RescriptProjectWizardStep(
    private val builder: RescriptModuleBuilder,
) : ModuleWizardStep() {
    private val packageManagerCombo =
        JComboBox(PackageManager.entries.toTypedArray()).apply {
            selectedItem = PackageManager.PNPM
        }
    private val apiStrategyCombo =
        JComboBox(ApiStrategy.entries.toTypedArray()).apply {
            selectedItem = ApiStrategy.REST
            toolTipText =
                "API shape for full-stack templates. REST uses Hono routes + fetch; " +
                "GraphQL mounts graphql-yoga on Hono and wires rescript-relay on the client. " +
                "Currently only affects the FULL_STACK template."
        }
    private val validationLibraryCombo =
        JComboBox(ValidationLibrary.entries.toTypedArray()).apply {
            selectedItem = ValidationLibrary.ZOD
            toolTipText =
                "HTTP input validation library used by server templates. " +
                "zod is TS-first; sury is ReScript-native."
        }
    private val validationLibraryLabel = JLabel("Validation library:")
    private val templateListModel = DefaultListModel<Any>()
    private val templateList = JBList(templateListModel)
    private val descriptionArea =
        JTextArea().apply {
            isEditable = false
            lineWrap = true
            wrapStyleWord = true
            border = EmptyBorder(8, 8, 8, 8)
        }

    init {
        populateTemplateList()
        configureTemplateList()
    }

    override fun getComponent(): JComponent {
        val panel = JPanel(BorderLayout())
        val innerPanel = JPanel(GridBagLayout())
        val gbc =
            GridBagConstraints().apply {
                fill = GridBagConstraints.BOTH
                insets = Insets(5, 5, 5, 5)
                anchor = GridBagConstraints.NORTHWEST
            }

        // Template label
        gbc.gridx = 0
        gbc.gridy = 0
        gbc.gridwidth = 2
        gbc.weightx = 1.0
        gbc.weighty = 0.0
        innerPanel.add(JLabel("Project template:"), gbc)

        // Template list (left)
        gbc.gridy = 1
        gbc.gridwidth = 1
        gbc.weightx = 0.5
        gbc.weighty = 1.0
        val listScroll =
            JBScrollPane(templateList).apply {
                preferredSize = Dimension(250, 300)
            }
        innerPanel.add(listScroll, gbc)

        // Description panel (right)
        gbc.gridx = 1
        gbc.weightx = 0.5
        val descScroll =
            JBScrollPane(descriptionArea).apply {
                preferredSize = Dimension(250, 300)
            }
        innerPanel.add(descScroll, gbc)

        // Package manager
        gbc.gridx = 0
        gbc.gridy = 2
        gbc.gridwidth = 1
        gbc.weightx = 0.0
        gbc.weighty = 0.0
        gbc.fill = GridBagConstraints.NONE
        innerPanel.add(JLabel("Package manager:"), gbc)

        gbc.gridx = 1
        gbc.fill = GridBagConstraints.HORIZONTAL
        innerPanel.add(packageManagerCombo, gbc)

        // API strategy
        gbc.gridx = 0
        gbc.gridy = 3
        gbc.fill = GridBagConstraints.NONE
        innerPanel.add(JLabel("API strategy:"), gbc)

        gbc.gridx = 1
        gbc.fill = GridBagConstraints.HORIZONTAL
        innerPanel.add(apiStrategyCombo, gbc)

        // Validation library (hidden for templates that opt out via supportsValidationSelection = false)
        gbc.gridx = 0
        gbc.gridy = 4
        gbc.fill = GridBagConstraints.NONE
        innerPanel.add(validationLibraryLabel, gbc)

        gbc.gridx = 1
        gbc.fill = GridBagConstraints.HORIZONTAL
        innerPanel.add(validationLibraryCombo, gbc)

        applyValidationVisibility(templateList.selectedValue as? ProjectTemplate)

        panel.add(innerPanel, BorderLayout.CENTER)
        return panel
    }

    override fun updateDataModel() {
        builder.packageManager = packageManagerCombo.selectedItem as PackageManager
        builder.apiStrategy = apiStrategyCombo.selectedItem as ApiStrategy
        builder.validationLibrary = validationLibraryCombo.selectedItem as ValidationLibrary
        val selected = templateList.selectedValue
        if (selected is ProjectTemplate) {
            builder.selectedTemplate = selected
        }
    }

    /**
     * Populates the list model with category headers and template entries.
     */
    private fun populateTemplateList() {
        var currentCategory: TemplateCategory? = null
        for (template in ProjectTemplate.entries) {
            if (template.category != currentCategory) {
                currentCategory = template.category
                templateListModel.addElement(currentCategory)
            }
            templateListModel.addElement(template)
        }
    }

    /**
     * Configures the template list with custom rendering and selection behavior.
     */
    private fun configureTemplateList() {
        templateList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        templateList.cellRenderer = TemplateCellRenderer()

        // Select the first template (BASIC) by default
        for (i in 0 until templateListModel.size()) {
            if (templateListModel.getElementAt(i) is ProjectTemplate) {
                templateList.selectedIndex = i
                break
            }
        }

        templateList.addListSelectionListener {
            val selected = templateList.selectedValue
            if (selected is ProjectTemplate) {
                descriptionArea.text = selected.description
                applyValidationVisibility(selected)
            } else if (selected is TemplateCategory) {
                // Skip category headers — select the next template instead
                val idx = templateList.selectedIndex
                if (idx + 1 < templateListModel.size()) {
                    templateList.selectedIndex = idx + 1
                }
            }
        }
    }

    /**
     * Toggles the validation library combo and label visibility based on whether
     * the selected template opts in to validation selection.
     *
     * Templates that ship their own data layer (TanStack Start, Remix, Astro, Waku)
     * pass `supportsValidationSelection = false` to hide this UI.
     */
    private fun applyValidationVisibility(template: ProjectTemplate?) {
        val supports = template?.supportsValidationSelection ?: true
        validationLibraryLabel.isVisible = supports
        validationLibraryCombo.isVisible = supports
    }

    /**
     * Custom cell renderer that distinguishes category headers from template items.
     *
     * Category headers are rendered in bold; template items are indented.
     */
    private class TemplateCellRenderer : DefaultListCellRenderer() {
        override fun getListCellRendererComponent(
            list: JList<*>?,
            value: Any?,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean,
        ): Component {
            val component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
            when (value) {
                is TemplateCategory -> {
                    text = value.displayName
                    font = font.deriveFont(java.awt.Font.BOLD)
                    border = EmptyBorder(4, 4, 2, 4)
                    // Category headers are not selectable visually
                    background = list?.background ?: background
                    foreground = list?.foreground ?: foreground
                }

                is ProjectTemplate -> {
                    text = value.displayName
                    font = font.deriveFont(java.awt.Font.PLAIN)
                    border = EmptyBorder(2, 16, 2, 4)
                }
            }
            return component
        }
    }
}
