package com.rescript.plugin.intention

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.rescript.plugin.lang.psi.RescriptDeclarationPsiElement
import com.rescript.plugin.lang.psi.RescriptElementTypes
import com.rescript.plugin.lang.psi.RescriptFile

/**
 * Native PSI fallback for the `extractLocalModuleToFile` LSP code action.
 *
 * Triggers on a top-level `module M = { ... }` declaration and offers an
 * intention that writes the body to a sibling `M.res` file and removes the
 * local declaration. Acts as a fallback when the language-server-emitted
 * code action's `WorkspaceEdit.documentChanges[].CreateFile` resource
 * operation is not honoured by the IntelliJ Platform LSP implementation.
 *
 * The intention does not rewrite references to `M` elsewhere in the source
 * file. When such references are detected, a warning balloon advises the
 * user to adjust them manually.
 *
 * @see RescriptBaseIntention
 */
class RescriptExtractLocalModuleToFileIntention : RescriptBaseIntention() {
    override fun getText(): String = "Extract module to file"

    override fun isAvailableInRescript(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ): Boolean {
        val declaration = findEnclosingTopLevelModuleDeclaration(element) ?: return false
        val moduleName = declaration.getDeclarationName()
        if (moduleName.isBlank() || moduleName == "(anonymous)") return false
        if (!hasBraceBody(declaration.text)) return false

        val parentDir = declaration.containingFile.virtualFile?.parent ?: return false
        if (parentDir.findChild("$moduleName.res") != null) return false

        return true
    }

    override fun invoke(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ) {
        editor ?: return
        val declaration = findEnclosingTopLevelModuleDeclaration(element) ?: return
        val moduleName = declaration.getDeclarationName()
        val containingFile = declaration.containingFile
        val parentDir = containingFile.virtualFile?.parent ?: return
        val body = extractModuleBody(declaration.text) ?: return

        val deletionRange =
            expandedDeletionRange(
                editor,
                declaration.textRange.startOffset,
                declaration.textRange.endOffset,
            )
        val fileText = editor.document.text
        val remainder =
            fileText.substring(0, deletionRange.first) +
                fileText.substring(deletionRange.last)
        val hasReferences = hasInternalReferences(remainder, moduleName)

        runWriteAction {
            val newFile = parentDir.createChildData(this, "$moduleName.res")
            newFile.setBinaryContent((body.trimEnd() + "\n").toByteArray(Charsets.UTF_8))

            WriteCommandAction.runWriteCommandAction(project) {
                editor.document.deleteString(deletionRange.first, deletionRange.last)
            }
            FileEditorManager.getInstance(project).openFile(newFile, true)
        }

        if (hasReferences) {
            NotificationGroupManager
                .getInstance()
                .getNotificationGroup("ReScript")
                .createNotification(
                    "Module $moduleName extracted",
                    "References to `$moduleName` in the original file may need adjustment. " +
                        "Consider adding `open $moduleName` or qualifying with the new module path.",
                    NotificationType.WARNING,
                ).notify(project)
        }
    }

    private fun findEnclosingTopLevelModuleDeclaration(element: PsiElement): RescriptDeclarationPsiElement? {
        val declaration =
            PsiTreeUtil.getParentOfType(element, RescriptDeclarationPsiElement::class.java, false)
                ?: return null
        if (declaration.node.elementType != RescriptElementTypes.MODULE_DECLARATION) return null
        if (declaration.parent !is RescriptFile) return null
        return declaration
    }

    /**
     * Expands the declaration's text range so that a trailing newline (and the
     * blank line after it, if present) is removed along with the declaration.
     * Keeps the file layout tidy when the local module is the sole content of
     * a contiguous block.
     */
    private fun expandedDeletionRange(
        editor: Editor,
        startOffset: Int,
        endOffset: Int,
    ): IntRange {
        val text = editor.document.text
        var end = endOffset
        if (end < text.length && text[end] == '\n') end++
        if (end < text.length && text[end] == '\n') end++
        return startOffset..end
    }

    companion object {
        // Matches a reference like `M.field` / `M.foo()` that uses the module
        // qualifier from another declaration in the same file.
        private fun referencePattern(name: String): Regex =
            Regex(
                """\b${Regex.escape(name)}\.\w""",
            )

        /**
         * Returns true when the declaration text starts with `module Name = {`
         * (allowing optional signature annotation) and therefore has a brace-bound
         * body that can be extracted. Alias forms like `module M = OtherMod` and
         * functor forms `module Make = (X: S) => ...` are rejected.
         */
        fun hasBraceBody(declarationText: String): Boolean {
            val equalsIndex = declarationText.indexOf('=')
            if (equalsIndex < 0) return false
            // Skip whitespace after `=` and check the first non-whitespace character.
            var i = equalsIndex + 1
            while (i < declarationText.length && declarationText[i].isWhitespace()) i++
            return i < declarationText.length && declarationText[i] == '{'
        }

        /**
         * Extracts the brace-bound body of a `module M = { ... }` declaration.
         *
         * @param declarationText the full text of the module declaration
         * @return the body text without the outer braces, or null when the
         *     declaration does not have a brace-bound body or has unbalanced braces
         */
        fun extractModuleBody(declarationText: String): String? {
            val openBrace = declarationText.indexOf('{')
            if (openBrace < 0) return null
            val closeBrace = matchingCloseBrace(declarationText, openBrace) ?: return null
            return declarationText.substring(openBrace + 1, closeBrace)
        }

        /**
         * Returns true when the source text contains a qualified reference
         * `Name.member` outside of the extracted declaration. Only a textual
         * scan is performed — comments and string literals are not excluded,
         * so this is a conservative check that may produce a warning on false
         * positives. The user only gets a notification, never an automatic edit.
         */
        fun hasInternalReferences(
            text: String,
            moduleName: String,
        ): Boolean = referencePattern(moduleName).containsMatchIn(text)

        private fun matchingCloseBrace(
            text: String,
            openIndex: Int,
        ): Int? {
            var depth = 0
            for (i in openIndex until text.length) {
                when (text[i]) {
                    '{' -> {
                        depth++
                    }

                    '}' -> {
                        depth--
                        if (depth == 0) return i
                    }
                }
            }
            return null
        }
    }
}
