package com.rescript.plugin.migration

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.rescript.plugin.settings.RescriptProjectSettings
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Drives `rescript convert` for one [MigrationCandidate]: builds the
 * argv with [buildCommand], runs the CLI through [ProcessBuilder],
 * and on success replaces the source file with a `.res` copy of the
 * converted output.
 *
 * The argv is built by a pure helper so the binary-resolution rules
 * can be unit-tested without spawning a real subprocess.
 */
object RescriptMigrationConverter {
    private val LOG = logger<RescriptMigrationConverter>()
    private const val TIMEOUT_SECONDS = 30L

    /**
     * Runs `rescript convert` against [candidate]. Must be invoked
     * off the EDT — both the process call and the post-success
     * filesystem mutation are blocking.
     */
    fun convert(
        project: Project,
        candidate: MigrationCandidate,
    ): ConversionResult {
        val settings = RescriptProjectSettings.getInstance(project)
        val command = buildCommand(settings.rescriptBinaryPath, candidate.file.path)
        val workingDir = project.basePath?.let(::File) ?: File(System.getProperty("user.dir"))
        return try {
            val process =
                ProcessBuilder(command)
                    .directory(workingDir)
                    .redirectErrorStream(false)
                    .start()
            val finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            if (!finished) {
                process.destroyForcibly()
                return ConversionResult(candidate, ConversionStatus.FAILED, "Timed out after ${TIMEOUT_SECONDS}s")
            }
            val stdout = process.inputStream.bufferedReader().readText()
            val stderr = process.errorStream.bufferedReader().readText()
            if (process.exitValue() != 0) {
                return ConversionResult(
                    candidate,
                    ConversionStatus.FAILED,
                    stderr.ifBlank { "rescript convert exited with code ${process.exitValue()}" },
                )
            }
            replaceSource(candidate, stdout)
            ConversionResult(candidate, ConversionStatus.SUCCESS, "")
        } catch (e: Exception) {
            LOG.warn("rescript convert failed for ${candidate.file.path}: ${e.message}")
            ConversionResult(candidate, ConversionStatus.FAILED, e.message ?: e.javaClass.simpleName)
        }
    }

    /**
     * Pure helper that picks the rescript binary to invoke and
     * appends the standard `convert <file>` arguments. When the user
     * has not configured an explicit binary path we fall through to
     * `npx rescript`, which is the canonical project-local resolution
     * elsewhere in the plugin.
     *
     * @param rescriptBinaryPath value of `RescriptProjectSettings.rescriptBinaryPath`
     * @param sourcePath absolute path of the `.re` / `.rei` file to convert
     */
    internal fun buildCommand(
        rescriptBinaryPath: String,
        sourcePath: String,
    ): List<String> =
        if (rescriptBinaryPath.isBlank()) {
            listOf("npx", "rescript", "convert", sourcePath)
        } else {
            listOf(rescriptBinaryPath, "convert", sourcePath)
        }

    /**
     * Replaces the on-disk content of [candidate] with the converted
     * text and renames the file from `.re`/`.rei` to `.res`/`.resi`.
     * Runs on the calling pooled thread but takes a write action for
     * the VFS mutation.
     */
    private fun replaceSource(
        candidate: MigrationCandidate,
        convertedText: String,
    ) {
        val source = candidate.file
        val newName =
            when {
                source.name.endsWith(".rei") -> source.nameWithoutExtension + ".resi"
                source.name.endsWith(".re") -> source.nameWithoutExtension + ".res"
                else -> source.name
            }
        ApplicationManager.getApplication().invokeAndWait {
            ApplicationManager.getApplication().runWriteAction {
                source.setBinaryContent(convertedText.toByteArray(source.charset))
                source.rename(this, newName)
                LocalFileSystem.getInstance().refresh(false)
            }
        }
    }
}
