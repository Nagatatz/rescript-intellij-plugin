package com.rescript.plugin.run

import com.intellij.execution.configurations.ConfigurationTypeBase
import com.intellij.openapi.util.NotNullLazyValue
import com.rescript.plugin.RescriptIcons

/**
 * Defines the "ReScript" run configuration type in the IDE's Run/Debug configurations.
 *
 * Registers the [RescriptConfigurationFactory] for creating new ReScript build configurations.
 */
class RescriptRunConfigurationType :
    ConfigurationTypeBase(
        ID,
        "ReScript",
        "Run ReScript build commands",
        NotNullLazyValue.createValue { RescriptIcons.FILE },
    ) {
    companion object {
        const val ID = "RescriptRunConfiguration"
    }

    init {
        addFactory(RescriptConfigurationFactory(this))
    }
}
