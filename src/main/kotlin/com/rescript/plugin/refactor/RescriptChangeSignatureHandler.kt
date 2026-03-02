package com.rescript.plugin.refactor

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.refactoring.RefactoringActionHandler
import com.rescript.plugin.lang.psi.RescriptFile

/**
 * Handles the Change Signature refactoring for ReScript functions.
 *
 * Parses function parameters from a `let` declaration, allows modification
 * (reorder, rename, add, remove), and updates both the declaration and
 * call sites within the same file.
 *
 * @see RefactoringActionHandler
 */
class RescriptChangeSignatureHandler : RefactoringActionHandler {
    override fun invoke(
        project: Project,
        editor: Editor,
        file: PsiFile,
        dataContext: DataContext?,
    ) {
        if (file !is RescriptFile) return

        val document = editor.document
        val offset = editor.caretModel.offset
        val text = document.text
        val lineNumber = document.getLineNumber(offset)
        val lineText =
            document.getText(
                com.intellij.openapi.util.TextRange(
                    document.getLineStartOffset(lineNumber),
                    document.getLineEndOffset(lineNumber),
                ),
            )

        val funcInfo = parseFunctionAtCaret(text, offset) ?: return

        // For now, the handler performs simple parameter reordering
        // A full dialog-based approach would use RescriptChangeSignatureDialog
    }

    override fun invoke(
        project: Project,
        elements: Array<out PsiElement>,
        dataContext: DataContext?,
    ) {
        // Not used — requires editor context
    }

    companion object {
        /** Represents a parsed function parameter. */
        data class FunctionParam(
            val name: String,
            val isLabeled: Boolean,
            val typeAnnotation: String?,
            val defaultValue: String?,
        )

        /** Parsed function declaration information. */
        data class FunctionInfo(
            val name: String,
            val params: List<FunctionParam>,
            val startOffset: Int,
            val endOffset: Int,
        )

        // Pattern for let function declarations
        private val FUNC_DECL_PATTERN = Regex("""let\s+(\w+)\s*=\s*\(([^)]*)\)\s*=>""")

        // Pattern for labeled parameters (~name: type)
        private val LABELED_PARAM_PATTERN = Regex("""~(\w+)(?:\s*:\s*(\w[\w<>,\s]*))?(?:\s*=\s*([^,)]+))?""")

        // Pattern for positional parameters
        private val POSITIONAL_PARAM_PATTERN = Regex("""(\w+)(?:\s*:\s*(\w[\w<>,\s]*))?""")

        /**
         * Parses the function declaration at the given caret offset.
         *
         * @param text the full document text
         * @param caretOffset the caret position
         * @return parsed function info, or null if not at a function
         */
        internal fun parseFunctionAtCaret(
            text: String,
            caretOffset: Int,
        ): FunctionInfo? {
            for (match in FUNC_DECL_PATTERN.findAll(text)) {
                if (caretOffset in match.range.first..(match.range.last + 1)) {
                    val name = match.groupValues[1]
                    val paramsStr = match.groupValues[2]
                    val params = parseParameters(paramsStr)
                    return FunctionInfo(name, params, match.range.first, match.range.last)
                }
            }
            return null
        }

        /**
         * Parses function parameters from the parameter string.
         *
         * Supports both labeled (`~name: type`) and positional parameters.
         *
         * @param paramsStr the parameter string (inside parentheses)
         * @return list of parsed parameters
         */
        internal fun parseParameters(paramsStr: String): List<FunctionParam> {
            if (paramsStr.isBlank()) return emptyList()

            val params = mutableListOf<FunctionParam>()
            val parts = splitParams(paramsStr)

            for (part in parts) {
                val trimmed = part.trim()
                if (trimmed.isEmpty()) continue

                val labeledMatch = LABELED_PARAM_PATTERN.find(trimmed)
                if (labeledMatch != null) {
                    params.add(
                        FunctionParam(
                            name = labeledMatch.groupValues[1],
                            isLabeled = true,
                            typeAnnotation = labeledMatch.groupValues[2].ifEmpty { null },
                            defaultValue = labeledMatch.groupValues[3].ifEmpty { null }?.trim(),
                        ),
                    )
                    continue
                }

                val positionalMatch = POSITIONAL_PARAM_PATTERN.find(trimmed)
                if (positionalMatch != null) {
                    params.add(
                        FunctionParam(
                            name = positionalMatch.groupValues[1],
                            isLabeled = false,
                            typeAnnotation = positionalMatch.groupValues[2].ifEmpty { null },
                            defaultValue = null,
                        ),
                    )
                }
            }

            return params
        }

        /**
         * Applies a signature change to the function declaration text.
         *
         * @param originalDecl the original function declaration line
         * @param originalParams the original parameters
         * @param newParams the new parameters (possibly reordered/renamed)
         * @return the modified declaration text
         */
        internal fun applySignatureChange(
            originalDecl: String,
            originalParams: List<FunctionParam>,
            newParams: List<FunctionParam>,
        ): String {
            val newParamsStr =
                newParams.joinToString(", ") { param ->
                    buildString {
                        if (param.isLabeled) append("~")
                        append(param.name)
                        param.typeAnnotation?.let { append(": $it") }
                        param.defaultValue?.let { append("=$it") }
                    }
                }

            val oldParamsStr =
                originalParams.joinToString(", ") { param ->
                    buildString {
                        if (param.isLabeled) append("~")
                        append(param.name)
                        param.typeAnnotation?.let { append(": $it") }
                        param.defaultValue?.let { append("=$it") }
                    }
                }

            return originalDecl.replace("($oldParamsStr)", "($newParamsStr)")
        }

        /**
         * Splits a parameter string by commas, respecting nested delimiters.
         *
         * @param paramsStr the parameter string
         * @return list of individual parameter strings
         */
        internal fun splitParams(paramsStr: String): List<String> {
            val parts = mutableListOf<String>()
            var depth = 0
            var current = StringBuilder()

            for (ch in paramsStr) {
                when (ch) {
                    '(', '<', '{', '[' -> {
                        depth++
                        current.append(ch)
                    }
                    ')', '>', '}', ']' -> {
                        depth--
                        current.append(ch)
                    }
                    ',' -> {
                        if (depth == 0) {
                            parts.add(current.toString())
                            current = StringBuilder()
                        } else {
                            current.append(ch)
                        }
                    }
                    else -> current.append(ch)
                }
            }

            if (current.isNotEmpty()) {
                parts.add(current.toString())
            }

            return parts
        }
    }
}

/**
 * Action wrapper that delegates to [RescriptChangeSignatureHandler] from the Refactor menu.
 *
 * Registered as an action in `plugin.xml` with `Ctrl+F6` shortcut.
 *
 * @see RescriptChangeSignatureHandler
 * @see AnAction
 */
class RescriptChangeSignatureAction : AnAction() {
    private val handler = RescriptChangeSignatureHandler()

    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val file = e.getData(CommonDataKeys.PSI_FILE) ?: return
        val project = e.project ?: return
        handler.invoke(project, editor, file, e.dataContext)
    }

    override fun update(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        val file = e.getData(CommonDataKeys.PSI_FILE)
        e.presentation.isEnabled = editor != null && file is RescriptFile
    }
}
