package com.rescript.plugin.intention

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.rescript.plugin.lsp.RescriptLspUtils
import com.rescript.plugin.util.RescriptEditorUtils.insertInWriteAction

/**
 * Intention action that fills the arms missing from a partial `switch`
 * expression. Triggered with Alt+Enter while the caret is on the `switch`
 * keyword or somewhere inside the scrutinee.
 *
 * The intention queries LSP hover for the scrutinee's type, parses the
 * variant constructors, computes the set difference against the constructors
 * already covered by existing arms, and inserts a skeleton arm for each
 * missing constructor (`| Name(_) => todo` or `| Name => todo`) just before
 * the closing `}` of the switch.
 *
 * Logic-free wrapper around [RescriptMissingArmsBuilder] — everything that
 * does not require an editor/document round-trip lives in the builder so it
 * can be tested without an IDE fixture.
 *
 * @see RescriptMissingArmsBuilder
 * @see RescriptLspUtils.parseVariantConstructors
 */
class RescriptAddMissingSwitchArmsIntention : RescriptBaseIntention() {
    override fun getText(): String = "Add missing switch arms"

    override fun isAvailableInRescript(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ): Boolean {
        val doc = editor?.document ?: return false
        val source = doc.text
        val offset = element.textRange.startOffset
        if (!RescriptMissingArmsBuilder.isInsideSwitch(source, offset)) return false
        if (RescriptMissingArmsBuilder.hasWildcardArm(source, offset)) return false
        return true
    }

    override fun invoke(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ) {
        editor ?: return
        val doc = editor.document
        val source = doc.text
        val offset = element.textRange.startOffset
        val virtualFile = element.containingFile?.virtualFile ?: return

        val scrutineeOffset = RescriptMissingArmsBuilder.scrutineeOffset(source, offset) ?: return
        val typeText = RescriptLspUtils.getHoverType(project, virtualFile, scrutineeOffset) ?: return
        val constructors = RescriptLspUtils.parseVariantConstructors(typeText)
        if (constructors.isEmpty()) return

        val result = RescriptMissingArmsBuilder.computeMissing(source, offset, constructors) ?: return
        doc.insertInWriteAction(project, result.insertOffset, result.insertText)
    }
}
