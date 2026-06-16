package com.rescript.plugin.intention

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.rescript.plugin.imports.RescriptImportUtil
import com.rescript.plugin.imports.RescriptModuleMemberExtractor
import com.rescript.plugin.imports.RescriptOpenExpansionPlanner
import com.rescript.plugin.lang.psi.RescriptElementTypes

/**
 * Intention action that expands a project-local `open M` back into explicit `M.name` references.
 *
 * Triggered via Alt+Enter while the caret is on the module name of an `open M` statement whose
 * module is defined inside the project (`M.res` / `M.resi`). The intention reads the opened
 * module's top-level member names, rewrites every unqualified reference that the `open` brought
 * into scope into its qualified `M.name` form, and removes the now-redundant `open` statement.
 * It is the inverse of [RescriptRemoveQualifierIntention].
 *
 * The expansion is purely syntactic and LSP-independent: it only handles single-segment modules
 * that resolve to a project source file, so library modules such as `Belt` (which live in
 * `node_modules` and have no in-project `.res`) are intentionally out of scope and the intention
 * is not offered for them.
 *
 * @see RescriptOpenExpansionPlanner which computes the concrete edits
 * @see RescriptModuleMemberExtractor which supplies the opened module's member names
 * @see com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
 */
class RescriptExpandOpenQualifierIntention : RescriptBaseIntention() {
    override fun getText(): String = "Expand open into qualified references"

    override fun isAvailableInRescript(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ): Boolean {
        editor ?: return false
        val moduleName = resolveSingleSegmentModule(element) ?: return false
        // Only project-local modules are expandable; library modules have no in-project source.
        return findModuleFile(project, moduleName) != null
    }

    override fun invoke(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ) {
        editor ?: return
        val openStmt =
            PsiTreeUtil.findFirstParent(element) { it.node?.elementType == RescriptElementTypes.OPEN_STATEMENT }
                ?: return
        val moduleName = RescriptImportUtil.extractModulePath(openStmt)
        if (moduleName.isEmpty() || moduleName.contains('.')) return

        val moduleFile = findModuleFile(project, moduleName) ?: return
        val memberNames =
            PsiManager.getInstance(project).findFile(moduleFile)?.let {
                RescriptModuleMemberExtractor.extractTopLevelNames(it.text)
            } ?: return

        val document = editor.document
        val plan =
            RescriptOpenExpansionPlanner.plan(
                document.text,
                moduleName,
                memberNames,
                openStmt.textRange,
            )

        if (!confirmExpansion(project, moduleName, plan.prefixInsertOffsets.size)) return

        val prefix = "$moduleName."
        WriteCommandAction.runWriteCommandAction(project, text, null, {
            // Insert qualifiers from the highest offset down so earlier insertions never shift
            // the offsets of later ones; the open-statement deletion (lowest offset) runs last.
            for (offset in plan.prefixInsertOffsets.sortedDescending()) {
                document.insertString(offset, prefix)
            }
            document.deleteString(plan.openDeletionRange.startOffset, plan.openDeletionRange.endOffset)
        })
    }

    /**
     * Asks the user to confirm the rewrite, reporting how many references will be qualified.
     *
     * @return true when the user approves the expansion
     */
    private fun confirmExpansion(
        project: Project,
        moduleName: String,
        occurrenceCount: Int,
    ): Boolean {
        val detail =
            if (occurrenceCount == 0) {
                "No references to qualify; the redundant `open $moduleName` will be removed."
            } else {
                "$occurrenceCount reference(s) will be qualified with `$moduleName.` and the `open $moduleName` will be removed."
            }
        val choice =
            Messages.showYesNoDialog(
                project,
                detail,
                "Expand open $moduleName",
                Messages.getQuestionIcon(),
            )
        return choice == Messages.YES
    }

    /**
     * Returns the single-segment module name when [element] is the module identifier of an
     * `open M` statement whose path has no dotted segments, or null otherwise.
     */
    private fun resolveSingleSegmentModule(element: PsiElement): String? {
        val openStmt =
            PsiTreeUtil.findFirstParent(element) {
                it.node?.elementType == RescriptElementTypes.OPEN_STATEMENT
            } ?: return null
        val moduleName = RescriptImportUtil.extractModulePath(openStmt)
        if (moduleName.isEmpty() || moduleName.contains('.')) return null
        return moduleName
    }

    /**
     * Locates the project source file (`<moduleName>.res` or `<moduleName>.resi`) that defines
     * the opened module.
     *
     * @return the module's virtual file, or null when no in-project source exists
     */
    private fun findModuleFile(
        project: Project,
        moduleName: String,
    ): VirtualFile? {
        val scope = GlobalSearchScope.projectScope(project)
        return FilenameIndex.getVirtualFilesByName("$moduleName.res", scope).firstOrNull()
            ?: FilenameIndex.getVirtualFilesByName("$moduleName.resi", scope).firstOrNull()
    }
}
