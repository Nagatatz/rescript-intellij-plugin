package com.rescript.plugin.run

import com.intellij.execution.ProgramRunnerUtil
import com.intellij.execution.RunManager
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.ide.util.PropertiesComponent
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.rescript.plugin.lsp.RescriptLspDetector

/**
 * Prompts the user to start `rescript build -w` when a ReScript project is opened.
 *
 * Displays a balloon notification with a "Start Build Watch" action if the project
 * is a ReScript project and the CLI is available. The user can dismiss the notification
 * permanently per project via "Don't show again".
 *
 * Implements [ProjectActivity] for asynchronous post-startup execution.
 *
 * @see RescriptCliDetector
 * @see RescriptLspDetector
 * @see RescriptRunConfiguration
 */
class RescriptBuildWatchStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        if (PropertiesComponent.getInstance(project).getBoolean(DISMISSED_KEY, false)) return
        if (!RescriptLspDetector.isRescriptProject(project.basePath)) return
        if (RescriptCliDetector.findCli(project.basePath, project.basePath) == null) return

        showBuildWatchNotification(project)
    }

    private fun showBuildWatchNotification(project: Project) {
        val notification =
            NotificationGroupManager
                .getInstance()
                .getNotificationGroup("ReScript")
                .createNotification(
                    "ReScript build watch",
                    "Start watching for file changes and rebuilding automatically?",
                    NotificationType.INFORMATION,
                )

        notification.addAction(
            NotificationAction.createSimpleExpiring("Start build watch") {
                startBuildWatch(project)
            },
        )

        notification.addAction(
            NotificationAction.createSimpleExpiring("Don\u2019t show again") {
                PropertiesComponent.getInstance(project).setValue(DISMISSED_KEY, true)
            },
        )

        notification.notify(project)
    }

    /**
     * Creates and executes a "ReScript: build -w" run configuration.
     *
     * @param project the current project
     */
    private fun startBuildWatch(project: Project) {
        val runManager = RunManager.getInstance(project)
        val configType = RescriptRunConfigurationType()
        val factory = configType.configurationFactories.first()

        val settings = runManager.createConfiguration("ReScript: build -w", factory)
        val config = settings.configuration as RescriptRunConfiguration
        config.command = RescriptCommand.BUILD_WATCH.id

        runManager.addConfiguration(settings)
        runManager.selectedConfiguration = settings

        ProgramRunnerUtil.executeConfiguration(settings, DefaultRunExecutor.getRunExecutorInstance())
    }

    companion object {
        private const val DISMISSED_KEY = "rescript.startup.build.watch.dismissed"
    }
}
