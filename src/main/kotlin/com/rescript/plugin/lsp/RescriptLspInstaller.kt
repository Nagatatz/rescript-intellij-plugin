package com.rescript.plugin.lsp

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.platform.lsp.api.LspServerManager
import com.intellij.ui.EditorNotifications
import com.rescript.plugin.util.RescriptMessageSanitizer
import java.io.File

/**
 * Handles background installation of `@rescript/language-server` via the detected package manager.
 *
 * Runs the install command as a background task with progress indication. On success,
 * restarts the LSP server and refreshes editor notifications. On failure, shows an
 * error balloon notification with stderr output.
 *
 * @see RescriptPackageManagerDetector
 * @see RescriptLspServerSupportProvider
 */
object RescriptLspInstaller {
    private val LOG = logger<RescriptLspInstaller>()

    // Longer timeout for package installation (network-dependent)
    private const val INSTALL_TIMEOUT_MS = 300_000L

    /**
     * Installs `@rescript/language-server` in the background using the specified package manager.
     *
     * @param project the current IntelliJ project
     * @param workingDirectory the directory to run the install command in
     * @param packageManager the package manager to use for installation
     * @param onSuccess optional callback invoked on successful installation
     * @param onFailure optional callback invoked on failed installation with error message
     */
    fun install(
        project: Project,
        workingDirectory: String,
        packageManager: PackageManager,
        onSuccess: (() -> Unit)? = null,
        onFailure: ((String) -> Unit)? = null,
    ) {
        ProgressManager.getInstance().run(
            object : Task.Backgroundable(
                project,
                "Installing @rescript/language-server...",
                true,
            ) {
                override fun run(indicator: ProgressIndicator) {
                    indicator.isIndeterminate = true

                    val commandLine = buildCommandLine(packageManager, workingDirectory)
                    LOG.info("Running: ${commandLine.commandLineString} in $workingDirectory")

                    val handler = OSProcessHandler(commandLine)
                    val stderr = StringBuilder()

                    handler.addProcessListener(
                        object : ProcessListener {
                            override fun onTextAvailable(
                                event: ProcessEvent,
                                outputType: Key<*>,
                            ) {
                                // Collect stderr for error reporting
                                if (outputType.toString() == "stderr") {
                                    stderr.append(event.text)
                                }
                            }
                        },
                    )

                    handler.startNotify()
                    val completed = handler.waitFor(INSTALL_TIMEOUT_MS)

                    if (!completed) {
                        handler.destroyProcess()
                        val errorMsg = "Installation timed out after ${INSTALL_TIMEOUT_MS / 1000} seconds"
                        LOG.warn(errorMsg)
                        onInstallFailure(project, errorMsg)
                        onFailure?.invoke(errorMsg)
                        return
                    }

                    val exitCode = handler.exitCode
                    if (exitCode == 0) {
                        LOG.info("@rescript/language-server installed successfully")
                        onInstallSuccess(project)
                        onSuccess?.invoke()
                    } else {
                        val rawErrorMsg = stderr.toString().trim().ifEmpty { "Exit code: $exitCode" }
                        // Strip absolute paths from package-manager stderr before display.
                        val errorMsg = RescriptMessageSanitizer.sanitize(project, rawErrorMsg)
                        LOG.warn("Failed to install @rescript/language-server: $errorMsg")
                        onInstallFailure(project, errorMsg)
                        onFailure?.invoke(errorMsg)
                    }
                }
            },
        )
    }

    /**
     * Builds the command line for installing `@rescript/language-server`.
     *
     * @param packageManager the package manager whose install command to use
     * @param workingDirectory the working directory for the command
     * @return the configured command line
     */
    internal fun buildCommandLine(
        packageManager: PackageManager,
        workingDirectory: String,
    ): GeneralCommandLine {
        val args = packageManager.installCommand + "@rescript/language-server"
        val cmd = GeneralCommandLine(args)
        cmd.workDirectory = File(workingDirectory)
        return cmd
    }

    private fun onInstallSuccess(project: Project) {
        // Restart LSP server so it picks up the newly installed language server.
        // LspServerManager is deprecated in 2026.2 EAP; the replacement LspClientDescriptor
        // API does not exist on the 2026.1.2 compile target. UnstableApiUsage kept too.
        @Suppress("UnstableApiUsage", "DEPRECATION")
        LspServerManager.getInstance(project).stopAndRestartIfNeeded(RescriptLspServerSupportProvider::class.java)

        // Update editor notifications to remove the warning bar
        EditorNotifications.getInstance(project).updateAllNotifications()

        // Show success notification
        NotificationGroupManager
            .getInstance()
            .getNotificationGroup("ReScript")
            .createNotification(
                "ReScript Language Server installed successfully.",
                NotificationType.INFORMATION,
            ).notify(project)
    }

    private fun onInstallFailure(
        project: Project,
        errorMessage: String,
    ) {
        NotificationGroupManager
            .getInstance()
            .getNotificationGroup("ReScript")
            .createNotification(
                "Failed to install @rescript/language-server",
                errorMessage,
                NotificationType.ERROR,
            ).notify(project)
    }
}
