package com.rescript.plugin.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.rescript.plugin.lang.psi.RescriptFile

/**
 * Local inspection that suggests refactoring opportunities for common code patterns.
 *
 * Detects three categories of code smells:
 * 1. **Long functions** — `let` bindings whose body exceeds a threshold line count
 * 2. **Repeated pipe chains** — identical pipe expressions appearing multiple times in the same file
 * 3. **Nested switch** — `switch` expressions inside another `switch` arm
 *
 * Each suggestion is reported at INFORMATION severity (weak hint) without automatic quick-fixes,
 * encouraging the developer to consider manual refactoring.
 *
 * @see com.intellij.codeInspection.LocalInspectionTool
 */
class RescriptSuggestedRefactoringInspection : LocalInspectionTool() {
    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
    ): PsiElementVisitor =
        object : PsiElementVisitor() {
            override fun visitFile(file: PsiFile) {
                if (file !is RescriptFile) return
                val text = file.text
                val lines = text.lines()

                // Check for long functions
                for (finding in findLongFunctions(lines)) {
                    val offset = getLineOffset(text, finding.line)
                    val element = file.findElementAt(offset) ?: continue
                    holder.registerProblem(
                        element,
                        "Function body is ${finding.lineCount} lines long — consider extracting into smaller functions",
                    )
                }

                // Check for repeated pipe chains
                for (finding in findRepeatedPipeChains(lines)) {
                    val offset = getLineOffset(text, finding.line)
                    val element = file.findElementAt(offset) ?: continue
                    holder.registerProblem(
                        element,
                        "Pipe chain '${finding.chain}' appears ${finding.count} times — consider extracting a shared function",
                    )
                }

                // Check for nested switch
                for (finding in findNestedSwitch(lines)) {
                    val offset = getLineOffset(text, finding.line)
                    val element = file.findElementAt(offset) ?: continue
                    holder.registerProblem(
                        element,
                        "Nested switch expression — consider flattening or extracting a helper",
                    )
                }
            }
        }

    companion object {
        /** Minimum number of lines for a function body to trigger the "long function" suggestion. */
        internal const val LONG_FUNCTION_THRESHOLD = 20

        /** Data class representing a long function finding. */
        data class LongFunctionFinding(
            val line: Int,
            val lineCount: Int,
        )

        /** Data class representing a repeated pipe chain finding. */
        data class RepeatedPipeChainFinding(
            val line: Int,
            val chain: String,
            val count: Int,
        )

        /** Data class representing a nested switch finding. */
        data class NestedSwitchFinding(
            val line: Int,
        )

        // Pattern matching "let name = ..." at the start of a line
        private val LET_PATTERN = Regex("""^\s*let\s+\w+\s*=""")

        // Pattern matching a pipe chain segment
        private val PIPE_CHAIN_PATTERN = Regex("""->\s*\w+\.\w+\([^)]*\)""")

        /**
         * Finds `let` bindings whose body exceeds [LONG_FUNCTION_THRESHOLD] lines.
         *
         * @param lines the source file split into lines
         * @return list of findings with line number and body length
         */
        internal fun findLongFunctions(lines: List<String>): List<LongFunctionFinding> {
            val findings = mutableListOf<LongFunctionFinding>()
            var i = 0
            while (i < lines.size) {
                if (LET_PATTERN.containsMatchIn(lines[i])) {
                    val startLine = i
                    val indent = lines[i].takeWhile { it == ' ' }.length
                    var braceDepth = lines[i].count { it == '{' } - lines[i].count { it == '}' }
                    i++

                    // Track brace depth to find the end of the function body
                    if (braceDepth > 0) {
                        while (i < lines.size && braceDepth > 0) {
                            braceDepth += lines[i].count { it == '{' } - lines[i].count { it == '}' }
                            i++
                        }
                    } else {
                        // No braces — look for the next top-level declaration
                        while (i < lines.size) {
                            val lineIndent = lines[i].takeWhile { it == ' ' }.length
                            val trimmed = lines[i].trim()
                            if (trimmed.isEmpty()) {
                                i++
                                continue
                            }
                            if (lineIndent <= indent && LET_PATTERN.containsMatchIn(lines[i])) break
                            if (lineIndent <= indent && trimmed.startsWith("let ")) break
                            if (lineIndent <= indent && trimmed.startsWith("type ")) break
                            if (lineIndent <= indent && trimmed.startsWith("module ")) break
                            if (lineIndent <= indent && trimmed.startsWith("external ")) break
                            i++
                        }
                    }

                    val bodyLines = i - startLine
                    if (bodyLines > LONG_FUNCTION_THRESHOLD) {
                        findings.add(LongFunctionFinding(startLine, bodyLines))
                    }
                } else {
                    i++
                }
            }
            return findings
        }

        /**
         * Finds pipe chain patterns that appear more than once in the same file.
         *
         * @param lines the source file split into lines
         * @return list of findings for the second and subsequent occurrences
         */
        internal fun findRepeatedPipeChains(lines: List<String>): List<RepeatedPipeChainFinding> {
            // Collect all pipe chain patterns with their line numbers
            val chainOccurrences = mutableMapOf<String, MutableList<Int>>()

            for ((lineNum, line) in lines.withIndex()) {
                val chains = PIPE_CHAIN_PATTERN.findAll(line).toList()
                if (chains.size >= 2) {
                    val chainStr = chains.joinToString("") { it.value }.trim()
                    chainOccurrences.getOrPut(chainStr) { mutableListOf() }.add(lineNum)
                }
            }

            val findings = mutableListOf<RepeatedPipeChainFinding>()
            for ((chain, lineNums) in chainOccurrences) {
                if (lineNums.size >= 2) {
                    // Report on the second occurrence onward
                    for (lineNum in lineNums.drop(1)) {
                        val displayChain = if (chain.length > 40) chain.take(40) + "..." else chain
                        findings.add(RepeatedPipeChainFinding(lineNum, displayChain, lineNums.size))
                    }
                }
            }
            return findings
        }

        /**
         * Finds `switch` expressions nested inside another `switch` arm.
         *
         * @param lines the source file split into lines
         * @return list of findings for inner switch expressions
         */
        internal fun findNestedSwitch(lines: List<String>): List<NestedSwitchFinding> {
            val findings = mutableListOf<NestedSwitchFinding>()
            var switchDepth = 0

            for ((lineNum, line) in lines.withIndex()) {
                val trimmed = line.trim()
                if (trimmed.startsWith("switch ") || trimmed == "switch") {
                    if (switchDepth > 0) {
                        findings.add(NestedSwitchFinding(lineNum))
                    }
                    switchDepth++
                }
                // Track closing braces that end a switch block
                val opens = line.count { it == '{' }
                val closes = line.count { it == '}' }
                if (closes > opens && switchDepth > 0) {
                    switchDepth = maxOf(0, switchDepth - (closes - opens))
                }
            }
            return findings
        }

        /**
         * Returns the character offset of the beginning of the given line number.
         *
         * @param text the full source text
         * @param lineNumber the 0-based line number
         * @return the character offset
         */
        private fun getLineOffset(
            text: String,
            lineNumber: Int,
        ): Int {
            var offset = 0
            var currentLine = 0
            for (ch in text) {
                if (currentLine == lineNumber) return offset
                if (ch == '\n') currentLine++
                offset++
            }
            return offset
        }
    }
}
