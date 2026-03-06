package com.rescript.plugin.intention

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.rescript.plugin.lang.psi.RescriptFile
import com.rescript.plugin.util.RescriptRegexPatterns

/**
 * Intention action that generates a doc comment stub (`/** ... */`) above
 * the current declaration. For function declarations, `@param` tags are
 * automatically generated based on the parameter names.
 *
 * @see RescriptAddGenTypeIntention for a similar declaration-targeting intention
 */
class RescriptGenerateDocCommentIntention : PsiElementBaseIntentionAction() {
    override fun getText(): String = "Generate doc comment"

    override fun getFamilyName(): String = "Generate doc comment"

    override fun isAvailable(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ): Boolean {
        if (element.containingFile !is RescriptFile) return false
        val declaration = RescriptAddGenTypeIntention.findParentDeclaration(element) ?: return false
        return !hasDocComment(declaration)
    }

    override fun invoke(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ) {
        val declaration = RescriptAddGenTypeIntention.findParentDeclaration(element) ?: return
        val document = editor?.document ?: return
        val offset = declaration.textRange.startOffset
        val lineNumber = document.getLineNumber(offset)
        val lineStartOffset = document.getLineStartOffset(lineNumber)
        val indent =
            document.getText(
                TextRange(lineStartOffset, offset),
            )

        val declarationText = declaration.text
        val params = extractParams(declarationText)
        val docComment = buildDocComment(params, indent)
        document.insertString(offset, docComment)
    }

    companion object {
        // Regex to match the parameter list in a function: (param1, ~label: type, ...) =>
        private val PARAM_LIST_REGEX = Regex("""\(([^)]*)\)\s*=>""")

        // Regex to match a single labeled parameter: ~name or ~name: type or ~name=?
        private val LABELED_PARAM_REGEX = RescriptRegexPatterns.LABELED_PARAM_NAME

        // Regex to match a single unlabeled parameter: name or name: type (excluding _ and ())
        private val UNLABELED_PARAM_REGEX = Regex("""^([a-z]\w*)""")

        /**
         * Extracts parameter names from a declaration text.
         * Handles labeled (~param) and unlabeled positional parameters.
         * Excludes wildcards (_) and unit (()).
         *
         * @param declarationText the full text of the declaration
         * @return list of parameter names
         */
        fun extractParams(declarationText: String): List<String> {
            val match = PARAM_LIST_REGEX.find(declarationText) ?: return emptyList()
            val paramsStr = match.groupValues[1]
            if (paramsStr.isBlank()) return emptyList()

            return paramsStr
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() && it != "_" && it != "()" }
                .mapNotNull { param ->
                    // Try labeled param first (~name)
                    LABELED_PARAM_REGEX.find(param)?.groupValues?.get(1)
                        ?: // Try unlabeled param (name or name: type)
                        UNLABELED_PARAM_REGEX.find(param)?.groupValues?.get(1)
                }
        }

        /**
         * Builds a doc comment string with optional `@param` tags.
         *
         * @param params list of parameter names to include as @param tags
         * @param indent the indentation prefix for each line
         * @return the formatted doc comment string ending with a newline
         */
        fun buildDocComment(
            params: List<String>,
            indent: String,
        ): String {
            val sb = StringBuilder()
            sb.append("/**\n")
            sb.append("$indent * \n")
            if (params.isNotEmpty()) {
                sb.append("$indent *\n")
                for (param in params) {
                    sb.append("$indent * @param $param \n")
                }
            }
            sb.append("$indent */\n")
            sb.append(indent)
            return sb.toString()
        }

        /**
         * Checks whether the declaration already has a doc comment immediately before it.
         */
        fun hasDocComment(declaration: PsiElement): Boolean {
            var prev = declaration.prevSibling
            while (prev != null) {
                val text = prev.text
                if (text.isNotBlank()) {
                    return text.trimStart().startsWith("/**")
                }
                prev = prev.prevSibling
            }
            return false
        }
    }
}
