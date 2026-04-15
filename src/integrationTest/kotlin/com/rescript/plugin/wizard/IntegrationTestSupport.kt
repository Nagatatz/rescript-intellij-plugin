package com.rescript.plugin.wizard

import org.junit.jupiter.api.Assumptions.assumeTrue
import java.lang.ProcessBuilder.Redirect
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.io.path.deleteIfExists

/**
 * Helpers for the template generation integration tests.
 *
 * Wraps `ProcessBuilder` invocation with a sane timeout, captures stdout/stderr for
 * diagnostics, and exposes a `requireBinary` helper so individual tests can short-circuit
 * with an `Assumption` failure when the test environment is missing pnpm or node.
 */
internal object IntegrationTestSupport {
    /** Result of an external process invocation. */
    data class ExecResult(
        val exitCode: Int,
        val stdout: String,
        val stderr: String,
    ) {
        val succeeded: Boolean get() = exitCode == 0
    }

    /**
     * Runs [command] in [workingDir] with a [timeout]. Returns stdout/stderr/exit code.
     */
    fun exec(
        workingDir: Path,
        command: List<String>,
        timeout: Long = 600,
        timeoutUnit: TimeUnit = TimeUnit.SECONDS,
        env: Map<String, String> = emptyMap(),
    ): ExecResult {
        // Redirect to temp files so the child process never blocks on a full pipe buffer
        // (OS pipes are typically 64KB — `pnpm install` / `rescript build` easily exceed this).
        val stdoutFile = Files.createTempFile("proofread-stdout", ".log")
        val stderrFile = Files.createTempFile("proofread-stderr", ".log")
        try {
            val process =
                ProcessBuilder(command)
                    .directory(workingDir.toFile())
                    .also { it.environment().putAll(env) }
                    .redirectOutput(Redirect.to(stdoutFile.toFile()))
                    .redirectError(Redirect.to(stderrFile.toFile()))
                    .start()
            val finished =
                try {
                    process.waitFor(timeout, timeoutUnit)
                } catch (ie: InterruptedException) {
                    process.destroyForcibly()
                    Thread.currentThread().interrupt()
                    throw ie
                }
            if (!finished) {
                process.destroyForcibly()
                throw AssertionError("Command timed out after $timeout $timeoutUnit: $command")
            }
            return ExecResult(
                exitCode = process.exitValue(),
                stdout = Files.readString(stdoutFile),
                stderr = Files.readString(stderrFile),
            )
        } finally {
            stdoutFile.deleteIfExists()
            stderrFile.deleteIfExists()
        }
    }

    /**
     * Skips the calling test (via JUnit `Assumptions`) if [binary] is not on PATH.
     */
    fun requireBinary(binary: String) {
        val onPath =
            try {
                exec(Path.of("."), listOf("which", binary), timeout = 5).succeeded
            } catch (ie: InterruptedException) {
                Thread.currentThread().interrupt()
                throw ie
            } catch (t: Throwable) {
                false
            }
        assumeTrue(onPath, "Skipping: required binary `$binary` not found on PATH")
    }

    /**
     * Materializes the supplied [files] map into [root], creating parent directories as needed.
     */
    fun writeFiles(
        root: Path,
        files: Map<String, String>,
    ) {
        for ((relative, content) in files) {
            val target = root.resolve(relative)
            Files.createDirectories(target.parent)
            Files.writeString(target, content)
        }
    }
}
