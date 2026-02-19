package com.rescript.plugin.test

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.project.Project

/**
 * Factory for creating [RescriptTestRunConfiguration] instances.
 *
 * Registered via [RescriptTestRunConfigurationType] to provide template configurations
 * for the Run/Debug Configurations dialog.
 */
class RescriptTestConfigurationFactory(
    type: ConfigurationType,
) : ConfigurationFactory(type) {
    override fun getId(): String = RescriptTestRunConfigurationType.ID

    override fun createTemplateConfiguration(project: Project): RunConfiguration =
        RescriptTestRunConfiguration(project, this, "ReScript Test")

    override fun getOptionsClass(): Class<out BaseState> = RescriptTestRunConfigurationOptions::class.java
}
