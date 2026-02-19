package com.rescript.plugin.intention

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.rescript.plugin.lang.psi.RescriptFile

/**
 * Base intention action that wraps selected text with a wrapper function.
 * Subclasses provide the wrapper name (e.g., "Some", "Ok", "Error").
 */
abstract class RescriptWrapWithIntention(
    private val wrapper: String,
) : PsiElementBaseIntentionAction() {
    override fun getText(): String = "Wrap with $wrapper(...)"

    override fun getFamilyName(): String = "Wrap with $wrapper(...)"

    override fun isAvailable(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ): Boolean {
        if (element.containingFile !is RescriptFile) return false
        val selectionModel = editor?.selectionModel ?: return false
        return selectionModel.hasSelection()
    }

    override fun invoke(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ) {
        val selectionModel = editor?.selectionModel ?: return
        val selectedText = selectionModel.selectedText ?: return
        val start = selectionModel.selectionStart
        val end = selectionModel.selectionEnd
        val replacement = "$wrapper($selectedText)"
        editor.document.replaceString(start, end, replacement)
        selectionModel.removeSelection()
    }
}

/** Wraps the selected expression with `Some(...)`. */
class RescriptWrapWithSomeIntention : RescriptWrapWithIntention("Some")

/** Wraps the selected expression with `Ok(...)`. */
class RescriptWrapWithOkIntention : RescriptWrapWithIntention("Ok")

/** Wraps the selected expression with `Error(...)`. */
class RescriptWrapWithErrorIntention : RescriptWrapWithIntention("Error")
