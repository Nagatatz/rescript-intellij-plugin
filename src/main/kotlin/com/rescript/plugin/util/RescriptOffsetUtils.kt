package com.rescript.plugin.util

import com.intellij.openapi.editor.Document
import org.eclipse.lsp4j.Position

/**
 * Shared utility for converting between editor offsets and LSP positions.
 *
 * Centralizes the duplicated offset↔position conversion logic that was
 * previously scattered across 14+ files. All conversions go through
 * [Document] line/column calculations.
 *
 * @see org.eclipse.lsp4j.Position
 * @see com.intellij.openapi.editor.Document
 */
object RescriptOffsetUtils {
    /**
     * Converts an editor offset to an LSP [Position] (0-based line and character).
     *
     * @param document the document to resolve the offset in
     * @param offset the character offset within the document
     * @return the corresponding LSP position
     */
    fun offsetToPosition(
        document: Document,
        offset: Int,
    ): Position {
        val line = document.getLineNumber(offset)
        val lineStart = document.getLineStartOffset(line)
        return Position(line, offset - lineStart)
    }

    /**
     * Converts an LSP [Position] (0-based line and character) to an editor offset.
     *
     * @param document the document to resolve the position in
     * @param position the LSP position to convert
     * @return the character offset, or -1 if the position is out of bounds
     */
    fun positionToOffset(
        document: Document,
        position: Position,
    ): Int {
        if (position.line < 0 || position.line >= document.lineCount) return -1
        val lineStart = document.getLineStartOffset(position.line)
        return lineStart + position.character
    }
}
