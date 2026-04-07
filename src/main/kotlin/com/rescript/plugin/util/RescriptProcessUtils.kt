package com.rescript.plugin.util

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.diagnostic.logger
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Shared utility for running simple external processes with timeout handling.
 *
 * Provides a common pattern for running a command, capturing the first line
 * of stdout, and handling timeouts. Used by LSP server detection and Node.js
 * availability checks.
 *
 * @see com.rescript.plugin.lsp.RescriptLspServerDescriptor for LSP path resolution
 * @see com.rescript.plugin.binding.DtsNodeDetector for Node.js detection
 */
object RescriptProcessUtils {
    private val LOG = logger<RescriptProcessUtils>()

    /**
     * Result of a simple process execution.
     *
     * @param exitCode the process exit code (-1 if timed out or not started)
     * @param firstLine the first line of stdout, trimmed (empty if no output)
     * @param timedOut true if the process exceeded the timeout
     */
    data class ProcessResult(
        val exitCode: Int,
        val firstLine: String,
        val timedOut: Boolean,
    )

    /**
     * Runs a command with timeout, capturing the first line of stdout.
     *
     * The process's stdout and stderr are merged. Only the first line of output
     * is captured — this is suitable for commands like `which`, `where`, and
     * `node --version` that produce single-line output.
     *
     * @param command the command and arguments to execute
     * @param timeoutSeconds maximum time to wait (default: [RescriptSecurityUtils.PROCESS_TIMEOUT_SECONDS])
     * @return the process result, or a timed-out/error result on failure
     */
    fun runSimpleCommand(
        vararg command: String,
        timeoutSeconds: Long = RescriptSecurityUtils.PROCESS_TIMEOUT_SECONDS,
    ): ProcessResult =
        try {
            val proc = ProcessBuilder(*command).redirectErrorStream(true).start()
            val output =
                proc.inputStream.use { stream ->
                    stream.bufferedReader().readLine()?.trim() ?: ""
                }
            val completed = proc.waitFor(timeoutSeconds, TimeUnit.SECONDS)
            if (!completed) {
                proc.destroyForcibly()
                ProcessResult(exitCode = -1, firstLine = output, timedOut = true)
            } else {
                ProcessResult(exitCode = proc.exitValue(), firstLine = output, timedOut = false)
            }
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            ProcessResult(exitCode = -1, firstLine = "", timedOut = false)
        } catch (e: Exception) {
            LOG.debug("Process execution failed for: ${command.joinToString(" ")}", e)
            ProcessResult(exitCode = -1, firstLine = "", timedOut = false)
        }

    /**
     * Result of a process execution with full stdin/stdout support.
     *
     * @param stdout the full stdout output
     * @param stderr the full stderr output
     * @param exitCode the process exit code (-1 if timed out)
     * @param timedOut true if the process exceeded the timeout
     */
    data class StdinProcessResult(
        val stdout: String,
        val stderr: String,
        val exitCode: Int,
        val timedOut: Boolean,
    )

    /**
     * Runs a command feeding [stdinContent] to its stdin and capturing full stdout/stderr.
     *
     * Uses separate daemon threads for stdin writing and stderr reading to avoid
     * deadlocks. The process is forcibly destroyed on timeout.
     *
     * @param commandLine the command to execute
     * @param stdinContent the text to write to the process's stdin
     * @param timeoutMs maximum time to wait in milliseconds
     * @return the process result with stdout, stderr, exit code, and timeout status
     */
    fun executeWithStdin(
        commandLine: GeneralCommandLine,
        stdinContent: String,
        timeoutMs: Long = 10_000L,
    ): StdinProcessResult {
        val process = commandLine.createProcess()

        // Write stdin in a separate thread to avoid deadlock
        val stdinThread =
            Thread(
                {
                    try {
                        process.outputStream.bufferedWriter(Charsets.UTF_8).use {
                            it.write(stdinContent)
                        }
                    } catch (_: IOException) {
                        // Expected when the process exits before stdin is fully written (broken pipe)
                    }
                },
                "rescript-process-stdin",
            ).apply { isDaemon = true }
        stdinThread.start()

        // Capture stderr in a separate thread
        var stderr = ""
        val stderrThread =
            Thread(
                {
                    try {
                        stderr = process.errorStream.reader(Charsets.UTF_8).use { it.readText() }
                    } catch (_: IOException) {
                        // Expected when the process exits before stderr is fully read
                    }
                },
                "rescript-process-stderr",
            ).apply { isDaemon = true }
        stderrThread.start()

        try {
            val stdout = process.inputStream.reader(Charsets.UTF_8).use { it.readText() }

            stdinThread.join(timeoutMs)
            stderrThread.join(timeoutMs)

            // Interrupt threads that are still alive after timeout
            if (stdinThread.isAlive) stdinThread.interrupt()
            if (stderrThread.isAlive) stderrThread.interrupt()

            val completed =
                process.waitFor(
                    RescriptSecurityUtils.PROCESS_TIMEOUT_SECONDS,
                    TimeUnit.SECONDS,
                )
            if (!completed) {
                process.destroyForcibly()
                LOG.debug("Process timed out: ${commandLine.commandLineString}")
                return StdinProcessResult(stdout, stderr, exitCode = -1, timedOut = true)
            }

            return StdinProcessResult(stdout, stderr, exitCode = process.exitValue(), timedOut = false)
        } finally {
            if (process.isAlive) process.destroyForcibly()
            if (stdinThread.isAlive) stdinThread.interrupt()
            if (stderrThread.isAlive) stderrThread.interrupt()
        }
    }
}
