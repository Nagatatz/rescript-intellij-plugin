package com.rescript.plugin.navigation

import com.intellij.codeInsight.CodeInsightActionHandler
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.rescript.plugin.lang.psi.RescriptElementTypes
import com.rescript.plugin.lang.psi.RescriptPsiUtils

/**
 * Goto Super handler for ReScript that navigates from a declaration
 * in a `.res` file to the corresponding declaration in the `.resi` interface,
 * or vice versa.
 *
 * Triggered by Ctrl+U. Finds the matching declaration by name and type
 * in the counterpart file.
 *
 * @see RescriptSwitchFileAction for simple file switching (Alt+O)
 */
class RescriptGotoSuperHandler : CodeInsightActionHandler {
    override fun invoke(
        project: Project,
        editor: Editor,
        file: PsiFile,
    ) {
        val virtualFile = file.virtualFile ?: return
        val ext = virtualFile.extension ?: return

        val targetExt =
            when (ext) {
                "res" -> "resi"
                "resi" -> "res"
                else -> return
            }

        val targetVirtualFile =
            virtualFile.parent?.findChild("${virtualFile.nameWithoutExtension}.$targetExt")
                ?: return

        val targetPsiFile = PsiManager.getInstance(project).findFile(targetVirtualFile) ?: return

        // Find the declaration at the caret position
        val offset = editor.caretModel.offset
        val element = file.findElementAt(offset)
        val declaration =
            element?.let {
                RescriptPsiUtils.findEnclosingDeclaration(it, RescriptPsiUtils.NAVIGABLE_TYPES)
            }

        if (declaration != null) {
            val name = RescriptPsiUtils.extractName(declaration)
            val declType = declaration.node.elementType

            // Search for matching declaration in target file
            val targetOffset = findMatchingDeclaration(targetPsiFile, name, declType)
            if (targetOffset >= 0) {
                val descriptor = OpenFileDescriptor(project, targetVirtualFile, targetOffset)
                FileEditorManager.getInstance(project).openTextEditor(descriptor, true)
                return
            }
        }

        // Fallback: open target file at the beginning
        FileEditorManager.getInstance(project).openFile(targetVirtualFile, true)
    }

    override fun startInWriteAction(): Boolean = false

    companion object {
        /**
         * Searches a PSI file for a declaration with the given name and type.
         *
         * @param file the PSI file to search
         * @param name the declaration name to match
         * @param declType the element type of the declaration
         * @return the text offset of the matching declaration, or -1 if not found
         */
        private fun findMatchingDeclaration(
            file: PsiFile,
            name: String,
            declType: com.intellij.psi.tree.IElementType,
        ): Int {
            val children = file.children
            for (child in children) {
                if (child.node?.elementType == declType) {
                    val childName = RescriptPsiUtils.extractName(child)
                    if (childName == name) {
                        return child.textOffset
                    }
                }
                // Also check nested module declarations
                if (child.node?.elementType == RescriptElementTypes.MODULE_DECLARATION) {
                    for (nested in child.children) {
                        if (nested.node?.elementType == declType) {
                            val nestedName = RescriptPsiUtils.extractName(nested)
                            if (nestedName == name) {
                                return nested.textOffset
                            }
                        }
                    }
                }
            }
            return -1
        }
    }
}
