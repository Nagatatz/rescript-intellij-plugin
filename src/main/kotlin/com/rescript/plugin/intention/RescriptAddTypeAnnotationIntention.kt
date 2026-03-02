package com.rescript.plugin.intention

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.rescript.plugin.lang.psi.RescriptFile
import com.rescript.plugin.lsp.RescriptLspUtils

/**
 * Intention action that adds an explicit type annotation to a `let` binding
 * by querying the LSP hover information.
 *
 * Detects `let name = expr` patterns without type annotations and uses
 * the language server to resolve the type, transforming to `let name: <type> = expr`.
 *
 * Triggered via Alt+Enter > "Add type annotation".
 *
 * Implements [PsiElementBaseIntentionAction] for intention action support.
 */
class RescriptAddTypeAnnotationIntention : PsiElementBaseIntentionAction() {
    override fun getText(): String = "Add type annotation"

    override fun getFamilyName(): String = "Add type annotation"

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

        // Check it's a let binding without type annotation
        return LET_WITHOUT_TYPE_PATTERN.containsMatchIn(line)
    }

    override fun invoke(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ) {
        editor ?: return
        val document = editor.document
        val virtualFile = element.containingFile.virtualFile ?: return
        val offset = element.textRange.startOffset

        // Query LSP for type at cursor position
        val typeText = RescriptLspUtils.getHoverType(project, virtualFile, offset)
        if (typeText.isNullOrBlank()) return

        // Extract just the type from the hover response
        val cleanType = extractTypeFromHover(typeText) ?: return

        val lineNumber = document.getLineNumber(offset)
        val lineStart = document.getLineStartOffset(lineNumber)
        val lineEnd = document.getLineEndOffset(lineNumber)
        val line = document.text.substring(lineStart, lineEnd)

        val annotated = insertTypeAnnotation(line, cleanType) ?: return

        WriteCommandAction.runWriteCommandAction(project) {
            document.replaceString(lineStart, lineEnd, annotated)
        }
    }

    companion object {
        // Matches: let name = expr (without type annotation)
        // Does NOT match: let name: type = expr
        internal val LET_WITHOUT_TYPE_PATTERN =
            Regex("""let\s+(\w+)\s*=""")

        // Matches: let name: type = expr (already annotated)
        internal val LET_WITH_TYPE_PATTERN =
            Regex("""let\s+\w+\s*:""")

        // Matches: let name: type (in LSP hover response)
        private val HOVER_LET_TYPE_PATTERN =
            Regex("""let\s+\w+\s*:\s*(.+)""")

        /**
         * Inserts a type annotation into a let binding line.
         *
         * Transforms `let name = expr` to `let name: type = expr`.
         * Returns null if the line already has a type annotation or doesn't match.
         *
         * @param line the source line containing `let name = expr`
         * @param typeAnnotation the type to insert
         * @return the annotated line, or null if not applicable
         */
        internal fun insertTypeAnnotation(
            line: String,
            typeAnnotation: String,
        ): String? {
            // Skip if already has a type annotation
            if (LET_WITH_TYPE_PATTERN.containsMatchIn(line)) return null

            val match = LET_WITHOUT_TYPE_PATTERN.find(line) ?: return null
            val name = match.groupValues[1]

            // Replace "let name =" with "let name: type ="
            return line.replaceFirst(
                Regex("""let\s+${Regex.escape(name)}\s*="""),
                "let $name: $typeAnnotation =",
            )
        }

        /**
         * Extracts the type string from an LSP hover response.
         *
         * Strips `let name:` prefix and any trailing documentation.
         *
         * @param hoverText the raw hover response
         * @return the clean type string, or null if unparseable
         */
        internal fun extractTypeFromHover(hoverText: String): String? {
            // Try to extract from "let name: type" pattern
            val letMatch = HOVER_LET_TYPE_PATTERN.find(hoverText.trim())
            if (letMatch != null) {
                return letMatch.groupValues[1].trim()
            }
            // Fall back to using the whole text as a type
            val trimmed = hoverText.trim()
            return trimmed.ifEmpty { null }
        }
    }
}
