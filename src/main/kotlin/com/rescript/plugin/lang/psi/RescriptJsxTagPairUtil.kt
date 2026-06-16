package com.rescript.plugin.lang.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.rescript.plugin.lang.RescriptTokenTypes

/**
 * Identifies which side of a paired JSX tag a position belongs to.
 *
 * @see RescriptJsxTagPairUtil
 */
enum class TagSide {
    /** The opening tag, e.g. the `div` in `<div>`. */
    OPEN,

    /** The closing tag, e.g. the `div` in `</div>`. */
    CLOSE,
}

/**
 * Holds the opening and closing tag-name text and their absolute document ranges
 * for a single `JSX_ELEMENT`.
 *
 * Any field may be `null` when the corresponding tag part is absent (for example a
 * closing tag that the user has not finished typing yet).
 *
 * @see RescriptJsxTagPairUtil
 */
data class JsxTagNames(
    val openName: String?,
    val openRange: TextRange?,
    val closeName: String?,
    val closeRange: TextRange?,
)

/**
 * Shared PSI helper for working with paired JSX tags (`<div>...</div>`).
 *
 * Extracts opening/closing tag names and their absolute ranges from a `JSX_ELEMENT`
 * node and resolves which tag side a caret offset falls on. Used by the mismatched
 * close-tag inspection, the tag-pair highlight handler, and synchronized rename.
 *
 * The element child layout produced by the lightweight parser is flat: an opening
 * `TAG_LT` followed by one or more name tokens (with optional `DOT`-separated path
 * segments), then attributes, the opening `>`, children, the `TAG_LT_SLASH` (`</`),
 * the closing name tokens, and the closing `>`.
 *
 * @see com.rescript.plugin.lang.RescriptJsxParser
 */
object RescriptJsxTagPairUtil {
    private val NAME_TOKENS =
        setOf(RescriptTokenTypes.JSX_TAG_NAME, RescriptTokenTypes.JSX_COMPONENT_NAME)

    private val OPEN_TAG_END =
        setOf(RescriptTokenTypes.TAG_GT, RescriptTokenTypes.GT)

    /**
     * Walks up from [element] to the nearest enclosing `JSX_ELEMENT`.
     *
     * Returns the innermost `JSX_ELEMENT` ancestor (or [element] itself when it is one),
     * or `null` if no `JSX_ELEMENT` encloses the position. Self-closing elements and
     * fragments are intentionally ignored because they have no paired tag names.
     *
     * @param element the PSI element to start from (typically a leaf token at the caret)
     * @return the enclosing `JSX_ELEMENT`, or `null`
     */
    fun findEnclosingJsxElement(element: PsiElement): PsiElement? {
        var current: PsiElement? = element
        while (current != null) {
            if (current.node?.elementType == RescriptElementTypes.JSX_ELEMENT) {
                return current
            }
            current = current.parent
        }
        return null
    }

    /**
     * Extracts the opening and closing tag names (and their absolute ranges) from a
     * `JSX_ELEMENT`.
     *
     * Iterates only the element's direct children, so nested JSX elements never leak
     * their own tag tokens into the result. A dotted module path such as `Module.Sub`
     * is collected verbatim, including the `.` separators, so both sides compare
     * structurally.
     *
     * @param jsxElement a PSI element whose node type is `JSX_ELEMENT`
     * @return the resolved names and ranges; absent parts are `null`
     */
    fun extractTagNames(jsxElement: PsiElement): JsxTagNames {
        var openName: StringBuilder? = null
        var openStart = -1
        var openEnd = -1
        var closeName: StringBuilder? = null
        var closeStart = -1
        var closeEnd = -1

        // Phases: 0 = before open name, 1 = collecting open name,
        // 2 = body (after open tag), 3 = collecting close name, 4 = done.
        var phase = 0

        for (child in jsxElement.node.getChildren(null)) {
            val type = child.elementType
            when (phase) {
                0 -> {
                    if (type == RescriptTokenTypes.TAG_LT) phase = 1
                }

                1 -> {
                    if (type in NAME_TOKENS || type == RescriptTokenTypes.DOT) {
                        if (openName == null) {
                            openName = StringBuilder()
                            openStart = child.startOffset
                        }
                        openName.append(child.text)
                        openEnd = child.startOffset + child.textLength
                    } else if (type in OPEN_TAG_END) {
                        phase = 2
                    } else if (openName != null) {
                        // First attribute token after the name ends the name run.
                        phase = 2
                    }
                }

                2 -> {
                    if (type == RescriptTokenTypes.TAG_LT_SLASH) phase = 3
                }

                3 -> {
                    if (type in NAME_TOKENS || type == RescriptTokenTypes.DOT) {
                        if (closeName == null) {
                            closeName = StringBuilder()
                            closeStart = child.startOffset
                        }
                        closeName.append(child.text)
                        closeEnd = child.startOffset + child.textLength
                    } else {
                        phase = 4
                    }
                }
            }
        }

        return JsxTagNames(
            openName = openName?.toString(),
            openRange = if (openStart >= 0) TextRange(openStart, openEnd) else null,
            closeName = closeName?.toString(),
            closeRange = if (closeStart >= 0) TextRange(closeStart, closeEnd) else null,
        )
    }

    /**
     * Resolves which paired-tag side the absolute [offset] falls on within [jsxElement].
     *
     * @param jsxElement a PSI element whose node type is `JSX_ELEMENT`
     * @param offset an absolute document offset
     * @return [TagSide.OPEN] / [TagSide.CLOSE], or `null` when the offset is outside both names
     */
    fun sideAt(
        jsxElement: PsiElement,
        offset: Int,
    ): TagSide? {
        val names = extractTagNames(jsxElement)
        if (names.openRange?.containsOffset(offset) == true) return TagSide.OPEN
        if (names.closeRange?.containsOffset(offset) == true) return TagSide.CLOSE
        return null
    }
}
