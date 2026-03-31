package com.rescript.plugin.repl

import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Executes ReScript code snippets by compiling to JavaScript and running with Node.js.
 *
 * Creates a temporary `.res` file inside the project's source directory,
 * invokes `npx rescript build` for compilation, then executes the generated
 * JavaScript with `node`. The temporary files are cleaned up after execution.
 */
object RescriptReplExecutor {
    /** Timeout in seconds for compile and execution steps. */
    private const val TIMEOUT_SECONDS = 30L

    /** Name of the temporary REPL file (without extension). */
    private const val REPL_FILE_NAME = "RescriptRepl__Eval"

    /**
     * Executes a ReScript code snippet and returns the output.
     *
     * @param code the ReScript code to execute
     * @param projectPath the project root path (where rescript.json lives)
     * @return the execution output (stdout + stderr), or an error message
     */
    fun execute(
        code: String,
        projectPath: String,
    ): String {
        val projectDir = File(projectPath).canonicalFile
        if (!projectDir.isDirectory) {
            return "Error: invalid project path"
        }

        val wrappedCode = wrapCode(code)
        return try {
            compileAndRun(wrappedCode, projectDir)
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    /**
     * Wraps user code so it prints its result via `Js.log`.
     *
     * If the code already contains output statements or declarations,
     * it is used as-is. Otherwise the last expression is wrapped in `Js.log(...)`.
     *
     * @param code the raw user code
     * @return the wrapped code ready for compilation
     */
    internal fun wrapCode(code: String): String {
        val trimmed = code.trim()
        if (trimmed.contains("Js.log") ||
            trimmed.contains("Console.log") ||
            trimmed.startsWith("open ") ||
            trimmed.startsWith("let ") ||
            trimmed.startsWith("type ") ||
            trimmed.startsWith("module ")
        ) {
            return trimmed
        }
        return "Js.log($trimmed)"
    }

    /**
     * Parses the combined output of a process execution.
     *
     * @param stdout the standard output text
     * @param stderr the standard error text
     * @return a combined output string, preferring stdout when available
     */
    internal fun parseOutput(
        stdout: String,
        stderr: String,
    ): String {
        val result = StringBuilder()
        if (stdout.isNotBlank()) result.append(stdout.trim())
        // Filter out Node.js warnings (e.g. MODULE_TYPELESS_PACKAGE_JSON)
        val filteredStderr =
            stderr
                .lines()
                .filter { !it.startsWith("(node:") && !it.startsWith("Reparsing as") && !it.startsWith("To eliminate") }
                .joinToString("\n")
                .trim()
        if (filteredStderr.isNotBlank()) {
            if (result.isNotEmpty()) result.append("\n")
            result.append(filteredStderr)
        }
        return if (result.isEmpty()) "(no output)" else result.toString()
    }

    private fun compileAndRun(
        code: String,
        projectDir: File,
    ): String {
        // Find the source directory from rescript.json (default: src/)
        val srcDir = File(projectDir, "src")
        if (!srcDir.isDirectory) {
            return "Error: src/ directory not found in project"
        }

        val resFile = File(srcDir, "$REPL_FILE_NAME.res")
        val jsFile = File(srcDir, "$REPL_FILE_NAME.res.js")
        try {
            resFile.writeText(code)

            // Compile with rescript build
            val compileResult =
                runProcess(
                    listOf("npx", "rescript", "build"),
                    projectDir,
                )

            // Check if the REPL file itself has errors (ignore errors in other files)
            val replErrors =
                compileResult.output
                    .lines()
                    .filter { it.contains(REPL_FILE_NAME) }
            if (replErrors.isNotEmpty()) {
                val relevantOutput =
                    compileResult.output
                        .lines()
                        .dropWhile { !it.contains(REPL_FILE_NAME) }
                        .joinToString("\n")
                        .trim()
                return "Compile error:\n$relevantOutput"
            }

            if (!jsFile.exists()) {
                // The REPL file compiled successfully but JS not found — might be
                // because other files have errors but ours was compiled before failure
                if (compileResult.exitCode != 0) {
                    return "Compile error in project (not in your code):\n" +
                        "The project has compilation errors. Fix them first, or use a simpler expression."
                }
                return "Compiled but JS output not found"
            }

            // Execute compiled JS
            val runResult =
                runProcess(
                    listOf("node", jsFile.absolutePath),
                    projectDir,
                )
            return parseOutput(runResult.stdout, runResult.stderr)
        } finally {
            resFile.delete()
            jsFile.delete()
            // Clean up any generated .res.js.map or similar
            File(srcDir, "$REPL_FILE_NAME.res.js.map").delete()
        }
    }

    /** Holds the result of an external process execution. */
    private data class ProcessResult(
        val exitCode: Int,
        val stdout: String,
        val stderr: String,
        val output: String,
    )

    private fun runProcess(
        command: List<String>,
        workDir: File,
    ): ProcessResult {
        val process =
            ProcessBuilder(command)
                .directory(workDir)
                .start()
        val completed =
            try {
                process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            } catch (e: InterruptedException) {
                process.destroyForcibly()
                Thread.currentThread().interrupt()
                return ProcessResult(-1, "", "", "Error: interrupted")
            }
        if (!completed) {
            process.destroyForcibly()
            return ProcessResult(-1, "", "", "Error: timed out")
        }
        val stdout = process.inputStream.use { it.bufferedReader().readText() }
        val stderr = process.errorStream.use { it.bufferedReader().readText() }
        return ProcessResult(process.exitValue(), stdout, stderr, stdout + stderr)
    }
}
