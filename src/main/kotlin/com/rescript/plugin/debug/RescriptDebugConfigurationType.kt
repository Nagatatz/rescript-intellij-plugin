package com.rescript.plugin.debug

import com.intellij.execution.configurations.ConfigurationTypeBase
import com.intellij.openapi.util.NotNullLazyValue
import com.rescript.plugin.RescriptIcons

/**
 * Defines the "ReScript Debug" run configuration type in the IDE's Run/Debug configurations.
 *
 * Registers [RescriptDebugConfigurationFactory] for creating new debug configurations
 * that launch compiled JavaScript via `node --inspect-brk`.
 *
 * @see RescriptDebugRunConfiguration
 */
class RescriptDebugConfigurationType :
    ConfigurationTypeBase(
        ID,
        "ReScript Debug",
        "Debug compiled JavaScript from ReScript files",
        NotNullLazyValue.createValue { RescriptIcons.FILE },
    ) {
    companion object {
        const val ID = "RescriptDebugConfiguration"
    }

    init {
        addFactory(RescriptDebugConfigurationFactory(this))
    }
}
