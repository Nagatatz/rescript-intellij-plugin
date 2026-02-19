package com.rescript.plugin.test

import com.intellij.execution.configurations.RunConfigurationOptions

/**
 * Persisted options for [RescriptTestRunConfiguration].
 *
 * Stores framework choice, working directory, test file path, test name filter,
 * and additional arguments. Values are serialized by the IntelliJ run configuration framework.
 */
class RescriptTestRunConfigurationOptions : RunConfigurationOptions() {
    var framework by string(TestFramework.JEST.id)
    var workingDirectory by string()
    var testFilePath by string()
    var testName by string()
    var additionalArguments by string()
}
