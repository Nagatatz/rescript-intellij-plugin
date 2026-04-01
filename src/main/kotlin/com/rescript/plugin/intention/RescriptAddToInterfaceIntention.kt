package com.rescript.plugin.intention

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.rescript.plugin.lang.psi.RescriptElementTypes
import com.rescript.plugin.lang.psi.RescriptPsiUtils
import com.rescript.plugin.util.RescriptFileUtil

/**
 * Intention action that publishes a declaration to the interface file.
 *
 * When invoked on a top-level declaration in a `.res` file that has a
 * corresponding `.resi` file, appends the declaration signature to the
 * interface file.
 *
 * For `type`, `external`, and `exception` declarations the full text is
 * copied. For `let` bindings a signature stub is generated since type
 * inference requires the LSP server.
 *
 * @see RescriptRemoveFromInterfaceIntention for the reverse operation
 */
class RescriptAddToInterfaceIntention : RescriptBaseIntention() {
    override fun getText(): String = "Add declaration to interface file"

    override fun isAvailableInRescript(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ): Boolean {
        val virtualFile = element.containingFile.virtualFile ?: return false
        if (!RescriptFileUtil.isResFile(virtualFile)) return false

        // Check that a .resi file exists
        val resiFile = RescriptFileUtil.findInterfaceFile(virtualFile) ?: return false
        if (!resiFile.exists()) return false

        // Check that cursor is on a top-level declaration
        val declaration =
            RescriptPsiUtils.findEnclosingDeclaration(element, RescriptPsiUtils.NAVIGABLE_TYPES)
                ?: return false
        val name = RescriptPsiUtils.extractName(declaration)
        if (name == "(anonymous)" || name == "(unknown)") return false

        // Check that the declaration is not already in the .resi file
        val resiPsiFile = PsiManager.getInstance(project).findFile(resiFile) ?: return false
        return !declarationExistsInFile(resiPsiFile, name, declaration.node.elementType)
    }

    override fun invoke(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ) {
        val virtualFile = element.containingFile.virtualFile ?: return
        val resiVirtualFile = RescriptFileUtil.findInterfaceFile(virtualFile) ?: return

        val declaration =
            RescriptPsiUtils.findEnclosingDeclaration(element, RescriptPsiUtils.NAVIGABLE_TYPES)
                ?: return
        val signature = buildSignature(declaration)

        val document =
            com.intellij.openapi.fileEditor.FileDocumentManager
                .getInstance()
                .getDocument(resiVirtualFile) ?: return

        // Append the signature at the end of the .resi file
        val text = document.text
        val suffix = if (text.isNotEmpty() && !text.endsWith("\n")) "\n" else ""
        document.insertString(document.textLength, "$suffix$signature\n")
    }

    companion object {
        /**
         * Checks whether a declaration with the given name and type exists in the file.
         *
         * @param file the PSI file to search
         * @param name the declaration name to find
         * @param elementType the expected element type
         * @return true if a matching declaration exists
         */
        internal fun declarationExistsInFile(
            file: PsiElement,
            name: String,
            elementType: com.intellij.psi.tree.IElementType,
        ): Boolean {
            for (child in file.children) {
                if (child.node?.elementType == elementType) {
                    if (RescriptPsiUtils.extractName(child) == name) return true
                }
            }
            return false
        }

        /**
         * Builds an interface signature string from a declaration PSI element.
         *
         * For `type`, `external`, and `exception` declarations, copies the full
         * text. For `let` bindings, generates a stub with a TODO comment since
         * full type inference requires the LSP server.
         *
         * @param declaration the declaration PSI element
         * @return the signature string suitable for a `.resi` file
         */
        internal fun buildSignature(declaration: PsiElement): String {
            val elementType = declaration.node?.elementType
            val text = declaration.text.trim()

            return when (elementType) {
                // type, external, exception: copy as-is
                RescriptElementTypes.TYPE_DECLARATION,
                RescriptElementTypes.EXTERNAL_DECLARATION,
                RescriptElementTypes.EXCEPTION_DECLARATION,
                -> {
                    text
                }

                // let: extract name and create a stub signature
                RescriptElementTypes.LET_DECLARATION -> {
                    val name = RescriptPsiUtils.extractName(declaration)
                    "let $name: _  // TODO: add type annotation"
                }

                // module: extract name and create a stub module type
                RescriptElementTypes.MODULE_DECLARATION -> {
                    val name = RescriptPsiUtils.extractName(declaration)
                    "module $name: {\n  // TODO: add module signature\n}"
                }

                else -> {
                    text
                }
            }
        }
    }
}
