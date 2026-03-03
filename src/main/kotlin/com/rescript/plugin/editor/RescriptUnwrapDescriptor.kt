package com.rescript.plugin.editor

import com.intellij.codeInsight.unwrap.UnwrapDescriptor
import com.intellij.codeInsight.unwrap.Unwrapper
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.Pair
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.rescript.plugin.lang.psi.RescriptFile

/**
 * Provides "Unwrap/Remove" actions (Ctrl+Shift+Delete) for ReScript code.
 *
 * Supports unwrapping: `Some(expr)`, `Ok(expr)`, `Error(expr)`,
 * `if (...) { body }`, `switch expr { | _ => body }`,
 * `try { body } catch { ... }`, and `{ body }`.
 *
 * @see com.rescript.plugin.surround.RescriptSurroundDescriptor for the inverse operation
 */
class RescriptUnwrapDescriptor : UnwrapDescriptor {
    companion object {
        /** Pattern matching `if` keyword followed by `(`, avoiding partial identifier matches. */
        private val IF_PATTERN = Regex("""(?<![a-zA-Z0-9_])if\s*\(""")

        /** Pattern matching `switch` keyword, avoiding partial identifier matches. */
        private val SWITCH_PATTERN = Regex("""(?<![a-zA-Z0-9_])switch\s""")

        /** Pattern matching `try` keyword followed by `{`, avoiding partial identifier matches. */
        private val TRY_PATTERN = Regex("""(?<![a-zA-Z0-9_])try\s*\{""")
    }

    override fun collectUnwrappers(
        project: com.intellij.openapi.project.Project,
        editor: Editor,
        file: PsiFile,
    ): List<Pair<PsiElement, Unwrapper>> {
        if (file !is RescriptFile) return emptyList()

        val offset = editor.caretModel.offset
        val document = editor.document
        val text = document.text
        val results = mutableListOf<Pair<PsiElement, Unwrapper>>()

        // Dummy element for pairing — we operate on text, not PSI structure
        val element = file.findElementAt(offset) ?: return emptyList()

        // Check for wrapper function patterns: Some(...), Ok(...), Error(...)
        for (wrapper in listOf("Some", "Ok", "Error")) {
            findWrapperRange(text, offset, wrapper)?.let { (start, end) ->
                results.add(Pair(element, RescriptFunctionUnwrapper(wrapper, start, end)))
            }
        }

        // Check for block patterns: if, switch, try, { }
        findIfRange(text, offset)?.let { (start, bodyStart, bodyEnd, end) ->
            results.add(Pair(element, RescriptBlockUnwrapper("if (...) { ... }", start, bodyStart, bodyEnd, end)))
        }

        findSwitchBodyRange(text, offset)?.let { (start, bodyStart, bodyEnd, end) ->
            results.add(Pair(element, RescriptBlockUnwrapper("switch ... { ... }", start, bodyStart, bodyEnd, end)))
        }

        findTryRange(text, offset)?.let { (start, bodyStart, bodyEnd, end) ->
            results.add(
                Pair(element, RescriptBlockUnwrapper("try { ... } catch { ... }", start, bodyStart, bodyEnd, end)),
            )
        }

        findBraceRange(text, offset)?.let { (start, end) ->
            results.add(Pair(element, RescriptBraceUnwrapper(start, end)))
        }

        return results
    }

    override fun showOptionsDialog(): Boolean = true

    override fun shouldTryToRestoreCaretPosition(): Boolean = true

    // ── Helper: find wrapper function range ──────────────────────

    /**
     * Finds a wrapper function pattern like `Some(expr)` around the cursor.
     *
     * @return pair of (wrapper start, closing paren + 1), or null
     */
    internal fun findWrapperRange(
        text: String,
        offset: Int,
        wrapper: String,
    ): kotlin.Pair<Int, Int>? {
        // Search backward from cursor for the wrapper name
        val searchStart = maxOf(0, offset - wrapper.length - 1)
        val searchEnd = minOf(text.length, offset + wrapper.length + 1)
        val region =
            text.substring(
                maxOf(0, searchStart - wrapper.length),
                minOf(
                    text.length,
                    searchEnd + wrapper.length,
                ),
            )
        val regionOffset = maxOf(0, searchStart - wrapper.length)

        var idx = region.indexOf("$wrapper(")
        while (idx >= 0) {
            val absStart = regionOffset + idx
            val parenStart = absStart + wrapper.length
            val parenEnd = findMatchingParen(text, parenStart)
            if (parenEnd != null && absStart <= offset && offset <= parenEnd + 1) {
                return kotlin.Pair(absStart, parenEnd + 1)
            }
            idx = region.indexOf("$wrapper(", idx + 1)
        }
        return null
    }

    /**
     * Finds the matching closing parenthesis for an opening paren at [openIndex].
     */
    internal fun findMatchingParen(
        text: String,
        openIndex: Int,
    ): Int? = findMatchingBracket(text, openIndex, '(', ')')

    /**
     * Finds `if (...) { body }` around cursor.
     *
     * @return (ifStart, bodyStart, bodyEnd, totalEnd) or null
     */
    internal fun findIfRange(
        text: String,
        offset: Int,
    ): Quadruple? {
        // Search backward for 'if' keyword
        val searchStart = maxOf(0, offset - 200)
        val region = text.substring(searchStart, minOf(text.length, offset + 200))
        for (match in IF_PATTERN.findAll(region)) {
            val absIfStart = searchStart + match.range.first
            // Find the condition's closing paren
            val condOpenIdx = text.indexOf('(', absIfStart + 2)
            if (condOpenIdx < 0) continue
            val condCloseIdx = findMatchingParen(text, condOpenIdx) ?: continue
            // Find the body opening brace
            val bodyOpenIdx = findNextNonWhitespace(text, condCloseIdx + 1)
            if (bodyOpenIdx == null || bodyOpenIdx >= text.length || text[bodyOpenIdx] != '{') continue
            val bodyCloseIdx = findMatchingBrace(text, bodyOpenIdx) ?: continue
            if (absIfStart <= offset && offset <= bodyCloseIdx + 1) {
                return Quadruple(absIfStart, bodyOpenIdx + 1, bodyCloseIdx, bodyCloseIdx + 1)
            }
        }
        return null
    }

    /**
     * Finds `switch expr { ... | _ => body }` around cursor and extracts the last arm's body.
     */
    internal fun findSwitchBodyRange(
        text: String,
        offset: Int,
    ): Quadruple? {
        val searchStart = maxOf(0, offset - 300)
        val region = text.substring(searchStart, minOf(text.length, offset + 300))
        for (match in SWITCH_PATTERN.findAll(region)) {
            val absSwitchStart = searchStart + match.range.first
            // Find the body opening brace
            val braceIdx = text.indexOf('{', absSwitchStart + 6)
            if (braceIdx < 0) continue
            val braceEndIdx = findMatchingBrace(text, braceIdx) ?: continue
            if (absSwitchStart <= offset && offset <= braceEndIdx + 1) {
                return Quadruple(absSwitchStart, braceIdx + 1, braceEndIdx, braceEndIdx + 1)
            }
        }
        return null
    }

    /**
     * Finds `try { body } catch { ... }` around cursor.
     */
    internal fun findTryRange(
        text: String,
        offset: Int,
    ): Quadruple? {
        val searchStart = maxOf(0, offset - 300)
        val region = text.substring(searchStart, minOf(text.length, offset + 300))
        for (match in TRY_PATTERN.findAll(region)) {
            val absTryStart = searchStart + match.range.first
            val tryBraceIdx = text.indexOf('{', absTryStart + 3)
            if (tryBraceIdx < 0) continue
            val tryBraceEnd = findMatchingBrace(text, tryBraceIdx) ?: continue
            // Find the catch block
            val catchIdx = findNextNonWhitespace(text, tryBraceEnd + 1) ?: continue
            val remaining = text.substring(catchIdx)
            if (!remaining.startsWith("catch")) continue
            val catchBraceIdx = text.indexOf('{', catchIdx + 5)
            if (catchBraceIdx < 0) continue
            val catchBraceEnd = findMatchingBrace(text, catchBraceIdx) ?: continue
            if (absTryStart <= offset && offset <= catchBraceEnd + 1) {
                return Quadruple(absTryStart, tryBraceIdx + 1, tryBraceEnd, catchBraceEnd + 1)
            }
        }
        return null
    }

    /**
     * Finds the nearest enclosing `{ ... }` block around cursor.
     */
    internal fun findBraceRange(
        text: String,
        offset: Int,
    ): kotlin.Pair<Int, Int>? {
        // Search backward for nearest unmatched '{'
        var depth = 0
        var i = offset - 1
        while (i >= 0) {
            when (text[i]) {
                '}' -> depth++
                '{' -> {
                    if (depth == 0) {
                        val end = findMatchingBrace(text, i) ?: return null
                        return kotlin.Pair(i, end + 1)
                    }
                    depth--
                }
            }
            i--
        }
        return null
    }

    internal fun findMatchingBrace(
        text: String,
        openIndex: Int,
    ): Int? = findMatchingBracket(text, openIndex, '{', '}')

    /**
     * Finds the matching closing bracket for an opening bracket at [openIndex].
     *
     * @param text the source text to scan
     * @param openIndex the index of the opening bracket character
     * @param openChar the opening bracket character (e.g. '(' or '{')
     * @param closeChar the closing bracket character (e.g. ')' or '}')
     * @return the index of the matching closing bracket, or null if not found
     */
    internal fun findMatchingBracket(
        text: String,
        openIndex: Int,
        openChar: Char,
        closeChar: Char,
    ): Int? {
        if (openIndex >= text.length || text[openIndex] != openChar) return null
        var depth = 0
        var i = openIndex
        while (i < text.length) {
            when (text[i]) {
                openChar -> depth++
                closeChar -> {
                    depth--
                    if (depth == 0) return i
                }
            }
            i++
        }
        return null
    }

    private fun findNextNonWhitespace(
        text: String,
        start: Int,
    ): Int? {
        var i = start
        while (i < text.length) {
            if (!text[i].isWhitespace()) return i
            i++
        }
        return null
    }

    /** Data holder for block unwrap ranges. */
    internal data class Quadruple(
        val outerStart: Int,
        val bodyStart: Int,
        val bodyEnd: Int,
        val outerEnd: Int,
    )
}
