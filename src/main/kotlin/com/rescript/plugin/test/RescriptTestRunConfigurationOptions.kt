package com.rescript.plugin.test

import com.intellij.execution.configurations.RunConfigurationOptions

class RescriptTestRunConfigurationOptions : RunConfigurationOptions() {
    var framework by string(TestFramework.JEST.id)
    var workingDirectory by string()
    var testFilePath by string()
    var testName by string()
    var additionalArguments by string()
}
