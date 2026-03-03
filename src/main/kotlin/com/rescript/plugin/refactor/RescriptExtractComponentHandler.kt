package com.rescript.plugin.refactor

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.refactoring.RefactoringActionHandler
import com.rescript.plugin.lang.psi.RescriptFile

/**
 * Handles the Extract React Component refactoring for ReScript JSX.
 *
 * Extracts selected JSX code into a new `@react.component` module declaration
 * and replaces the original selection with a reference to the new component.
 * Detects free variables in the selected JSX and adds them as component props.
 *
 * @see RefactoringActionHandler
 */
class RescriptExtractComponentHandler : RefactoringActionHandler {
    override fun invoke(
        project: Project,
        editor: Editor,
        file: PsiFile,
        dataContext: DataContext?,
    ) {
        if (file !is RescriptFile) return

        val selectedText = editor.selectionModel.selectedText
        if (selectedText.isNullOrBlank()) return
        if (!looksLikeJsx(selectedText)) return

        val selectionStart = editor.selectionModel.selectionStart
        val selectionEnd = editor.selectionModel.selectionEnd
        val document = editor.document

        val componentName = "NewComponent"
        val props = extractProps(selectedText)
        val componentCode = generateComponent(componentName, selectedText, props)
        val usage = generateUsage(componentName, props)

        WriteCommandAction.runWriteCommandAction(project, "Extract Component", null, {
            // Insert component module before the current declaration
            val lineNumber = document.getLineNumber(selectionStart)
            // Find the start of the enclosing top-level declaration
            var insertLine = lineNumber
            while (insertLine > 0) {
                val lineText =
                    document
                        .getText(
                            com.intellij.openapi.util.TextRange(
                                document.getLineStartOffset(insertLine - 1),
                                document.getLineEndOffset(insertLine - 1),
                            ),
                        ).trim()
                if (lineText.startsWith("let ") ||
                    lineText.startsWith("module ") ||
                    lineText.startsWith("@react.component") ||
                    lineText.isEmpty()
                ) {
                    break
                }
                insertLine--
            }

            val insertOffset = document.getLineStartOffset(insertLine)
            document.insertString(insertOffset, "$componentCode\n\n")

            // Replace original selection (shifted by the inserted text)
            val shift = componentCode.length + 2
            document.replaceString(selectionStart + shift, selectionEnd + shift, usage)

            // Position caret at component name for rename
            val nameStart = insertOffset + "module ".length
            editor.caretModel.moveToOffset(nameStart + componentName.length)
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
        /** Pattern matching `{variable}` expressions in JSX. */
        private val JSX_EXPRESSION_PATTERN = Regex("""\{(\w+)}""")

        /**
         * Checks whether the selected text appears to contain JSX.
         *
         * @param text the selected text
         * @return true if the text looks like JSX content
         */
        internal fun looksLikeJsx(text: String): Boolean {
            val trimmed = text.trim()
            return trimmed.startsWith("<") && trimmed.contains(">")
        }

        /**
         * Extracts variable references from JSX that should become component props.
         *
         * Detects `{variable}` expressions within JSX content and collects unique
         * variable names, excluding known globals and React built-ins.
         *
         * @param jsx the JSX source text
         * @return list of variable names to use as props
         */
        internal fun extractProps(jsx: String): List<String> {
            val props = mutableSetOf<String>()

            // Find {expression} patterns in JSX
            for (match in JSX_EXPRESSION_PATTERN.findAll(jsx)) {
                val name = match.groupValues[1]
                // Exclude known globals and common non-prop values
                if (name !in EXCLUDED_NAMES && name.first().isLowerCase()) {
                    props.add(name)
                }
            }

            return props.toList().sorted()
        }

        /**
         * Generates a `@react.component` module definition.
         *
         * @param name the component module name
         * @param jsx the JSX body
         * @param props the prop names to include in the make function
         * @return the module source text
         */
        internal fun generateComponent(
            name: String,
            jsx: String,
            props: List<String>,
        ): String {
            val propsParam =
                if (props.isEmpty()) {
                    "()"
                } else {
                    "(~${props.joinToString(", ~")})"
                }

            return buildString {
                appendLine("module $name = {")
                appendLine("  @react.component")
                appendLine("  let make = $propsParam => {")
                // Indent the JSX
                for (line in jsx.lines()) {
                    appendLine("    $line")
                }
                appendLine("  }")
                append("}")
            }
        }

        /**
         * Generates a JSX usage of the extracted component.
         *
         * @param name the component module name
         * @param props the prop names to pass
         * @return the JSX element string (e.g., `<NewComponent prop1 prop2 />`)
         */
        internal fun generateUsage(
            name: String,
            props: List<String>,
        ): String {
            if (props.isEmpty()) return "<$name />"
            val propsStr = props.joinToString(" ") { "$it={$it}" }
            return "<$name $propsStr />"
        }

        // Names to exclude from prop extraction
        private val EXCLUDED_NAMES =
            setOf(
                "true",
                "false",
                "None",
                "Some",
                "Ok",
                "Error",
                "React",
                "ReactDOM",
                "Js",
                "Belt",
                "Array",
                "String",
                "Int",
                "Float",
                "switch",
                "if",
                "else",
            )
    }
}
