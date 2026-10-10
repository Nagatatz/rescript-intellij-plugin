package com.rescript.plugin.formatter

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.formatting.service.AsyncDocumentFormattingService
import com.intellij.formatting.service.AsyncFormattingRequest
import com.intellij.formatting.service.FormattingService.Feature
import com.intellij.psi.PsiFile
import com.rescript.plugin.RescriptFileType
import com.rescript.plugin.RescriptInterfaceFileType
import com.rescript.plugin.run.RescriptCliDetector
import com.rescript.plugin.util.RescriptProcessRunner

/**
 * Integrates the ReScript CLI formatter (`rescript format`) with IntelliJ's
 * code formatting system (Cmd+Option+L).
 *
 * Pipes the document content to `rescript format --stdin .<ext>` via stdin
 * and replaces the document with the formatted output. Runs asynchronously
 * to avoid blocking the UI thread.
 */
class RescriptFormattingService : AsyncDocumentFormattingService() {
    companion object {
        private const val NOTIFICATION_GROUP = "ReScript"
        private const val TIMEOUT_MS = 10_000L
    }

    override fun getFeatures(): Set<Feature> = emptySet()

    override fun canFormat(file: PsiFile): Boolean =
        file.fileType is RescriptFileType || file.fileType is RescriptInterfaceFileType

    override fun createFormattingTask(request: AsyncFormattingRequest): FormattingTask? {
        val project = request.context.project
        val ioFile = request.ioFile ?: return null
        val ext = ioFile.extension.ifEmpty { "res" }

        val cliPath =
            RescriptCliDetector.findCli(
                ioFile.parent,
                project.basePath,
            ) ?: run {
                @Suppress("DialogTitleCapitalization")
                request.onError("ReScript", "rescript CLI not found in node_modules")
                return null
            }

        val documentText = request.documentText

        return object : FormattingTask {
            private val cancellation = RescriptProcessRunner.Cancellation()

            override fun run() {
                try {
                    val commandLine =
                        GeneralCommandLine(cliPath, "format", "--stdin", ".$ext")
                            .withCharset(Charsets.UTF_8)
                    val result =
                        RescriptProcessRunner.run(
                            start = { commandLine.createProcess() },
                            stdin = documentText,
                            timeoutMs = TIMEOUT_MS,
                            cancellation = cancellation,
                            checkCancelled = {
                                com.intellij.openapi.progress.ProgressManager
                                    .checkCanceled()
                            },
                        )
                    if (cancellation.isCancelled) return
                    if (result.successful && result.stdout.isNotEmpty()) {
                        request.onTextReady(result.stdout)
                    } else {
                        val message =
                            if (result.failure != null) {
                                result.failureMessage
                            } else {
                                com.rescript.plugin.util.RescriptMessageSanitizer.sanitize(
                                    project,
                                    result.stderr.take(4096).ifBlank { result.failureMessage },
                                )
                            }
                        request.onError("ReScript", message)
                    }
                } catch (e: com.intellij.openapi.progress.ProcessCanceledException) {
                    throw e
                } catch (_: Exception) {
                    if (!cancellation.isCancelled) request.onError("ReScript", "Formatter could not be started")
                }
            }

            override fun cancel(): Boolean {
                cancellation.cancel()
                return true
            }
        }
    }

    override fun getNotificationGroupId(): String = NOTIFICATION_GROUP

    @Suppress("DialogTitleCapitalization") // "rescript format" is a CLI command name
    override fun getName(): String = "rescript format"
}
