package com.rescript.plugin.binding

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.diagnostic.logger
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.concurrent.TimeUnit

/**
 * Spawns the bundled `dts-to-json.js` Node.js script to parse a `.d.ts` file
 * and collects the JSON output from stdout.
 *
 * The script is extracted from plugin resources to a temporary file on first use
 * and cached for the lifetime of the JVM.
 *
 * @see DtsJsonModel for the JSON schema
 * @see DtsNodeDetector for Node.js and TypeScript detection
 */
object DtsParserProcess {
    private const val SCRIPT_RESOURCE = "/scripts/dts-to-json.js"
    private const val TIMEOUT_MS = 30_000L
    private val LOG = logger<DtsParserProcess>()

    @Volatile
    private var cachedScriptPath: Path? = null

    /**
     * Runs the parser on a `.d.ts` file and returns the JSON output.
     *
     * @param nodePath the Node.js executable path
     * @param dtsFilePath the absolute path to the `.d.ts` file
     * @param typeScriptPath the path to the `node_modules/typescript` directory
     * @return the JSON string from stdout
     * @throws DtsParserException if the process fails or times out
     */
    fun parse(
        nodePath: String,
        dtsFilePath: String,
        typeScriptPath: String,
    ): String {
        val scriptPath = extractScript()

        val commandLine =
            GeneralCommandLine(nodePath, scriptPath.toString(), dtsFilePath)
                .withCharset(Charsets.UTF_8)
                .withEnvironment("TS_PATH", typeScriptPath)

        LOG.info("Running dts-to-json: ${commandLine.commandLineString}")

        val proc = commandLine.createProcess()

        val stderr = StringBuilder()
        val stderrThread =
            Thread(
                {
                    try {
                        proc.errorStream.reader(Charsets.UTF_8).use {
                            stderr.append(it.readText())
                        }
                    } catch (e: IOException) {
                        LOG.debug("Failed to read stderr from dts-to-json process", e)
                    }
                },
                "dts-to-json-stderr",
            )
        stderrThread.start()

        val stdout =
            proc.inputStream.reader(Charsets.UTF_8).use {
                it.readText()
            }

        stderrThread.join(TIMEOUT_MS)
        val completed = proc.waitFor(TIMEOUT_MS, TimeUnit.MILLISECONDS)
        if (!completed) {
            proc.destroyForcibly()
            throw DtsParserException("dts-to-json timed out after ${TIMEOUT_MS}ms")
        }
        val exitCode = proc.exitValue()

        if (exitCode != 0) {
            val errorMsg = stderr.toString().ifBlank { "dts-to-json failed with exit code $exitCode" }
            LOG.warn("dts-to-json failed: $errorMsg")
            throw DtsParserException(errorMsg)
        }

        if (stdout.isBlank()) {
            throw DtsParserException("dts-to-json produced empty output")
        }

        return stdout
    }

    /**
     * Extracts the bundled script to a temporary file, caching the result.
     *
     * Synchronized to prevent multiple threads from creating duplicate temp files
     * when called concurrently (e.g., parallel binding generation).
     *
     * @return the path to the extracted script file
     * @throws DtsParserException if the resource cannot be found or extracted
     */
    fun extractScript(): Path =
        synchronized(this) {
            cachedScriptPath?.let { path ->
                if (Files.isRegularFile(path)) return path
            }

            val inputStream =
                DtsParserProcess::class.java.getResourceAsStream(SCRIPT_RESOURCE)
                    ?: throw DtsParserException("Bundled script not found: $SCRIPT_RESOURCE")

            val tempFile = Files.createTempFile("dts-to-json-", ".js")
            inputStream.use { stream ->
                Files.copy(stream, tempFile, StandardCopyOption.REPLACE_EXISTING)
            }

            cachedScriptPath = tempFile
            LOG.info("Extracted dts-to-json.js to: $tempFile")
            tempFile
        }
}

/**
 * Exception thrown when the dts-to-json parser process fails.
 */
class DtsParserException(
    message: String,
) : RuntimeException(message)
