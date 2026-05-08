package com.rescript.plugin.migration

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope

/**
 * Enumerates `.re` and `.rei` files within the project as
 * [MigrationCandidate]s for the migration pilot.
 *
 * The IO entry point [findCandidates] uses [FilenameIndex] to avoid
 * a manual VFS walk; the pure helper [toCandidates] performs the
 * project-base filtering and relative-path computation so the
 * filtering rules can be unit-tested without an IDE fixture.
 */
object RescriptMigrationFinder {
    /**
     * Returns every `.re` / `.rei` file inside [project]'s scope,
     * mapped to a [MigrationCandidate] with a project-relative path.
     */
    fun findCandidates(project: Project): List<MigrationCandidate> {
        val basePath = project.basePath ?: return emptyList()
        val collected = mutableListOf<VirtualFile>()
        ApplicationManager.getApplication().runReadAction {
            val scope = GlobalSearchScope.projectScope(project)
            collected.addAll(FilenameIndex.getAllFilesByExt(project, "re", scope))
            collected.addAll(FilenameIndex.getAllFilesByExt(project, "rei", scope))
        }
        return toCandidates(basePath, collected.asSequence())
    }

    /**
     * Pure helper: keeps only files whose path starts with
     * [projectBasePath] and assigns each a normalised relative path.
     * Stable order: alphabetical by relative path.
     */
    internal fun toCandidates(
        projectBasePath: String,
        files: Sequence<VirtualFile>,
    ): List<MigrationCandidate> {
        val normalisedBase = projectBasePath.removeSuffix("/")
        val prefix = "$normalisedBase/"
        return files
            .mapNotNull { file ->
                val path = file.path
                if (!path.startsWith(prefix)) return@mapNotNull null
                MigrationCandidate(file = file, relativePath = path.removePrefix(prefix))
            }.sortedBy { it.relativePath }
            .toList()
    }
}
