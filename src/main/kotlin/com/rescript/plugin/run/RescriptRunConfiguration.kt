package com.rescript.plugin.run

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
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project

class RescriptRunConfiguration(
    project: Project,
    factory: ConfigurationFactory,
    name: String,
) : RunConfigurationBase<RescriptRunConfigurationOptions>(project, factory, name) {
    companion object {
        private val WHITESPACE_REGEX = "\\s+".toRegex()
    }

    public override fun getOptions(): RescriptRunConfigurationOptions =
        super.getOptions() as RescriptRunConfigurationOptions

    var command: String
        get() = options.command ?: RescriptCommand.BUILD.id
        set(value) {
            options.command = value
        }

    var workingDirectory: String?
        get() = options.workingDirectory
        set(value) {
            options.workingDirectory = value
        }

    var additionalArguments: String?
        get() = options.additionalArguments
        set(value) {
            options.additionalArguments = value
        }

    override fun getConfigurationEditor(): SettingsEditor<RescriptRunConfiguration> = RescriptSettingsEditor(project)

    override fun checkConfiguration() {
        val effectiveWorkDir = workingDirectory ?: project.basePath
        RescriptCliDetector.findCli(effectiveWorkDir, project.basePath)
            ?: throw RuntimeConfigurationError(
                "Cannot find 'rescript' CLI. Ensure it is installed via npm in the project.",
            )
    }

    override fun getState(
        executor: Executor,
        environment: ExecutionEnvironment,
    ): RunProfileState {
        return object : CommandLineState(environment) {
            override fun startProcess(): ProcessHandler {
                val effectiveWorkDir = workingDirectory ?: project.basePath
                val cliPath =
                    RescriptCliDetector.findCli(effectiveWorkDir, project.basePath)
                        ?: throw ExecutionException(
                            "Cannot find 'rescript' CLI. Ensure it is installed via npm in the project.",
                        )

                val cmd =
                    GeneralCommandLine(cliPath)
                        .withWorkDirectory(effectiveWorkDir)
                        .withCharset(Charsets.UTF_8)
                        .withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)

                val rescriptCommand = RescriptCommand.fromId(command)
                cmd.addParameters(rescriptCommand.args)

                additionalArguments?.let { args ->
                    val trimmed = args.trim()
                    if (trimmed.isNotEmpty()) {
                        cmd.addParameters(trimmed.split(WHITESPACE_REGEX))
                    }
                }

                val processHandler =
                    ProcessHandlerFactory
                        .getInstance()
                        .createColoredProcessHandler(cmd)
                ProcessTerminatedListener.attach(processHandler)
                return processHandler
            }
        }
    }
}
