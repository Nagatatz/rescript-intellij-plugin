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
import com.rescript.plugin.settings.RescriptProjectSettings

/**
 * Provides inline type hints for each step in a pipe chain (`->`).
 *
 * When a pipe chain like `arr->Array.map(f)->Array.filter(g)` is encountered,
 * this provider queries LSP hover at each pipe operator position and displays
 * the intermediate type as an inlay hint after the expression.
 *
 * Can be toggled via Settings > Editor > Inlay Hints > ReScript > Pipe chain types.
 *
 * @see InlayHintsProvider
 * Uses unstable `InlayHintsProvider` API — review on platform upgrade.
 *
 * @see RescriptLspUtils
 */
@Suppress("UnstableApiUsage")
class RescriptPipeChainTypeHintsProvider : InlayHintsProvider<NoSettings> {
    override val key: SettingsKey<NoSettings> = SettingsKey("rescript.pipe.chain.type.hints")

    override val name: String = "Pipe chain intermediate types"

    override val previewText: String =
        """
        let result = arr
          ->Array.map(x => x + 1)
          ->Array.filter(x => x > 0)
          ->Array.length
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

        val project = file.project
        val projectSettings = RescriptProjectSettings.getInstance(project)
        if (!projectSettings.pipeChainHintsEnabled) return null

        return object : InlayHintsCollector {
            override fun collect(
                element: PsiElement,
                editor: Editor,
                sink: InlayHintsSink,
            ): Boolean {
                if (element != file) return true

                val virtualFile = file.virtualFile ?: return false
                val text = editor.document.text
                val pipePositions = findPipePositions(text)

                for (pipePos in pipePositions) {
                    // Get type at the expression before the pipe
                    val typeText = RescriptLspUtils.getHoverType(project, virtualFile, pipePos) ?: continue

                    // Extract just the type (remove let binding etc.)
                    val displayType = extractReturnType(typeText) ?: typeText
                    if (displayType.isBlank()) continue

                    val factory = PresentationFactory(editor)
                    val presentation =
                        factory.roundWithBackground(
                            factory.smallText(": $displayType"),
                        )

                    sink.addInlineElement(pipePos, false, presentation, false)
                }

                return false
            }
        }
    }

    companion object {
        /**
         * Finds all pipe operator (->) positions in the text.
         *
         * @param text the document text
         * @return list of offsets where pipe operators start
         */
        internal fun findPipePositions(text: String): List<Int> {
            val positions = mutableListOf<Int>()
            val lexer = RescriptLexer()
            lexer.start(text)

            while (lexer.tokenType != null) {
                if (lexer.tokenType == RescriptTokenTypes.ARROW) {
                    positions.add(lexer.tokenStart)
                }
                lexer.advance()
            }

            return positions
        }

        /**
         * Extracts the return type from a full type signature.
         *
         * For `(int, string) => bool`, returns `bool`.
         * For simple types like `int`, returns as-is.
         */
        internal fun extractReturnType(typeText: String): String? {
            val arrowIndex = typeText.lastIndexOf("=>")
            return if (arrowIndex >= 0) {
                typeText.substring(arrowIndex + 2).trim()
            } else {
                typeText.trim()
            }
        }
    }
}
