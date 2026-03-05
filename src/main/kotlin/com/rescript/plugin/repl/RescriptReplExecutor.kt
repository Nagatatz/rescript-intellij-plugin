package com.rescript.plugin.repl

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.util.io.FileUtil
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Executes ReScript code snippets by compiling to JavaScript and running with Node.js.
 *
 * Uses a temporary `.res` file, invokes `npx rescript` for compilation, then
 * executes the generated JavaScript with `node`. Provides a simplified REPL
 * experience without requiring a persistent REPL subprocess.
 */
object RescriptReplExecutor {
    private val LOG = logger<RescriptReplExecutor>()

    /** Timeout in seconds for compile and execution steps. */
    private const val TIMEOUT_SECONDS = 30L

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
        // Validate projectPath before any file operations
        val projectDir = File(projectPath).canonicalFile
        if (!projectDir.isDirectory) {
            return "Error: invalid project path"
        }

        val wrappedCode = wrapCode(code)
        return try {
            runWithNode(wrappedCode, projectDir)
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    /**
     * Wraps user code so it prints its result via `Js.log`.
     *
     * If the code already contains `Js.log`, `Console.log`, or an `open` statement,
     * it is used as-is. Otherwise the last expression is wrapped in `Js.log(...)`.
     *
     * @param code the raw user code
     * @return the wrapped code ready for compilation
     */
    internal fun wrapCode(code: String): String {
        val trimmed = code.trim()
        // If user already has output statements or multi-line code with declarations, use as-is
        if (trimmed.contains("Js.log") ||
            trimmed.contains("Console.log") ||
            trimmed.startsWith("open ") ||
            trimmed.startsWith("let ") ||
            trimmed.startsWith("type ") ||
            trimmed.startsWith("module ")
        ) {
            return trimmed
        }
        // Single expression — wrap in Js.log
        return "Js.log($trimmed)"
    }

    /**
     * Builds the command line arguments for running a Node.js eval of compiled JS.
     *
     * @param jsCode the JavaScript code to evaluate
     * @return the command arguments list
     */
    internal fun buildNodeCommand(jsCode: String): List<String> = listOf("node", "-e", jsCode)

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
        if (stderr.isNotBlank()) {
            if (result.isNotEmpty()) result.append("\n")
            result.append(stderr.trim())
        }
        return if (result.isEmpty()) "(no output)" else result.toString()
    }

    private fun runWithNode(
        code: String,
        projectDir: File,
    ): String {
        // Use system temp directory instead of project lib/ to avoid artifact leakage
        val tmpDir = FileUtil.createTempDirectory("rescript-repl", null, true)
        val tmpFile = File(tmpDir, "repl_eval.res")
        var jsFile: File? = null
        try {
            tmpFile.writeText(code)

            // Compile with rescript
            val compileProcess =
                ProcessBuilder("npx", "rescript", "build", "-e", tmpFile.nameWithoutExtension)
                    .directory(projectDir)
                    .redirectErrorStream(true)
                    .start()
            val compileCompleted =
                try {
                    compileProcess.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                } catch (e: InterruptedException) {
                    compileProcess.destroyForcibly()
                    Thread.currentThread().interrupt()
                    return "Error: interrupted"
                }
            if (!compileCompleted) {
                compileProcess.destroyForcibly()
                return "Error: compilation timed out"
            }
            val compileOutput = compileProcess.inputStream.use { it.bufferedReader().readText() }

            if (compileProcess.exitValue() != 0) {
                return "Compile error:\n$compileOutput"
            }

            // Try to find compiled JS output
            jsFile = File(tmpFile.parentFile, tmpFile.nameWithoutExtension + ".js")
            if (!jsFile.exists()) {
                return "Compiled but output file not found. Compile output:\n$compileOutput"
            }

            // Execute compiled JS
            val runProcess =
                ProcessBuilder("node", jsFile.absolutePath)
                    .directory(projectDir)
                    .start()
            val runCompleted =
                try {
                    runProcess.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                } catch (e: InterruptedException) {
                    runProcess.destroyForcibly()
                    Thread.currentThread().interrupt()
                    return "Error: interrupted"
                }
            if (!runCompleted) {
                runProcess.destroyForcibly()
                return "Error: execution timed out"
            }

            val stdout = runProcess.inputStream.use { it.bufferedReader().readText() }
            val stderr = runProcess.errorStream.use { it.bufferedReader().readText() }

            return parseOutput(stdout, stderr)
        } finally {
            tmpFile.delete()
            jsFile?.delete()
            FileUtil.delete(tmpDir)
        }
    }
}
