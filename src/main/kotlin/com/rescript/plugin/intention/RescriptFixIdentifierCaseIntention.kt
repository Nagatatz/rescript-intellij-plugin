package com.rescript.plugin.intention

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.rescript.plugin.lang.RescriptTokenTypes
import com.rescript.plugin.lang.psi.RescriptElementTypes
import com.rescript.plugin.lang.psi.RescriptFile

/**
 * Intention action that fixes identifier casing to match ReScript conventions.
 *
 * Module names should use PascalCase (first letter uppercase), while variable
 * names should use camelCase (first letter lowercase). This intention detects
 * identifiers that violate these conventions and offers to fix them.
 *
 * - LIDENT inside MODULE_DECLARATION -> convert to PascalCase
 * - UIDENT inside LET_DECLARATION -> convert to camelCase
 *
 * @see com.rescript.plugin.refactor.RescriptNamesValidator for identifier validation
 */
class RescriptFixIdentifierCaseIntention : PsiElementBaseIntentionAction() {
    override fun getText(): String = actionText

    override fun getFamilyName(): String = "Fix identifier case"

    override fun isAvailable(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ): Boolean {
        if (element.containingFile !is RescriptFile) return false
        val tokenType = element.node?.elementType ?: return false
        val text = element.text
        if (text.isEmpty()) return false

        return when (tokenType) {
            // Lowercase identifier in a module declaration -> should be PascalCase
            RescriptTokenTypes.LIDENT -> {
                if (isInsideModuleDeclaration(element)) {
                    actionText = "Convert to PascalCase"
                    true
                } else {
                    false
                }
            }

            // Uppercase identifier in a let declaration -> should be camelCase
            RescriptTokenTypes.UIDENT -> {
                if (isInsideLetDeclaration(element)) {
                    actionText = "Convert to camelCase"
                    true
                } else {
                    false
                }
            }

            else -> {
                false
            }
        }
    }

    override fun invoke(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ) {
        val document = editor?.document ?: return
        val text = element.text
        if (text.isEmpty()) return

        val tokenType = element.node?.elementType ?: return
        val newText =
            when (tokenType) {
                RescriptTokenTypes.LIDENT -> if (isInsideModuleDeclaration(element)) toPascalCase(text) else return
                RescriptTokenTypes.UIDENT -> if (isInsideLetDeclaration(element)) toCamelCase(text) else return
                else -> return
            }

        val range = element.textRange
        document.replaceString(range.startOffset, range.endOffset, newText)
    }

    // Mutable field to hold dynamic intention text based on context
    private var actionText: String = "Fix identifier case"

    companion object {
        /**
         * Converts a string to PascalCase by uppercasing the first character.
         *
         * @param text the identifier text
         * @return the PascalCase version
         */
        fun toPascalCase(text: String): String {
            if (text.isEmpty()) return text
            return text[0].uppercase() + text.substring(1)
        }

        /**
         * Converts a string to camelCase by lowercasing the first character.
         *
         * @param text the identifier text
         * @return the camelCase version
         */
        fun toCamelCase(text: String): String {
            if (text.isEmpty()) return text
            return text[0].lowercase() + text.substring(1)
        }

        /**
         * Checks whether the element is inside a MODULE_DECLARATION.
         *
         * @param element the PSI element to check
         * @return true if a parent module declaration is found
         */
        fun isInsideModuleDeclaration(element: PsiElement): Boolean {
            var current: PsiElement? = element.parent
            while (current != null) {
                if (current.node?.elementType == RescriptElementTypes.MODULE_DECLARATION) return true
                current = current.parent
            }
            return false
        }

        /**
         * Checks whether the element is inside a LET_DECLARATION.
         *
         * @param element the PSI element to check
         * @return true if a parent let declaration is found
         */
        fun isInsideLetDeclaration(element: PsiElement): Boolean {
            var current: PsiElement? = element.parent
            while (current != null) {
                if (current.node?.elementType == RescriptElementTypes.LET_DECLARATION) return true
                current = current.parent
            }
            return false
        }
    }
}
