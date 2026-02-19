package com.rescript.plugin.run

import com.intellij.execution.configurations.RunConfigurationOptions

/**
 * Persistent options for [RescriptRunConfiguration].
 *
 * Stores the selected command, working directory, and additional CLI arguments
 * using IntelliJ's serialization framework.
 */
class RescriptRunConfigurationOptions : RunConfigurationOptions() {
    var command by string("build")
    var workingDirectory by string()
    var additionalArguments by string()
}
