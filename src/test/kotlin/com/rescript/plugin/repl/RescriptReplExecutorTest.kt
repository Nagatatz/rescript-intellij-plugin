package com.rescript.plugin.repl

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class RescriptReplExecutorTest {
    @TempDir
    lateinit var projectDir: Path

    @Test
    fun `wrapCode returns let binding as-is`() {
        val result = RescriptReplExecutor.wrapCode("let x = 42")
        assertEquals("let x = 42", result)
    }

    @Test
    fun `wrapCode wraps simple expression in Js log`() {
        val result = RescriptReplExecutor.wrapCode("1 + 2")
        assertEquals("Js.log(1 + 2)", result)
    }

    @Test
    fun `wrapCode does not double wrap Js log`() {
        val result = RescriptReplExecutor.wrapCode("Js.log(\"hello\")")
        assertEquals("Js.log(\"hello\")", result)
    }

    @Test
    fun `wrapCode returns module declaration as-is`() {
        val result = RescriptReplExecutor.wrapCode("module M = { let x = 1 }")
        assertEquals("module M = { let x = 1 }", result)
    }

    @Test
    fun `parseOutput extracts stdout`() {
        val result = RescriptReplExecutor.parseOutput("42\n", "")
        assertEquals("42", result)
    }

    @Test
    fun `parseOutput returns stderr on error`() {
        val result = RescriptReplExecutor.parseOutput("", "Error: something failed\n")
        assertTrue(result.contains("Error"))
    }

    @Test
    fun `parseOutput trims whitespace`() {
        val result = RescriptReplExecutor.parseOutput("  hello  \n", "")
        assertEquals("hello", result)
    }

    @Test
    fun `parseOutput includes both stdout and stderr`() {
        val result = RescriptReplExecutor.parseOutput("42\n", "warning: something\n")
        assertTrue(result.contains("42"))
        assertTrue(result.contains("warning: something"))
    }

    @Test
    fun `parseOutput returns no output for empty streams`() {
        val result = RescriptReplExecutor.parseOutput("", "")
        assertEquals("(no output)", result)
    }

    @Test
    fun `execute rejects invalid project path`() {
        val result = RescriptReplExecutor.execute("1 + 2", "/nonexistent/path")
        assertEquals("Error: invalid project path", result)
    }

    @Test
    fun `execute rejects file path instead of directory`() {
        val tmpFile = java.io.File.createTempFile("test", ".txt")
        try {
            val result = RescriptReplExecutor.execute("1 + 2", tmpFile.absolutePath)
            assertEquals("Error: invalid project path", result)
        } finally {
            tmpFile.delete()
        }
    }

    @Test
    fun `execute preserves existing source output and map after compiler failure`() {
        val src = Files.createDirectory(projectDir.resolve("src"))
        val sentinels =
            listOf(".res", ".res.js", ".res.js.map").associate { extension ->
                src.resolve("RescriptRepl__Eval$extension") to "original $extension"
            }
        sentinels.forEach { (path, content) -> Files.writeString(path, content) }
        val result =
            RescriptReplExecutor.execute("1", projectDir.toString()) { _, _ ->
                RescriptReplExecutor.ProcessResult(1, "", "compiler failed", "compiler failed")
            }
        assertTrue(result.startsWith("Compile error"))
        sentinels.forEach { (path, content) -> assertEquals(content, Files.readString(path)) }
        assertEquals(
            sentinels.keys,
            src
                .toFile()
                .listFiles()!!
                .map { it.toPath() }
                .toSet(),
        )
    }

    @Test
    fun `execute cleans its files after successful evaluation and preserves symlinks`() {
        val src = Files.createDirectory(projectDir.resolve("src"))
        val target = Files.writeString(projectDir.resolve("original.res"), "original")
        val link = Files.createSymbolicLink(src.resolve("RescriptRepl__Eval.res"), target)
        var generatedOutput: Path? = null
        val result =
            RescriptReplExecutor.execute("42", projectDir.toString()) { command, _ ->
                if (command.first() == "npx") {
                    val source =
                        src.toFile().listFiles()!!.single {
                            it.name.startsWith("RescriptRepl__Eval_") &&
                                it.extension == "res"
                        }
                    assertEquals("Js.log(42)", source.readText())
                    generatedOutput = source.toPath().resolveSibling(source.name + ".js")
                    Files.writeString(generatedOutput!!, "console.log(42)")
                    RescriptReplExecutor.ProcessResult(0, "", "", "")
                } else {
                    assertEquals(generatedOutput!!.toFile().canonicalPath, command[1])
                    RescriptReplExecutor.ProcessResult(0, "42", "", "42")
                }
            }
        assertEquals("42", result)
        assertTrue(Files.isSymbolicLink(link))
        assertEquals("original", Files.readString(target))
        assertEquals(listOf(link), src.toFile().listFiles()!!.map { it.toPath() })
    }

    @Test
    fun `successful compilation with source diagnostics still executes generated output`() {
        val src = Files.createDirectory(projectDir.resolve("src"))
        val result =
            RescriptReplExecutor.execute("42", projectDir.toString()) { command, _ ->
                if (command.first() == "npx") {
                    val source = src.toFile().listFiles()!!.single { it.extension == "res" }
                    Files.writeString(source.toPath().resolveSibling(source.name + ".js"), "console.log(42)")
                    val warning = "${source.name}: deprecated: Js.log"
                    RescriptReplExecutor.ProcessResult(0, "", warning, warning)
                } else {
                    RescriptReplExecutor.ProcessResult(0, "42", "", "42")
                }
            }
        assertEquals("42", result)
        assertTrue(src.toFile().listFiles()!!.isEmpty())
    }

    @Test
    fun `execute never runs an empty reserved output`() {
        Files.createDirectory(projectDir.resolve("src"))
        var commands = 0
        val result =
            RescriptReplExecutor.execute("1", projectDir.toString()) { _, _ ->
                commands++
                RescriptReplExecutor.ProcessResult(0, "", "", "")
            }
        assertEquals("Compiled but JS output not found", result)
        assertEquals(1, commands)
        assertTrue(
            projectDir
                .resolve("src")
                .toFile()
                .listFiles()!!
                .isEmpty(),
        )
    }

    @Test
    fun `execute cleans owned files when command runner throws`() {
        Files.createDirectory(projectDir.resolve("src"))
        val result =
            RescriptReplExecutor.execute("1", projectDir.toString()) { _, _ ->
                throw java.io.IOException(projectDir.toString())
            }
        assertEquals("Error: REPL execution failed (IOException)", result)
        assertTrue(
            projectDir
                .resolve("src")
                .toFile()
                .listFiles()!!
                .isEmpty(),
        )
    }

    @Test
    fun `overlapping executions own distinct files and do not remove another run`() {
        val src = Files.createDirectory(projectDir.resolve("src"))
        val executor = Executors.newSingleThreadExecutor()
        val entered = java.util.concurrent.CountDownLatch(1)
        val release = java.util.concurrent.CountDownLatch(1)
        try {
            val first =
                executor.submit<String> {
                    RescriptReplExecutor.execute("1", projectDir.toString()) { _, _ ->
                        entered.countDown()
                        assertTrue(release.await(10, TimeUnit.SECONDS))
                        RescriptReplExecutor.ProcessResult(1, "", "failed", "failed")
                    }
                }
            assertTrue(entered.await(10, TimeUnit.SECONDS))
            val firstFiles =
                src
                    .toFile()
                    .listFiles()!!
                    .map { it.toPath() }
                    .toSet()
            assertEquals(3, firstFiles.size)
            RescriptReplExecutor.execute("2", projectDir.toString()) { _, _ ->
                assertEquals(6, src.toFile().listFiles()!!.size)
                RescriptReplExecutor.ProcessResult(1, "", "failed", "failed")
            }
            assertEquals(
                firstFiles,
                src
                    .toFile()
                    .listFiles()!!
                    .map { it.toPath() }
                    .toSet(),
            )
            release.countDown()
            assertTrue(first.get(10, TimeUnit.SECONDS).startsWith("Compile error"))
            assertTrue(src.toFile().listFiles()!!.isEmpty())
        } finally {
            release.countDown()
            executor.shutdownNow()
        }
    }

    @Test
    fun `output collision preserves existing symlink and cleans only new reservations`() {
        val src = Files.createDirectory(projectDir.resolve("src"))
        val target = Files.writeString(projectDir.resolve("original.map"), "original map")
        val link = Files.createSymbolicLink(src.resolve("RescriptRepl__Eval_collision.res.js.map"), target)
        val result =
            RescriptReplExecutor.execute(
                "1",
                projectDir.toString(),
                createSource = { Files.createFile(it.resolve("RescriptRepl__Eval_collision.res")) },
            ) { _, _ ->
                throw AssertionError("No command may run after a reservation collision")
            }
        assertEquals("Error: REPL execution failed (FileAlreadyExistsException)", result)
        assertTrue(Files.isSymbolicLink(link))
        assertEquals("original map", Files.readString(target))
        assertEquals(listOf(link), src.toFile().listFiles()!!.map { it.toPath() })
    }

    @Test
    fun `runProcess drains large stdout and stderr before waiting for exit`() {
        val result =
            RescriptReplExecutor.runProcess(
                com.rescript.plugin.util.ProcessFixture
                    .command("flood")
                    .toList(),
                java.io.File("."),
            )
        assertTrue(result.successful, result.failureMessage)
        assertEquals(1024 * 1024, result.stdout.length)
        assertEquals(1024 * 1024, result.stderr.length)
    }

    @Test
    fun `runProcess retains nonzero exit for REPL error reporting`() {
        val result =
            RescriptReplExecutor.runProcess(
                com.rescript.plugin.util.ProcessFixture
                    .command("exit")
                    .toList(),
                java.io.File("."),
            )
        assertEquals(42, result.exitCode)
        assertEquals("bad input", result.stderr)
        assertEquals("Process failed (exit code 42)", result.failureMessage)
    }

    @Test
    fun `process timeout is reported and owned files are cleaned`() {
        val src = Files.createDirectory(projectDir.resolve("src"))
        val result =
            RescriptReplExecutor.execute("1", projectDir.toString()) { _, _ ->
                RescriptReplExecutor.ProcessResult(
                    -1,
                    "",
                    "",
                    "",
                    com.rescript.plugin.util.RescriptProcessRunner.Failure.TIMEOUT,
                )
            }
        assertEquals("Error: Process timed out", result)
        Files.list(src).use { assertEquals(0, it.count()) }
    }

    @Test
    fun `IDE cancellation propagates after owned file cleanup`() {
        val src = Files.createDirectory(projectDir.resolve("src"))
        assertThrows(com.intellij.openapi.progress.ProcessCanceledException::class.java) {
            RescriptReplExecutor.execute("1", projectDir.toString()) { _, _ ->
                throw com.intellij.openapi.progress
                    .ProcessCanceledException()
            }
        }
        Files.list(src).use { assertEquals(0, it.count()) }
    }
}
