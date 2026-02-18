package com.rescript.plugin.editor

import com.intellij.codeInsight.editorActions.moveUpDown.LineRange
import com.intellij.codeInsight.editorActions.moveUpDown.StatementUpDownMover
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType
import com.intellij.psi.util.PsiTreeUtil
import com.rescript.plugin.lang.RescriptTokenTypes
import com.rescript.plugin.lang.psi.RescriptElementTypes
import com.rescript.plugin.lang.psi.RescriptFile

class RescriptStatementUpDownMover : StatementUpDownMover() {
    companion object {
        /** All element types that represent movable declarations. */
        internal val DECLARATION_TYPES =
            setOf(
                RescriptElementTypes.LET_DECLARATION,
                RescriptElementTypes.TYPE_DECLARATION,
                RescriptElementTypes.MODULE_DECLARATION,
                RescriptElementTypes.EXTERNAL_DECLARATION,
                RescriptElementTypes.OPEN_STATEMENT,
                RescriptElementTypes.INCLUDE_STATEMENT,
                RescriptElementTypes.EXCEPTION_DECLARATION,
            )

        private val WHITESPACE_OR_COMMENT =
            setOf(
                TokenType.WHITE_SPACE,
                RescriptTokenTypes.EOL,
                RescriptTokenTypes.SINGLE_COMMENT,
                RescriptTokenTypes.MULTI_COMMENT,
            )
    }

    override fun checkAvailable(
        editor: Editor,
        file: PsiFile,
        info: MoveInfo,
        down: Boolean,
    ): Boolean {
        if (file !is RescriptFile) return false

        val offset = editor.caretModel.offset
        val element = file.findElementAt(offset) ?: return false

        val declaration = findDeclaration(element) ?: return false
        val firstElement = findLeadingAnnotation(declaration)

        val document = editor.document
        val sourceStartLine = document.getLineNumber(firstElement.textRange.startOffset)
        val sourceEndLine = document.getLineNumber(declaration.textRange.endOffset)

        val sibling =
            if (down) {
                findNextDeclaration(declaration)
            } else {
                findPreviousDeclaration(firstElement)
            } ?: return false

        val siblingFirst = findLeadingAnnotation(sibling)
        val siblingStartLine = document.getLineNumber(siblingFirst.textRange.startOffset)
        val siblingEndLine = document.getLineNumber(sibling.textRange.endOffset)

        info.toMove = LineRange(sourceStartLine, sourceEndLine + 1)
        info.toMove2 = LineRange(siblingStartLine, siblingEndLine + 1)

        return true
    }

    internal fun findDeclaration(element: PsiElement): PsiElement? =
        PsiTreeUtil.findFirstParent(element, false) { parent ->
            parent.node?.elementType in DECLARATION_TYPES
        }

    internal fun findLeadingAnnotation(declaration: PsiElement): PsiElement {
        var current = declaration
        while (true) {
            val prev = skipBackward(current) ?: break
            if (prev.node?.elementType == RescriptElementTypes.ANNOTATION) {
                current = prev
            } else {
                break
            }
        }
        return current
    }

    internal fun findNextDeclaration(element: PsiElement): PsiElement? {
        var current = element.nextSibling
        while (current != null) {
            val type = current.node?.elementType
            if (type in DECLARATION_TYPES) return current
            if (type == RescriptElementTypes.ANNOTATION) {
                // Annotations belong to the next declaration — find it
                var after = current.nextSibling
                while (after != null) {
                    val afterType = after.node?.elementType
                    if (afterType in DECLARATION_TYPES) return after
                    if (afterType == RescriptElementTypes.ANNOTATION || afterType in WHITESPACE_OR_COMMENT) {
                        after = after.nextSibling
                    } else {
                        break
                    }
                }
                return null
            }
            if (type !in WHITESPACE_OR_COMMENT) return null
            current = current.nextSibling
        }
        return null
    }

    internal fun findPreviousDeclaration(element: PsiElement): PsiElement? {
        var current = element.prevSibling
        while (current != null) {
            val type = current.node?.elementType
            if (type in DECLARATION_TYPES) return current
            if (type == RescriptElementTypes.ANNOTATION) {
                current = current.prevSibling
                continue
            }
            if (type !in WHITESPACE_OR_COMMENT) return null
            current = current.prevSibling
        }
        return null
    }

    private fun skipBackward(element: PsiElement): PsiElement? {
        var current = element.prevSibling
        while (current != null && current.node?.elementType in WHITESPACE_OR_COMMENT) {
            current = current.prevSibling
        }
        return current
    }
}
