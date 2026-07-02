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
     * The `character` field of an LSP [Position] is a UTF-16 code-unit index
     * within the line. A language server may legitimately (or erroneously) emit
     * a `character` past the actual line end — e.g. a stale range after an edit,
     * or a malformed response. The returned offset is therefore **clamped to the
     * line's end offset** so it can never exceed the document bounds; callers
     * that pass the result straight to `Document.replaceString` would otherwise
     * throw an out-of-bounds exception.
     *
     * @param document the document to resolve the position in
     * @param position the LSP position to convert
     * @return the character offset (clamped to the line end), or -1 if the line
     *   index or character is out of bounds
     */
    fun positionToOffset(
        document: Document,
        position: Position,
    ): Int {
        if (position.line < 0 || position.line >= document.lineCount) return -1
        if (position.character < 0) return -1
        val lineStart = document.getLineStartOffset(position.line)
        val lineEnd = document.getLineEndOffset(position.line)
        return (lineStart + position.character).coerceAtMost(lineEnd)
    }

    /**
     * Returns the **1-based** line number for [offset] inside [source].
     *
     * Used by features that compute display labels (e.g. `path:line`)
     * straight from a `String` snapshot of the file, without needing
     * the `Document` API. Mirrors the semantics callers expect:
     * `offset <= 0` → line 1; `offset > source.length` → clamped to
     * the last line so the result is always at least 1.
     *
     * @param source the textual snapshot to count newlines in
     * @param offset the character offset whose line number is wanted
     * @return the 1-based line number containing [offset]
     */
    fun lineNumberAt(
        source: String,
        offset: Int,
    ): Int {
        if (offset <= 0) return 1
        var line = 1
        val end = minOf(offset, source.length)
        for (i in 0 until end) if (source[i] == '\n') line++
        return line
    }
}
