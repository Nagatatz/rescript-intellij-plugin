package com.rescript.plugin.debug

import com.intellij.execution.configurations.RunConfigurationOptions

/**
 * Persistent options for [RescriptDebugRunConfiguration].
 *
 * Stores the source file path, Node.js executable path, additional Node.js arguments,
 * and working directory using IntelliJ's serialization framework.
 */
class RescriptDebugRunConfigurationOptions : RunConfigurationOptions() {
    var sourceFilePath by string()
    var nodeExecutable by string("node")
    var nodeArgs by string()
    var workingDirectory by string()
}
