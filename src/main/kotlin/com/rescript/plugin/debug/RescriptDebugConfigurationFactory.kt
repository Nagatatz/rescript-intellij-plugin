package com.rescript.plugin.debug

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.project.Project

/**
 * Factory that creates [RescriptDebugRunConfiguration] instances
 * for the ReScript debug configuration type.
 *
 * @see RescriptDebugConfigurationType
 */
class RescriptDebugConfigurationFactory(
    type: ConfigurationType,
) : ConfigurationFactory(type) {
    override fun getId(): String = RescriptDebugConfigurationType.ID

    override fun createTemplateConfiguration(project: Project): RunConfiguration =
        RescriptDebugRunConfiguration(project, this, "ReScript Debug")

    override fun getOptionsClass(): Class<out BaseState> = RescriptDebugRunConfigurationOptions::class.java
}
