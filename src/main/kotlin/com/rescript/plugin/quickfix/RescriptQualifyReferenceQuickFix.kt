package com.rescript.plugin.quickfix

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.rescript.plugin.lang.RescriptTokenTypes
import com.rescript.plugin.lang.psi.RescriptFile

/**
 * Quick fix that qualifies an unresolved reference with a module prefix.
 *
 * When the caret is on an unresolved lowercase identifier, this action
 * attempts to find the containing module and adds the module qualification
 * prefix (e.g., `value` becomes `Module.value`).
 *
 * This works with the file's open statements to suggest possible module
 * qualifications for identifiers that would be resolved with a qualifier.
 *
 * Triggered via Alt+Enter > "Qualify with module name".
 *
 * @see RescriptAddOpenQuickFix
 */
class RescriptQualifyReferenceQuickFix : PsiElementBaseIntentionAction() {
    override fun getText(): String = "Qualify with module name"

    override fun getFamilyName(): String = "Qualify with module name"

    override fun isAvailable(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ): Boolean {
        if (element.containingFile !is RescriptFile) return false
        val tokenType = element.node?.elementType ?: return false

        // Available on lowercase identifiers (potential unresolved references)
        if (tokenType != RescriptTokenTypes.LIDENT) return false

        // Check that this is not already qualified (no dot before it)
        val text = editor?.document?.text ?: return false
        val startOffset = element.textRange.startOffset
        if (startOffset > 0) {
            val charBefore = text[startOffset - 1]
            if (charBefore == '.') return false
        }

        return true
    }

    override fun invoke(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ) {
        editor ?: return
        val document = editor.document
        val text = document.text
        val identifier = element.text

        // Find potential module names from open statements in the file
        val modules = collectOpenModules(text)
        if (modules.isEmpty()) return

        // Use the first available module as the qualifier
        val moduleName = modules.firstOrNull() ?: return

        WriteCommandAction.runWriteCommandAction(project) {
            document.replaceString(
                element.textRange.startOffset,
                element.textRange.endOffset,
                "$moduleName.$identifier",
            )
        }
    }

    companion object {
        /**
         * Collects module names from open statements in the document.
         *
         * @param text the document text
         * @return list of module names from open statements
         */
        internal fun collectOpenModules(text: String): List<String> {
            val modules = mutableListOf<String>()
            val openPattern = Regex("""(?m)^open\s+(\S+)""")
            for (match in openPattern.findAll(text)) {
                modules.add(match.groupValues[1])
            }
            return modules
        }
    }
}
