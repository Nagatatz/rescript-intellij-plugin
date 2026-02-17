package com.rescript.plugin.formatter

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.formatting.service.AsyncDocumentFormattingService
import com.intellij.formatting.service.AsyncFormattingRequest
import com.intellij.formatting.service.FormattingService.Feature
import com.intellij.psi.PsiFile
import com.rescript.plugin.RescriptFileType
import com.rescript.plugin.RescriptInterfaceFileType
import com.rescript.plugin.run.RescriptCliDetector
import java.io.IOException

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
                request.onError("ReScript", "rescript CLI not found in node_modules")
                return null
            }

        val documentText = request.documentText

        return object : FormattingTask {
            private var process: Process? = null

            override fun run() {
                try {
                    val commandLine =
                        GeneralCommandLine(cliPath, "format", "--stdin", ".$ext")
                            .withCharset(Charsets.UTF_8)

                    val proc = commandLine.createProcess()
                    process = proc

                    val stdinThread =
                        Thread(
                            {
                                try {
                                    proc.outputStream.bufferedWriter(Charsets.UTF_8).use {
                                        it.write(documentText)
                                    }
                                } catch (_: IOException) {
                                }
                            },
                            "rescript-format-stdin",
                        )
                    stdinThread.start()

                    val stderr = StringBuilder()
                    val stderrThread =
                        Thread(
                            {
                                try {
                                    proc.errorStream.reader(Charsets.UTF_8).use {
                                        stderr.append(it.readText())
                                    }
                                } catch (_: IOException) {
                                }
                            },
                            "rescript-format-stderr",
                        )
                    stderrThread.start()

                    val stdout =
                        proc.inputStream.reader(Charsets.UTF_8).use {
                            it.readText()
                        }

                    stdinThread.join(TIMEOUT_MS)
                    stderrThread.join(TIMEOUT_MS)
                    val exitCode = proc.waitFor()

                    if (exitCode == 0 && stdout.isNotEmpty()) {
                        request.onTextReady(stdout)
                    } else {
                        request.onError(
                            "ReScript",
                            stderr.toString().ifBlank { "rescript format failed (exit code $exitCode)" },
                        )
                    }
                } catch (e: Exception) {
                    request.onError("ReScript", e.message ?: "Unknown error")
                }
            }

            override fun cancel(): Boolean {
                process?.destroyForcibly()
                return true
            }
        }
    }

    override fun getNotificationGroupId(): String = NOTIFICATION_GROUP

    override fun getName(): String = "rescript format"
}
