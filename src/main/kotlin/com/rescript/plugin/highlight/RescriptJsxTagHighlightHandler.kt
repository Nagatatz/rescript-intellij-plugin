package com.rescript.plugin.highlight

import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerBase
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.util.Consumer
import com.rescript.plugin.lang.psi.RescriptJsxTagPairUtil

/**
 * Highlights the matching opposite tag name when the caret is on a JSX tag name.
 *
 * Given the enclosing `JSX_ELEMENT`, both the opening and closing tag-name ranges
 * are highlighted so the user can see the pair at a glance. When the closing tag is
 * absent (an unterminated element) only the opening range is highlighted.
 *
 * @param editor the current editor
 * @param file the current PSI file
 * @param jsxElement the enclosing `JSX_ELEMENT` resolved from the caret
 * @see RescriptJsxTagPairUtil
 */
internal class RescriptJsxTagHighlightHandler(
    editor: Editor,
    file: PsiFile,
    private val jsxElement: PsiElement,
) : HighlightUsagesHandlerBase<PsiElement>(editor, file) {
    override fun getTargets(): List<PsiElement> = listOf(jsxElement)

    override fun selectTargets(
        targets: List<PsiElement>,
        selectionConsumer: Consumer<in List<PsiElement>>,
    ) {
        selectionConsumer.consume(targets)
    }

    override fun computeUsages(targets: List<PsiElement>) {
        computeHighlightRanges(jsxElement).forEach { myReadUsages.add(it) }
    }

    companion object {
        /**
         * Computes the tag-name ranges to highlight for [jsxElement].
         *
         * Returns the opening and closing tag-name ranges in document order; either
         * may be absent (an unterminated element yields a single opening range, and a
         * fragment with no names yields an empty list).
         *
         * @param jsxElement a PSI element whose node type is `JSX_ELEMENT`
         * @return the ranges to highlight, in document order
         */
        fun computeHighlightRanges(jsxElement: PsiElement): List<TextRange> {
            val names = RescriptJsxTagPairUtil.extractTagNames(jsxElement)
            return listOfNotNull(names.openRange, names.closeRange)
        }
    }
}
