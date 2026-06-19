package com.rescript.plugin.intention

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.rescript.plugin.lsp.RescriptLspUtils

/**
 * Intention action that batch-inserts inferred `: T` type annotations into
 * every un-annotated top-level `let` binding of the current ReScript file.
 *
 * Availability is decided by cheap, LSP-free checks only: the language server
 * must be running and the file must contain at least one inferred top-level
 * `let`. The expensive per-binding hover resolution and the document mutation
 * are deferred to [RescriptBatchAnnotationRunner.run], which performs them off
 * the EDT and in a single undoable write action.
 *
 * Triggered via Alt+Enter > "Insert inferred type annotations". Shares its
 * runner with the Type Coverage Heat Map row action.
 *
 * @see RescriptBaseIntention for intention action support
 * @see RescriptBatchAnnotationRunner for the execution glue
 */
class RescriptBatchInsertInferredTypesIntention : RescriptBaseIntention() {
    override fun getText(): String = "Insert inferred type annotations"

    override fun isAvailableInRescript(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ): Boolean {
        editor ?: return false
        // Cheap gate only: the server must be up and there must be at least one
        // inferred top-level let. No hover is queried here — that happens in
        // invoke() off the EDT.
        if (RescriptLspUtils.getServer(project) == null) return false
        return RescriptBatchAnnotationPlanner.collectInferredLets(editor.document.text).isNotEmpty()
    }

    override fun invoke(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ) {
        editor ?: return
        val virtualFile = element.containingFile?.virtualFile ?: return
        RescriptBatchAnnotationRunner.run(project, editor.document, virtualFile)
    }
}
