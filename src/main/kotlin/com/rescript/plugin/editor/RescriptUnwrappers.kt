package com.rescript.plugin.editor

import com.intellij.codeInsight.unwrap.Unwrapper
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement

/**
 * Base unwrapper for ReScript that operates on text ranges rather than PSI structure.
 *
 * Since the ReScript parser is lightweight (top-level declarations only),
 * unwrap operations use document text offsets instead of PSI manipulation.
 */
abstract class RescriptBaseUnwrapper(
    private val description: String,
) : Unwrapper {
    override fun isApplicableTo(e: PsiElement): Boolean = true

    override fun getDescription(e: PsiElement): String = description

    override fun collectElementsToIgnore(
        element: PsiElement,
        toIgnore: MutableSet<PsiElement>,
    ) {}

    override fun collectAffectedElements(
        element: PsiElement,
        toExtract: MutableList<in PsiElement>,
    ): PsiElement {
        toExtract.add(element)
        return element
    }
}

/**
 * Unwraps a function call pattern like `Some(expr)` to `expr`.
 */
class RescriptFunctionUnwrapper(
    private val wrapper: String,
    private val rangeStart: Int,
    private val rangeEnd: Int,
) : RescriptBaseUnwrapper("Remove $wrapper(...)") {
    override fun unwrap(
        editor: Editor,
        element: PsiElement,
    ): MutableList<PsiElement> {
        val document = editor.document
        val text = document.text
        val innerStart = rangeStart + wrapper.length + 1
        val innerEnd = rangeEnd - 1
        if (innerStart < innerEnd) {
            val inner = text.substring(innerStart, innerEnd)
            document.replaceString(rangeStart, rangeEnd, inner)
        }
        return mutableListOf()
    }
}

/**
 * Unwraps a block construct like `if (...) { body }` to `body`.
 */
class RescriptBlockUnwrapper(
    description: String,
    private val outerStart: Int,
    private val bodyStart: Int,
    private val bodyEnd: Int,
    private val outerEnd: Int,
) : RescriptBaseUnwrapper(description) {
    override fun unwrap(
        editor: Editor,
        element: PsiElement,
    ): MutableList<PsiElement> {
        val document = editor.document
        val text = document.text
        val body = text.substring(bodyStart, bodyEnd).trim()
        document.replaceString(outerStart, outerEnd, body)
        return mutableListOf()
    }
}

/**
 * Unwraps a bare brace block `{ body }` to `body`.
 */
class RescriptBraceUnwrapper(
    private val braceStart: Int,
    private val braceEnd: Int,
) : RescriptBaseUnwrapper("Remove { }") {
    override fun unwrap(
        editor: Editor,
        element: PsiElement,
    ): MutableList<PsiElement> {
        val document = editor.document
        val text = document.text
        val body = text.substring(braceStart + 1, braceEnd - 1).trim()
        document.replaceString(braceStart, braceEnd, body)
        return mutableListOf()
    }
}
