package com.rescript.plugin.util

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/** Runs finite commands with bounded output, concurrent I/O and a single monotonic deadline. */
object RescriptProcessRunner {
    const val DEFAULT_OUTPUT_LIMIT = 4 * 1024 * 1024

    /** Describes why a command did not produce a complete successful result. */
    enum class Failure(
        val description: String,
    ) {
        TIMEOUT("Process timed out"),
        CANCELLED("Process cancelled"),
        OUTPUT_LIMIT("Process output exceeded the size limit"),
        START_FAILED("Process could not be started"),
        IO_ERROR("Process I/O failed"),
    }

    /** Captures bounded UTF-8 output; failed output must never be used as formatted text or JSON. */
    data class Result(
        val stdout: String,
        val stderr: String,
        val exitCode: Int,
        val failure: Failure? = null,
    ) {
        val successful: Boolean get() = failure == null && exitCode == 0
        val failureMessage: String get() = failure?.description ?: "Process failed (exit code $exitCode)"
    }

    /** A sticky cancellation flag, including cancellation before process creation. */
    class Cancellation {
        private val cancelled = AtomicBoolean()

        fun cancel() {
            cancelled.set(true)
        }

        val isCancelled: Boolean get() = cancelled.get()
    }

    /**
     * Starts and owns a finite process. Polling observes cancellation and descendants while all I/O runs concurrently.
     *
     * @param start creates the process with an explicit argument list
     * @param stdin UTF-8 input, closed even when empty
     * @param timeoutMs total execution and I/O budget measured before process creation
     * @param outputLimit maximum captured bytes per output stream
     * @param cancellation sticky cancellation flag
     * @param checkCancelled optional IDE progress cancellation check (exceptions propagate after cleanup)
     * @return bounded output and a structured failure reason
     */
    fun run(
        start: () -> Process,
        stdin: String = "",
        timeoutMs: Long = 10_000,
        outputLimit: Int = DEFAULT_OUTPUT_LIMIT,
        cancellation: Cancellation = Cancellation(),
        checkCancelled: () -> Unit = {},
    ): Result {
        require(timeoutMs > 0 && outputLimit > 0)
        val started = System.nanoTime()
        val budget =
            java.util.concurrent.TimeUnit.MILLISECONDS
                .toNanos(timeoutMs)
        if (cancellation.isCancelled ||
            Thread.currentThread().isInterrupted
        ) {
            return Result("", "", -1, Failure.CANCELLED)
        }
        checkCancelled()
        if (cancellation.isCancelled ||
            Thread.currentThread().isInterrupted
        ) {
            return Result("", "", -1, Failure.CANCELLED)
        }
        if (System.nanoTime() - started >= budget) return Result("", "", -1, Failure.TIMEOUT)
        val process =
            try {
                start()
            } catch (e: com.intellij.openapi.progress.ProcessCanceledException) {
                throw e
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
                return Result("", "", -1, Failure.CANCELLED)
            } catch (_: Exception) {
                return Result("", "", -1, Failure.START_FAILED)
            }
        val descendants = linkedSetOf<ProcessHandle>()
        val executor =
            Executors.newFixedThreadPool(3) { task ->
                Thread(task, "rescript-process-io").apply { isDaemon = true }
            }
        val failure = AtomicReference<Failure?>()
        val stdout = Capture(outputLimit, failure)
        val stderr = Capture(outputLimit, failure)
        val jobs =
            listOf(
                executor.submit { stdout.read(process.inputStream) },
                executor.submit { stderr.read(process.errorStream) },
                executor.submit {
                    try {
                        process.outputStream.bufferedWriter(Charsets.UTF_8).use { it.write(stdin) }
                    } catch (_: java.io.IOException) {
                        // An early exit may close stdin before consuming all input.
                        if (process.isAlive) failure.compareAndSet(null, Failure.IO_ERROR)
                    }
                },
            )
        try {
            while (true) {
                process.descendants().use { handles -> handles.forEach { descendants.add(it) } }
                checkCancelled()
                jobs.filter { it.isDone }.forEach { job ->
                    try {
                        job.get()
                    } catch (_: java.util.concurrent.ExecutionException) {
                        failure.compareAndSet(null, Failure.IO_ERROR)
                    }
                }
                if (cancellation.isCancelled || Thread.currentThread().isInterrupted) {
                    failure.set(Failure.CANCELLED)
                }
                if (System.nanoTime() - started >= budget) failure.compareAndSet(null, Failure.TIMEOUT)
                if (failure.get() != null || (!process.isAlive && jobs.all { it.isDone })) break
                Thread.sleep(10)
            }
            val reason = failure.get()
            val exitCode = if (reason == null) process.exitValue() else -1
            return Result(stdout.text(), stderr.text(), exitCode, reason)
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
            return Result(stdout.text(), stderr.text(), -1, Failure.CANCELLED)
        } finally {
            // Retain observed handles: a child may be reparented after its parent exits.
            process.descendants().use { handles -> handles.forEach { descendants.add(it) } }
            descendants.toList().asReversed().forEach { if (it.isAlive) it.destroyForcibly() }
            // Give the live parent a short chance to reap children before stopping it.
            val interrupted = Thread.interrupted()
            try {
                val cleanupEnd = System.nanoTime() + 100_000_000
                while (descendants.any { it.isAlive } && System.nanoTime() < cleanupEnd) Thread.sleep(5)
                if (process.isAlive) process.destroyForcibly()
                process.waitFor(100, java.util.concurrent.TimeUnit.MILLISECONDS)
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
            } finally {
                if (interrupted) Thread.currentThread().interrupt()
                if (process.isAlive) process.destroyForcibly()
            }
            jobs.forEach { it.cancel(true) }
            executor.shutdownNow()
            // Closing a Java pipe can acquire the blocked reader/writer's monitor. Never do it on the caller.
            Thread({
                runCatching { process.outputStream.close() }
                runCatching { process.inputStream.close() }
                runCatching { process.errorStream.close() }
            }, "rescript-process-close").apply { isDaemon = true }.start()
        }
    }

    /** Accumulates at most the configured number of bytes, preserving partial output for diagnostics. */
    private class Capture(
        private val limit: Int,
        private val failure: AtomicReference<Failure?>,
    ) {
        private val bytes = ByteArrayOutputStream()

        fun read(stream: InputStream) {
            try {
                val buffer = ByteArray(8192)
                while (true) {
                    val count = stream.read(buffer)
                    if (count < 0) return
                    synchronized(bytes) {
                        val remaining = limit - bytes.size()
                        bytes.write(buffer, 0, minOf(count, remaining))
                        if (count > remaining) {
                            failure.compareAndSet(null, Failure.OUTPUT_LIMIT)
                            return
                        }
                    }
                }
            } catch (_: java.io.IOException) {
                failure.compareAndSet(null, Failure.IO_ERROR)
            }
        }

        fun text(): String = synchronized(bytes) { bytes.toString(Charsets.UTF_8) }
    }
}
