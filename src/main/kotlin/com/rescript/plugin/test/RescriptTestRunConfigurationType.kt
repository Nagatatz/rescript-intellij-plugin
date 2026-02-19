package com.rescript.plugin.test

import com.intellij.execution.configurations.ConfigurationTypeBase
import com.intellij.openapi.util.NotNullLazyValue
import com.rescript.plugin.RescriptIcons

class RescriptTestRunConfigurationType :
    ConfigurationTypeBase(
        ID,
        "ReScript Test",
        "Run ReScript tests with jest or vitest",
        NotNullLazyValue.createValue { RescriptIcons.FILE },
    ) {
    companion object {
        const val ID = "RescriptTestRunConfiguration"
    }

    init {
        addFactory(RescriptTestConfigurationFactory(this))
    }
}
