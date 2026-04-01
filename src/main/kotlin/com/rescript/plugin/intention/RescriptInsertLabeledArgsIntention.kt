package com.rescript.plugin.intention

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.rescript.plugin.lang.RescriptTokenTypes
import com.rescript.plugin.lsp.RescriptLspUtils
import com.rescript.plugin.util.RescriptBraceBalanceUtil

/**
 * Intention action that inserts labeled argument placeholders for a function call.
 *
 * When the caret is on a function identifier, queries LSP hover for the function
 * signature and inserts `(~label1=_, ~label2=_)` placeholders. Optional parameters
 * receive `=?` suffix.
 *
 * Triggered via Alt+Enter > "Insert labeled arguments".
 *
 * @see RescriptLspUtils.parseSignatureLabels
 */
class RescriptInsertLabeledArgsIntention : RescriptBaseIntention() {
    override fun getText(): String = "Insert labeled arguments"

    override fun isAvailableInRescript(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ): Boolean {
        val tokenType = element.node?.elementType ?: return false
        // Available on identifiers (function names)
        return tokenType == RescriptTokenTypes.LIDENT || tokenType == RescriptTokenTypes.UIDENT
    }

    override fun invoke(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ) {
        editor ?: return
        val file = element.containingFile?.virtualFile ?: return
        val offset = element.textRange.startOffset

        // Get function signature from LSP hover
        val typeText = RescriptLspUtils.getHoverType(project, file, offset) ?: return
        val labels = RescriptLspUtils.parseSignatureLabels(typeText)
        if (labels.isEmpty()) return

        // Build the argument string
        val argsText =
            labels.joinToString(", ") { param ->
                if (param.isOptional) {
                    "~${param.name}=?"
                } else {
                    "~${param.name}=_"
                }
            }

        // Find the insertion point (after the function identifier)
        val insertOffset = element.textRange.endOffset
        val document = editor.document
        val text = document.text

        // Check if there are already parentheses
        val afterIdent = text.substring(insertOffset).trimStart()
        val replacement =
            if (afterIdent.startsWith("(")) {
                // Replace existing parens content
                val parenEnd = RescriptBraceBalanceUtil.findMatchingParen(text, text.indexOf('(', insertOffset))
                if (parenEnd != null) {
                    document.replaceString(insertOffset, parenEnd + 1, "($argsText)")
                    return
                }
                "($argsText)"
            } else {
                "($argsText)"
            }

        document.insertString(insertOffset, replacement)

        // Move caret to first placeholder
        val firstPlaceholder = insertOffset + replacement.indexOf('_')
        if (firstPlaceholder > insertOffset) {
            editor.caretModel.moveToOffset(firstPlaceholder)
        }
    }
}
