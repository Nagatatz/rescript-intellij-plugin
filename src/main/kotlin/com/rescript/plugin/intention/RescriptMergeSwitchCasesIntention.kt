package com.rescript.plugin.intention

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.rescript.plugin.lang.psi.RescriptFile

/**
 * Intention action to merge switch cases that have identical bodies.
 *
 * Detects when multiple cases in a switch expression have the same body
 * and offers to merge them into `| Pattern1 | Pattern2 => body` format.
 * The wildcard pattern `_` is excluded from merging.
 *
 * Triggered via Alt+Enter > "Merge switch cases with same body".
 */
class RescriptMergeSwitchCasesIntention : PsiElementBaseIntentionAction() {
    override fun getText(): String = "Merge switch cases with same body"

    override fun getFamilyName(): String = "Merge switch cases with same body"

    override fun isAvailable(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ): Boolean {
        if (element.containingFile !is RescriptFile) return false
        editor ?: return false

        // Check if we're inside a switch expression
        val switchBlock = findSwitchBlock(editor.document.text, editor.caretModel.offset) ?: return false

        // Check if there are cases with duplicate bodies
        val cases = parseSwitchCases(switchBlock)
        return hasDuplicateBodies(cases)
    }

    override fun invoke(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ) {
        editor ?: return
        val document = editor.document
        val text = document.text
        val offset = editor.caretModel.offset

        val switchRange = findSwitchRange(text, offset) ?: return
        val switchBlock = text.substring(switchRange.first, switchRange.second)
        val cases = parseSwitchCases(switchBlock)
        if (cases.isEmpty()) return

        val merged = mergeCases(cases)
        val newSwitchBody = buildMergedSwitch(merged)

        // Find the { after switch expr and replace body
        val bodyStart = switchBlock.indexOf('{')
        val bodyEnd = switchBlock.lastIndexOf('}')
        if (bodyStart < 0 || bodyEnd < 0) return

        val newBlock =
            switchBlock.substring(0, bodyStart + 1) +
                "\n" + newSwitchBody + "\n" +
                switchBlock.substring(bodyEnd)

        WriteCommandAction.runWriteCommandAction(project) {
            document.replaceString(switchRange.first, switchRange.second, newBlock)
        }
    }

    /** Represents a single case in a switch expression. */
    internal data class SwitchCase(
        val pattern: String,
        val body: String,
    )

    companion object {
        /**
         * Finds the switch block text containing the given offset.
         *
         * @param text the full document text
         * @param offset caret offset
         * @return the switch block text, or null if not inside a switch
         */
        internal fun findSwitchBlock(
            text: String,
            offset: Int,
        ): String? {
            // Search backwards for 'switch'
            val beforeCaret = text.substring(0, offset.coerceAtMost(text.length))
            val switchIndex = beforeCaret.lastIndexOf("switch")
            if (switchIndex < 0) return null

            // Find the matching closing brace
            val braceStart = text.indexOf('{', switchIndex)
            if (braceStart < 0) return null

            var depth = 0
            for (i in braceStart until text.length) {
                when (text[i]) {
                    '{' -> depth++
                    '}' -> {
                        depth--
                        if (depth == 0) {
                            return text.substring(switchIndex, i + 1)
                        }
                    }
                }
            }
            return null
        }

        internal fun findSwitchRange(
            text: String,
            offset: Int,
        ): Pair<Int, Int>? {
            val beforeCaret = text.substring(0, offset.coerceAtMost(text.length))
            val switchIndex = beforeCaret.lastIndexOf("switch")
            if (switchIndex < 0) return null

            val braceStart = text.indexOf('{', switchIndex)
            if (braceStart < 0) return null

            var depth = 0
            for (i in braceStart until text.length) {
                when (text[i]) {
                    '{' -> depth++
                    '}' -> {
                        depth--
                        if (depth == 0) return Pair(switchIndex, i + 1)
                    }
                }
            }
            return null
        }

        /**
         * Parses switch cases from a switch block text.
         *
         * @param switchBlock the text of the switch expression including `switch ... { ... }`
         * @return list of parsed cases
         */
        internal fun parseSwitchCases(switchBlock: String): List<SwitchCase> {
            val cases = mutableListOf<SwitchCase>()

            // Extract body between { and }
            val braceStart = switchBlock.indexOf('{')
            val braceEnd = switchBlock.lastIndexOf('}')
            if (braceStart < 0 || braceEnd < 0) return cases

            val body = switchBlock.substring(braceStart + 1, braceEnd).trim()

            // Split by | at the start of lines
            val caseTexts =
                body
                    .split(Regex("""(?m)^\s*\|"""))
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }

            for (caseText in caseTexts) {
                val arrowIndex = caseText.indexOf("=>")
                if (arrowIndex < 0) continue

                val pattern = caseText.substring(0, arrowIndex).trim()
                val caseBody = caseText.substring(arrowIndex + 2).trim()

                cases.add(SwitchCase(pattern, caseBody))
            }

            return cases
        }

        internal fun hasDuplicateBodies(cases: List<SwitchCase>): Boolean {
            val bodies =
                cases
                    .filter { it.pattern != "_" }
                    .map { it.body }
            return bodies.size != bodies.toSet().size
        }

        /**
         * Merges cases with identical bodies.
         *
         * @param cases the list of cases to merge
         * @return list of merged cases
         */
        internal fun mergeCases(cases: List<SwitchCase>): List<SwitchCase> {
            val merged = mutableListOf<SwitchCase>()
            val groupedByBody = mutableLinkedMapOf<String, MutableList<String>>()

            for (case in cases) {
                // Don't merge wildcard patterns
                if (case.pattern == "_") {
                    merged.add(case)
                    continue
                }
                groupedByBody.getOrPut(case.body) { mutableListOf() }.add(case.pattern)
            }

            for ((body, patterns) in groupedByBody) {
                val combinedPattern = patterns.joinToString(" | ")
                merged.add(SwitchCase(combinedPattern, body))
            }

            return merged
        }

        internal fun buildMergedSwitch(cases: List<SwitchCase>): String =
            cases.joinToString("\n") { case ->
                "  | ${case.pattern} => ${case.body}"
            }

        private fun <K, V> mutableLinkedMapOf(): LinkedHashMap<K, V> = LinkedHashMap()
    }
}
