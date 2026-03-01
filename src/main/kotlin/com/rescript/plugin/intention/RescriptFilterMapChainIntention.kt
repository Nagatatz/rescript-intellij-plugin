package com.rescript.plugin.intention

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.rescript.plugin.lang.psi.RescriptFile

/**
 * Intention action that converts a `filter(...)->map(...)` chain into a
 * single `filterMap(...)` call for Array and List modules.
 *
 * Detects patterns like `->Array.filter(pred)->Array.map(f)` on the
 * same line and transforms them into `->Array.filterMap(x => pred(x) ? Some(f(x)) : None)`.
 *
 * Triggered via Alt+Enter > "Convert filter+map to filterMap".
 *
 * Implements [PsiElementBaseIntentionAction] for intention action support.
 */
class RescriptFilterMapChainIntention : PsiElementBaseIntentionAction() {
    override fun getText(): String = "Convert filter+map to filterMap"

    override fun getFamilyName(): String = "Convert filter+map to filterMap"

    override fun isAvailable(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ): Boolean {
        if (element.containingFile !is RescriptFile) return false
        val document = editor?.document ?: return false
        val offset = element.textRange.startOffset
        val lineNumber = document.getLineNumber(offset)
        val lineStart = document.getLineStartOffset(lineNumber)
        val lineEnd = document.getLineEndOffset(lineNumber)
        val line = document.text.substring(lineStart, lineEnd)
        return FILTER_MAP_CHAIN_PATTERN.containsMatchIn(line)
    }

    override fun invoke(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ) {
        editor ?: return
        val document = editor.document
        val offset = element.textRange.startOffset
        val lineNumber = document.getLineNumber(offset)
        val lineStart = document.getLineStartOffset(lineNumber)
        val lineEnd = document.getLineEndOffset(lineNumber)
        val line = document.text.substring(lineStart, lineEnd)

        val converted = convertToFilterMap(line) ?: return

        WriteCommandAction.runWriteCommandAction(project) {
            document.replaceString(lineStart, lineEnd, converted)
        }
    }

    companion object {
        // Matches: ->Array.filter(pred)->Array.map(f) or ->List.filter(pred)->List.map(f)
        internal val FILTER_MAP_CHAIN_PATTERN =
            Regex("""->(Array|List)\.filter\(([^)]+)\)\s*->\1\.map\(([^)]+)\)""")

        /**
         * Converts a filter+map chain to a single filterMap call.
         *
         * Transforms `->Array.filter(pred)->Array.map(f)` into
         * `->Array.filterMap(x => pred(x) ? Some(f(x)) : None)`.
         *
         * @param line the source line containing the chain
         * @return the line with the chain converted, or null if no match
         */
        internal fun convertToFilterMap(line: String): String? {
            val match = FILTER_MAP_CHAIN_PATTERN.find(line) ?: return null
            val module = match.groupValues[1]
            val predicate = match.groupValues[2].trim()
            val mapper = match.groupValues[3].trim()

            val filterMapBody = "x => $predicate(x) ? Some($mapper(x)) : None"
            val replacement = "->$module.filterMap($filterMapBody)"

            return line.substring(0, match.range.first) +
                replacement +
                line.substring(match.range.last + 1)
        }
    }
}
