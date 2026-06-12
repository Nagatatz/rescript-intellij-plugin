package com.rescript.plugin.diagram

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import com.rescript.plugin.RescriptFileType
import com.rescript.plugin.lang.psi.RescriptFile
import com.rescript.plugin.util.RescriptRegexPatterns

/**
 * Provides dependency diagram data for ReScript module relationships.
 *
 * Analyzes `open` and `include` statements across all ReScript files in the
 * project to build a directed graph of module dependencies. The graph model
 * can be consumed by the diagram tool window for visualization.
 *
 * @see RescriptDependencyDiagramModel for the graph data structure
 */
object RescriptDependencyDiagramProvider {
    /**
     * Builds a dependency graph for all ReScript modules in the project.
     *
     * Must be invoked off the EDT; performs a read action internally to access
     * the file-type index and PSI trees.
     *
     * @param project the IntelliJ project
     * @return the dependency diagram model
     */
    fun buildDiagram(project: Project): RescriptDependencyDiagramModel {
        val model = RescriptDependencyDiagramModel()
        val psiManager = PsiManager.getInstance(project)

        ApplicationManager.getApplication().runReadAction {
            com.intellij.psi.search.FileTypeIndex
                .getFiles(
                    RescriptFileType,
                    com.intellij.psi.search.GlobalSearchScope
                        .projectScope(project),
                ).forEach { file ->
                    val psiFile = psiManager.findFile(file) as? RescriptFile ?: return@forEach
                    val moduleName = file.nameWithoutExtension
                    val dependencies = extractDependencies(psiFile.text)
                    model.addModule(moduleName, dependencies)
                }
        }

        return model
    }

    /**
     * Extracts module dependency names from `open` and `include` statements in source text.
     *
     * @param sourceText the ReScript source code
     * @return list of module names referenced by open/include
     */
    internal fun extractDependencies(sourceText: String): List<String> {
        val deps = mutableListOf<String>()

        for (line in sourceText.lines()) {
            val trimmed = line.trim()
            val openMatch = RescriptRegexPatterns.OPEN_MODULE_CAPTURE.find(trimmed)
            if (openMatch != null) {
                deps.add(openMatch.groupValues[1])
                continue
            }
            val includeMatch = INCLUDE_PATTERN.find(trimmed)
            if (includeMatch != null) {
                deps.add(includeMatch.groupValues[1])
            }
        }

        return deps.distinct()
    }

    // Pattern for `include ModuleName`
    private val INCLUDE_PATTERN = RescriptRegexPatterns.INCLUDE_MODULE_CAPTURE
}
