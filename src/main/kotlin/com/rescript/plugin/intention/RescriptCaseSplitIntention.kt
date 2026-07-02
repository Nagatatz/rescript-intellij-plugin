package com.rescript.plugin.intention

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.rescript.plugin.lang.RescriptTokenTypes
import com.rescript.plugin.lsp.RescriptLspSignatureParser
import com.rescript.plugin.lsp.RescriptLspUtils
import com.rescript.plugin.narrowing.RescriptSwitchArmCollector
import com.rescript.plugin.util.RescriptEditorUtils.replaceInWriteAction

/**
 * Intention action to split a pattern variable into all variant constructors.
 *
 * When the caret is on a pattern variable in a switch case, this intention
 * queries LSP hover for the variable's type and expands it into individual
 * cases for each constructor of the variant type.
 *
 * Example: `| x => body` with x: option<int> becomes:
 * ```
 * | Some(_) => body
 * | None => body
 * ```
 *
 * Triggered via Alt+Enter > "Split into constructor cases".
 *
 * @see RescriptLspSignatureParser.parseVariantConstructors
 */
class RescriptCaseSplitIntention : RescriptBaseIntention() {
    override fun getText(): String = "Split into constructor cases"

    override fun isAvailableInRescript(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ): Boolean {
        val tokenType = element.node?.elementType ?: return false
        // Available on lowercase identifiers (pattern variables)
        if (tokenType != RescriptTokenTypes.LIDENT) return false

        // Check if inside a switch case pattern (before =>)
        return isInsideSwitchPattern(editor?.document?.text ?: return false, element.textRange.startOffset)
    }

    override fun invoke(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ) {
        editor ?: return
        val file = element.containingFile?.virtualFile ?: return
        val offset = element.textRange.startOffset
        val document = editor.document
        val text = document.text

        // Get the type of the variable from LSP
        val typeText = RescriptLspUtils.getHoverType(project, file, offset) ?: return
        val constructors = RescriptLspSignatureParser.parseVariantConstructors(typeText)
        if (constructors.isEmpty()) return

        // Determine the full arm range (its body may span multiple physical
        // lines) so the whole arm is replaced rather than just the caret line.
        val target = findSplitTarget(text, offset) ?: return

        // Build expanded cases, reusing the full multi-line body text.
        val expandedCases =
            constructors.joinToString("\n") { constructor ->
                val pattern = if (constructor.hasPayload) "${constructor.name}(_)" else constructor.name
                "${target.indent}| $pattern => ${target.body}"
            }

        document.replaceInWriteAction(project, target.replaceStart, target.replaceEnd, expandedCases)
    }

    companion object {
        /**
         * Describes where and how to replace a switch arm when expanding a
         * pattern variable into one arm per variant constructor.
         *
         * @property replaceStart offset of the start of the arm's leading line
         *   (indentation + `|`), where the replacement begins
         * @property replaceEnd exclusive offset just after the last character
         *   of the arm body (trailing whitespace/newlines excluded)
         * @property body the arm body text, verbatim from source but trimmed,
         *   possibly spanning multiple physical lines
         * @property indent leading whitespace of the arm's `|` line, reused as
         *   the prefix of each generated arm
         */
        internal data class SplitTarget(
            val replaceStart: Int,
            val replaceEnd: Int,
            val body: String,
            val indent: String,
        )

        /**
         * Computes the full replacement range for the switch arm whose
         * pattern contains [offset], capturing the entire arm body even when
         * it spans multiple physical lines.
         *
         * The arm is located by walking lexer tokens via
         * [RescriptSwitchArmCollector], so the body extent runs from just
         * after `=>` up to (but excluding) the trailing whitespace before the
         * next `|` at the same brace depth or the closing `}`.
         *
         * @param text full document text
         * @param offset caret offset, expected to sit on a pattern variable
         * @return the replacement target, or `null` if no arm pattern contains
         *   [offset] or the body is empty
         */
        internal fun findSplitTarget(
            text: String,
            offset: Int,
        ): SplitTarget? {
            val arm =
                RescriptSwitchArmCollector
                    .collect(text)
                    .filter { offset >= it.patternOffset && offset < it.arrowOffset }
                    .minByOrNull { it.arrowOffset - it.patternOffset }
                    ?: return null

            val rawBody = text.substring(arm.arrowOffset, arm.bodyEndOffset)
            val body = rawBody.trim()
            if (body.isEmpty()) return null
            // Exclude trailing whitespace/newlines so the replacement ends at
            // the last body character (the newline before the next arm stays).
            val bodyContentEnd = arm.arrowOffset + rawBody.trimEnd().length

            val lineStart =
                text.lastIndexOf('\n', (offset - 1).coerceAtLeast(0)).let { if (it < 0) 0 else it + 1 }
            val lineEnd = text.indexOf('\n', lineStart).let { if (it < 0) text.length else it }
            val line = text.substring(lineStart, lineEnd)
            val indent = line.takeWhile { it == ' ' || it == '\t' }

            return SplitTarget(lineStart, bodyContentEnd, body, indent)
        }

        /**
         * Checks if the given offset is inside a switch case pattern (before =>).
         *
         * @param text document text
         * @param offset the caret offset
         * @return true if inside a switch pattern
         */
        internal fun isInsideSwitchPattern(
            text: String,
            offset: Int,
        ): Boolean {
            // Check if there's a | before us and => after us on the same logical line
            val lineStart = text.lastIndexOf('\n', offset - 1).let { if (it < 0) 0 else it + 1 }
            val lineEnd = text.indexOf('\n', offset).let { if (it < 0) text.length else it }
            val line = text.substring(lineStart, lineEnd)
            val posInLine = offset - lineStart

            val pipeIndex = line.indexOf('|')
            val arrowIndex = line.indexOf("=>")

            return pipeIndex >= 0 && arrowIndex > 0 && posInLine > pipeIndex && posInLine < arrowIndex
        }
    }
}
