package com.rescript.plugin.debug

import com.intellij.execution.ExecutionManager
import com.intellij.execution.RunManager
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.runners.ExecutionEnvironmentBuilder
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.rescript.plugin.navigation.RescriptOpenCompiledJsAction

/**
 * Action to debug the compiled JavaScript for the current ReScript file.
 *
 * Resolves the active `.res` or `.resi` file to its compiled JavaScript in `lib/js/`,
 * then creates and executes a [RescriptDebugRunConfiguration] with `node --inspect-brk`
 * to enable debugging via Chrome DevTools or any V8 inspector client.
 *
 * Available in the GoTo menu with the shortcut `Alt+Shift+D`.
 *
 * @see RescriptOpenCompiledJsAction for the compiled JS file resolution logic
 * @see RescriptDebugRunConfiguration for the underlying run configuration
 */
class RescriptDebugCompiledJsAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        val ext = file.extension ?: return
        if (ext != "res" && ext != "resi") return

        val jsFile = RescriptOpenCompiledJsAction.findCompiledJsFile(project, file)
        if (jsFile == null) {
            Messages.showInfoMessage(
                project,
                "Compile your project first to generate JavaScript output.",
                "Compiled File Not Found",
            )
            return
        }

        executeDebug(project, file, jsFile)
    }

    /**
     * Creates a temporary ReScript Debug run configuration and executes it.
     *
     * @param project the current project
     * @param sourceFile the original ReScript source file
     * @param jsFile the compiled JavaScript virtual file
     */
    private fun executeDebug(
        project: Project,
        sourceFile: VirtualFile,
        jsFile: VirtualFile,
    ) {
        try {
            val runManager = RunManager.getInstance(project)
            val configType = RescriptDebugConfigurationType()
            val factory = configType.configurationFactories.first()

            val configName = "Debug ${sourceFile.name}"
            val settings = runManager.createConfiguration(configName, factory)
            val config = settings.configuration as RescriptDebugRunConfiguration
            config.sourceFilePath = sourceFile.path
            // Working directory defaults to project base path
            config.workingDirectory = project.basePath

            settings.isTemporary = true
            runManager.addConfiguration(settings)
            runManager.selectedConfiguration = settings

            val executor = DefaultRunExecutor.getRunExecutorInstance()
            val environment =
                ExecutionEnvironmentBuilder
                    .createOrNull(executor, settings)
                    ?.build()
                    ?: return

            ExecutionManager.getInstance(project).restartRunProfile(environment)
        } catch (ex: Exception) {
            LOG.warn("Failed to launch debug session for ${sourceFile.path}", ex)
            Messages.showErrorDialog(
                project,
                "Failed to start debug session: ${ex.message}",
                "Debug Error",
            )
        }
    }

    override fun update(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        val ext = file?.extension
        e.presentation.isEnabledAndVisible = ext == "res" || ext == "resi"
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    companion object {
        private val LOG = logger<RescriptDebugCompiledJsAction>()
    }
}
