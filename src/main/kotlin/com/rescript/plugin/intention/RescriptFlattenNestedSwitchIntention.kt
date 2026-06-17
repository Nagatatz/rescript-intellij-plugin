package com.rescript.plugin.intention

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.rescript.plugin.util.RescriptEditorUtils.replaceInWriteAction

/**
 * Intention action that flattens a nested `switch` arm into a single level.
 *
 * When the caret is on an outer arm whose body is solely a `switch` over the
 * arm's single binding, this collapses the two levels by folding each inner
 * pattern into the binding position. For example
 * `| Some(y) => switch y { | Some(z) => a | None => b }`
 * becomes `| Some(Some(z)) => a` and `| Some(None) => b`.
 *
 * The analysis is delegated to [RescriptNestedSwitchFlattener]; this class only
 * wires the [RescriptNestedSwitchFlattener.plan] result to the editor document.
 * Triggered via Alt+Enter > "Flatten nested switch".
 *
 * @see RescriptBaseIntention
 * @see RescriptNestedSwitchFlattener
 */
class RescriptFlattenNestedSwitchIntention : RescriptBaseIntention() {
    override fun getText(): String = "Flatten nested switch"

    override fun isAvailableInRescript(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ): Boolean {
        editor ?: return false
        return RescriptNestedSwitchFlattener.plan(editor.document.text, editor.caretModel.offset) != null
    }

    override fun invoke(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ) {
        editor ?: return
        val document = editor.document
        val plan = RescriptNestedSwitchFlattener.plan(document.text, editor.caretModel.offset) ?: return
        document.replaceInWriteAction(project, plan.replaceStart, plan.replaceEnd, plan.replacementText)
    }
}
