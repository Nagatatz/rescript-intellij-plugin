package com.rescript.plugin.test

import com.intellij.execution.Executor
import com.intellij.execution.testframework.sm.runner.SMTRunnerConsoleProperties
import com.intellij.execution.testframework.sm.runner.SMTestLocator

/**
 * Console properties for the ReScript test runner, enabling the SMTestRunner tree UI.
 *
 * Uses ID-based test tree identification and delegates test location resolution
 * to [RescriptTestLocator] for navigating from test results back to `.res` source files.
 */
class RescriptTestConsoleProperties(
    configuration: RescriptTestRunConfiguration,
    executor: Executor,
) : SMTRunnerConsoleProperties(configuration, "ReScript Test", executor) {
    init {
        isIdBasedTestTree = true
    }

    override fun getTestLocator(): SMTestLocator = RescriptTestLocator.INSTANCE
}
