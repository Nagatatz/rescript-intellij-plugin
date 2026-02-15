package com.rescript.plugin.run

import com.intellij.execution.configurations.RunConfigurationOptions

class RescriptRunConfigurationOptions : RunConfigurationOptions() {
    var command by string("build")
    var workingDirectory by string()
    var additionalArguments by string()
}
