package com.rescript.plugin.highlight

import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerBase
import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerFactory
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.util.Consumer
import com.rescript.plugin.RescriptLanguage
import com.rescript.plugin.lang.RescriptTokenTypes as T

/**
 * Factory for ReScript keyword-based highlight usages handlers.
 *
 * When the caret is on a control-flow keyword (`switch`, `if`, `try`, `|`),
 * all related branches are highlighted to show the structure at a glance.
 */
class RescriptHighlightUsagesHandlerFactory : HighlightUsagesHandlerFactory {
    override fun createHighlightUsagesHandler(
        editor: Editor,
        file: PsiFile,
    ): HighlightUsagesHandlerBase<PsiElement>? {
        if (file.language != RescriptLanguage) return null

        val offset = editor.caretModel.offset
        val element = file.findElementAt(offset) ?: return null
        val tokenType = element.node?.elementType ?: return null

        return when (tokenType) {
            T.SWITCH -> RescriptKeywordHighlightHandler(editor, file, element, KeywordKind.SWITCH)
            T.IF -> RescriptKeywordHighlightHandler(editor, file, element, KeywordKind.IF)
            T.TRY -> RescriptKeywordHighlightHandler(editor, file, element, KeywordKind.TRY)
            T.PIPE -> RescriptKeywordHighlightHandler(editor, file, element, KeywordKind.PIPE)
            T.CATCH -> RescriptKeywordHighlightHandler(editor, file, element, KeywordKind.CATCH)
            T.ELSE -> RescriptKeywordHighlightHandler(editor, file, element, KeywordKind.ELSE)
            else -> null
        }
    }
}

/** The kind of keyword that triggered the highlight. */
internal enum class KeywordKind {
    SWITCH,
    IF,
    TRY,
    PIPE,
    CATCH,
    ELSE,
}

/**
 * Highlights related control-flow keywords for a given trigger keyword.
 *
 * Uses brace-balance scanning to find matching keywords at the same nesting level.
 *
 * @param editor the current editor
 * @param file the current PSI file
 * @param target the keyword element that triggered the highlight
 * @param kind the type of keyword
 */
internal class RescriptKeywordHighlightHandler(
    editor: Editor,
    file: PsiFile,
    private val target: PsiElement,
    private val kind: KeywordKind,
) : HighlightUsagesHandlerBase<PsiElement>(editor, file) {
    override fun getTargets(): List<PsiElement> = listOf(target)

    override fun selectTargets(
        targets: List<PsiElement>,
        selectionConsumer: Consumer<in List<PsiElement>>,
    ) {
        selectionConsumer.consume(targets)
    }

    override fun computeUsages(targets: List<PsiElement>) {
        val results =
            when (kind) {
                KeywordKind.SWITCH -> collectSwitchRelated(target)
                KeywordKind.IF -> collectIfRelated(target)
                KeywordKind.TRY -> collectTryRelated(target)
                KeywordKind.PIPE -> collectPipeRelated(target)
                KeywordKind.CATCH -> collectCatchRelated(target)
                KeywordKind.ELSE -> collectElseRelated(target)
            }
        results.forEach { myReadUsages.add(it) }
    }

    /**
     * From a `switch` keyword, collects the switch keyword itself and all
     * pipe (`|`) tokens at the same brace nesting level.
     */
    private fun collectSwitchRelated(switchElement: PsiElement): List<TextRange> {
        val ranges = mutableListOf(switchElement.textRange)

        // Scan forward for the opening brace, then collect pipes at depth 0
        var current: PsiElement? = switchElement.nextSibling
        var foundBrace = false
        var braceDepth = 0

        while (current != null) {
            val type = current.node?.elementType
            when {
                type == T.LBRACE -> {
                    if (!foundBrace) {
                        foundBrace = true
                        braceDepth = 0
                    } else {
                        braceDepth++
                    }
                }
                type == T.RBRACE -> {
                    if (foundBrace && braceDepth == 0) break
                    braceDepth--
                }
                type == T.PIPE && foundBrace && braceDepth == 0 -> {
                    ranges.add(current.textRange)
                }
            }
            current = current.nextSibling
        }

        return ranges
    }

    /**
     * From an `if` keyword, collects the if itself and all `else`/`else if` branches.
     */
    private fun collectIfRelated(ifElement: PsiElement): List<TextRange> {
        val ranges = mutableListOf(ifElement.textRange)
        var current: PsiElement? = ifElement.nextSibling
        var braceDepth = 0

        while (current != null) {
            val type = current.node?.elementType
            when {
                type == T.LBRACE -> braceDepth++
                type == T.RBRACE -> {
                    braceDepth--
                    if (braceDepth <= 0) {
                        // Check if next meaningful token is ELSE
                        val next = skipWhitespace(current.nextSibling)
                        if (next?.node?.elementType == T.ELSE) {
                            ranges.add(next.textRange)
                            // Check for else if
                            val afterElse = skipWhitespace(next.nextSibling)
                            if (afterElse?.node?.elementType == T.IF) {
                                ranges.add(afterElse.textRange)
                                current = afterElse.nextSibling
                                braceDepth = 0
                                continue
                            }
                        }
                        break
                    }
                }
            }
            current = current.nextSibling
        }

        return ranges
    }

    /** From a `try` keyword, collects try and catch keywords. */
    private fun collectTryRelated(tryElement: PsiElement): List<TextRange> {
        val ranges = mutableListOf(tryElement.textRange)
        var current: PsiElement? = tryElement.nextSibling
        var braceDepth = 0

        while (current != null) {
            val type = current.node?.elementType
            when {
                type == T.LBRACE -> braceDepth++
                type == T.RBRACE -> braceDepth--
                type == T.CATCH && braceDepth <= 0 -> {
                    ranges.add(current.textRange)
                    break
                }
            }
            current = current.nextSibling
        }

        return ranges
    }

    /** From a `|` (pipe pattern arm), collects the enclosing switch and all pipes. */
    private fun collectPipeRelated(pipeElement: PsiElement): List<TextRange> {
        // Scan backward to find the enclosing switch
        var current: PsiElement? = pipeElement.prevSibling
        var braceDepth = 0

        while (current != null) {
            val type = current.node?.elementType
            when {
                type == T.RBRACE -> braceDepth++
                type == T.LBRACE -> {
                    if (braceDepth == 0) {
                        // Found the opening brace, look for switch before it
                        val beforeBrace = skipWhitespaceBackward(current.prevSibling)
                        if (beforeBrace != null) {
                            // Walk back to find the switch keyword
                            var scan: PsiElement? = beforeBrace
                            while (scan != null) {
                                if (scan.node?.elementType == T.SWITCH) {
                                    return collectSwitchRelated(scan)
                                }
                                scan = scan.prevSibling
                            }
                        }
                        break
                    }
                    braceDepth--
                }
            }
            current = current.prevSibling
        }

        return listOf(pipeElement.textRange)
    }

    /** From a `catch` keyword, finds the enclosing try. */
    private fun collectCatchRelated(catchElement: PsiElement): List<TextRange> {
        var current: PsiElement? = catchElement.prevSibling
        var braceDepth = 0

        while (current != null) {
            val type = current.node?.elementType
            when {
                type == T.RBRACE -> braceDepth++
                type == T.LBRACE -> braceDepth--
                type == T.TRY && braceDepth <= 0 -> {
                    return collectTryRelated(current)
                }
            }
            current = current.prevSibling
        }

        return listOf(catchElement.textRange)
    }

    /** From an `else` keyword, finds the enclosing if chain. */
    private fun collectElseRelated(elseElement: PsiElement): List<TextRange> {
        var current: PsiElement? = elseElement.prevSibling
        var braceDepth = 0

        while (current != null) {
            val type = current.node?.elementType
            when {
                type == T.RBRACE -> braceDepth++
                type == T.LBRACE -> braceDepth--
                type == T.IF && braceDepth <= 0 -> {
                    return collectIfRelated(current)
                }
            }
            current = current.prevSibling
        }

        return listOf(elseElement.textRange)
    }

    companion object {
        private fun skipWhitespace(element: PsiElement?): PsiElement? {
            var e = element
            while (e != null && e.node?.elementType == com.intellij.psi.TokenType.WHITE_SPACE) {
                e = e.nextSibling
            }
            return e
        }

        private fun skipWhitespaceBackward(element: PsiElement?): PsiElement? {
            var e = element
            while (e != null && e.node?.elementType == com.intellij.psi.TokenType.WHITE_SPACE) {
                e = e.prevSibling
            }
            return e
        }
    }
}
