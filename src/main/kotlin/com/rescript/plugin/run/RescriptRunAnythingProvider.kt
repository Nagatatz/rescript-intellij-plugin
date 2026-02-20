package com.rescript.plugin.run

import com.intellij.ide.actions.runAnything.activity.RunAnythingProviderBase
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.project.Project
import javax.swing.Icon

/**
 * Run Anything provider for ReScript CLI commands.
 *
 * Adds `rescript build`, `rescript clean`, and `rescript format` commands
 * to the Run Anything dialog (Ctrl+Ctrl). Only active in projects where
 * `rescript.json` is detected.
 *
 * @see RescriptCliDetector for CLI discovery logic
 */
class RescriptRunAnythingProvider : RunAnythingProviderBase<String>() {
    override fun getCommand(value: String): String = value

    override fun findMatchingValue(
        dataContext: DataContext,
        pattern: String,
    ): String? {
        if (!pattern.startsWith(HELP_COMMAND)) return null
        return COMMANDS.find { it == pattern }
    }

    override fun getValues(
        dataContext: DataContext,
        pattern: String,
    ): Collection<String> {
        if (!pattern.startsWith("rescript")) return emptyList()
        val project = dataContext.getData(CommonDataKeys.PROJECT) ?: return emptyList()
        if (!isRescriptProject(project)) return emptyList()
        return COMMANDS.filter { it.startsWith(pattern) }
    }

    override fun execute(
        dataContext: DataContext,
        value: String,
    ) {
        val project = dataContext.getData(CommonDataKeys.PROJECT) ?: return
        val cliPath = RescriptCliDetector.findCli(project.basePath, project.basePath) ?: return

        val parts = value.removePrefix("rescript").trim().split(" ")
        val args = mutableListOf(cliPath)
        args.addAll(parts.filter { it.isNotBlank() })

        val commandLine =
            com.intellij.execution.configurations
                .GeneralCommandLine(args)
        project.basePath?.let { commandLine.workDirectory = java.io.File(it) }

        val processHandler =
            com.intellij.execution.process
                .OSProcessHandler(commandLine)
        processHandler.startNotify()
    }

    override fun getHelpCommand(): String = HELP_COMMAND

    override fun getHelpGroupTitle(): String = "ReScript"

    override fun getHelpCommandPlaceholder(): String = "rescript <command>"

    override fun getCompletionGroupTitle(): String = "ReScript"

    override fun getHelpIcon(): Icon? = com.rescript.plugin.RescriptIcons.FILE

    companion object {
        private const val HELP_COMMAND = "rescript"

        private val COMMANDS =
            listOf(
                "rescript build",
                "rescript build -w",
                "rescript clean",
                "rescript format",
            )

        /**
         * Checks if the project contains a `rescript.json` configuration file.
         *
         * @param project the current project
         * @return true if rescript.json exists at the project root
         */
        private fun isRescriptProject(project: Project): Boolean {
            val basePath = project.basePath ?: return false
            return java.io.File(basePath, "rescript.json").exists() ||
                java.io.File(basePath, "bsconfig.json").exists()
        }
    }
}
