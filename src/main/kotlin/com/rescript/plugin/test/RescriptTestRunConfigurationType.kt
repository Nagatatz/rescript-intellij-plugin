package com.rescript.plugin.test

import com.intellij.execution.configurations.ConfigurationTypeBase
import com.intellij.openapi.util.NotNullLazyValue
import com.rescript.plugin.RescriptIcons

/**
 * Defines the "ReScript Test" run configuration type that appears in Run/Debug Configurations.
 *
 * Supports running tests with Jest, Vitest, or a custom command via [RescriptTestConfigurationFactory].
 */
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
