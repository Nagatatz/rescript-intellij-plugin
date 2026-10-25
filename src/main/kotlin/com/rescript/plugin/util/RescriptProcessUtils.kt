package com.rescript.plugin.util

import com.intellij.execution.configurations.GeneralCommandLine
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
     * The process's stdout and stderr are merged and drained with bounded capacity. The first line
     * is returned — this is suitable for commands like `which`, `where`, and
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
        RescriptProcessRunner
            .run(
                start = { ProcessBuilder(*command).redirectErrorStream(true).start() },
                timeoutMs = TimeUnit.SECONDS.toMillis(timeoutSeconds),
            ).let {
                ProcessResult(
                    it.exitCode,
                    it.stdout
                        .lineSequence()
                        .firstOrNull()
                        ?.trim()
                        .orEmpty(),
                    it.failure == RescriptProcessRunner.Failure.TIMEOUT,
                )
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
        val failure: RescriptProcessRunner.Failure? = null,
    )

    /**
     * Runs a command feeding [stdinContent] to its stdin and capturing full stdout/stderr.
     *
     * Delegates to the shared runner for concurrent I/O, a single deadline, cancellation
     * and bounded output. The process tree is stopped on failure.
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
        val result =
            RescriptProcessRunner.run(
                start = { commandLine.createProcess() },
                stdin = stdinContent,
                timeoutMs = timeoutMs,
                checkCancelled = {
                    com.intellij.openapi.progress.ProgressManager
                        .checkCanceled()
                },
            )
        return StdinProcessResult(
            result.stdout,
            result.stderr,
            result.exitCode,
            result.failure == RescriptProcessRunner.Failure.TIMEOUT,
            result.failure,
        )
    }
}
