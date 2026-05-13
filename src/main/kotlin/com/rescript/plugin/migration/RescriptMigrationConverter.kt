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
    private const val VERSION_PROBE_TIMEOUT_SECONDS = 5L

    // ReScript 12 (the rewatch-based Rust CLI) removed the `convert`
    // subcommand entirely. Trying to run it surfaces a clap parser
    // error like `unexpected argument 'src/Main.re' found`, which is
    // confusing for users. We refuse early instead.
    private const val FIRST_UNSUPPORTED_MAJOR = 12

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
        val workingDir = project.basePath?.let(::File) ?: File(System.getProperty("user.dir"))

        // Refuse early on rescript@12+: the `convert` subcommand was
        // removed and any further work would just produce a cryptic
        // clap error from the rewatch CLI.
        val major = probeMajorVersion(settings.rescriptBinaryPath, workingDir)
        if (major != null && major >= FIRST_UNSUPPORTED_MAJOR) {
            return ConversionResult(
                candidate,
                ConversionStatus.FAILED,
                "ReScript $major removed the `convert` subcommand. " +
                    "Pin `rescript@^11` in this project to use the Migration Pilot, " +
                    "or convert the file manually.",
            )
        }

        // `rescript convert` resolves its argument relative to its cwd
        // (which we pin to the project base path), so we must pass the
        // project-relative path here. Passing the absolute path causes
        // the CLI to reject the file with `don't know what to do with`.
        val command = buildCommand(settings.rescriptBinaryPath, candidate.relativePath)
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
     * Spawns `rescript --version` in [workingDir] and returns the
     * major component of the first line. Returns `null` if the CLI
     * cannot be invoked or the output cannot be parsed — in that case
     * the caller should fall through to the regular `convert` path so
     * the user still gets the CLI's own error message.
     *
     * @param rescriptBinaryPath value of `RescriptProjectSettings.rescriptBinaryPath`;
     *   when blank the probe falls back to `npx rescript`.
     * @param workingDir cwd for the probe so node_modules-local
     *   binaries resolve the same way they do for the actual convert.
     * @return parsed major version, or null on probe failure
     */
    internal fun probeMajorVersion(
        rescriptBinaryPath: String,
        workingDir: File,
    ): Int? {
        val argv =
            if (rescriptBinaryPath.isBlank()) {
                listOf("npx", "rescript", "--version")
            } else {
                listOf(rescriptBinaryPath, "--version")
            }
        return try {
            val process =
                ProcessBuilder(argv)
                    .directory(workingDir)
                    .redirectErrorStream(true)
                    .start()
            val finished = process.waitFor(VERSION_PROBE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            if (!finished) {
                process.destroyForcibly()
                return null
            }
            val firstLine =
                process.inputStream
                    .bufferedReader()
                    .readText()
                    .lineSequence()
                    .firstOrNull()
                    .orEmpty()
            parseRescriptMajorVersion(firstLine)
        } catch (e: Exception) {
            LOG.info("rescript --version probe failed: ${e.message}")
            null
        }
    }

    /**
     * Extracts the major version integer from a `rescript --version`
     * output line. Accepts both the bare `<major>.<minor>.<patch>`
     * format (v12 rewatch CLI) and the `rescript <major>.<minor>.<patch>`
     * format (older Node CLI). Returns `null` if no `digits.digits`
     * pattern matches anywhere in the line.
     *
     * @param versionLine first stdout line of `rescript --version`
     * @return parsed major version, or null when the line carries no
     *   recognisable version triplet
     */
    internal fun parseRescriptMajorVersion(versionLine: String): Int? {
        // Optional leading "rescript " banner, then digits.digits(.digits)?
        // The regex is anchored loosely so both "12.2.0" and
        // "rescript 11.1.4 (some build)" parse correctly.
        val pattern = Regex("""(?:^|\s)(\d+)\.(\d+)(?:\.(\d+))?""")
        return pattern
            .find(versionLine)
            ?.groupValues
            ?.get(1)
            ?.toIntOrNull()
    }

    /**
     * Pure helper that picks the rescript binary to invoke and
     * appends the standard `convert <file>` arguments. When the user
     * has not configured an explicit binary path we fall through to
     * `npx rescript`, which is the canonical project-local resolution
     * elsewhere in the plugin.
     *
     * @param rescriptBinaryPath value of `RescriptProjectSettings.rescriptBinaryPath`
     * @param sourcePath project-relative path of the `.re` / `.rei`
     *   file to convert. `rescript convert` resolves this against its
     *   cwd, so absolute paths are rejected by the CLI.
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
