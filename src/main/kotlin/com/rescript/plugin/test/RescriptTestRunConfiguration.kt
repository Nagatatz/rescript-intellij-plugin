package com.rescript.plugin.test

import com.intellij.execution.ExecutionException
import com.intellij.execution.Executor
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.RunConfigurationBase
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.configurations.RuntimeConfigurationError
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.testframework.sm.SMTestRunnerConnectionUtil
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project

/**
 * Run configuration for executing ReScript tests with Jest, Vitest, or a custom command.
 *
 * Builds a command line with the appropriate test runner flags (including TeamCity reporters
 * for SMTestRunner integration), optional test file path, test name filter (`-t`),
 * and additional user-supplied arguments.
 *
 * @see RescriptTestConsoleProperties for SMTestRunner console setup
 * @see RescriptTestSettingsEditor for the configuration UI
 */
class RescriptTestRunConfiguration(
    project: Project,
    factory: ConfigurationFactory,
    name: String,
) : RunConfigurationBase<RescriptTestRunConfigurationOptions>(project, factory, name) {
    companion object {
        private val WHITESPACE_REGEX = "\\s+".toRegex()
    }

    override fun getOptions(): RescriptTestRunConfigurationOptions =
        super.getOptions() as RescriptTestRunConfigurationOptions

    var framework: TestFramework
        get() = TestFramework.entries.find { it.id == options.framework } ?: TestFramework.JEST
        set(value) {
            options.framework = value.id
        }

    var workingDirectory: String?
        get() = options.workingDirectory
        set(value) {
            options.workingDirectory = value
        }

    var testFilePath: String?
        get() = options.testFilePath
        set(value) {
            options.testFilePath = value
        }

    var testName: String?
        get() = options.testName
        set(value) {
            options.testName = value
        }

    var additionalArguments: String?
        get() = options.additionalArguments
        set(value) {
            options.additionalArguments = value
        }

    override fun getConfigurationEditor(): SettingsEditor<RescriptTestRunConfiguration> =
        RescriptTestSettingsEditor(project)

    override fun checkConfiguration() {
        if (framework == TestFramework.CUSTOM) return
        val effectiveWorkDir =
            workingDirectory ?: project.basePath
                ?: throw RuntimeConfigurationError("Working directory is not set")
        val packageJson = java.io.File(effectiveWorkDir, "package.json")
        if (!packageJson.exists()) {
            @Suppress("DialogTitleCapitalization")
            throw RuntimeConfigurationError("package.json not found in working directory")
        }
    }

    override fun getState(
        executor: Executor,
        environment: ExecutionEnvironment,
    ): RunProfileState =
        object : CommandLineState(environment) {
            override fun startProcess(): ProcessHandler {
                val effectiveWorkDir =
                    workingDirectory ?: project.basePath
                        ?: throw ExecutionException("Working directory is not set")

                val cmd = buildCommandLine(effectiveWorkDir)

                val processHandler =
                    ProcessHandlerFactory
                        .getInstance()
                        .createColoredProcessHandler(cmd)
                ProcessTerminatedListener.attach(processHandler)

                val consoleProperties = RescriptTestConsoleProperties(this@RescriptTestRunConfiguration, executor)
                SMTestRunnerConnectionUtil.createAndAttachConsole(
                    "ReScript Test",
                    processHandler,
                    consoleProperties,
                )

                return processHandler
            }
        }

    internal fun buildCommandLine(workDir: String): GeneralCommandLine {
        val args = mutableListOf<String>()

        when (framework) {
            TestFramework.JEST -> {
                args.add("npx")
                args.add("jest")
                args.add("--reporters=default")
                args.add("--reporters=jest-teamcity")
            }
            TestFramework.VITEST -> {
                args.add("npx")
                args.add("vitest")
                args.add("run")
                args.add("--reporter=default")
                args.add("--reporter=teamcity")
            }
            TestFramework.CUSTOM -> {
                // Custom framework: user provides full command via additional arguments
            }
        }

        testFilePath?.let { path ->
            if (path.isNotBlank()) args.add(path)
        }

        testName?.let { name ->
            if (name.isNotBlank()) {
                args.add("-t")
                args.add(name)
            }
        }

        additionalArguments?.let { extra ->
            val trimmed = extra.trim()
            if (trimmed.isNotEmpty()) {
                args.addAll(trimmed.split(WHITESPACE_REGEX))
            }
        }

        if (args.isEmpty()) {
            throw ExecutionException("No command configured for test execution")
        }

        return GeneralCommandLine(args)
            .withWorkDirectory(workDir)
            .withCharset(Charsets.UTF_8)
            .withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
    }
}
