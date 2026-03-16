package com.rescript.plugin.analysis

import com.intellij.codeInspection.GlobalInspectionContext
import com.intellij.codeInspection.GlobalInspectionTool
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.rescript.plugin.lang.psi.RescriptFile
import java.io.IOException

/**
 * Global inspection that runs `rescript-tools reanalyze -json` to find unused code
 * across the entire project (Analyze > Inspect Code).
 *
 * Unlike [RescriptReanalyzeAnnotator] which operates on individual files in the editor,
 * this inspection collects all diagnostics from a single reanalyze invocation and
 * distributes them to the corresponding [RescriptFile] instances via [ProblemDescriptionsProcessor].
 *
 * @see RescriptReanalyzeAnnotator for per-file annotation in the editor
 */
class RescriptUnusedCodeInspection : GlobalInspectionTool() {
    override fun runInspection(
        scope: com.intellij.analysis.AnalysisScope,
        manager: InspectionManager,
        globalContext: GlobalInspectionContext,
        problemDescriptionsProcessor: com.intellij.codeInspection.ProblemDescriptionsProcessor,
    ) {
        val project = manager.project
        val basePath = project.basePath ?: return

        val toolPath = RescriptReanalyzeAnnotator.findReanalyzeTool(basePath) ?: return

        val json =
            try {
                val commandLine =
                    GeneralCommandLine(toolPath, "reanalyze", "-json")
                        .withWorkDirectory(basePath)
                        .withCharset(Charsets.UTF_8)

                val process = commandLine.createProcess()
                val stdout = process.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                val exitCode = process.waitFor()

                if (exitCode != 0) {
                    LOG.debug("reanalyze exited with code $exitCode")
                    return
                }
                stdout
            } catch (e: ExecutionException) {
                LOG.warn(
                    "Failed to create reanalyze process" +
                        " (command: $toolPath reanalyze -json, workDir: $basePath)",
                    e,
                )
                return
            } catch (e: IOException) {
                LOG.warn(
                    "I/O error while running reanalyze" +
                        " (command: $toolPath reanalyze -json, workDir: $basePath)",
                    e,
                )
                return
            }

        val allDiagnostics = RescriptReanalyzeAnnotator.parseAllDiagnostics(json)

        // Group diagnostics by file
        val byFile = allDiagnostics.groupBy { it.first }

        val psiManager = PsiManager.getInstance(project)
        val searchScope = GlobalSearchScope.projectScope(project)

        for ((filePath, diagnostics) in byFile) {
            // Find the PsiFile for this path
            val vFile =
                com.intellij.openapi.vfs.LocalFileSystem
                    .getInstance()
                    .findFileByPath(filePath)
                    ?: com.intellij.openapi.vfs.LocalFileSystem
                        .getInstance()
                        .findFileByPath("$basePath/$filePath")
                    ?: continue

            if (!searchScope.contains(vFile)) continue

            val psiFile = psiManager.findFile(vFile) ?: continue
            if (psiFile !is RescriptFile) continue

            val document = PsiDocumentManager.getInstance(project).getDocument(psiFile) ?: continue

            for ((_, diag) in diagnostics) {
                val lineCount = document.lineCount
                if (diag.startLine >= lineCount || diag.endLine >= lineCount) continue

                val startOffset = document.getLineStartOffset(diag.startLine) + diag.startChar
                val endOffset = document.getLineStartOffset(diag.endLine) + diag.endChar
                val docLength = document.textLength

                if (startOffset > docLength || endOffset > docLength || startOffset >= endOffset) continue

                val element = psiFile.findElementAt(startOffset) ?: continue

                val descriptor =
                    manager.createProblemDescriptor(
                        element,
                        TextRange(0, (endOffset - startOffset).coerceAtMost(element.textLength)),
                        diag.message,
                        ProblemHighlightType.WARNING,
                        false,
                    )

                problemDescriptionsProcessor.addProblemElement(
                    globalContext.refManager.getReference(psiFile),
                    descriptor,
                )
            }
        }
    }

    override fun isGraphNeeded(): Boolean = false

    companion object {
        private val LOG = logger<RescriptUnusedCodeInspection>()
    }
}
