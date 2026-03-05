package com.rescript.plugin.util

import com.intellij.openapi.diagnostic.logger
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
}
