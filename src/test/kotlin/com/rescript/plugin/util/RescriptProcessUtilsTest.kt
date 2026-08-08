package com.rescript.plugin.util

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.util.SystemInfo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledOnOs
import org.junit.jupiter.api.condition.OS
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

/**
 * Tests for [RescriptProcessUtils] — shared process execution utility.
 *
 * Verifies command execution, exit code capture, output reading, and timeout handling.
 */
class RescriptProcessUtilsTest {
    /**
     * Writes a small script that behaves the same way on both platforms.
     *
     * These tests used to invoke `bash` directly. On the Windows CI runner
     * `bash` resolves to the WSL launcher (`C:\Windows\System32\bash.exe`),
     * which exits 1 regardless of its arguments because no distribution is
     * installed, so every assertion about exit codes and timeouts failed.
     *
     * @param dir the directory to create the script in
     * @param name the base file name, without extension
     * @param win the cmd.exe body, used on Windows
     * @param posix the sh body, used everywhere else
     * @return the path to the generated script
     */
    private fun script(
        dir: Path,
        name: String,
        win: String,
        posix: String,
    ): Path =
        if (SystemInfo.isWindows) {
            dir.resolve("$name.bat").also { Files.writeString(it, "@echo off\r\n$win\r\n") }
        } else {
            dir.resolve("$name.sh").also {
                Files.writeString(it, "#!/bin/sh\n$posix\n")
                it.toFile().setExecutable(true)
            }
        }

    @Test
    fun testRunSimpleCommandReturnsOutput() {
        val result = RescriptProcessUtils.runSimpleCommand("echo", "hello")
        assertEquals(0, result.exitCode)
        assertEquals("hello", result.firstLine)
        assertFalse(result.timedOut)
    }

    @Test
    fun testRunSimpleCommandCapturesExitCode() {
        val result = RescriptProcessUtils.runSimpleCommand("false")
        assertTrue(result.exitCode != 0, "Exit code should be non-zero")
        assertFalse(result.timedOut)
    }

    @Test
    fun testRunSimpleCommandHandlesNonExistentCommand() {
        val result = RescriptProcessUtils.runSimpleCommand("__nonexistent_command_12345__")
        // Should not throw, returns error result
        assertTrue(result.exitCode == -1, "Exit code should be -1 for failed process")
        assertFalse(result.timedOut)
    }

    @Test
    fun testRunSimpleCommandTimesOut(
        @TempDir tempDir: Path,
    ) {
        // Produce output first (so readLine() returns), then block indefinitely.
        // This exercises the timeout branch in waitFor().
        // `ping` is the blocking primitive on Windows: `timeout /t` aborts when
        // stdin is redirected, which it is here.
        val blocking =
            script(
                tempDir,
                "block",
                win = "echo started\r\nping -n 61 127.0.0.1 >nul",
                posix = "echo started; sleep 60",
            )
        val result = RescriptProcessUtils.runSimpleCommand(blocking.toString(), timeoutSeconds = 1)
        assertEquals(-1, result.exitCode)
        assertTrue(result.timedOut, "Should report timed out")
        assertEquals("started", result.firstLine)
    }

    @Test
    fun testRunSimpleCommandHandlesEmptyOutput() {
        // `true` produces no output, so readLine() returns null
        val result = RescriptProcessUtils.runSimpleCommand("true")
        assertEquals(0, result.exitCode)
        assertEquals("", result.firstLine, "Empty output should return empty string")
        assertFalse(result.timedOut)
    }

    @Test
    fun testProcessResultDataClass() {
        val result = RescriptProcessUtils.ProcessResult(exitCode = 42, firstLine = "test", timedOut = true)
        assertEquals(42, result.exitCode)
        assertEquals("test", result.firstLine)
        assertTrue(result.timedOut)
    }

    // ── executeWithStdin ────────────────────────────────────────

    @Test
    fun `executeWithStdin passes input to process and captures output`() {
        val cmd = GeneralCommandLine("cat").withCharset(Charsets.UTF_8)
        val result = RescriptProcessUtils.executeWithStdin(cmd, "hello world")
        assertEquals(0, result.exitCode)
        assertEquals("hello world", result.stdout)
        assertFalse(result.timedOut)
    }

    @Test
    fun `executeWithStdin captures stderr`(
        @TempDir tempDir: Path,
    ) {
        val cmd =
            GeneralCommandLine(
                script(tempDir, "err", win = "echo err 1>&2", posix = "echo err >&2").toString(),
            ).withCharset(Charsets.UTF_8)
        val result = RescriptProcessUtils.executeWithStdin(cmd, "")
        assertEquals(0, result.exitCode)
        assertTrue(result.stderr.contains("err"))
        assertFalse(result.timedOut)
    }

    @Test
    fun `executeWithStdin reports non-zero exit code`(
        @TempDir tempDir: Path,
    ) {
        val cmd =
            GeneralCommandLine(
                script(tempDir, "code", win = "exit /b 42", posix = "exit 42").toString(),
            ).withCharset(Charsets.UTF_8)
        val result = RescriptProcessUtils.executeWithStdin(cmd, "")
        assertEquals(42, result.exitCode)
        assertFalse(result.timedOut)
    }

    @Test
    @DisabledOnOs(
        value = [OS.WINDOWS],
        disabledReason =
            "executeWithStdin drains stdout to EOF before waitFor, so reaching the timeout branch " +
                "needs a process that stays alive with stdout closed. cmd.exe has no equivalent of `exec 1>&-`.",
    )
    fun `executeWithStdin handles timeout`() {
        // Close stdout immediately but keep the process alive so waitFor times out
        val cmd = GeneralCommandLine("bash", "-c", "exec 1>&-; sleep 60").withCharset(Charsets.UTF_8)
        val result = RescriptProcessUtils.executeWithStdin(cmd, "", timeoutMs = 500L)
        assertTrue(result.timedOut)
        assertEquals(-1, result.exitCode)
    }

    @Test
    fun `StdinProcessResult data class stores all fields`() {
        val result = RescriptProcessUtils.StdinProcessResult("out", "err", 1, true)
        assertEquals("out", result.stdout)
        assertEquals("err", result.stderr)
        assertEquals(1, result.exitCode)
        assertTrue(result.timedOut)
    }
}
