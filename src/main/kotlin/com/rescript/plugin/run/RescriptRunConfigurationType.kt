package com.rescript.plugin.run

import com.intellij.execution.configurations.ConfigurationTypeBase
import com.intellij.openapi.util.NotNullLazyValue
import com.rescript.plugin.RescriptIcons

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
