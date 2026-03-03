package com.rescript.plugin.editor

import com.intellij.codeInsight.hints.ChangeListener
import com.intellij.codeInsight.hints.ImmediateConfigurable
import com.intellij.codeInsight.hints.InlayHintsCollector
import com.intellij.codeInsight.hints.InlayHintsProvider
import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.NoSettings
import com.intellij.codeInsight.hints.SettingsKey
import com.intellij.codeInsight.hints.presentation.PresentationFactory
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.rescript.plugin.RescriptLanguage

/**
 * Inlay hints provider that validates code examples found in ReScript
 * documentation comments (`/** ... */`).
 *
 * Detects `@example` blocks or code fences within doc comments and performs
 * basic syntax validation using keyword matching. Displays a green checkmark
 * for valid syntax or a warning marker for potentially invalid code.
 *
 * Uses unstable `InlayHintsProvider` API — review on platform upgrade.
 *
 * @see InlayHintsProvider
 */
@Suppress("UnstableApiUsage")
class RescriptCommentEvalProvider : InlayHintsProvider<NoSettings> {
    override val key: SettingsKey<NoSettings> = SettingsKey("rescript.comment.eval.hints")

    override val name: String = "Comment code validation"

    override val previewText: String =
        """
        /**
         * @example
         * let x = 42
         * Js.log(x)
         */
        let add = (a, b) => a + b
        """.trimIndent()

    override fun createSettings(): NoSettings = NoSettings()

    override fun createConfigurable(settings: NoSettings): ImmediateConfigurable =
        object : ImmediateConfigurable {
            override fun createComponent(listener: ChangeListener): javax.swing.JComponent = javax.swing.JPanel()
        }

    override fun getCollectorFor(
        file: PsiFile,
        editor: Editor,
        settings: NoSettings,
        sink: InlayHintsSink,
    ): InlayHintsCollector? {
        if (file.language != RescriptLanguage) return null

        return object : InlayHintsCollector {
            override fun collect(
                element: PsiElement,
                editor: Editor,
                sink: InlayHintsSink,
            ): Boolean {
                if (element != file) return true

                val factory = PresentationFactory(editor)
                val examples = findCodeExamples(element.text)

                for (example in examples) {
                    val isValid = validateSyntax(example.code)
                    val label = if (isValid) "valid syntax" else "syntax warning"
                    val presentation = factory.smallText(label)
                    sink.addInlineElement(example.endOffset, false, presentation, false)
                }

                return false
            }
        }
    }

    companion object {
        /** A code example extracted from a documentation comment. */
        data class CodeExample(
            val code: String,
            val startOffset: Int,
            val endOffset: Int,
        )

        // Pattern to find doc comments (/** ... */)
        private val DOC_COMMENT_PATTERN = Regex("""/\*\*[\s\S]*?\*/""")

        // Pattern to find @example blocks
        private val EXAMPLE_BLOCK_PATTERN = Regex("""@example\s*\n([\s\S]*?)(?=\n\s*\*\s*@|\n\s*\*/)""")

        // Pattern to find code fences (```rescript ... ``` or ``` ... ```)
        private val CODE_FENCE_PATTERN = Regex("""```(?:rescript)?\s*\n([\s\S]*?)```""")

        /**
         * Finds code examples within documentation comments in source text.
         *
         * Detects both `@example` blocks and code fences (```) within
         * `/** ... */` comments.
         *
         * @param sourceText the full source text
         * @return list of code examples with their positions
         */
        internal fun findCodeExamples(sourceText: String): List<CodeExample> {
            val examples = mutableListOf<CodeExample>()

            for (docMatch in DOC_COMMENT_PATTERN.findAll(sourceText)) {
                val docText = docMatch.value
                val docStart = docMatch.range.first

                // Look for @example blocks
                for (exampleMatch in EXAMPLE_BLOCK_PATTERN.findAll(docText)) {
                    val code = cleanExampleCode(exampleMatch.groupValues[1])
                    if (code.isNotBlank()) {
                        examples.add(
                            CodeExample(
                                code = code,
                                startOffset = docStart + exampleMatch.range.first,
                                endOffset = docStart + exampleMatch.range.last + 1,
                            ),
                        )
                    }
                }

                // Look for code fences
                for (fenceMatch in CODE_FENCE_PATTERN.findAll(docText)) {
                    val code = cleanExampleCode(fenceMatch.groupValues[1])
                    if (code.isNotBlank()) {
                        examples.add(
                            CodeExample(
                                code = code,
                                startOffset = docStart + fenceMatch.range.first,
                                endOffset = docStart + fenceMatch.range.last + 1,
                            ),
                        )
                    }
                }
            }

            return examples
        }

        /**
         * Validates whether a code example has valid ReScript-like syntax.
         *
         * Performs keyword-based heuristic validation: checks that the code
         * contains recognizable ReScript keywords or patterns and doesn't
         * contain obviously invalid constructs.
         *
         * @param code the code example text
         * @return true if the syntax appears valid
         */
        internal fun validateSyntax(code: String): Boolean {
            val trimmed = code.trim()
            if (trimmed.isEmpty()) return false

            // Check for balanced delimiters
            if (!hasBalancedDelimiters(trimmed)) return false

            // Check for ReScript keywords/patterns
            val hasValidPatterns = VALID_PATTERNS.any { it.containsMatchIn(trimmed) }
            val hasInvalidPatterns = INVALID_PATTERNS.any { it.containsMatchIn(trimmed) }

            return hasValidPatterns && !hasInvalidPatterns
        }

        /**
         * Cleans up code extracted from doc comments by removing leading `*` markers.
         *
         * @param code the raw code from the doc comment
         * @return cleaned code text
         */
        internal fun cleanExampleCode(code: String): String =
            code
                .lines()
                .joinToString("\n") { it.trimStart().removePrefix("* ").removePrefix("*") }
                .trim()

        private fun hasBalancedDelimiters(code: String): Boolean {
            var parens = 0
            var braces = 0
            var brackets = 0
            for (ch in code) {
                when (ch) {
                    '(' -> parens++
                    ')' -> parens--
                    '{' -> braces++
                    '}' -> braces--
                    '[' -> brackets++
                    ']' -> brackets--
                }
                if (parens < 0 || braces < 0 || brackets < 0) return false
            }
            return parens == 0 && braces == 0 && brackets == 0
        }

        // Patterns that indicate valid ReScript code
        private val VALID_PATTERNS =
            listOf(
                Regex("""^\s*let\s""", RegexOption.MULTILINE),
                Regex("""^\s*type\s""", RegexOption.MULTILINE),
                Regex("""^\s*module\s""", RegexOption.MULTILINE),
                Regex("""^\s*open\s""", RegexOption.MULTILINE),
                Regex("""^\s*switch\s""", RegexOption.MULTILINE),
                Regex("""^\s*if\s""", RegexOption.MULTILINE),
                Regex("""=>"""),
                Regex("""->\w"""),
                Regex("""Js\.\w"""),
                Regex("""<\w+"""),
            )

        // Patterns that indicate non-ReScript code
        private val INVALID_PATTERNS =
            listOf(
                Regex("""^\s*var\s""", RegexOption.MULTILINE),
                Regex("""^\s*const\s""", RegexOption.MULTILINE),
                Regex("""^\s*function\s""", RegexOption.MULTILINE),
                Regex("""===|!=="""),
                Regex("""console\.log"""),
            )
    }
}
