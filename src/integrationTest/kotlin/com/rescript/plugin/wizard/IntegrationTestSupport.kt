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
            // Resolve the executable before handing it to ProcessBuilder: on Windows the
            // tools these tests drive (pnpm, bun) exist only as `.CMD` shims, which
            // ProcessBuilder will not find from a bare name.
            val resolved = listOf(resolveExecutable(command.first())) + command.drop(1)
            val process =
                ProcessBuilder(resolved)
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
     * Locates [command] the way the OS would, and returns a path `ProcessBuilder` can launch.
     *
     * Shelling out to `which` does not work here: on Windows it is Git's POSIX `which`, which
     * does not apply `PATHEXT`, so `which pnpm.CMD` fails while `which pnpm` finds only the
     * extension-less shell wrapper that `ProcessBuilder` in turn cannot execute. Walking `PATH`
     * ourselves is both cross-platform and one process cheaper.
     *
     * @param command an executable name, or a path that is used as-is
     * @return the resolved absolute path, or [command] unchanged when nothing matches
     */
    fun resolveExecutable(command: String): String {
        if (command.contains('/') || command.contains('\\')) return command

        val isWindows = System.getProperty("os.name").startsWith("Windows", ignoreCase = true)
        val pathExt =
            (System.getenv("PATHEXT") ?: ".COM;.EXE;.BAT;.CMD").split(";").filter { it.isNotBlank() }
        // PATHEXT suffixes come first on Windows. Node tool installs ship both `pnpm` (a POSIX
        // shell wrapper) and `pnpm.CMD` in the same directory, and Files.isExecutable reports
        // true for the extension-less one even though CreateProcess rejects it with error=193.
        // The bare name stays as a last resort for commands that already carry an extension.
        val suffixes =
            when {
                !isWindows -> listOf("")
                pathExt.any { command.endsWith(it, ignoreCase = true) } -> listOf("") + pathExt
                else -> pathExt + listOf("")
            }

        for (dir in (System.getenv("PATH") ?: "").split(java.io.File.pathSeparator)) {
            if (dir.isBlank()) continue
            for (suffix in suffixes) {
                val candidate =
                    try {
                        Path.of(dir, command + suffix)
                    } catch (_: java.nio.file.InvalidPathException) {
                        continue // PATH can hold entries that are not valid paths on this OS
                    }
                if (Files.isRegularFile(candidate) && Files.isExecutable(candidate)) {
                    return candidate.toString()
                }
            }
        }
        return command
    }

    /**
     * Skips the calling test (via JUnit `Assumptions`) if [binary] cannot be located.
     */
    fun requireBinary(binary: String) {
        val resolved = resolveExecutable(binary)
        val found = resolved != binary || Files.isExecutable(Path.of(binary))
        assumeTrue(found, "Skipping: required binary `$binary` not found on PATH")
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
