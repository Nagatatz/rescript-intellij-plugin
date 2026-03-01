package com.rescript.plugin.lsp

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
import com.rescript.plugin.lang.RescriptLexer
import com.rescript.plugin.lang.RescriptTokenTypes

/**
 * Provides inlay hints that visualize the effect of PPX annotations
 * in ReScript source files.
 *
 * Scans for `@` tokens using the lexer, extracts the annotation name,
 * and displays a concise description of what the PPX generates or does
 * (e.g., `@react.component` → "generates React.createElement").
 *
 * Can be toggled via Settings > Editor > Inlay Hints > ReScript > PPX annotations.
 *
 * @see InlayHintsProvider
 */
@Suppress("UnstableApiUsage")
class RescriptPpxVisualizationProvider : InlayHintsProvider<NoSettings> {
    override val key: SettingsKey<NoSettings> = SettingsKey("rescript.ppx.visualization")

    override val name: String = "PPX annotation descriptions"

    override val previewText: String =
        """
        @react.component
        let make = (~name: string) => {
          <div> {React.string(name)} </div>
        }
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

                val text = editor.document.text
                val annotations = findAnnotations(text)

                for ((offset, name) in annotations) {
                    val hint = ppxHintFor(name) ?: continue
                    val factory = PresentationFactory(editor)
                    val presentation =
                        factory.roundWithBackground(
                            factory.smallText(hint),
                        )
                    // Place the hint at the end of the annotation line
                    val lineNumber = editor.document.getLineNumber(offset)
                    val lineEnd = editor.document.getLineEndOffset(lineNumber)
                    sink.addInlineElement(lineEnd, false, presentation, false)
                }

                return false
            }
        }
    }

    companion object {
        /** Static mapping of PPX annotation names to human-readable descriptions. */
        internal val PPX_HINTS =
            mapOf(
                "react.component" to "generates React.createElement",
                "genType" to "generates .gen.tsx",
                "module" to "binds to JS module",
                "val" to "binds to JS value",
                "send" to "binds to JS method call",
                "get" to "binds to JS property getter",
                "set" to "binds to JS property setter",
                "new" to "binds to JS constructor",
                "deriving(json)" to "generates toJson + fromJson",
                "deriving(accessors)" to "generates field accessors",
                "unboxed" to "unboxed representation",
                "scope" to "binds to JS global scope",
                "string" to "generates string enum",
                "int" to "generates int enum",
                "unwrap" to "unwraps variant to JS",
                "return" to "wraps return in option",
                "obj" to "generates JS object",
                "variadic" to "variadic function binding",
                "as" to "alias binding name",
                "live" to "marks as intentionally unused",
                "dead" to "dead code annotation",
                "inline" to "inlines the value",
            )

        /**
         * Returns the hint text for a given PPX annotation name.
         *
         * @param annotationName the annotation name (without `@`)
         * @return the hint description, or null if unknown
         */
        internal fun ppxHintFor(annotationName: String): String? = PPX_HINTS[annotationName]

        /**
         * Finds all annotations in the source text using the lexer.
         *
         * Scans for `@` (ARROBASE) tokens followed by identifier tokens
         * and returns pairs of (offset, annotation name).
         *
         * @param text the source text
         * @return list of (offset, annotationName) pairs
         */
        internal fun findAnnotations(text: String): List<Pair<Int, String>> {
            val annotations = mutableListOf<Pair<Int, String>>()
            val lexer = RescriptLexer()
            lexer.start(text)

            while (lexer.tokenType != null) {
                if (lexer.tokenType == RescriptTokenTypes.ARROBASE) {
                    val atOffset = lexer.tokenStart
                    lexer.advance()

                    // Collect the annotation name (may contain dots like react.component)
                    val nameBuilder = StringBuilder()
                    while (lexer.tokenType != null) {
                        val tokenType = lexer.tokenType
                        val tokenText = text.substring(lexer.tokenStart, lexer.tokenEnd)

                        if (tokenType == RescriptTokenTypes.LIDENT ||
                            tokenType == RescriptTokenTypes.UIDENT ||
                            tokenType == RescriptTokenTypes.DOT
                        ) {
                            nameBuilder.append(tokenText)
                            lexer.advance()
                        } else if (tokenType == RescriptTokenTypes.LPAREN) {
                            // Include parenthesized argument like deriving(json)
                            nameBuilder.append(tokenText)
                            lexer.advance()
                            // Collect until closing paren
                            while (lexer.tokenType != null && lexer.tokenType != RescriptTokenTypes.RPAREN) {
                                nameBuilder.append(text.substring(lexer.tokenStart, lexer.tokenEnd))
                                lexer.advance()
                            }
                            if (lexer.tokenType == RescriptTokenTypes.RPAREN) {
                                nameBuilder.append(")")
                                lexer.advance()
                            }
                            break
                        } else {
                            break
                        }
                    }

                    val name = nameBuilder.toString()
                    if (name.isNotEmpty()) {
                        annotations.add(atOffset to name)
                    }
                    continue
                }
                lexer.advance()
            }

            return annotations
        }
    }
}
