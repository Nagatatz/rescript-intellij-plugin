package com.rescript.plugin.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicReference

/** Exercises real JVM subprocesses without depending on shell tools or platform-specific scripts. */
class RescriptProcessRunnerTest {
    private fun start(
        mode: String,
        vararg args: String,
    ): Process = ProcessBuilder(command(mode, *args)).start()

    private fun command(
        mode: String,
        vararg args: String,
    ): List<String> = ProcessFixture.command(mode, *args).toList()

    @Test
    fun `silent process obeys deadline including stdout wait`() {
        val before = System.nanoTime()
        val result = RescriptProcessRunner.run(start = { start("sleep") }, timeoutMs = 200)
        assertEquals(RescriptProcessRunner.Failure.TIMEOUT, result.failure)
        assertTrue((System.nanoTime() - before) / 1_000_000 < 1500)
    }

    @Test
    fun `unread stdin does not extend deadline`() {
        val before = System.nanoTime()
        val result =
            RescriptProcessRunner.run(
                start = { start("sleep") },
                stdin = "x".repeat(2_000_000),
                timeoutMs = 200,
            )
        assertEquals(RescriptProcessRunner.Failure.TIMEOUT, result.failure)
        assertTrue((System.nanoTime() - before) / 1_000_000 < 1500)
    }

    @Test
    fun `stdin stdout and stderr are processed concurrently`() {
        val input = "hello\n".repeat(50_000)
        val result = RescriptProcessRunner.run(start = { start("echo") }, stdin = input)
        assertTrue(result.successful, result.failureMessage)
        assertEquals(input, result.stdout)
        assertEquals("diagnostic", result.stderr)
    }

    @Test
    fun `large stdout and stderr do not deadlock`() {
        val result = RescriptProcessRunner.run(start = { start("flood") })
        assertTrue(result.successful, result.failureMessage)
        assertEquals(1024 * 1024, result.stdout.length)
        assertEquals(1024 * 1024, result.stderr.length)
    }

    @Test
    fun `capacity overflow fails instead of returning truncated success`() {
        val result = RescriptProcessRunner.run(start = { start("flood") }, outputLimit = 1024)
        assertEquals(RescriptProcessRunner.Failure.OUTPUT_LIMIT, result.failure)
        assertFalse(result.successful)
        assertTrue(result.stdout.length <= 1024 && result.stderr.length <= 1024)
    }

    @Test
    fun `cancellation before start prevents process creation`() {
        val cancellation = RescriptProcessRunner.Cancellation().apply { cancel() }
        val result = RescriptProcessRunner.run(start = { error("Must not start") }, cancellation = cancellation)
        assertEquals(RescriptProcessRunner.Failure.CANCELLED, result.failure)
    }

    @Test
    fun `cancellation stops owner and child`(
        @TempDir dir: Path,
    ) {
        val pidFile = dir.resolve("pid")
        val owner = AtomicReference<Process>()
        val cancellation = RescriptProcessRunner.Cancellation()
        val result =
            RescriptProcessRunner.run(
                start = { start("child", pidFile.toString()).also { owner.set(it) } },
                cancellation = cancellation,
                checkCancelled = { if (Files.exists(pidFile) && Files.size(pidFile) > 0) cancellation.cancel() },
            )
        assertEquals(RescriptProcessRunner.Failure.CANCELLED, result.failure)
        assertStopped(owner.get().toHandle())
        assertStopped(ProcessHandle.of(Files.readString(pidFile).toLong()).orElse(null))
    }

    @Test
    fun `timeout stops owner and child`(
        @TempDir dir: Path,
    ) {
        val pidFile = dir.resolve("pid")
        val owner = AtomicReference<Process>()
        val result =
            RescriptProcessRunner.run(
                start = { start("child", pidFile.toString()).also { owner.set(it) } },
                timeoutMs = 1000,
            )
        assertEquals(RescriptProcessRunner.Failure.TIMEOUT, result.failure)
        assertStopped(owner.get().toHandle())
        assertStopped(ProcessHandle.of(Files.readString(pidFile).toLong()).orElse(null))
    }

    @Test
    fun `callback exception cleans up before propagating`() {
        val owner = AtomicReference<Process>()
        assertThrows(IllegalStateException::class.java) {
            RescriptProcessRunner.run(
                start = { start("sleep").also { owner.set(it) } },
                checkCancelled = { if (owner.get() != null) error("cancel") },
            )
        }
        assertStopped(owner.get().toHandle())
    }

    @Test
    fun `interruption is preserved and stops process`() {
        val owner = AtomicReference<Process>()
        try {
            val result =
                RescriptProcessRunner.run(
                    start = { start("sleep").also { owner.set(it) } },
                    checkCancelled = { if (owner.get() != null) Thread.currentThread().interrupt() },
                )
            assertEquals(RescriptProcessRunner.Failure.CANCELLED, result.failure)
            assertTrue(Thread.currentThread().isInterrupted)
        } finally {
            Thread.interrupted()
        }
        assertStopped(owner.get().toHandle())
    }

    @Test
    fun `start failure and nonzero exit retain distinct reasons`() {
        val missing = RescriptProcessRunner.run(start = { ProcessBuilder("__missing_process_98__").start() })
        assertEquals(RescriptProcessRunner.Failure.START_FAILED, missing.failure)
        val exited = RescriptProcessRunner.run(start = { start("exit") })
        assertNull(exited.failure)
        assertEquals(42, exited.exitCode)
        assertEquals("bad input", exited.stderr)
        assertFalse(exited.successful)
    }

    @Test
    fun `output read exception stops the owner`() {
        val owner = AtomicReference<Process>()
        val result =
            RescriptProcessRunner.run(
                start = {
                    start("sleep").also {
                        owner.set(it)
                        it.inputStream.close()
                    }
                },
            )
        assertEquals(RescriptProcessRunner.Failure.IO_ERROR, result.failure)
        assertStopped(owner.get().toHandle())
    }

    @Test
    fun `interrupted thread does not launch a process`() {
        try {
            Thread.currentThread().interrupt()
            val result = RescriptProcessRunner.run(start = { error("Must not launch") })
            assertEquals(RescriptProcessRunner.Failure.CANCELLED, result.failure)
            assertTrue(Thread.currentThread().isInterrupted)
        } finally {
            Thread.interrupted()
        }
    }

    @Test
    fun `observed child is stopped after parent exits with inherited pipes`(
        @TempDir dir: Path,
    ) {
        val pidFile = dir.resolve("pid")
        val result =
            RescriptProcessRunner.run(
                start = { start("child-exit", pidFile.toString()) },
                timeoutMs = 500,
            )
        assertTrue(result.successful || result.failure == RescriptProcessRunner.Failure.TIMEOUT)
        assertStopped(ProcessHandle.of(Files.readString(pidFile).toLong()).orElse(null))
    }

    @Test
    fun `immediate parent exit with inherited pipes never blocks the caller`(
        @TempDir dir: Path,
    ) {
        val pidFile = dir.resolve("pid")
        val before = System.nanoTime()
        try {
            RescriptProcessRunner.run(start = { start("child-immediate-exit", pidFile.toString()) }, timeoutMs = 500)
            assertTrue((System.nanoTime() - before) / 1_000_000 < 1500)
        } finally {
            // An immediately reparented child can escape portable ProcessHandle discovery.
            // The fixture records its PID so the test can clean up that explicitly documented case.
            if (Files.exists(pidFile)) {
                ProcessHandle.of(Files.readString(pidFile).toLong()).ifPresent { it.destroyForcibly() }
            }
        }
    }

    private fun assertStopped(handle: ProcessHandle?) {
        val deadline = System.nanoTime() + 2_000_000_000
        while (handle?.isAlive == true && System.nanoTime() < deadline) Thread.sleep(10)
        assertFalse(handle?.isAlive == true, "Owned process should be stopped")
    }
}
