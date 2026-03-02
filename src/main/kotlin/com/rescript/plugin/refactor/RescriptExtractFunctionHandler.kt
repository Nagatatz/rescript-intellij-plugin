package com.rescript.plugin.refactor

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.refactoring.RefactoringActionHandler
import com.rescript.plugin.lang.psi.RescriptFile

/**
 * Handles the Extract Function refactoring for ReScript.
 *
 * Extracts the selected code into a new `let` binding above the current
 * statement, detecting free variables in the selection to use as function
 * parameters. The original selection is replaced with a call to the new function.
 *
 * @see RefactoringActionHandler
 * @see RescriptRefactoringSupportProvider which registers this handler
 */
class RescriptExtractFunctionHandler : RefactoringActionHandler {
    override fun invoke(
        project: Project,
        editor: Editor,
        file: PsiFile,
        dataContext: DataContext?,
    ) {
        if (file !is RescriptFile) return

        val selectedText = editor.selectionModel.selectedText
        if (selectedText.isNullOrBlank()) return

        val selectionStart = editor.selectionModel.selectionStart
        val selectionEnd = editor.selectionModel.selectionEnd
        val document = editor.document

        val functionName = "extractedFunction"
        val freeVars = findFreeVariables(selectedText, document.text, selectionStart)
        val functionCode = generateFunction(functionName, selectedText, freeVars)
        val callSite = generateCallSite(functionName, freeVars)

        WriteCommandAction.runWriteCommandAction(project, "Extract Function", null, {
            // Find the start of the enclosing top-level declaration
            val lineNumber = document.getLineNumber(selectionStart)
            var insertLine = lineNumber
            while (insertLine > 0) {
                val lineText =
                    document
                        .getText(
                            TextRange(
                                document.getLineStartOffset(insertLine - 1),
                                document.getLineEndOffset(insertLine - 1),
                            ),
                        ).trim()
                if (lineText.startsWith("let ") ||
                    lineText.startsWith("module ") ||
                    lineText.startsWith("@") ||
                    lineText.isEmpty()
                ) {
                    break
                }
                insertLine--
            }

            val insertOffset = document.getLineStartOffset(insertLine)
            document.insertString(insertOffset, "$functionCode\n\n")

            // Replace original selection (shifted by inserted text)
            val shift = functionCode.length + 2
            document.replaceString(selectionStart + shift, selectionEnd + shift, callSite)

            // Position caret at function name for rename
            val nameStart = insertOffset + "let ".length
            editor.caretModel.moveToOffset(nameStart + functionName.length)
        })
    }

    override fun invoke(
        project: Project,
        elements: Array<out PsiElement>,
        dataContext: DataContext?,
    ) {
        // Not used — extraction requires an editor with a selection
    }

    companion object {
        // Pattern matching identifiers (variable references)
        private val IDENTIFIER_PATTERN = Regex("""\b([a-z_]\w*)\b""")

        /** Pattern matching let bindings for detecting locally-defined variables. */
        private val LET_BINDING_PATTERN = Regex("""let\s+(\w+)""")

        // ReScript keywords that should not be treated as free variables
        private val KEYWORDS =
            setOf(
                "let",
                "type",
                "module",
                "open",
                "include",
                "external",
                "if",
                "else",
                "switch",
                "try",
                "catch",
                "raise",
                "exception",
                "true",
                "false",
                "and",
                "or",
                "not",
                "mod",
                "land",
                "lor",
                "lxor",
                "lsl",
                "lsr",
                "asr",
                "as",
                "when",
                "rec",
                "private",
                "mutable",
                "lazy",
                "assert",
                "while",
                "for",
                "in",
                "to",
                "downto",
                "do",
                "done",
                "of",
                "ref",
            )

        // Common global names that shouldn't be parameters
        private val GLOBALS =
            setOf(
                "None",
                "Some",
                "Ok",
                "Error",
                "Js",
                "Belt",
                "Array",
                "String",
                "Int",
                "Float",
                "Promise",
                "Console",
                "React",
                "ReactDOM",
                "Json",
                "Dict",
                "Map",
                "Set",
                "List",
                "Option",
                "Result",
                "Nullable",
            )

        /**
         * Finds free variables in the selected code that need to become function parameters.
         *
         * A free variable is an identifier used in the selection that is defined
         * outside it (in the surrounding context).
         *
         * @param selectedText the selected code
         * @param fullText the full document text
         * @param selectionStart the start offset of the selection
         * @return sorted list of free variable names
         */
        internal fun findFreeVariables(
            selectedText: String,
            fullText: String,
            selectionStart: Int,
        ): List<String> {
            // Collect identifiers used in the selection
            val usedIdentifiers =
                IDENTIFIER_PATTERN
                    .findAll(selectedText)
                    .map { it.groupValues[1] }
                    .filter { it !in KEYWORDS && it !in GLOBALS }
                    .toSet()

            // Check which ones are defined within the selection itself
            val locallyDefined = mutableSetOf<String>()
            for (match in LET_BINDING_PATTERN.findAll(selectedText)) {
                locallyDefined.add(match.groupValues[1])
            }

            // Free variables = used but not locally defined
            return (usedIdentifiers - locallyDefined).sorted()
        }

        /**
         * Generates a `let` function binding from the selected code and free variables.
         *
         * @param name the function name
         * @param body the function body (selected code)
         * @param params the parameter names (free variables)
         * @return the function source text
         */
        internal fun generateFunction(
            name: String,
            body: String,
            params: List<String>,
        ): String {
            val paramList = if (params.isEmpty()) "()" else "(${params.joinToString(", ")})"
            val indentedBody = body.lines().joinToString("\n") { "  $it" }

            return if (body.lines().size == 1) {
                "let $name = $paramList => $body"
            } else {
                "let $name = $paramList => {\n$indentedBody\n}"
            }
        }

        /**
         * Generates a call expression for the extracted function.
         *
         * @param name the function name
         * @param args the argument names (matching the parameters)
         * @return the call expression string
         */
        internal fun generateCallSite(
            name: String,
            args: List<String>,
        ): String =
            if (args.isEmpty()) {
                "$name()"
            } else {
                "$name(${args.joinToString(", ")})"
            }
    }
}
