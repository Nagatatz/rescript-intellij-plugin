package com.rescript.plugin.intention

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement

/**
 * Base intention action that wraps selected text with a wrapper function.
 * Subclasses provide the wrapper name (e.g., "Some", "Ok", "Error").
 */
abstract class RescriptWrapWithIntention(
    private val wrapper: String,
) : RescriptBaseIntention() {
    override fun getText(): String = "Wrap with $wrapper(...)"

    override fun isAvailableInRescript(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ): Boolean {
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
