package com.rescript.plugin.analysis

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.ExternalAnnotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.rescript.plugin.lang.psi.RescriptFile
import com.rescript.plugin.util.RescriptSecurityUtils
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit

/**
 * External annotator that runs `rescript-tools reanalyze -json` to detect dead code
 * and unused declarations in ReScript files.
 *
 * The three-phase ExternalAnnotator lifecycle:
 * 1. [collectInformation] — gathers file path and project base path on the EDT
 * 2. [doAnnotate] — runs `reanalyze` CLI in a background thread, parses JSON output
 * 3. [apply] — maps diagnostics to editor annotations with quick-fix actions
 *
 * The reanalyze tool is located by walking up from the project root looking for
 * `node_modules/rescript/rescript-tools` (or `.exe` on Windows).
 *
 * @see RescriptReanalyzeQuickFix for the quick-fix actions attached to annotations
 * @see RescriptUnusedCodeInspection for project-wide analysis using the same tool
 */
class RescriptReanalyzeAnnotator :
    ExternalAnnotator<RescriptReanalyzeAnnotator.CollectedInfo, RescriptReanalyzeAnnotator.AnnotationResult>() {
    /** Information gathered on the EDT for the background reanalyze invocation. */
    data class CollectedInfo(
        val filePath: String,
        val projectBasePath: String,
    )

    /** A single diagnostic entry parsed from reanalyze JSON output. */
    data class ReanalyzeDiagnostic(
        val name: String,
        val message: String,
        val startLine: Int,
        val startChar: Int,
        val endLine: Int,
        val endChar: Int,
    )

    /** Result of the background reanalyze pass, containing all diagnostics for the file. */
    data class AnnotationResult(
        val diagnostics: List<ReanalyzeDiagnostic>,
    )

    override fun collectInformation(file: PsiFile): CollectedInfo? {
        if (file !is RescriptFile) return null
        val vFile = file.virtualFile ?: return null
        val basePath = file.project.basePath ?: return null
        if (findReanalyzeTool(basePath) == null) return null
        return CollectedInfo(vFile.path, basePath)
    }

    override fun doAnnotate(info: CollectedInfo?): AnnotationResult? {
        if (info == null) return null

        val toolPath = findReanalyzeTool(info.projectBasePath) ?: return null

        return try {
            val commandLine =
                GeneralCommandLine(toolPath, "reanalyze", "-json")
                    .withWorkDirectory(info.projectBasePath)
                    .withCharset(Charsets.UTF_8)

            val process = commandLine.createProcess()
            val stdout = process.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            val completed = process.waitFor(RescriptSecurityUtils.PROCESS_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            if (!completed) {
                process.destroyForcibly()
                LOG.debug("reanalyze timed out")
                return null
            }
            val exitCode = process.exitValue()

            if (exitCode != 0) {
                LOG.debug("reanalyze exited with code $exitCode")
                return null
            }

            val diagnostics = parseJsonOutput(stdout, info.filePath)
            AnnotationResult(diagnostics)
        } catch (e: Exception) {
            LOG.debug("Failed to run reanalyze", e)
            null
        }
    }

    override fun apply(
        file: PsiFile,
        result: AnnotationResult?,
        holder: AnnotationHolder,
    ) {
        if (result == null) return
        val document = PsiDocumentManager.getInstance(file.project).getDocument(file) ?: return

        for (diag in result.diagnostics) {
            val lineCount = document.lineCount
            if (diag.startLine >= lineCount || diag.endLine >= lineCount) continue

            val startOffset = document.getLineStartOffset(diag.startLine) + diag.startChar
            val endOffset = document.getLineStartOffset(diag.endLine) + diag.endChar
            val docLength = document.textLength

            if (startOffset > docLength || endOffset > docLength || startOffset >= endOffset) continue

            val quickFixes = RescriptReanalyzeQuickFix.createQuickFixes(diag.name)

            val builder =
                holder
                    .newAnnotation(HighlightSeverity.WARNING, diag.message)
                    .range(TextRange(startOffset, endOffset))

            for (fix in quickFixes) {
                builder.withFix(fix)
            }

            builder.create()
        }
    }

    companion object {
        private val LOG = logger<RescriptReanalyzeAnnotator>()

        fun findReanalyzeTool(projectBasePath: String): String? {
            var dir: Path? = Path.of(projectBasePath)
            var depth = 0
            while (dir != null && depth < RescriptSecurityUtils.MAX_PARENT_TRAVERSAL_DEPTH) {
                // Check node_modules/rescript/ for rescript-tools.exe or rescript-tools
                val toolsExe = dir.resolve("node_modules/rescript/rescript-tools.exe")
                if (Files.isExecutable(toolsExe)) return toolsExe.toString()

                val tools = dir.resolve("node_modules/rescript/rescript-tools")
                if (Files.isExecutable(tools)) return tools.toString()

                // Check node_modules/.bin/
                val bin = dir.resolve("node_modules/.bin/rescript-tools")
                if (Files.isExecutable(bin)) return bin.toString()

                dir = dir.parent
                depth++
            }
            return null
        }

        /**
         * Parses a single JSON object entry from reanalyze output into a file-diagnostic pair.
         *
         * @param obj the JSON object representing one diagnostic entry
         * @return a pair of (file path, diagnostic), or null if the entry is malformed
         */
        fun parseDiagnosticEntry(obj: com.google.gson.JsonObject): Pair<String, ReanalyzeDiagnostic>? {
            val file = obj.get("file")?.asString ?: return null

            val range = obj.getAsJsonArray("range") ?: return null
            if (range.size() < 4) return null

            val message = obj.get("message")?.asString ?: return null
            val name = obj.get("name")?.asString ?: "unknown"

            return file to
                ReanalyzeDiagnostic(
                    name = name,
                    message = message,
                    startLine = range[0].asInt,
                    startChar = range[1].asInt,
                    endLine = range[2].asInt,
                    endChar = range[3].asInt,
                )
        }

        /**
         * Parses reanalyze JSON output and returns diagnostics matching the given file path.
         *
         * @param json the raw JSON string from reanalyze
         * @param filePath the absolute file path to filter diagnostics for
         * @return list of diagnostics for the specified file
         */
        fun parseJsonOutput(
            json: String,
            filePath: String,
        ): List<ReanalyzeDiagnostic> {
            if (json.isBlank()) return emptyList()

            return try {
                val array =
                    com.google.gson.JsonParser
                        .parseString(json)
                        .asJsonArray
                array.mapNotNull { element ->
                    val (file, diagnostic) = parseDiagnosticEntry(element.asJsonObject) ?: return@mapNotNull null
                    // Filter: keep only diagnostics matching the target file
                    if (!file.endsWith(filePath.substringAfterLast("/")) &&
                        file != filePath &&
                        !filePath.endsWith(file)
                    ) {
                        return@mapNotNull null
                    }
                    diagnostic
                }
            } catch (e: Exception) {
                LOG.debug("Failed to parse reanalyze JSON output", e)
                emptyList()
            }
        }

        /**
         * Parse all diagnostics from reanalyze JSON output, without filtering by file.
         * Used by GlobalInspectionTool for project-wide analysis.
         *
         * @param json the raw JSON string from reanalyze
         * @return list of (file path, diagnostic) pairs for all entries
         */
        fun parseAllDiagnostics(json: String): List<Pair<String, ReanalyzeDiagnostic>> {
            if (json.isBlank()) return emptyList()

            return try {
                val array =
                    com.google.gson.JsonParser
                        .parseString(json)
                        .asJsonArray
                array.mapNotNull { element ->
                    parseDiagnosticEntry(element.asJsonObject)
                }
            } catch (e: Exception) {
                LOG.debug("Failed to parse reanalyze JSON output", e)
                emptyList()
            }
        }
    }
}
