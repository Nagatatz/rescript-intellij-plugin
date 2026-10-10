package com.rescript.plugin.refactor

import com.intellij.lang.Language
import com.intellij.lang.refactoring.InlineActionHandler
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.rescript.plugin.RescriptLanguage
import com.rescript.plugin.lang.psi.RescriptFile

/**
 * Inlines immutable constant expressions using a validated lexical binding plan.
 *
 * Unresolved binding forms and effectful expressions are unavailable. Top-level
 * declarations remain in place because other files may refer to their exports.
 * Local declarations and their references are edited in one undoable command.
 *
 * @see InlineActionHandler
 * @see RescriptInlinePlan
 */
class RescriptInlineHandler : InlineActionHandler() {
    override fun isEnabledForLanguage(language: Language): Boolean = language == RescriptLanguage

    override fun canInlineElement(element: PsiElement): Boolean {
        if (element.containingFile !is RescriptFile) return false
        val document =
            PsiDocumentManager.getInstance(element.project).getDocument(element.containingFile) ?: return false
        return RescriptInlinePlan.create(document.text, element.textRange.startOffset) != null
    }

    override fun inlineElement(
        project: Project,
        editor: Editor,
        element: PsiElement,
    ) {
        if (element.containingFile !is RescriptFile) return
        val document = PsiDocumentManager.getInstance(project).getDocument(element.containingFile) ?: return
        if (document !== editor.document) return
        val original = document.text
        val plan = RescriptInlinePlan.create(original, element.textRange.startOffset) ?: return
        WriteCommandAction.runWriteCommandAction(project, "Inline Constant", null, {
            // A plan may only edit the snapshot against which its offsets were validated.
            if (document.text != original) return@runWriteCommandAction
            for (offset in plan.usages.asReversed()) {
                document.replaceString(offset, offset + plan.name.length, "(${plan.value})")
            }
            if (plan.removeDeclaration) document.deleteString(plan.declarationStart, plan.declarationEnd)
        })
    }
}
