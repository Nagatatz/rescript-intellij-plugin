package com.rescript.plugin.test

import com.intellij.execution.Executor
import com.intellij.execution.testframework.sm.runner.SMTRunnerConsoleProperties
import com.intellij.execution.testframework.sm.runner.SMTestLocator

class RescriptTestConsoleProperties(
    configuration: RescriptTestRunConfiguration,
    executor: Executor,
) : SMTRunnerConsoleProperties(configuration, "ReScript Test", executor) {
    init {
        isIdBasedTestTree = true
    }

    override fun getTestLocator(): SMTestLocator = RescriptTestLocator.INSTANCE
}
