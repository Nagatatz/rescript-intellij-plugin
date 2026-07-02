package com.rescript.plugin.quickfix

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.rescript.plugin.lang.psi.RescriptFile

/**
 * Inspection that detects type holes (`_` in type annotation positions) in ReScript
 * code and provides quick fixes to replace them with concrete types.
 *
 * Offers common type suggestions (string, int, float, bool, unit, etc.) when
 * a type hole is detected in patterns like `let x: _ = ...`.
 *
 * @see LocalInspectionTool
 */
class RescriptTypeHoleQuickFix : LocalInspectionTool() {
    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
    ): PsiElementVisitor =
        object : PsiElementVisitor() {
            override fun visitFile(file: PsiFile) {
                if (file !is RescriptFile) return
                val text = file.text

                val holes = detectTypeHoles(text)
                for (hole in holes) {
                    val element = file.findElementAt(hole.offset) ?: continue
                    val fixes =
                        COMMON_TYPES
                            .map { type ->
                                ReplaceTypeHoleFix(type)
                            }.toTypedArray()

                    holder.registerProblem(
                        element,
                        "Type hole: consider specifying a concrete type",
                        *fixes,
                    )
                }
            }
        }

    /** Quick fix that replaces a type hole `_` with a specified concrete type. */
    private class ReplaceTypeHoleFix(
        private val replacement: String,
    ) : LocalQuickFix {
        override fun getFamilyName(): String = "Replace type hole"

        override fun getName(): String = "Replace '_' with '$replacement'"

        override fun applyFix(
            project: Project,
            descriptor: ProblemDescriptor,
        ) {
            val element = descriptor.psiElement ?: return
            val file = element.containingFile ?: return
            val document = file.viewProvider.document ?: return

            val fileText = document.text
            val elementOffset = element.textOffset

            // Find the type hole pattern containing this element
            val lineStart = fileText.lastIndexOf('\n', elementOffset).let { if (it < 0) 0 else it + 1 }
            val lineEnd = fileText.indexOf('\n', elementOffset).let { if (it < 0) fileText.length else it }
            val line = fileText.substring(lineStart, lineEnd)

            val holeMatch = TYPE_HOLE_PATTERN.find(line)
            if (holeMatch != null) {
                val matchStart = lineStart + holeMatch.range.first
                val matchEnd = lineStart + holeMatch.range.last + 1
                val newText = holeMatch.value.replace("_", replacement)
                document.replaceString(matchStart, matchEnd, newText)
            }
        }
    }

    companion object {
        /** Result of detecting a type hole in source text. */
        data class TypeHole(
            val offset: Int,
            val line: Int,
            val fullMatch: String,
        )

        // Pattern: `let name: _ =` or `type t = _`
        private val TYPE_HOLE_PATTERN = Regex(""":\s*_(?:\s*=|\s*$|\s*,|\s*\))""")

        // Pattern for type holes in type declarations
        private val TYPE_DECL_HOLE_PATTERN = Regex("""type\s+\w+\s*=\s*_""")

        /** Common types offered as replacements for type holes. */
        internal val COMMON_TYPES =
            listOf(
                "string",
                "int",
                "float",
                "bool",
                "unit",
                "array<'a>",
                "option<'a>",
                "result<'a, 'b>",
            )

        /**
         * Detects type holes (`_` in type positions) in source text.
         *
         * Line-start offsets are precomputed once as a running sum so that
         * per-match offset lookups are O(1) rather than O(n) each, giving
         * overall O(lines + matches) instead of O(lines × matches).
         *
         * @param sourceText the ReScript source code
         * @return list of detected type holes with their positions
         */
        internal fun detectTypeHoles(sourceText: String): List<TypeHole> {
            val holes = mutableListOf<TypeHole>()
            val lines = sourceText.lines()

            // Precompute the absolute offset of the first character of each line.
            // lineStartOffsets[i] = sum of (length + 1) for all lines before i.
            val lineStartOffsets = IntArray(lines.size)
            var runningOffset = 0
            for (i in lines.indices) {
                lineStartOffsets[i] = runningOffset
                runningOffset += lines[i].length + 1 // +1 for the line separator
            }

            for ((lineNum, line) in lines.withIndex()) {
                val lineOffset = lineStartOffsets[lineNum]

                // Check for `: _` pattern (type annotation holes)
                for (match in TYPE_HOLE_PATTERN.findAll(line)) {
                    // Find the offset of `_` within the match
                    val underscoreIndex = match.value.indexOf('_')
                    holes.add(
                        TypeHole(
                            offset = lineOffset + match.range.first + underscoreIndex,
                            line = lineNum + 1,
                            fullMatch = match.value,
                        ),
                    )
                }

                // Check for `type t = _` pattern
                for (match in TYPE_DECL_HOLE_PATTERN.findAll(line)) {
                    val underscoreIndex = match.value.lastIndexOf('_')
                    // Only add if not already captured by the first pattern
                    val candidateOffset = lineOffset + match.range.first + underscoreIndex
                    if (holes.none { it.offset == candidateOffset }) {
                        holes.add(
                            TypeHole(
                                offset = candidateOffset,
                                line = lineNum + 1,
                                fullMatch = match.value,
                            ),
                        )
                    }
                }
            }

            return holes
        }

        /**
         * Suggests common ReScript types appropriate for a given context.
         *
         * @return list of type suggestion strings
         */
        internal fun suggestTypes(): List<String> = COMMON_TYPES
    }
}
