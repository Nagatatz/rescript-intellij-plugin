package com.rescript.plugin.debug

import com.intellij.execution.ExecutionException
import com.intellij.execution.Executor
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.RunConfigurationBase
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.configurations.RuntimeConfigurationError
import com.intellij.execution.configurations.RuntimeConfigurationWarning
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFileManager
import com.rescript.plugin.run.RescriptRunUtils
import com.rescript.plugin.util.RescriptFileUtil
import com.rescript.plugin.util.RescriptSecurityUtils
import java.nio.file.Path

/**
 * Run configuration for debugging compiled JavaScript from ReScript files.
 *
 * Resolves the specified `.res` source file to its compiled JavaScript counterpart
 * in `lib/js/`, then launches `node --inspect-brk <jsFile>` to enable debugging
 * with Chrome DevTools or any V8 inspector client.
 *
 * @see RescriptDebugConfigurationType
 * @see RescriptDebugConfigurationFactory
 */
class RescriptDebugRunConfiguration(
    project: Project,
    factory: ConfigurationFactory,
    name: String,
) : RunConfigurationBase<RescriptDebugRunConfigurationOptions>(project, factory, name) {
    companion object {
        private val JS_SUFFIXES = listOf(".bs.js", ".mjs", ".js")
    }

    override fun getOptions(): RescriptDebugRunConfigurationOptions =
        super.getOptions() as RescriptDebugRunConfigurationOptions

    var sourceFilePath: String?
        get() = options.sourceFilePath
        set(value) {
            options.sourceFilePath = value
        }

    var nodeExecutable: String
        get() = options.nodeExecutable ?: "node"
        set(value) {
            options.nodeExecutable = value
        }

    var nodeArgs: String?
        get() = options.nodeArgs
        set(value) {
            options.nodeArgs = value
        }

    var workingDirectory: String?
        get() = options.workingDirectory
        set(value) {
            options.workingDirectory = value
        }

    override fun getConfigurationEditor(): SettingsEditor<RescriptDebugRunConfiguration> =
        RescriptDebugSettingsEditor(project)

    override fun checkConfiguration() {
        if (sourceFilePath.isNullOrBlank()) {
            throw RuntimeConfigurationError("Source file path is not specified.")
        }
        val srcPath = Path.of(sourceFilePath ?: throw RuntimeConfigurationError("Source file path is not specified."))
        val fileName = srcPath.fileName?.toString() ?: ""
        if (!RescriptFileUtil.isRescriptFileName(fileName)) {
            throw RuntimeConfigurationError("Source file must be a .res or .resi file.")
        }
        if (!srcPath.toFile().exists()) {
            throw RuntimeConfigurationWarning("Source file does not exist: $sourceFilePath")
        }

        // Validate custom node executable if not the default "node"
        val node = options.nodeExecutable
        if (!node.isNullOrEmpty() && node != "node" && !RescriptSecurityUtils.isValidExecutable(node)) {
            throw RuntimeConfigurationWarning("Node.js executable is not valid: $node")
        }
    }

    override fun getState(
        executor: Executor,
        environment: ExecutionEnvironment,
    ): RunProfileState {
        return object : CommandLineState(environment) {
            override fun startProcess(): ProcessHandler {
                val jsFile =
                    resolveCompiledJsFile()
                        ?: throw ExecutionException(
                            "Cannot find compiled JavaScript for '$sourceFilePath'. " +
                                "Compile your project first.",
                        )

                val effectiveWorkDir = workingDirectory ?: project.basePath

                val cmd =
                    GeneralCommandLine(nodeExecutable, "--inspect-brk", jsFile)
                        .withWorkDirectory(effectiveWorkDir)
                        .withCharset(Charsets.UTF_8)
                        .withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)

                // Append additional node arguments if provided
                nodeArgs?.let { args ->
                    val trimmed = args.trim()
                    if (trimmed.isNotEmpty()) {
                        cmd.addParameters(trimmed.split(RescriptRunUtils.WHITESPACE_REGEX))
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

    /**
     * Resolves the configured source `.res` file to its compiled JavaScript path.
     *
     * Searches `lib/js/` under the project root for files with `.bs.js`, `.mjs`, or `.js` suffixes
     * matching the relative path of the source file.
     *
     * @return the absolute path to the compiled JS file, or null if not found
     */
    internal fun resolveCompiledJsFile(): String? {
        val srcPath = sourceFilePath ?: return null
        val projectDir = project.guessProjectDir() ?: return null

        val srcVirtualFile =
            VirtualFileManager.getInstance().findFileByNioPath(Path.of(srcPath))
                ?: return null

        val relativePath = VfsUtil.getRelativePath(srcVirtualFile, projectDir) ?: return null
        val basePath = relativePath.removeSuffix(".resi").removeSuffix(".res")

        val libJsDir = projectDir.findFileByRelativePath("lib/js") ?: return null
        for (suffix in JS_SUFFIXES) {
            val jsFile = libJsDir.findFileByRelativePath("$basePath$suffix")
            if (jsFile != null) {
                return jsFile.path
            }
        }
        return null
    }
}
