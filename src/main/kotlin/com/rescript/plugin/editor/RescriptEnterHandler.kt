package com.rescript.plugin.editor

import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegateAdapter
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiFile
import com.rescript.plugin.RescriptLanguage
import com.rescript.plugin.lang.RescriptTokenTypes
import com.rescript.plugin.util.RescriptEditorUtils.getLineRangeAt
import com.rescript.plugin.util.RescriptEditorUtils.getLineTextAt

/**
 * Enter key handler for ReScript that auto-continues documentation
 * comments and single-line comments.
 *
 * @see RescriptSmartEnterProcessor for Shift+Enter handling
 */
class RescriptEnterHandler : EnterHandlerDelegateAdapter() {
    override fun preprocessEnter(
        file: PsiFile,
        editor: Editor,
        caretOffset: Ref<Int>,
        caretAdvance: Ref<Int>,
        dataContext: DataContext,
        originalHandler: EditorActionHandler?,
    ): EnterHandlerDelegate.Result {
        if (file.language != RescriptLanguage) return EnterHandlerDelegate.Result.Continue

        val offset = caretOffset.get()
        val document = editor.document
        val (lineStart, _) = document.getLineRangeAt(offset)
        val lineText = document.getLineTextAt(offset)
        val indent = lineText.takeWhile { it.isWhitespace() }

        val element = file.findElementAt(offset)
        val tokenType = element?.node?.elementType

        // Handle multi-line comment continuation
        if (tokenType == RescriptTokenTypes.MULTI_COMMENT) {
            val commentText = element.text
            val isDocComment = commentText.startsWith("/**")

            val elementStart = element.textRange.startOffset
            val relativeOffset = offset - elementStart

            // Caret right after opening "/**" with no body yet
            if (isDocComment && relativeOffset <= 3 && commentText.trimEnd() == "/**") {
                return EnterHandlerDelegate.Result.Continue
            }

            // Caret near the start of a doc comment
            if (isDocComment && relativeOffset <= 3) {
                val prefix = "$indent * "
                caretAdvance.set(prefix.length)
                return EnterHandlerDelegate.Result.Default
            }

            // Inside the body: add " * " prefix
            val beforeCaret = lineText.substring(0, offset - lineStart)
            val trimmedBefore = beforeCaret.trimStart()
            if (trimmedBefore.startsWith("*") || trimmedBefore.startsWith("/**")) {
                val insertText = "\n$indent * "
                document.insertString(offset, insertText)
                editor.caretModel.moveToOffset(offset + insertText.length)
                return EnterHandlerDelegate.Result.Stop
            }

            return EnterHandlerDelegate.Result.Continue
        }

        // Handle single-line comment continuation
        if (tokenType == RescriptTokenTypes.SINGLE_COMMENT) {
            val trimmed = lineText.trimStart()
            if (trimmed.startsWith("//")) {
                val commentPrefix =
                    if (trimmed.startsWith("/// ")) {
                        "$indent/// "
                    } else {
                        "$indent// "
                    }
                val insertText = "\n$commentPrefix"
                document.insertString(offset, insertText)
                editor.caretModel.moveToOffset(offset + insertText.length)
                return EnterHandlerDelegate.Result.Stop
            }
        }

        return EnterHandlerDelegate.Result.Continue
    }
}
