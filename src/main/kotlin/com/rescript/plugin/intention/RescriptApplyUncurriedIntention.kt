package com.rescript.plugin.intention

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.rescript.plugin.indexing.RescriptNameIndex
import com.rescript.plugin.lang.psi.RescriptDeclarationPsiElement
import com.rescript.plugin.util.RescriptEditorUtils.getLineRangeAt
import com.rescript.plugin.util.RescriptEditorUtils.getLineTextAt
import com.rescript.plugin.util.RescriptEditorUtils.replaceInWriteAction

/**
 * Native PSI fallback for the `applyUncurried` LSP code action.
 *
 * Detects a curried call site `f(x, y)` whose target `f` is defined with the
 * uncurried syntax `let f = (. x, y) => ...` and offers an intention that
 * rewrites the call to `f(. x, y)`. Useful on legacy ReScript v10 / v11
 * codebases where the language server's `applyUncurried` quick fix may not
 * trigger; provides feature parity through pure PSI / lexer analysis.
 *
 * Only the call site is rewritten — the definition is left unchanged.
 *
 * @see RescriptBaseIntention
 */
class RescriptApplyUncurriedIntention : RescriptBaseIntention() {
    override fun getText(): String = "Convert call to uncurried form"

    override fun isAvailableInRescript(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ): Boolean {
        val document = editor?.document ?: return false
        val offset = element.textRange.startOffset
        val line = document.getLineTextAt(offset)
        val (lineStart, _) = document.getLineRangeAt(offset)
        val column = offset - lineStart

        val callRange = findCallExpressionAt(line, column) ?: return false
        if (isAlreadyUncurried(line, callRange)) return false
        val identifier = line.substring(callRange.first, callRange.last + 1).substringBefore('(')

        return hasUncurriedDefinition(project, identifier)
    }

    override fun invoke(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ) {
        editor ?: return
        val document = editor.document
        val offset = element.textRange.startOffset
        val line = document.getLineTextAt(offset)
        val (lineStart, lineEnd) = document.getLineRangeAt(offset)
        val column = offset - lineStart

        val callRange = findCallExpressionAt(line, column) ?: return
        val callText = line.substring(callRange.first, callRange.last + 1)
        val newCallText = insertDotAfterOpenParen(callText) ?: return

        val rewritten = line.substring(0, callRange.first) + newCallText + line.substring(callRange.last + 1)
        document.replaceInWriteAction(project, lineStart, lineEnd, rewritten)
    }

    private fun hasUncurriedDefinition(
        project: Project,
        name: String,
    ): Boolean {
        val scope = GlobalSearchScope.allScope(project)
        var found = false
        StubIndex.getInstance().processElements(
            RescriptNameIndex.KEY,
            name,
            project,
            scope,
            RescriptDeclarationPsiElement::class.java,
        ) { decl ->
            val text = decl.text
            // Inspect only the head of the declaration; uncurried syntax appears
            // before the first `=>` or before the first `;`/newline.
            val head = text.substringBefore("=>", text).substringBefore('\n')
            if (LET_UNCURRIED_HEAD_PATTERN.containsMatchIn(head)) {
                found = true
                false // stop iteration
            } else {
                true
            }
        }
        return found
    }

    companion object {
        // Matches the head of a `let` declaration with uncurried first-arg syntax,
        // e.g. `let f = (. x, y)` or `let g = (.x)`. The dot may be followed by
        // whitespace and either an identifier or the closing paren (zero-arg form).
        private val LET_UNCURRIED_HEAD_PATTERN = Regex("""\blet\s+\w+(?:\s*:\s*[^=]+)?\s*=\s*\(\s*\.\s*""")

        // Matches an identifier directly followed by `(`, ignoring intervening whitespace,
        // so we can locate the call expression bounds at the caret column.
        private val IDENT_WITH_PAREN_PATTERN = Regex("""([A-Za-z_][A-Za-z0-9_']*)\s*\(""")

        /**
         * Locates the byte range of an `identifier(args)` call expression that contains
         * the given caret column on a single line.
         *
         * @param line the editor line text (no line separator)
         * @param column zero-based caret column on the line
         * @return the inclusive range of `identifier(...)` covering the column, or null
         *     when the column is not inside a call expression
         */
        fun findCallExpressionAt(
            line: String,
            column: Int,
        ): IntRange? {
            for (match in IDENT_WITH_PAREN_PATTERN.findAll(line)) {
                val identStart = match.range.first
                val openParen = line.indexOf('(', identStart)
                if (openParen < 0) continue
                val closeParen = matchingCloseParen(line, openParen) ?: continue
                if (column in identStart..closeParen) {
                    return identStart..closeParen
                }
            }
            return null
        }

        /**
         * Returns true when the call expression at [callRange] in [line] already uses
         * the uncurried `(.` form, in which case the intention should not be offered.
         */
        fun isAlreadyUncurried(
            line: String,
            callRange: IntRange,
        ): Boolean {
            val openParen = line.indexOf('(', callRange.first)
            if (openParen < 0 || openParen >= callRange.last) return false
            // Inspect characters after `(`, skipping whitespace, looking for a leading `.`.
            var i = openParen + 1
            while (i <= callRange.last && line[i].isWhitespace()) i++
            return i <= callRange.last && line[i] == '.'
        }

        /**
         * Rewrites a call expression text from curried to uncurried form by inserting
         * `. ` after the opening parenthesis (or `.` for the zero-argument form).
         *
         * Preserves the identifier prefix and the closing `)`. Returns null when the
         * input does not look like an `identifier(...)` call.
         */
        fun insertDotAfterOpenParen(callText: String): String? {
            val openParen = callText.indexOf('(')
            val closeParen = callText.lastIndexOf(')')
            if (openParen < 0 || closeParen < openParen) return null

            val prefix = callText.substring(0, openParen + 1)
            val argsBlock = callText.substring(openParen + 1, closeParen)
            val suffix = callText.substring(closeParen) // ")" plus anything after

            return if (argsBlock.isBlank()) {
                "$prefix.$suffix"
            } else {
                "$prefix. $argsBlock$suffix"
            }
        }

        private fun matchingCloseParen(
            line: String,
            openIndex: Int,
        ): Int? {
            var depth = 0
            for (i in openIndex until line.length) {
                when (line[i]) {
                    '(' -> {
                        depth++
                    }

                    ')' -> {
                        depth--
                        if (depth == 0) return i
                    }
                }
            }
            return null
        }
    }
}
