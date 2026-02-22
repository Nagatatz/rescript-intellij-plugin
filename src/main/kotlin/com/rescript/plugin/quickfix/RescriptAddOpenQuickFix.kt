package com.rescript.plugin.quickfix

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.rescript.plugin.lang.RescriptTokenTypes
import com.rescript.plugin.lang.psi.RescriptFile

/**
 * Quick fix that adds an `open Module` statement to resolve an unresolved reference.
 *
 * When the caret is on a qualified identifier like `Module.value`, this action
 * adds `open Module` at the top of the file (after existing open statements)
 * and removes the module prefix from the identifier.
 *
 * Triggered via Alt+Enter > "Add open statement".
 *
 * @see RescriptQualifyReferenceQuickFix
 */
class RescriptAddOpenQuickFix : PsiElementBaseIntentionAction() {
    override fun getText(): String = "Add open statement"

    override fun getFamilyName(): String = "Add open statement"

    override fun isAvailable(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ): Boolean {
        if (element.containingFile !is RescriptFile) return false
        val tokenType = element.node?.elementType ?: return false

        // Available on uppercase identifiers (module names) followed by a dot
        if (tokenType != RescriptTokenTypes.UIDENT) return false

        // Check if there's a dot after this identifier (qualified access)
        val text = editor?.document?.text ?: return false
        val endOffset = element.textRange.endOffset
        if (endOffset >= text.length) return false

        val afterIdent = text.substring(endOffset).trimStart()
        return afterIdent.startsWith(".")
    }

    override fun invoke(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ) {
        editor ?: return
        val document = editor.document
        val text = document.text
        val moduleName = element.text

        // Find the last open statement to insert after it
        val insertOffset = findOpenInsertOffset(text)

        // Find the dot after the module name and remove "Module."
        val endOffset = element.textRange.endOffset
        val dotOffset = text.indexOf('.', endOffset - 1)
        if (dotOffset < 0) return

        WriteCommandAction.runWriteCommandAction(project) {
            // Remove "Module." from the qualified reference
            document.deleteString(element.textRange.startOffset, dotOffset + 1)

            // Insert open statement
            val openStatement = "open $moduleName\n"
            document.insertString(insertOffset, openStatement)
        }
    }

    companion object {
        /**
         * Finds the offset where a new open statement should be inserted.
         *
         * Inserts after the last existing open statement, or at the top of the file
         * after any leading comments.
         *
         * @param text the document text
         * @return the insertion offset
         */
        internal fun findOpenInsertOffset(text: String): Int {
            // Find the last "open" statement
            val openPattern = Regex("""(?m)^open\s+\S+""")
            val matches = openPattern.findAll(text).toList()
            if (matches.isNotEmpty()) {
                val lastMatch = matches.last()
                val lineEnd = text.indexOf('\n', lastMatch.range.last)
                return if (lineEnd >= 0) lineEnd + 1 else text.length
            }

            // No open statements found; insert after leading comments/blank lines
            val lines = text.lines()
            var offset = 0
            for (line in lines) {
                val trimmed = line.trim()
                if (trimmed.isEmpty() || trimmed.startsWith("//") || trimmed.startsWith("/*")) {
                    offset += line.length + 1
                } else {
                    break
                }
            }
            return offset
        }
    }
}
