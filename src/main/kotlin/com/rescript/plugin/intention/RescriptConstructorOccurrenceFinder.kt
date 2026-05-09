package com.rescript.plugin.intention

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiSearchHelper
import com.intellij.psi.search.UsageSearchContext
import com.rescript.plugin.RescriptFileType
import com.rescript.plugin.RescriptInterfaceFileType

/**
 * Locates every constructor / pattern / module-qualified-tail
 * occurrence of [name] inside the project's `.res` and `.resi`
 * files. Backed by IntelliJ's word index
 * (`PsiSearchHelper.processElementsWithWord`) so the heavy text
 * scan is delegated to the platform; per-hit classification is
 * delegated to [RescriptConstructorOccurrenceClassifier].
 *
 * The IO entry point ([findAll]) does the index lookup; the pure
 * helper ([findInText]) drives the classifier against a single
 * file's text and is what the unit tests exercise. Splitting the
 * two keeps the heuristic test-covered without dragging an
 * IntelliJ Platform fixture into every case.
 *
 * A [HARD_CAP] cap stops the search short on widely-used names so
 * the rename intention can refuse to operate on enormous diffs.
 */
object RescriptConstructorOccurrenceFinder {
    /** Hard cap above which the rename intention bails out entirely. */
    const val HARD_CAP = 500

    /**
     * Result wrapper exposing the matched occurrences alongside a
     * truncation flag — the rename intention turns the flag into a
     * "too many occurrences, use Shift+F6 instead" error.
     */
    data class Result(
        val occurrences: List<RescriptConstructorOccurrence>,
        val truncated: Boolean,
    )

    /**
     * Searches [project] for occurrences of [name] (treated as a UIDENT)
     * inside `.res` / `.resi` files under
     * [GlobalSearchScope.projectScope] and returns up to [HARD_CAP] hits.
     *
     * Must be called off the EDT — runs a read action internally.
     */
    fun findAll(
        project: Project,
        name: String,
    ): Result {
        val collected = mutableListOf<RescriptConstructorOccurrence>()
        var truncated = false
        val helper = PsiSearchHelper.getInstance(project)
        val scope = GlobalSearchScope.projectScope(project)
        val app = ApplicationManager.getApplication()

        app.runReadAction {
            helper.processElementsWithWord(
                { element, offsetInElement ->
                    if (collected.size >= HARD_CAP) {
                        truncated = true
                        return@processElementsWithWord false
                    }
                    val virtualFile =
                        element.containingFile?.virtualFile
                            ?: return@processElementsWithWord true
                    if (virtualFile.fileType != RescriptFileType &&
                        virtualFile.fileType != RescriptInterfaceFileType
                    ) {
                        return@processElementsWithWord true
                    }
                    val source = element.containingFile.text
                    val absoluteOffset = element.textRange.startOffset + offsetInElement
                    val occurrence = classifyHit(virtualFile, source, absoluteOffset, name)
                    if (occurrence != null) collected.add(occurrence)
                    true
                },
                scope,
                name,
                UsageSearchContext.IN_CODE,
                true,
            )
        }
        return Result(occurrences = collected, truncated = truncated)
    }

    /**
     * Pure helper used by the unit tests: walks [text] looking for the
     * UIDENT [name] and emits one occurrence per qualifying hit. Any
     * substring matches that aren't standalone identifier boundaries
     * (e.g. `FooBar` when searching for `Foo`) are filtered out so the
     * caller doesn't have to teach the word-boundary rule to its
     * fixtures.
     */
    internal fun findInText(
        file: VirtualFile,
        text: String,
        name: String,
    ): List<RescriptConstructorOccurrence> {
        val results = mutableListOf<RescriptConstructorOccurrence>()
        var index = 0
        while (true) {
            val hit = text.indexOf(name, index)
            if (hit < 0) break
            index = hit + name.length
            // Word-boundary check: the surrounding characters must not be
            // valid identifier characters, otherwise we'd rename `FooBar`
            // when the user asked for `Foo`.
            val before = if (hit == 0) ' ' else text[hit - 1]
            val after = if (index >= text.length) ' ' else text[index]
            if (isIdentifierChar(before) || isIdentifierChar(after)) continue
            val occurrence = classifyHit(file, text, hit, name) ?: continue
            results.add(occurrence)
        }
        return results
    }

    /**
     * Runs the classifier against a single hit and decides whether the
     * occurrence should participate in the rename. Returns `null` when
     * the classifier reports `OTHER`.
     */
    private fun classifyHit(
        file: VirtualFile,
        source: String,
        offset: Int,
        name: String,
    ): RescriptConstructorOccurrence? {
        val kind = RescriptConstructorOccurrenceClassifier.classifyAt(source, offset)
        if (kind == ConstructorOccurrenceKind.OTHER) return null
        val range = TextRange(offset, offset + name.length)
        return RescriptConstructorOccurrence(file = file, range = range, kind = kind)
    }

    private fun isIdentifierChar(ch: Char): Boolean = ch.isLetterOrDigit() || ch == '_' || ch == '\''
}
