package com.rescript.plugin.intention

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.rescript.plugin.lang.RescriptTokenTypes
import com.rescript.plugin.lsp.RescriptLspUtils
import com.rescript.plugin.util.RescriptEditorUtils.getLineRangeAt
import com.rescript.plugin.util.RescriptEditorUtils.getLineTextAt
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
 * @see RescriptLspUtils.parseVariantConstructors
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

        // Get the type of the variable from LSP
        val typeText = RescriptLspUtils.getHoverType(project, file, offset) ?: return
        val constructors = RescriptLspUtils.parseVariantConstructors(typeText)
        if (constructors.isEmpty()) return

        // Find the case line containing this variable
        val (lineStart, lineEnd) = document.getLineRangeAt(offset)
        val line = document.getLineTextAt(offset)

        // Parse the case: | pattern => body
        val arrowIndex = line.indexOf("=>")
        if (arrowIndex < 0) return

        val body = line.substring(arrowIndex + 2).trim()
        val indent = line.takeWhile { it == ' ' || it == '\t' }

        // Build expanded cases
        val expandedCases =
            constructors.joinToString("\n") { constructor ->
                val pattern = if (constructor.hasPayload) "${constructor.name}(_)" else constructor.name
                "$indent| $pattern => $body"
            }

        document.replaceInWriteAction(project, lineStart, lineEnd, expandedCases)
    }

    companion object {
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
