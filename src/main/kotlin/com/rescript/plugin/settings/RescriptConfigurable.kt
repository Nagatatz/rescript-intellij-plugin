package com.rescript.plugin.settings

import com.intellij.openapi.components.serviceOrNull
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.project.Project
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * Settings UI for the ReScript plugin, accessible via
 * Settings > Languages & Frameworks > ReScript.
 *
 * Layout, labels, tooltips, and default values are driven by
 * [RescriptSettingsSchema]; this class only coordinates the UI lifecycle
 * (FormBuilder assembly, isModified / apply / reset walks) and the LSP and
 * reanalyze-server side effects triggered by Apply.
 */
class RescriptConfigurable(
    private val project: Project,
) : Configurable {
    private var panel: JPanel? = null
    private var components: Map<String, SettingComponent<*>> = emptyMap()

    override fun getDisplayName(): String = "ReScript"

    override fun createComponent(): JComponent {
        val built = mutableMapOf<String, SettingComponent<*>>()
        val builder = FormBuilder.createFormBuilder()
        for (entry in RescriptSettingsSchema.entries) {
            when (entry) {
                SchemaEntry.Separator -> {
                    builder.addSeparator()
                }

                is SchemaEntry.Field<*> -> {
                    val descriptor = entry.descriptor
                    val component = descriptor.createComponent(project)
                    built[descriptor.id] = component
                    if (entry.label != null) {
                        builder.addLabeledComponent(entry.label, component.swing)
                    } else {
                        builder.addComponent(component.swing)
                    }
                    entry.tooltip?.let { builder.addTooltip(it) }
                }
            }
        }
        val formPanel = builder.addComponentFillVertically(JPanel(), 0).panel
        components = built
        panel = formPanel
        return formPanel
    }

    override fun isModified(): Boolean {
        val settings = RescriptProjectSettings.getInstance(project)
        return fieldEntries().any { entry -> entryIsModified(entry, settings) }
    }

    // LspServerManager is deprecated in 2026.2 EAP; the replacement LspClientDescriptor
    // API does not exist on the 2026.1.2 compile target.
    @Suppress("UnstableApiUsage", "DEPRECATION")
    @Throws(ConfigurationException::class)
    override fun apply() {
        // Validate filesystem paths before writing any value, preserving the
        // original "abort-on-first-failure" order from the legacy apply() flow.
        val pathSnapshot =
            RescriptSettingsSchema.pathDescriptorIds.associateWith { id ->
                pathComponent(id).getValue()
            }
        RescriptSettingsValidator.validateLspPath(pathSnapshot.getValue("lspServerPath"))
        RescriptSettingsValidator.validateNodePath(pathSnapshot.getValue("nodePath"))
        RescriptSettingsValidator.validateRescriptBinaryPath(pathSnapshot.getValue("rescriptBinaryPath"))
        RescriptSettingsValidator.validatePlatformPath(pathSnapshot.getValue("platformPath"))
        RescriptSettingsValidator.validateRuntimePath(pathSnapshot.getValue("runtimePath"))

        val settings = RescriptProjectSettings.getInstance(project)
        val previousReanalyzeEnabled = settings.reanalyzeServerEnabled
        for (entry in fieldEntries()) {
            applyEntry(entry, settings)
        }

        val newReanalyzeEnabled = settings.reanalyzeServerEnabled
        if (newReanalyzeEnabled != previousReanalyzeEnabled) {
            val serverService =
                project.serviceOrNull<com.rescript.plugin.analysis.RescriptReanalyzeServerService>()
            if (newReanalyzeEnabled) {
                serverService?.startServer()
            } else {
                serverService?.stopServer()
            }
        }

        com.intellij.platform.lsp.api.LspServerManager
            .getInstance(project)
            .stopAndRestartIfNeeded(com.rescript.plugin.lsp.RescriptLspServerSupportProvider::class.java)
    }

    override fun reset() {
        val settings = RescriptProjectSettings.getInstance(project)
        for (entry in fieldEntries()) {
            resetEntry(entry, settings)
        }
    }

    override fun disposeUIResources() {
        panel = null
        components = emptyMap()
    }

    private fun fieldEntries(): List<SchemaEntry.Field<*>> =
        RescriptSettingsSchema.entries.filterIsInstance<SchemaEntry.Field<*>>()

    /**
     * Type-safe accessor for the heterogeneous [components] map. The cast
     * is safe by construction: each field entry was registered into the
     * map under `entry.descriptor.id` with a matching
     * `SettingComponent<T>` during `createComponent()`, so retrieving by
     * the same entry guarantees the runtime type lines up with the
     * declared `T`.
     */
    @Suppress("UNCHECKED_CAST")
    private fun <T> componentFor(entry: SchemaEntry.Field<T>): SettingComponent<T> =
        components[entry.descriptor.id] as SettingComponent<T>

    /**
     * String-typed accessor used by the path-validation block in
     * [apply]. Path descriptor ids come from
     * [RescriptSettingsSchema.pathDescriptorIds], which only lists
     * `String`-valued descriptors, so the cast is invariant-checked at
     * registration time rather than at the call site.
     */
    @Suppress("UNCHECKED_CAST")
    private fun pathComponent(id: String): SettingComponent<String> = components[id] as SettingComponent<String>

    private fun <T> entryIsModified(
        entry: SchemaEntry.Field<T>,
        settings: RescriptProjectSettings,
    ): Boolean = componentFor(entry).getValue() != entry.descriptor.currentValue(settings)

    private fun <T> applyEntry(
        entry: SchemaEntry.Field<T>,
        settings: RescriptProjectSettings,
    ) {
        entry.descriptor.applyValue(settings, componentFor(entry).getValue())
    }

    private fun <T> resetEntry(
        entry: SchemaEntry.Field<T>,
        settings: RescriptProjectSettings,
    ) {
        componentFor(entry).setValue(entry.descriptor.currentValue(settings))
    }
}
