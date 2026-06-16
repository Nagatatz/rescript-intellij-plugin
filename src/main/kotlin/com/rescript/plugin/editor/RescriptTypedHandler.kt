package com.rescript.plugin.editor

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.rescript.plugin.lang.RescriptTokenTypes
import com.rescript.plugin.lang.psi.RescriptFile
import com.rescript.plugin.lang.psi.RescriptJsxTagPairUtil
import com.rescript.plugin.lang.psi.TagSide

/**
 * Handles special character typing behavior in ReScript files.
 *
 * Supports JSX auto-close: when the user types `>` to complete a JSX opening
 * tag like `<div>`, the corresponding closing tag `</div>` is auto-inserted.
 * Also keeps paired JSX tag names in sync: editing the name on one side of a
 * matched `<div>...</div>` pair mirrors the change onto the other side within
 * the same typing command, so the whole edit undoes in a single step.
 *
 * @see RescriptJsxTagPairUtil for the shared tag-pair resolution logic
 * @see RescriptBackspaceHandler for the complementary backspace behavior
 */
class RescriptTypedHandler : TypedHandlerDelegate() {
    // Captured pre-edit state (side + name) carried from beforeCharTyped to charTyped on
    // the EDT for a single keystroke; null when the keystroke does not edit a tag name.
    private var pendingSync: Pair<TagSide, String>? = null

    override fun beforeCharTyped(
        c: Char,
        project: Project,
        editor: Editor,
        file: PsiFile,
        fileType: FileType,
    ): Result {
        pendingSync = null
        if (file !is RescriptFile) return Result.CONTINUE
        // Only name-continuing characters extend a tag name.
        if (!(c.isLetterOrDigit() || c == '_')) return Result.CONTINUE

        val offset = editor.caretModel.offset
        val jsx = RescriptJsxTagPairUtil.findEnclosingJsxElementAt(file, offset) ?: return Result.CONTINUE
        val side = RescriptJsxTagPairUtil.sideAt(jsx, offset) ?: return Result.CONTINUE
        val names = RescriptJsxTagPairUtil.extractTagNames(jsx)
        val preEditName = if (side == TagSide.OPEN) names.openName else names.closeName
        if (preEditName != null) pendingSync = side to preEditName
        return Result.CONTINUE
    }

    override fun charTyped(
        c: Char,
        project: Project,
        editor: Editor,
        file: PsiFile,
    ): Result {
        if (file !is RescriptFile) return Result.CONTINUE

        if (c != '>') {
            applySyncRename(project, editor, file)
            return Result.CONTINUE
        }

        val offset = editor.caretModel.offset
        val document = editor.document
        val text = document.charsSequence

        // Don't act if we're past the end of text
        if (offset < 2) return Result.CONTINUE

        // Check the character before '>' — if it's '/', this is a self-closing tag
        if (text[offset - 2] == '/') return Result.CONTINUE

        // Check if this looks like a JSX opening tag by scanning backward for '<'
        val tagName = extractJsxTagName(text, offset - 1) ?: return Result.CONTINUE

        // Don't insert closing tag if we're in a comment or string
        val psiElement = file.findElementAt(offset - 1)
        if (psiElement != null) {
            val tokenType = psiElement.node?.elementType
            if (tokenType == RescriptTokenTypes.SINGLE_COMMENT ||
                tokenType == RescriptTokenTypes.MULTI_COMMENT ||
                tokenType == RescriptTokenTypes.STRING_VALUE
            ) {
                return Result.CONTINUE
            }
        }

        // Insert closing tag
        val closingTag = "</$tagName>"
        document.insertString(offset, closingTag)

        return Result.STOP
    }

    /**
     * Mirrors the just-typed tag-name edit onto the paired tag, if any was captured.
     *
     * Runs inside the active typing command, so the mirrored replacement groups with the
     * original keystroke into a single undo step.
     *
     * @param project the current project
     * @param editor the editor being typed into
     * @param file the ReScript PSI file
     */
    private fun applySyncRename(
        project: Project,
        editor: Editor,
        file: PsiFile,
    ) {
        val (side, preEditName) = pendingSync ?: return
        pendingSync = null

        val document = editor.document
        PsiDocumentManager.getInstance(project).commitDocument(document)
        val offset = editor.caretModel.offset
        val jsx = RescriptJsxTagPairUtil.findEnclosingJsxElementAt(file, offset) ?: return
        val names = RescriptJsxTagPairUtil.extractTagNames(jsx)
        val edit = RescriptJsxTagPairUtil.computeSyncEdit(names, side, preEditName) ?: return
        document.replaceString(edit.range.startOffset, edit.range.endOffset, edit.replacement)
    }

    /**
     * Extracts the JSX tag name by scanning backward from the `>` position.
     *
     * Handles simple tags (`<div>`), component tags (`<Component>`),
     * and module-qualified tags (`<Module.Component>`).
     *
     * @param text the document text
     * @param gtIndex the index of the `>` character
     * @return the full tag name, or null if this doesn't look like a JSX tag
     */
    internal fun extractJsxTagName(
        text: CharSequence,
        gtIndex: Int,
    ): String? {
        // Scan backward from '>' to find '<'
        var i = gtIndex - 1

        // Skip attributes and whitespace to find the tag name region
        // We need to find '<' and then extract the tag name after it
        var depth = 0
        while (i >= 0) {
            when (text[i]) {
                '<' -> {
                    if (depth == 0) {
                        // Found the opening '<'
                        val afterLt = i + 1
                        if (afterLt >= text.length) return null

                        // Check for closing tag pattern '</'
                        if (text[afterLt] == '/') return null

                        // Extract tag name (letters, digits, dots for module paths)
                        val tagName = extractTagNameAfterLt(text, afterLt) ?: return null

                        // Validate: tag name must start with a letter
                        if (tagName.isEmpty() || !tagName[0].isLetter()) return null

                        return tagName
                    }
                }

                '{' -> {
                    depth++
                }

                '}' -> {
                    depth--
                }
            }
            i--
        }
        return null
    }

    /**
     * Extracts the tag name immediately following a `<` character.
     * Supports dotted paths like `Module.Component`.
     */
    private fun extractTagNameAfterLt(
        text: CharSequence,
        start: Int,
    ): String? {
        val sb = StringBuilder()
        var i = start
        while (i < text.length) {
            val c = text[i]
            if (c.isLetterOrDigit() || c == '_' || c == '.') {
                sb.append(c)
            } else {
                break
            }
            i++
        }
        val name = sb.toString()
        // Don't treat as JSX if it's empty or ends with a dot
        if (name.isEmpty() || name.endsWith('.')) return null
        return name
    }
}
