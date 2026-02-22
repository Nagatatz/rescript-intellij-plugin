package com.rescript.plugin.intention

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.rescript.plugin.lang.RescriptTokenTypes
import com.rescript.plugin.lang.psi.RescriptFile

/**
 * Intention action to remove redundant module qualifiers.
 *
 * When a module is already opened via `open Module`, qualified references
 * like `Module.value` can be simplified to just `value`. This intention
 * detects such redundant qualifications and offers to remove them.
 *
 * Triggered via Alt+Enter > "Remove redundant qualifier".
 *
 * @see com.rescript.plugin.imports.RescriptImportOptimizer
 */
class RescriptRemoveQualifierIntention : PsiElementBaseIntentionAction() {
    override fun getText(): String = "Remove redundant qualifier"

    override fun getFamilyName(): String = "Remove redundant qualifier"

    override fun isAvailable(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ): Boolean {
        if (element.containingFile !is RescriptFile) return false
        val tokenType = element.node?.elementType ?: return false

        // Available on uppercase identifiers (module qualifiers)
        if (tokenType != RescriptTokenTypes.UIDENT) return false

        val text = editor?.document?.text ?: return false
        val endOffset = element.textRange.endOffset

        // Check if followed by a dot (qualified access pattern)
        if (endOffset >= text.length) return false
        if (text[endOffset] != '.') return false

        // Check if the module is already opened in this file
        val moduleName = element.text
        return isModuleOpened(text, moduleName)
    }

    override fun invoke(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ) {
        editor ?: return
        val document = editor.document
        val endOffset = element.textRange.endOffset

        // Remove "Module." (the identifier + the dot)
        WriteCommandAction.runWriteCommandAction(project) {
            document.deleteString(element.textRange.startOffset, endOffset + 1)
        }
    }

    companion object {
        /**
         * Checks if a module is opened in the file.
         *
         * @param text the document text
         * @param moduleName the module name to check
         * @return true if `open ModuleName` exists in the file
         */
        internal fun isModuleOpened(
            text: String,
            moduleName: String,
        ): Boolean {
            val pattern = Regex("""(?m)^open\s+$moduleName\s*$""")
            return pattern.containsMatchIn(text)
        }
    }
}
