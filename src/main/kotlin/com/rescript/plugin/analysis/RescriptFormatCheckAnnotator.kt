package com.rescript.plugin.analysis

import com.intellij.codeInsight.actions.ReformatCodeProcessor
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.ExternalAnnotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.rescript.plugin.lang.psi.RescriptFile
import com.rescript.plugin.run.RescriptCliDetector
import com.rescript.plugin.settings.RescriptProjectSettings
import com.rescript.plugin.util.RescriptSecurityUtils
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * External annotator that checks whether a ReScript file is formatted according to
 * `rescript format`. Compares the current editor content against the formatted output
 * and shows an INFO-level annotation when the file is not formatted.
 *
 * The three-phase ExternalAnnotator lifecycle:
 * 1. [collectInformation] — gathers document text and file metadata on the EDT
 * 2. [doAnnotate] — runs `rescript format --stdin` in a background thread and compares output
 * 3. [apply] — creates an annotation with a quick-fix if the file is not formatted
 *
 * This annotator is disabled by default and can be enabled via
 * Settings > Languages & Frameworks > ReScript > "Enable format check".
 *
 * @see RescriptFormatQuickFix for the quick-fix that reformats the file
 * @see com.rescript.plugin.formatter.RescriptFormattingService for the formatting implementation
 */
class RescriptFormatCheckAnnotator :
    ExternalAnnotator<RescriptFormatCheckAnnotator.CollectedInfo, RescriptFormatCheckAnnotator.AnnotationResult>() {
    /**
     * Information collected on the EDT for background processing.
     *
     * @param documentText the current editor content
     * @param extension the file extension ("res" or "resi")
     * @param projectBasePath the project root directory path
     * @param filePath the absolute path of the file being checked
     */
    data class CollectedInfo(
        val documentText: String,
        val extension: String,
        val projectBasePath: String,
        val filePath: String,
    )

    /**
     * Result from the background format check.
     *
     * @param message the annotation message to display
     */
    data class AnnotationResult(
        val message: String,
    )

    override fun collectInformation(file: PsiFile): CollectedInfo? {
        if (file !is RescriptFile) return null

        val project = file.project
        val settings = RescriptProjectSettings.getInstance(project)
        if (!settings.formatCheckEnabled) return null

        val vFile = file.virtualFile ?: return null
        val basePath = project.basePath ?: return null

        if (RescriptCliDetector.findCli(vFile.parent?.path, basePath) == null) return null

        val document = file.viewProvider.document ?: return null
        val ext = vFile.extension ?: "res"

        return CollectedInfo(document.text, ext, basePath, vFile.path)
    }

    override fun doAnnotate(info: CollectedInfo?): AnnotationResult? {
        if (info == null) return null

        val cliPath =
            RescriptCliDetector.findCli(
                info.filePath.substringBeforeLast("/"),
                info.projectBasePath,
            ) ?: return null

        return try {
            val formatted =
                runFormatCheck(cliPath, info.extension, info.documentText)
                    ?: return null

            if (formatted == info.documentText) {
                // File is already formatted
                null
            } else {
                val shortcut = if (SystemInfo.isMac) "Cmd+Option+L" else "Ctrl+Alt+L"
                AnnotationResult("Code is not formatted. Use $shortcut to format.")
            }
        } catch (e: Exception) {
            LOG.debug("Failed to run format check", e)
            null
        }
    }

    override fun apply(
        file: PsiFile,
        result: AnnotationResult?,
        holder: AnnotationHolder,
    ) {
        if (result == null) return

        // Annotate at the very beginning of the file
        val range = TextRange(0, minOf(1, file.textLength))

        holder
            .newAnnotation(HighlightSeverity.WEAK_WARNING, result.message)
            .range(range)
            .withFix(RescriptFormatQuickFix())
            .fileLevel()
            .create()
    }

    companion object {
        private val LOG = logger<RescriptFormatCheckAnnotator>()
        private const val TIMEOUT_MS = 10_000L

        /**
         * Runs `rescript format --stdin .<ext>` and returns the formatted output,
         * or null if the process fails or times out.
         *
         * @param cliPath path to the rescript CLI binary
         * @param extension the file extension (without dot)
         * @param documentText the source code to format
         * @return the formatted text, or null on failure
         */
        fun runFormatCheck(
            cliPath: String,
            extension: String,
            documentText: String,
        ): String? {
            val commandLine =
                GeneralCommandLine(cliPath, "format", "--stdin", ".$extension")
                    .withCharset(Charsets.UTF_8)

            val process = commandLine.createProcess()

            // Write to stdin in a separate thread to avoid deadlock
            val stdinThread =
                Thread(
                    {
                        try {
                            process.outputStream.bufferedWriter(Charsets.UTF_8).use {
                                it.write(documentText)
                            }
                        } catch (_: IOException) {
                            // Expected when the process exits before stdin is fully written (broken pipe)
                        }
                    },
                    "rescript-format-check-stdin",
                )
            stdinThread.start()

            // Capture stderr in a separate thread
            val stderrThread =
                Thread(
                    {
                        try {
                            process.errorStream.reader(Charsets.UTF_8).use { it.readText() }
                        } catch (_: IOException) {
                            // Expected when the process exits before stderr is fully read (stream closed)
                        }
                    },
                    "rescript-format-check-stderr",
                )
            stderrThread.start()

            val stdout = process.inputStream.reader(Charsets.UTF_8).use { it.readText() }

            stdinThread.join(TIMEOUT_MS)
            stderrThread.join(TIMEOUT_MS)
            val completed =
                process.waitFor(
                    RescriptSecurityUtils.PROCESS_TIMEOUT_SECONDS,
                    TimeUnit.SECONDS,
                )
            if (!completed) {
                process.destroyForcibly()
                LOG.debug("rescript format check timed out")
                return null
            }

            val exitCode = process.exitValue()
            if (exitCode != 0) {
                // Syntax error or other formatting failure — silently skip
                LOG.debug("rescript format exited with code $exitCode")
                return null
            }

            return stdout
        }
    }
}

/**
 * Quick-fix that reformats the current ReScript file using the IDE's built-in
 * reformat action, which delegates to [com.rescript.plugin.formatter.RescriptFormattingService].
 *
 * Implements [IntentionAction] to be compatible with the annotation holder's `withFix` API.
 *
 * @see RescriptFormatCheckAnnotator for the annotator that attaches this fix
 */
class RescriptFormatQuickFix : IntentionAction {
    override fun getText(): String = "Format this file"

    override fun getFamilyName(): String = "ReScript format check"

    override fun isAvailable(
        project: Project,
        editor: Editor?,
        file: PsiFile?,
    ): Boolean = file is RescriptFile

    override fun invoke(
        project: Project,
        editor: Editor?,
        file: PsiFile?,
    ) {
        if (file == null) return
        ReformatCodeProcessor(file, false).run()
    }

    override fun startInWriteAction(): Boolean = false
}
