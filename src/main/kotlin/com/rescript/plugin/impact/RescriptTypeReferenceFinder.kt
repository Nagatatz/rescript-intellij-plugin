package com.rescript.plugin.impact

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiSearchHelper
import com.intellij.psi.search.UsageSearchContext
import com.rescript.plugin.util.RescriptOffsetUtils

/**
 * Finds project-wide references to a [TypeTarget] using IntelliJ
 * Platform's word index (`PsiSearchHelper.processElementsWithWord`).
 *
 * The implementation is intentionally lightweight: it scans every
 * occurrence of the target's local name in code, skips the
 * declaration site, and lets [RescriptReferenceClassifier] tag each
 * candidate. References that classify as [TypeRefKind.UNKNOWN] are
 * still reported so the panel doesn't silently drop ambiguous hits.
 *
 * A 200-entry soft cap keeps the panel responsive even on
 * widely-used types; callers can detect truncation via
 * [Result.truncated].
 */
object RescriptTypeReferenceFinder {
    /** Soft upper bound on the number of references shown in the panel. */
    internal const val MAX_REFERENCES = 200

    /**
     * Result wrapper carrying the matched [entries] alongside a
     * truncation flag, so the caller knows whether to render the
     * "N more references…" footer.
     */
    data class Result(
        val entries: List<ReferenceEntry>,
        val truncated: Boolean,
    )

    /**
     * Searches [project] for references to [target] within
     * [GlobalSearchScope.projectScope], classifies each, and returns
     * up to [MAX_REFERENCES] entries.
     *
     * Must be invoked off the EDT — this function performs a read
     * action internally.
     */
    fun findReferences(
        project: Project,
        target: TypeTarget,
    ): Result {
        val collected = mutableListOf<ReferenceEntry>()
        var truncated = false
        val helper = PsiSearchHelper.getInstance(project)
        val scope = GlobalSearchScope.projectScope(project)
        val app = ApplicationManager.getApplication()

        app.runReadAction {
            helper.processElementsWithWord(
                { element, offsetInElement ->
                    if (collected.size >= MAX_REFERENCES) {
                        truncated = true
                        return@processElementsWithWord false
                    }
                    val containingFile =
                        element.containingFile?.virtualFile
                            ?: return@processElementsWithWord true
                    val absoluteOffset = element.textRange.startOffset + offsetInElement
                    if (containingFile == target.declarationFile && absoluteOffset == target.declarationOffset) {
                        return@processElementsWithWord true
                    }

                    val psiFile =
                        PsiManager.getInstance(project).findFile(containingFile)
                            ?: return@processElementsWithWord true
                    val source = psiFile.text
                    val kind = RescriptReferenceClassifier.classify(source, absoluteOffset)
                    val (line, preview) = lineAndPreview(source, absoluteOffset)

                    collected.add(
                        ReferenceEntry(
                            file = containingFile,
                            offset = absoluteOffset,
                            lineNumber = line,
                            previewLine = preview,
                            kind = kind,
                        ),
                    )
                    true
                },
                scope,
                target.localName,
                UsageSearchContext.IN_CODE,
                true,
            )
        }
        return Result(entries = collected, truncated = truncated)
    }

    /**
     * Returns `(lineNumber, previewLine)` for [offset] in [source].
     * `lineNumber` is 1-based; `previewLine` is the trimmed source
     * line (or blank if the offset is past EOF).
     */
    internal fun lineAndPreview(
        source: String,
        offset: Int,
    ): Pair<Int, String> {
        if (offset < 0 || offset > source.length) return 0 to ""
        val line = RescriptOffsetUtils.lineNumberAt(source, offset)
        // Locate the line bounds.
        val lineStart = source.lastIndexOf('\n', offset - 1).let { if (it < 0) 0 else it + 1 }
        val lineEnd = source.indexOf('\n', offset).let { if (it < 0) source.length else it }
        return line to source.substring(lineStart, lineEnd).trim()
    }

    /**
     * Used by callers that want to verify that a reference still maps
     * to a current document line (e.g. after the file was edited).
     * Pure helper kept here for symmetry with [lineAndPreview].
     */
    @Suppress("unused")
    internal fun documentSnapshot(file: com.intellij.openapi.vfs.VirtualFile): String? =
        FileDocumentManager.getInstance().getDocument(file)?.text
}
