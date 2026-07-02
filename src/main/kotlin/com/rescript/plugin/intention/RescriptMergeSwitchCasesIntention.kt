package com.rescript.plugin.intention

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.rescript.plugin.lang.RescriptTokenScanner
import com.rescript.plugin.lang.RescriptTokenTypes
import com.rescript.plugin.util.RescriptEditorUtils.replaceInWriteAction

/**
 * Intention action to merge switch cases that have identical bodies.
 *
 * Detects when multiple cases in a switch expression have the same body
 * and offers to merge them into `| Pattern1 | Pattern2 => body` format.
 * The wildcard pattern `_` is excluded from merging.
 *
 * Triggered via Alt+Enter > "Merge switch cases with same body".
 */
class RescriptMergeSwitchCasesIntention : RescriptBaseIntention() {
    override fun getText(): String = "Merge switch cases with same body"

    override fun isAvailableInRescript(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ): Boolean {
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

        document.replaceInWriteAction(project, switchRange.first, switchRange.second, newBlock)
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
                    '{' -> {
                        depth++
                    }

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
                    '{' -> {
                        depth++
                    }

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
         * Splits arms by walking the lexer tokens and treating a `PIPE`
         * (`|`) token at the switch body's brace depth 1 as the arm
         * separator. Because the lexer tokenizes `|>` as `PIPE_FORWARD`,
         * `||` as `L_OR`, and the `|` inside a nested `switch { ... }` at a
         * deeper brace depth, none of those are mistaken for a case
         * separator — unlike the previous line-anchored regex split.
         *
         * @param switchBlock the text of the switch expression including `switch ... { ... }`
         * @return list of parsed cases
         */
        internal fun parseSwitchCases(switchBlock: String): List<SwitchCase> {
            val cases = mutableListOf<SwitchCase>()
            val tokens = RescriptTokenScanner.tokenize(switchBlock)

            // Locate the switch body's opening `{` at paren depth 0.
            val switchIdx = tokens.indexOfFirst { it.type == RescriptTokenTypes.SWITCH }
            if (switchIdx < 0) return cases
            var parenDepth = 0
            var bodyBraceIdx = -1
            var i = switchIdx + 1
            while (i < tokens.size) {
                when (tokens[i].type) {
                    RescriptTokenTypes.LPAREN -> parenDepth++
                    RescriptTokenTypes.RPAREN -> if (parenDepth > 0) parenDepth--
                    RescriptTokenTypes.LBRACE -> if (parenDepth == 0) bodyBraceIdx = i
                }
                if (bodyBraceIdx >= 0) break
                i++
            }
            if (bodyBraceIdx < 0) return cases

            // Walk the body: an arm opens on a `|` at brace depth 1 and its
            // pattern/body split happens on the first `=>` at that depth.
            i = bodyBraceIdx + 1
            var braceDepth = 1
            parenDepth = 0
            var inArm = false
            var arrowSeen = false
            var patternStartIdx = -1
            var arrowIdx = -1
            var bodyStartIdx = -1

            // Finalizes the arm currently being accumulated by slicing its
            // pattern and body text from [switchBlock]. Skips arms without a
            // `=>` (e.g. an or-pattern's leading alternative), matching the
            // prior parser's behavior.
            fun finalize(endExclusive: Int) {
                if (!inArm || !arrowSeen) return
                val patternTokens = tokens.subList(patternStartIdx, arrowIdx)
                val bodyTokens = tokens.subList(bodyStartIdx, endExclusive)
                if (patternTokens.isEmpty() || bodyTokens.isEmpty()) return
                val pattern =
                    switchBlock.substring(patternTokens.first().start, patternTokens.last().end)
                val caseBody =
                    switchBlock.substring(bodyTokens.first().start, bodyTokens.last().end)
                cases.add(SwitchCase(pattern.trim(), caseBody.trim()))
            }

            while (i < tokens.size) {
                when (tokens[i].type) {
                    RescriptTokenTypes.LBRACE -> {
                        braceDepth++
                    }

                    RescriptTokenTypes.RBRACE -> {
                        braceDepth--
                        if (braceDepth == 0) {
                            finalize(i)
                            return cases
                        }
                    }

                    RescriptTokenTypes.LPAREN -> {
                        parenDepth++
                    }

                    RescriptTokenTypes.RPAREN -> {
                        if (parenDepth > 0) parenDepth--
                    }

                    RescriptTokenTypes.PIPE -> {
                        if (braceDepth == 1 && parenDepth == 0) {
                            when {
                                !inArm -> {
                                    // First arm of the switch opens.
                                    inArm = true
                                    arrowSeen = false
                                    patternStartIdx = i + 1
                                }

                                arrowSeen -> {
                                    // The current arm already has its `=>` body,
                                    // so this `|` starts a new arm.
                                    finalize(i)
                                    arrowSeen = false
                                    patternStartIdx = i + 1
                                }
                                // Otherwise this `|` precedes the arm's `=>`, i.e.
                                // it is an or-pattern alternative (`| A | B => …`);
                                // keep it inside the current pattern.
                            }
                        }
                    }

                    RescriptTokenTypes.ARROW -> {
                        if (braceDepth == 1 && parenDepth == 0 && inArm && !arrowSeen) {
                            arrowSeen = true
                            arrowIdx = i
                            bodyStartIdx = i + 1
                        }
                    }
                }
                i++
            }
            finalize(tokens.size)
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
