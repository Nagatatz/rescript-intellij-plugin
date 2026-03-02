package com.rescript.plugin.intention

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.rescript.plugin.lang.RescriptTokenTypes
import com.rescript.plugin.lang.psi.RescriptFile
import com.rescript.plugin.lsp.RescriptLspUtils

/**
 * Intention action to convert positional arguments to labeled arguments.
 *
 * When the caret is on a function identifier, queries LSP hover for the
 * function signature and converts existing positional arguments to labeled
 * form using the label names from the signature.
 *
 * Example: `foo(1, "hello")` with signature `(~id: int, ~name: string) => unit`
 * becomes `foo(~id=1, ~name="hello")`.
 *
 * Triggered via Alt+Enter > "Convert to labeled arguments".
 *
 * @see RescriptInsertLabeledArgsIntention
 * @see RescriptLspUtils.parseSignatureLabels
 */
class RescriptConvertToLabeledArgsIntention : PsiElementBaseIntentionAction() {
    override fun getText(): String = "Convert to labeled arguments"

    override fun getFamilyName(): String = "Convert to labeled arguments"

    override fun isAvailable(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ): Boolean {
        if (element.containingFile !is RescriptFile) return false
        val tokenType = element.node?.elementType ?: return false
        if (tokenType != RescriptTokenTypes.LIDENT && tokenType != RescriptTokenTypes.UIDENT) return false

        // Check if followed by parentheses with arguments
        val text = editor?.document?.text ?: return false
        val endOffset = element.textRange.endOffset
        if (endOffset >= text.length) return false

        val afterIdent = text.substring(endOffset).trimStart()
        if (!afterIdent.startsWith("(")) return false

        // Check that arguments don't already have labels
        val parenContent = RescriptLspUtils.extractParenContent(text.substring(endOffset))
        if (parenContent.isNullOrBlank()) return false

        return !parenContent.contains("~")
    }

    override fun invoke(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ) {
        editor ?: return
        val file = element.containingFile?.virtualFile ?: return
        val offset = element.textRange.startOffset
        val document = editor.document
        val text = document.text

        // Get function signature from LSP hover
        val typeText = RescriptLspUtils.getHoverType(project, file, offset) ?: return
        val labels = RescriptLspUtils.parseSignatureLabels(typeText)
        if (labels.isEmpty()) return

        // Find the argument list
        val endOffset = element.textRange.endOffset
        val parenStart = text.indexOf('(', endOffset)
        if (parenStart < 0) return

        val parenEnd = findMatchingParen(text, parenStart)
        if (parenEnd < 0) return

        val argsText = text.substring(parenStart + 1, parenEnd)
        val args = RescriptLspUtils.splitByComma(argsText)

        // Match arguments with labels
        val labeledArgs =
            buildString {
                append("(")
                val minCount = minOf(labels.size, args.size)
                for (i in 0 until minCount) {
                    if (i > 0) append(", ")
                    val arg = args[i].trim()
                    val label = labels[i]
                    append("~${label.name}=$arg")
                }
                // Append any remaining positional args without labels
                for (i in minCount until args.size) {
                    if (i > 0) append(", ")
                    append(args[i].trim())
                }
                append(")")
            }

        WriteCommandAction.runWriteCommandAction(project) {
            document.replaceString(parenStart, parenEnd + 1, labeledArgs)
        }
    }

    private fun findMatchingParen(
        text: String,
        openIndex: Int,
    ): Int {
        if (openIndex < 0) return -1
        var depth = 0
        for (i in openIndex until text.length) {
            when (text[i]) {
                '(' -> depth++
                ')' -> {
                    depth--
                    if (depth == 0) return i
                }
            }
        }
        return -1
    }
}
