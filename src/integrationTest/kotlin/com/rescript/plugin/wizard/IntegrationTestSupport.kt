package com.rescript.plugin.wizard

import org.junit.jupiter.api.Assumptions.assumeTrue
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit

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
        val process =
            ProcessBuilder(command)
                .directory(workingDir.toFile())
                .also { it.environment().putAll(env) }
                .redirectErrorStream(false)
                .start()
        val finished = process.waitFor(timeout, timeoutUnit)
        if (!finished) {
            process.destroyForcibly()
            throw AssertionError("Command timed out after $timeout $timeoutUnit: $command")
        }
        return ExecResult(
            exitCode = process.exitValue(),
            stdout = process.inputStream.bufferedReader().readText(),
            stderr = process.errorStream.bufferedReader().readText(),
        )
    }

    /**
     * Skips the calling test (via JUnit `Assumptions`) if [binary] is not on PATH.
     */
    fun requireBinary(binary: String) {
        val onPath =
            try {
                exec(Path.of("."), listOf("which", binary), timeout = 5).succeeded
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
