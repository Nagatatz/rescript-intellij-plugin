package com.rescript.plugin.run

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.project.Project

/**
 * Factory that creates [RescriptRunConfiguration] instances for the ReScript run configuration type.
 */
class RescriptConfigurationFactory(
    type: ConfigurationType,
) : ConfigurationFactory(type) {
    override fun getId(): String = RescriptRunConfigurationType.ID

    override fun createTemplateConfiguration(project: Project): RunConfiguration =
        RescriptRunConfiguration(project, this, "ReScript Build")

    override fun getOptionsClass(): Class<out BaseState> = RescriptRunConfigurationOptions::class.java
}
