package com.rescript.plugin.util

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange

/**
 * Extension functions for common editor and document operations.
 *
 * Wraps frequent [WriteCommandAction] patterns and document line access
 * to reduce boilerplate in intention actions, quick fixes, and code
 * generation actions.
 */
object RescriptEditorUtils {
    /**
     * Returns the full text of the line containing the given offset.
     *
     * @param offset a character offset within the document
     * @return the line text (without line separator)
     */
    fun Document.getLineTextAt(offset: Int): String {
        val lineNumber = getLineNumber(offset)
        val lineStart = getLineStartOffset(lineNumber)
        val lineEnd = getLineEndOffset(lineNumber)
        return getText(TextRange(lineStart, lineEnd))
    }

    /**
     * Returns the start and end offsets of the line containing the given offset.
     *
     * @param offset a character offset within the document
     * @return a pair of (lineStartOffset, lineEndOffset)
     */
    fun Document.getLineRangeAt(offset: Int): Pair<Int, Int> {
        val lineNumber = getLineNumber(offset)
        return getLineStartOffset(lineNumber) to getLineEndOffset(lineNumber)
    }

    /**
     * Replaces a text range inside a [WriteCommandAction].
     *
     * @param project the current project for undo grouping
     * @param startOffset the start of the range to replace
     * @param endOffset the end of the range to replace
     * @param newText the replacement text
     */
    fun Document.replaceInWriteAction(
        project: Project,
        startOffset: Int,
        endOffset: Int,
        newText: String,
    ) {
        WriteCommandAction.runWriteCommandAction(project) {
            replaceString(startOffset, endOffset, newText)
        }
    }

    /**
     * Inserts text at an offset inside a [WriteCommandAction].
     *
     * @param project the current project for undo grouping
     * @param offset the insertion point
     * @param text the text to insert
     */
    fun Document.insertInWriteAction(
        project: Project,
        offset: Int,
        text: String,
    ) {
        WriteCommandAction.runWriteCommandAction(project) {
            insertString(offset, text)
        }
    }

    /**
     * Deletes a text range inside a [WriteCommandAction].
     *
     * @param project the current project for undo grouping
     * @param startOffset the start of the range to delete
     * @param endOffset the end of the range to delete
     */
    fun Document.deleteInWriteAction(
        project: Project,
        startOffset: Int,
        endOffset: Int,
    ) {
        WriteCommandAction.runWriteCommandAction(project) {
            deleteString(startOffset, endOffset)
        }
    }
}
