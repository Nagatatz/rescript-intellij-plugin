package com.rescript.plugin.intention

import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.rescript.plugin.lang.RescriptTokenTypes
import com.rescript.plugin.lang.psi.RescriptFile

/**
 * Removes a value qualifier only when a direct local module and preceding open prove identity.
 * This [RescriptBaseIntention] refuses external symbols, shadowing and unsupported scopes;
 * availability and invocation share the same source proof instead of file-wide open matching.
 */
class RescriptRemoveQualifierIntention : RescriptBaseIntention() {
    private var prepared: Prepared? = null

    override fun getText(): String = "Remove redundant qualifier"

    override fun isAvailableInRescript(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ): Boolean {
        prepared = null
        editor ?: return false
        if (element.node.elementType != RescriptTokenTypes.UIDENT) return false
        val file = element.containingFile
        if (file.viewProvider.document !== editor.document) return false
        val offset = editor.caretModel.offset
        val plan = RescriptQualifierRemovalPlanner.plan(editor.document.text, offset) ?: return false
        prepared = Prepared(plan, file, editor.document, offset)
        return true
    }

    override fun invoke(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ) {
        editor ?: return
        val file = element.containingFile
        if (file !is RescriptFile || !file.isValid || file.viewProvider.document !== editor.document) return
        val offset = editor.caretModel.offset
        val proof = prepared
        if (proof != null &&
            (proof.file !== file || proof.document !== editor.document || proof.offset != offset)
        ) {
            return
        }
        val plan = proof?.plan ?: RescriptQualifierRemovalPlanner.plan(editor.document.text, offset) ?: return
        val replacement = plan.replacementFor(editor.document.text) ?: return
        // Intention invocation already runs in the platform's write command and undo transaction.
        editor.document.replaceString(0, editor.document.textLength, replacement)
        prepared = null
    }

    /** Binds the immutable source proof to the exact editor, PSI file and caret. */
    private data class Prepared(
        val plan: RescriptQualifierRemovalPlanner.Plan,
        val file: PsiFile,
        val document: Document,
        val offset: Int,
    )
}
