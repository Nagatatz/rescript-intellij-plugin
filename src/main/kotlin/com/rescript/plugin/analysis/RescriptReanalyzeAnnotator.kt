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
import java.nio.file.Files
import java.nio.file.Path

class RescriptReanalyzeAnnotator :
    ExternalAnnotator<RescriptReanalyzeAnnotator.CollectedInfo, RescriptReanalyzeAnnotator.AnnotationResult>() {
    data class CollectedInfo(
        val filePath: String,
        val projectBasePath: String,
    )

    data class ReanalyzeDiagnostic(
        val message: String,
        val startLine: Int,
        val startChar: Int,
        val endLine: Int,
        val endChar: Int,
    )

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
            val exitCode = process.waitFor()

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

            holder
                .newAnnotation(HighlightSeverity.WARNING, diag.message)
                .range(TextRange(startOffset, endOffset))
                .create()
        }
    }

    companion object {
        private val LOG = logger<RescriptReanalyzeAnnotator>()
        private const val TIMEOUT_MS = 30_000L

        fun findReanalyzeTool(projectBasePath: String): String? {
            var dir: Path? = Path.of(projectBasePath)
            while (dir != null) {
                // Check node_modules/rescript/ for rescript-tools.exe or rescript-tools
                val toolsExe = dir.resolve("node_modules/rescript/rescript-tools.exe")
                if (Files.isExecutable(toolsExe)) return toolsExe.toString()

                val tools = dir.resolve("node_modules/rescript/rescript-tools")
                if (Files.isExecutable(tools)) return tools.toString()

                // Check node_modules/.bin/
                val bin = dir.resolve("node_modules/.bin/rescript-tools")
                if (Files.isExecutable(bin)) return bin.toString()

                dir = dir.parent
            }
            return null
        }

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
                    val obj = element.asJsonObject
                    val file = obj.get("file")?.asString ?: return@mapNotNull null
                    if (!file.endsWith(filePath.substringAfterLast("/")) &&
                        file != filePath &&
                        !filePath.endsWith(file)
                    ) {
                        return@mapNotNull null
                    }

                    val range = obj.getAsJsonArray("range") ?: return@mapNotNull null
                    if (range.size() < 4) return@mapNotNull null

                    val message = obj.get("message")?.asString ?: return@mapNotNull null

                    ReanalyzeDiagnostic(
                        message = message,
                        startLine = range[0].asInt,
                        startChar = range[1].asInt,
                        endLine = range[2].asInt,
                        endChar = range[3].asInt,
                    )
                }
            } catch (e: Exception) {
                LOG.debug("Failed to parse reanalyze JSON output", e)
                emptyList()
            }
        }
    }
}
